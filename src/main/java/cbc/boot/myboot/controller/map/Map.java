package cbc.boot.myboot.controller.map;

import cbc.boot.myboot.controller.map.util.ReadMapFile;
import cbc.boot.myboot.util.GetIPUtil;
import cbc.boot.myboot.util.ReadPropertiesConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;



/**
 * Created by cbc on 2018/7/5.
 */
@RestController
public class Map {
    @Autowired
    private ReadPropertiesConfig readPropertiesConfig;

    @PostMapping("/cbc/map.cbc")
    public List<Object>  map(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("获取地图组件!");
        System.out.println("传入参数:");
        System.out.println(param);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        String realIP = GetIPUtil.getIpAddr(request);
        System.out.println("请求客户端IP地址：" + realIP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        Date date1=new Date();
        String nowTime = dateFormat.format(date1);
        System.out.println("请求时间：" + nowTime);
        try {
            ObjectMapper mapper = new ObjectMapper();
            java.util.Map map=mapper.readValue(param, java.util.Map.class);
            String url=(String) map.get("url");
            String path="";
            for (String key : readPropertiesConfig.getComponent().keySet()) {
                if(key.equals(url)){
                    path=readPropertiesConfig.getComponent().get(key);
                }
            }
            System.out.println("地图组件地址：" + path);
            String classPathFile=Map.class.getResource("/").getFile();
            String classPath=java.net.URLDecoder.decode(classPathFile,"utf-8");
            ReadMapFile readMapFile=new ReadMapFile();
            String content=readMapFile.readMinContent(classPath+"/static/map/vue-map-component-min/"+path);
            hashMap.put("state", "success");
            hashMap.put("content", content);
        }catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        } finally {
            returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }
    }




//    public static String encrypt(String data) throws Exception {
//        byte[] bt = encrypt(data.getBytes(ENCODE), defaultKey.getBytes(ENCODE));
//        String strs = new BASE64Encoder().encode(bt);
//        return strs;
//    }









}
