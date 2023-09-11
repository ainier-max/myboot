package cbc.boot.myboot.controller.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@RestController
public class FileOP {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostMapping("/cbc/upload.cbc")
    public List<Object> uploadFile(@RequestParam("param") String param, @RequestParam("file") List<MultipartFile> files, HttpServletRequest request) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行upload操作!");
        System.out.println("请求参数!" + param);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(param, Map.class);
            sqlSession = sqlSessionFactory.openSession();
            byte[] byteArray = null;
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                byteArray = file.getBytes();
            }
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            map.put("uuid", uuid);
            map.put("content", byteArray);
            int insert_flag = sqlSession.insert("gather_file.insert", map);
            sqlSession.commit();
            hashMap.put("state", "success");
            hashMap.put("uuid", uuid);
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

    @GetMapping("/cbc/getFile.cbc")
    public void getFile(@RequestParam("uuid") String uuid,@RequestParam("type") String type,  HttpServletRequest request, HttpServletResponse response) {
        //http://10.11.0.87:8087/cbc/findFile.cbc?uuid=8817497b-26dc-4596-810f-ee7a396827ae&type=video
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行findFile操作!");
        System.out.println("请求文件的UUID值：" + uuid);
        System.out.println("请求文件类型值：" + type);
        SqlSession sqlSession=null;
        OutputStream outputStream = null;

        try {
            Map map =new HashMap();
            map.put("uuid",uuid);
            sqlSession=sqlSessionFactory.openSession();
            List<Map<String, Object>> list=sqlSession.selectList("gather_file.find", map);
            System.out.println(list);
            if(list.size()>0){
                if(type.equals("photo")){
                    byte[] data =(byte[])list.get(0).get("content");
                    outputStream = response.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                }else if(type.equals("video")){
                    //google浏览器目前只支持mp4格式的视频播放
                    response.setHeader("Content-Type", "video/mp4");
                    byte[] data =(byte[])list.get(0).get("content");
                    outputStream = response.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                }else if(type.equals("audio")){
                    response.setHeader("Content-Type", "audio/mpeg");
                    byte[] data =(byte[])list.get(0).get("content");
                    outputStream = response.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                }else if(type.equals("umd")){
                    response.setHeader("Content-Type", "application/json");
                    byte[] data =(byte[])list.get(0).get("content");
                    outputStream = response.getOutputStream();
                    outputStream.write(data);
                    outputStream.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sqlSession != null) {
                sqlSession.close();
                sqlSession = null;
            }
            System.out.println("----------End(Author:陈斌才)----------");
        }
    }

    @PostMapping("/cbc/deleteFile.cbc")
    public List<Object> deleteFile(@RequestBody String param, HttpServletRequest request,
                                   HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行deleteFile操作!");
        System.out.println("传入参数：" + param);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);
            sqlSession = sqlSessionFactory.openSession();
            int insert_flag = sqlSession.insert("gather_file.delete", map);
            sqlSession.commit();
            hashMap.put("state", "success");
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
