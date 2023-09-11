package cbc.boot.myboot.controller.gis;


import cbc.boot.myboot.controller.db.util.DynamicDataSourceHolder;
import cbc.boot.myboot.controller.gis.util.ESSearch;
import cbc.boot.myboot.controller.gis.util.GeoUtil;
import cbc.boot.myboot.controller.gis.util.GeometryOperation;
import cbc.boot.myboot.controller.gis.util.WKTReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class Region {
	@Autowired
	private RestHighLevelClient highLevelClient;
	@Autowired
	private ESSearch esSearch;
	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@PostMapping("/cbc/judge/region/xy.cbc")
	public Map<String, Object> judgeRegionByXY(@RequestBody String param,
											   HttpServletRequest request, HttpServletResponse response){
		System.out.println("----------Start(Author:陈斌才)----------");
		System.out.println("判断行政区划与点的关系");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map map=mapper.readValue(param, Map.class);
			//设置数据源
			String db=(String) map.get("db");
			if(db!=null && !"".equals(db)){
				DynamicDataSourceHolder.setDataSourceType(db);
			}
			String regioncode=(String) map.get("regioncode");
			String lat=(String) map.get("lat");
			String lng=(String) map.get("lng");
			//ES查询
			//List<Map<String, Object>> result=esSearch.getZzjg(regioncode,highLevelClient);
			//mysql查询
			Map<String, Object> paramMap=new HashMap<>();
			paramMap.put("id",regioncode);
			SqlSession sqlSession=sqlSessionFactory.openSession();
			List<Map<String, Object>> result=sqlSession.selectList("mysql_zzjg.zzjg_info", paramMap);
			String areaBoundaryPoints="";
			if(result.size()>0){
				areaBoundaryPoints=result.get(0).get("zbc").toString();
			}
			if(null ==result || result.size()==0){
				throw new Exception("未找到对应的区域信息！");
			}else{
				WKTReader wktReader = new WKTReader();
				String wktSource=areaBoundaryPoints;
				String wktDestination="Point("+lng+" "+lat+")";
				boolean flag=false;
				try {
					flag = GeometryOperation.contains(wktReader.read(wktSource), wktReader.read(wktDestination));
				}catch (Exception e){
					e.printStackTrace();
				}
				resultMap.put("flag",flag);
				resultMap.put("state","success");
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("state", "error");
			resultMap.put("message", e.getMessage());
		} finally {
			System.out.println("----------End(Author:陈斌才)----------");
			return resultMap;
		}
	}

	@PostMapping("/cbc/wkt/to/geojson.cbc")
	public Map<String, Object> wktToGeojson(@RequestBody String param,
											   HttpServletRequest request, HttpServletResponse response){
		System.out.println("----------Start(Author:陈斌才)----------");
		System.out.println("wkt格式坐标转geojson格式坐标");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map map=mapper.readValue(param, Map.class);
			String zbc=(String) map.get("zbc");
			GeoUtil geoUtil=new GeoUtil();
			String geojsonZBC=geoUtil.wktToJson(zbc);
			resultMap.put("state", "success");
			resultMap.put("geojson", geojsonZBC);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("state", "error");
			resultMap.put("message", e.getMessage());
		} finally {
			System.out.println("----------End(Author:陈斌才)----------");
			return resultMap;
		}
	}

}
