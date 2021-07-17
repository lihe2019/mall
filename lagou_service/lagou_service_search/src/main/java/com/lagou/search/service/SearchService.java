package com.lagou.search.service;

import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
public interface SearchService {

    public void createIndexAndMapping();

    void importAll();

    void importDataToES(String spuId);

    Map search(Map<String, String> paramMap);
}
