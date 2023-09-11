package cbc.boot.myboot.controller.gis;


import cbc.boot.myboot.controller.gis.util.ESSearch;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
public class Space {
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ESSearch esSearch;

    @SuppressWarnings("unchecked")
    @PostMapping("/cbc/space/rect.cbc")
    public List<Map<String, Object>> spaceByRect(@RequestBody String param,
                                                 HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("矩形空间查");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            String count = (String) map.get("count");
            //条件过滤
            BoolQueryBuilder boolQuery = esSearch.filter(map);
            //矩形空间过滤
            double north = Double.parseDouble((String) map.get("north"));
            double west = Double.parseDouble((String) map.get("west"));
            double south = Double.parseDouble((String) map.get("south"));
            double east = Double.parseDouble((String) map.get("east"));
            GeoBoundingBoxQueryBuilder queryBuilder = QueryBuilders.geoBoundingBoxQuery("point");
            queryBuilder.setCorners(north, west, south, east);
            boolQuery.must(queryBuilder);
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
    @PostMapping("/cbc/space/circle.cbc")
    public List<Map<String, Object>> spaceByCircle(@RequestBody String param,
                                                   HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("圆形空间查");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            String count = (String) map.get("count");
            //条件过滤
            BoolQueryBuilder boolQuery = esSearch.filter(map);
            //圆形空间过滤
            double lat = Double.parseDouble((String) map.get("lat"));
            double lng = Double.parseDouble((String) map.get("lng"));
            String radius = (String) map.get("radius");
            GeoDistanceQueryBuilder queryBuilder = QueryBuilders.geoDistanceQuery("point");
            queryBuilder.point(lat, lng).distance(radius, DistanceUnit.METERS);
            boolQuery.must(queryBuilder);
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
    @PostMapping("/cbc/space/polygon.cbc")
    public List<Map<String, Object>> spaceByPolygon(@RequestBody String param,
                                                    HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("多边形空间查");
        List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            String count = (String) map.get("count");
            //条件过滤
            BoolQueryBuilder boolQuery = esSearch.filter(map);
            //多边形空间过滤
            List<Object> points = (List<Object>) map.get("points");
            List<GeoPoint> polygonPoints = new ArrayList<>();
            for (int i = 0; i < points.size(); i++) {
                List<HashMap<String, Object>> pointListTemp = (List<HashMap<String, Object>>) points.get(i);
                for (int j = 0; j < pointListTemp.size(); j++) {
                    List pointListTemp2 = (List) pointListTemp.get(j);
                    double lat = Double.parseDouble(pointListTemp2.get(0).toString());
                    double lng = Double.parseDouble(pointListTemp2.get(1).toString());
                    polygonPoints.add(new GeoPoint(lat, lng));
                }
            }
            GeoPolygonQueryBuilder queryBuilder = QueryBuilders.geoPolygonQuery("point", polygonPoints);
            boolQuery.must(queryBuilder);
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
}

