package com.atguigu.gmall.search.service.impl;

import com.atguigu.gmall.bean.PmsSearchParm;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.payment.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    JestClient jestClient;

    /**
     * 通过三级分类id，关键字，属性, 进行查询，过滤(搜索->京东的搜索)
     * @param pmsSearchParm
     * @return
     */
    @Override
    public List<PmsSearchSkuInfo> list(PmsSearchParm pmsSearchParm) {

        String dslStr = getSearchDsl(pmsSearchParm);

        System.err.println(dslStr);

        //执行复杂查询
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = new ArrayList<>();

        Search search = new Search.Builder("dsl的json sql语句").addIndex("索引名").addType("表名").build();

        SearchResult execute = null;
        try {
            execute = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);

        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;

            Map<String,List<String>> highlight = hit.highlight; //高亮是在搜索关键字的前提下触发的，三级分类和属性是没有高亮的

            if (highlight != null){
                String skuName = highlight.get("skuName").get(0); //可能会出现空指针,所以要加if
                source.setSkuName(skuName);
            }
            pmsSearchSkuInfos.add(source);
        }
        System.out.println(pmsSearchSkuInfos.size());
        return pmsSearchSkuInfos;
    }

    private String getSearchDsl(PmsSearchParm pmsSearchParm) {
        String[] skuAttrValueList = pmsSearchParm.getValueId();
        String keyWord = pmsSearchParm.getKeyWord();
        String catalog3Id = pmsSearchParm.getCatalog3Id();

        //用api执行复杂查询
        //jest的del工具 sql语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        //filter 过滤的参数
        if (StringUtils.isNotBlank(catalog3Id)){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        if (skuAttrValueList != null){
            for (String pmsSkuAttrValue : skuAttrValueList) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.value", pmsSkuAttrValue);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //must
        if (StringUtils.isNotBlank(keyWord)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "华为");
            boolQueryBuilder.must(matchQueryBuilder);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //from
        searchSourceBuilder.from(0); //分页
        //size
        searchSourceBuilder.size(20);

        //highlighter
        searchSourceBuilder.highlighter(null);

        //sort
        searchSourceBuilder.sort("id",SortOrder.DESC);
        return searchSourceBuilder.toString();

    }
}




















