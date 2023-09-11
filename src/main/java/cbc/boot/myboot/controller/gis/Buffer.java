package cbc.boot.myboot.controller.gis;

import cbc.boot.myboot.controller.gis.util.ESSearch;
import cbc.boot.myboot.controller.gis.util.GeometryOperation;
import cbc.boot.myboot.controller.gis.util.WKTReader;
import cbc.boot.myboot.controller.gis.util.WKTWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Geometry;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
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
public class Buffer {
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ESSearch esSearch;

    @SuppressWarnings("unchecked")
    @PostMapping("/cbc/buffer.cbc")
    public List<Object> buffer(@RequestBody String param,
                               HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("获取缓冲区域");
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            String wktSource = (String) map.get("wktString");
            double distance = Double.parseDouble((String) map.get("distance"));
            System.out.println("传入参数：" + wktSource + ":" + distance);
            WKTReader wktReader = new WKTReader();
            WKTWriter wktWriter = new WKTWriter();
            Geometry geometry = GeometryOperation.buffer(
                    wktReader.read(wktSource), distance);
            String wktResult = wktWriter.write(geometry);
			System.out.println("wktResult：" + wktResult);
            hashMap.put("wktResult", wktResult);
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
    @PostMapping("/cbc/polyline/buffer.cbc")
    public List<Object> spaceByPolylineBuffer(@RequestBody String param,
                                              HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("线周边查询");
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            List<Map<String, Object>> points = (List<Map<String, Object>>) map.get("points");
            double radius = Double.parseDouble((String) map.get("radius")) / (1000 * 101.11);
            String count = (String) map.get("count");
            List<List<Double[]>> polygonList = new ArrayList<List<Double[]>>();
            StringBuilder stringBuilder = new StringBuilder();
			for (int j = 0; j < points.size(); j++) {
				double lat = Double.parseDouble(points.get(j).get("lat").toString());
				double lng = Double.parseDouble(points.get(j).get("lng").toString());
				stringBuilder.append(lng + " " + lat + ",");
			}
            String wktString = "LineString(" + stringBuilder.toString().substring(0, stringBuilder.length() - 1) + ")";
            System.out.println("wktString:" + wktString);
            WKTReader wktReader = new WKTReader();
            WKTWriter wktWriter = new WKTWriter();
            Geometry geometry = GeometryOperation.buffer(
                    wktReader.read(wktString), radius);
            String wktResult = wktWriter.write(geometry);
            String wktResultTemp1 = wktResult.substring(wktResult.indexOf("((") + 2, wktResult.length());
            String wktResultTemp2 = wktResultTemp1.split("\\)\\)")[0];
            String[] polygonArr = wktResultTemp2.split("\\), \\(");
            //单个缓存
            for (int j = 0; j < polygonArr.length; j++) {
                StringBuilder bufferArea = new StringBuilder();
                String[] xysArr = polygonArr[j].split(",");
                for (int i = 0; i < xysArr.length; i++) {
                    if (xysArr[i].indexOf(" ") == 0) {
                        String xyTemp = xysArr[i].toString().substring(1, xysArr[i].length());
                        bufferArea.append(xyTemp.split(" ")[0] + "," + xyTemp.split(" ")[1] + ",");
                    } else {
                        bufferArea.append(xysArr[i].split(" ")[0] + "," + xysArr[i].split(" ")[1] + ",");
                    }
                }
                String newBufferArea = bufferArea.substring(0, bufferArea.length() - 1);
                List<Double[]> bufferList = new ArrayList<Double[]>();
                String[] bufferXys = newBufferArea.split(",");
                for (int i = 0; i < bufferXys.length; i = i + 2) {
                    Double[] point = {Double.parseDouble(bufferXys[i + 1].toString()), Double.parseDouble(bufferXys[i])};
                    bufferList.add(point);
                }
                polygonList.add(bufferList);
            }
            List<List<GeoPoint>> geoPointList = new ArrayList<List<GeoPoint>>();
            for (int i = 0; i < polygonList.size(); i++) {
                List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
                for (int j = 0; j < polygonList.get(i).size(); j++) {
                    Double[] pointTemp = polygonList.get(i).get(j);
                    geoPoints.add(new GeoPoint(pointTemp[0], pointTemp[1]));
                }
                geoPointList.add(geoPoints);
            }
            List<List<Map<String, Object>>> resultListTemp = new ArrayList<List<Map<String, Object>>>();
            if (geoPointList.size() == 1) {
                List<Map<String, Object>> resultTemp = this.spaceBufferByPolygon(geoPointList.get(0), null, map, count);
                resultListTemp.add(resultTemp);
            }
            if (geoPointList.size() > 1) {
                List<GeoPoint> inGeoPoints = new ArrayList<GeoPoint>();
                List<List<GeoPoint>> notGeoPoints = new ArrayList<List<GeoPoint>>();
                for (int i = 0; i < geoPointList.size(); i++) {
                    if (i == 0) {
                        inGeoPoints = geoPointList.get(i);
                    }
                    if (i != 0) {
                        notGeoPoints.add(geoPointList.get(i));
                    }
                }
                List<Map<String, Object>> resultTemp = this.spaceBufferByPolygon(inGeoPoints, notGeoPoints, map, count);
                resultListTemp.add(resultTemp);
            }
            hashMap.put("state", "success");
            hashMap.put("objects", resultListTemp);
            hashMap.put("polylineBuffer", polygonList);
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

    /**
     * 供缓冲区查询ES数据的方法
     */
    public List<Map<String, Object>> spaceBufferByPolygon(List<GeoPoint> geoPoints, List<List<GeoPoint>> notgeoPoints, Map<String, Object> map, String count) throws Exception{
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = esSearch.filter(map);
        //多边形空间
        GeoPolygonQueryBuilder queryBuilder = QueryBuilders.geoPolygonQuery("point", geoPoints);
        boolQuery.must(queryBuilder);
        if (notgeoPoints != null) {
            for (int i = 0; i < notgeoPoints.size(); i++) {
                GeoPolygonQueryBuilder queryBuilderTemp = QueryBuilders.geoPolygonQuery("point", notgeoPoints.get(i));
                boolQuery.mustNot().add(queryBuilderTemp);
            }
        }
        sourceBuilder.query(boolQuery);
		List<Map<String, Object>> returnList=esSearch.search(sourceBuilder, count, highLevelClient);
        return returnList;
    }


}
