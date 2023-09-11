package cbc.boot.myboot.controller.gis.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ESSearch {
    //为了拿取该值，ESSearch不能被new新建实例，而应用@Autowired
    @Value("${elasticsearch.poi}")
    private String poi;

    @Value("${elasticsearch.zzjg}")
    private String zzjg;

    public List<Map<String, Object>> search(SearchSourceBuilder sourceBuilder, String count, RestHighLevelClient highLevelClient) throws Exception {
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        sourceBuilder.from(0);
        sourceBuilder.size(Integer.parseInt(count));
        SearchRequest searchRequest = new SearchRequest(poi);
        searchRequest.source(sourceBuilder);
        System.out.println("请求报文：");
        System.out.println(JSON.toJSONString(JSON.parseObject(sourceBuilder.toString()), SerializerFeature.PrettyFormat));
        SearchResponse res = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //System.out.println("ES返回长度："+res.getHits().getHits().length);
        for (int i = 0; i < res.getHits().getHits().length; i++) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("lng", res.getHits().getHits()[i].getSourceAsMap().get("lng"));
            hashMap.put("lat", res.getHits().getHits()[i].getSourceAsMap().get("lat"));
            hashMap.put("type", res.getHits().getHits()[i].getSourceAsMap().get("type"));
            hashMap.put("address", res.getHits().getHits()[i].getSourceAsMap().get("address"));
            hashMap.put("fullname", res.getHits().getHits()[i].getSourceAsMap().get("fullname"));
            hashMap.put("abbrname", res.getHits().getHits()[i].getSourceAsMap().get("abbrname"));
            hashMap.put("id", res.getHits().getHits()[i].getSourceAsMap().get("id"));
            returnList.add(hashMap);
        }
        return returnList;
    }

    public BoolQueryBuilder filter(Map<String, Object> map) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String regionCode = (String) map.get("regioncode");
        String type = (String) map.get("type");
        String keyword = (String) map.get("keyword");
        //区域类型
        if (regionCode != null && !regionCode.equals("")) {
            //System.out.println("区域代码regionCode："+regionCode);
            TermsQueryBuilder typeQueryBuilders = QueryBuilders.termsQuery("regioncode", regionCode.split(","));
            boolQuery.must(typeQueryBuilders);
        }
        //图层类型
        if (type != null && !type.equals("")) {
            //System.out.println("图层类型type："+type);
            TermsQueryBuilder typeQueryBuilders = QueryBuilders.termsQuery("type", type.split(","));
            boolQuery.must(typeQueryBuilders);
        }
        //中文关键字
        if (keyword != null && !keyword.equals("")) {
            //System.out.println("关键字keyword："+keyword);
            MatchQueryBuilder addressQueryBuilders = QueryBuilders.matchQuery("fullname", keyword);
            boolQuery.must(addressQueryBuilders);
        }
        return boolQuery;
    }

    //获取行政区划
    public List<Map<String, Object>> getZzjg(String regioncode, RestHighLevelClient highLevelClient) {
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        try {
            //条件过滤
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //中文关键字
            if (regioncode != null && !regioncode.equals("")) {
                MatchQueryBuilder zzjgdmQueryBuilders = QueryBuilders.matchQuery("id", regioncode);
                boolQuery.must(zzjgdmQueryBuilders);
            }
            sourceBuilder.query(boolQuery);
            SearchRequest searchRequest = new SearchRequest(zzjg);
            searchRequest.source(sourceBuilder);
            System.out.println("请求报文：");
            System.out.println(JSON.toJSONString(JSON.parseObject(sourceBuilder.toString()), SerializerFeature.PrettyFormat));
            SearchResponse res = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //System.out.println("ES返回长度："+res.getHits().getHits().length);
            for (int i = 0; i < res.getHits().getHits().length; i++) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("id", res.getHits().getHits()[i].getSourceAsMap().get("id"));
                hashMap.put("lng", res.getHits().getHits()[i].getSourceAsMap().get("lng"));
                hashMap.put("lat", res.getHits().getHits()[i].getSourceAsMap().get("lat"));
                hashMap.put("name", res.getHits().getHits()[i].getSourceAsMap().get("name"));
                hashMap.put("pid", res.getHits().getHits()[i].getSourceAsMap().get("pid"));
                hashMap.put("zbc", res.getHits().getHits()[i].getSourceAsMap().get("zbc").toString());
                returnList.add(hashMap);
            }

            return returnList;
        } catch (Exception e) {
            e.printStackTrace();
            return returnList;
        }
    }

}
