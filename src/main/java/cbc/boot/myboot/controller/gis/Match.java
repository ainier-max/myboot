package cbc.boot.myboot.controller.gis;


import cbc.boot.myboot.controller.gis.util.ESSearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Match {
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ESSearch esSearch;

    @SuppressWarnings("unchecked")
    @PostMapping("/cbc/positive/match.cbc")
    public List<Map<String, Object>> positiveMatch(@RequestBody String param,
                                                   HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("正向匹配查");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            String count = (String) map.get("count");
            //条件过滤
            BoolQueryBuilder boolQuery = esSearch.filter(map);
            sourceBuilder.query(boolQuery);
			hashMap.put("state", "success");
			hashMap.put("objects", esSearch.search(sourceBuilder, count, highLevelClient));
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        } finally {
			returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/cbc/reverse/match.cbc")
    public List<Map<String, Object>> reverseMatch(@RequestBody String param,
                                                  HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("反向匹配查");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            String count = (String) map.get("count");
            //条件过滤
            BoolQueryBuilder boolQuery = esSearch.filter(map);
            //圆形空间
            double lat = Double.parseDouble((String) map.get("lat"));
            double lng = Double.parseDouble((String) map.get("lng"));
            String radius = (String) map.get("radius");
            GeoDistanceQueryBuilder queryBuilder = QueryBuilders.geoDistanceQuery("point");
            queryBuilder.point(lat, lng).distance(radius, DistanceUnit.METERS);
            boolQuery.must(queryBuilder);
            sourceBuilder.query(boolQuery);
            //排序
            GeoDistanceSortBuilder sort = new GeoDistanceSortBuilder("point", new GeoPoint(lat, lng));
            sort.unit(DistanceUnit.METERS);
            sourceBuilder.sort(sort);

			hashMap.put("state", "success");
			hashMap.put("objects", esSearch.search(sourceBuilder, count, highLevelClient));
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        } finally {
			returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }
    }
}
