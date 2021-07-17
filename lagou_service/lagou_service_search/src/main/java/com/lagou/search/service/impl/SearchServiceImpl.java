package com.lagou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.goods.pojo.Sku;
import com.lagou.search.feign.SkuFeign;
import com.lagou.search.mapper.SearchMapper;
import com.lagou.search.pojo.SkuInfo;
import com.lagou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import rx.internal.operators.BackpressureUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lihe
 * @Version 1.0
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SearchMapper searchMapper;

    private static final Integer PAGE_SIZE = 5;

    @Override
    public void createIndexAndMapping() {
        //创建索引
        esTemplate.createIndex(SkuInfo.class);
        //创建映射
        esTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importAll() {
        //通过Feign调用商品微服务中的接口获取库存列表
        Map paramMap = new HashMap();
        paramMap.put("status", "1");
        Result result = skuFeign.findList(paramMap);
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfoList);
    }

    @Override
    public void importDataToES(String spuId) {
        //通过id查询库存列表
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        //转换
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuList), SkuInfo.class);
        //设置规格
        for (SkuInfo skuInfo : skuInfoList) {
            skuInfo.setSpecMap(JSON.parseObject(skuInfo.getSpec(), Map.class));
        }
        searchMapper.saveAll(skuInfoList);
    }

    /**
     * 商品搜索
     *
     * @param paramMap
     * @return
     */
    @Override
    public Map search(Map<String, String> paramMap) {
        if (paramMap == null) {
            return null;
        }
        //定义返回结果集
        Map<String, Object> resultMap = new HashMap();
        //指定关键词
        BoolQueryBuilder boolQueryBuilder = setKeyWord(paramMap);
        //指定过滤条件
        fieldsFilter(paramMap, boolQueryBuilder);
        //1.构建查询条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        //添加品牌（分组）聚合
        String skuBrand = "skuBrand";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
        //添加规格（分组）聚合
        String skuSpec = "skuSpec";
        //nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword").size(10000));
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));
        //设置高亮域
        setHighlight(nativeSearchQueryBuilder);
        //设置排序
        setOrder(paramMap, nativeSearchQueryBuilder);
        //设置分页  页码，每页展示多少条数据
        setPageInfo(paramMap, nativeSearchQueryBuilder);
        //2.执行查询
        //AggregatedPage<SkuInfo> aggregatedPage = esTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> aggregatedPage = executeQuery(nativeSearchQueryBuilder);
        //3.从返回结果中获得信息
        //结果集
        resultMap.put("rows", aggregatedPage.getContent());
        //总条目数
        resultMap.put("total", aggregatedPage.getTotalElements());
        //总页数
        resultMap.put("totalPages", aggregatedPage.getTotalPages());
        //取出品牌聚合
        getBrandAgg(resultMap, skuBrand, aggregatedPage);
        //取出规格聚合并完成类型转换
        getSpecAgg(resultMap, skuSpec, aggregatedPage);
        return resultMap;
    }

    private void getSpecAgg(Map<String, Object> resultMap, String skuSpec, AggregatedPage<SkuInfo> aggregatedPage) {
        StringTerms specTerms = (StringTerms) aggregatedPage.getAggregation(skuSpec);
        //[{'颜色': '蓝色', '尺码': '44'},{'颜色': '灰色', '尺码': '35'}]
        List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        //定义返回结果
        Map<String, Set<String>> specMap = specList(specList);
        resultMap.put("specList", specMap);
    }

    private void getBrandAgg(Map<String, Object> resultMap, String skuBrand, AggregatedPage<SkuInfo> aggregatedPage) {
        StringTerms brandTerms = (StringTerms) aggregatedPage.getAggregation(skuBrand);
        //将StringTerms转换为List集合
        List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brandList", brandList);
    }

    private AggregatedPage<SkuInfo> executeQuery(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        return esTemplate.queryForPage(
                    nativeSearchQueryBuilder.build(),
                    SkuInfo.class,
                    new SearchResultMapper() {
                        /**
                         * 将高亮数据替换非高亮的数据
                         * @param response 封装了返回结果集
                         * @param clazz    映射类型
                         * @param pageable 分页对象
                         * @param <T>
                         * @return
                         */
                        @Override
                        public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                            List<T> list = new ArrayList<>();
                            //获得结果集数据
                            SearchHits hits = response.getHits();
                            for (SearchHit searchHit : hits) {
                                //没有高亮的数据,需要获得高亮域的数据，替换SkuInfo中没有高亮的数据
                                SkuInfo skuInfo = JSON.parseObject(searchHit.getSourceAsString(), SkuInfo.class);
                                //获取高亮的数据
                                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                                //指定了高亮域
                                if (highlightFields != null && highlightFields.size() > 0) {
                                    HighlightField highlightField = highlightFields.get("name");
                                    if (highlightField != null) {
                                        //取出高亮数据
                                        Text[] fragments = highlightField.getFragments();
                                        StringBuffer stringBuffer = new StringBuffer();
                                        for (Text text : fragments) {
                                            stringBuffer.append(text.toString());
                                        }
                                        //替换
                                        skuInfo.setName(stringBuffer.toString());
                                        list.add((T)skuInfo);
                                    }
                                }
                            }
                            return new AggregatedPageImpl<T>(list,pageable,hits.getTotalHits(),response.getAggregations());
                        }
                    });
    }

    private void setPageInfo(Map<String, String> paramMap, NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        String pageNum = paramMap.get("pageNum");
        if (StringUtils.isEmpty(pageNum)) {
            pageNum = "1";
        }
        nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, PAGE_SIZE));
    }

    private void setOrder(Map<String, String> paramMap, NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        String sortField = paramMap.get("sortField");
        String sortRole = paramMap.get("sortRule");
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRole)) {
            nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRole.toUpperCase())));
        }
    }

    private void setHighlight(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //指定前缀和后缀
        field.preTags("<span style='color:red'>");
        field.postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(field);
    }

    private void fieldsFilter(Map<String, String> paramMap, BoolQueryBuilder boolQueryBuilder) {
        //品牌过滤
        if (!StringUtils.isEmpty(paramMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", paramMap.get("brand")));
        }

        //规格过滤  spec_xxx=value
        for (String key : paramMap.keySet()) {
            if (key.startsWith("spec_")) {
                String value = paramMap.get(key);
                boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
            }
        }
        //价格过滤
        //价格 0-500元   500-1000元   1000-1500元   1500-2000元   2000-3000元   3000元以上
        //中文替换掉
        String price = paramMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            price = price.replace("元", "").replace("以上", "");
            String[] prices = price.split("-");
            if (prices != null && prices.length > 0) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
                if (prices.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
            }
        }
    }

    private BoolQueryBuilder setKeyWord(Map<String, String> paramMap) {
        String keywords = paramMap.get("keywords");
        if(!StringUtils.isEmpty(keywords)){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchQuery("name", keywords).operator(Operator.AND));
            return boolQueryBuilder;
        }
       return null;
    }

    /**
     * 格式转换
     *
     * @param specList
     * @return
     */
    private Map<String, Set<String>> specList(List<String> specList) {
        Map<String, Set<String>> specMap = new HashMap<>();
        for (String spec : specList) {
            //将json串转换为Map
            Map map = JSON.parseObject(spec, Map.class);
            //遍历map
            Set<Map.Entry<String, String>> entries = map.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                //获取key和value
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<>();
                }
                //将value添加到Set集合中
                specValues.add(value);
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }
}









