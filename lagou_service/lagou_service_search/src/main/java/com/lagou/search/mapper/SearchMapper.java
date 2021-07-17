package com.lagou.search.mapper;

import com.lagou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author lihe
 * @Version 1.0
 */
public interface SearchMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
