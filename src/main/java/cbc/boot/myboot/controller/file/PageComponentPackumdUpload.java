package cbc.boot.myboot.controller.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.*;

@RestController
public class PageComponentPackumdUpload {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostMapping("/cbc/PageComponentPackUpload.cbc")
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
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            map.put("uuid", uuid);
            System.out.println("文件大小"+files.size());
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                if(i==0){
                    map.put("component_umdjs", file.getBytes());
                }
                if(i==1){
                    map.put("component_mjs", file.getBytes());
                }
                if(i==2){
                    map.put("component_project", file.getBytes());
                }
                if(i==3){
                    map.put("component_photo", file.getBytes());
                }
            }
            int insert_flag = sqlSession.insert("page_component_pack.insert", map);
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

    /*http://10.11.0.87:8087/cbc/GetPageComponentPackProject.cbc?component_id=EchartMap*/
    @GetMapping("/cbc/GetPageComponentPackProject.cbc")
    public void getProjectFile(@RequestParam("component_id") String component_id,  HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行findFile操作!");
        System.out.println("请求文件的component_id值：" + component_id);
        SqlSession sqlSession=null;
        OutputStream outputStream = null;

        try {
            Map map =new HashMap();
            map.put("component_id",component_id);
            sqlSession=sqlSessionFactory.openSession();
            List<Map<String, Object>> list=sqlSession.selectList("page_component_pack.findProject", map);
            System.out.println(list);
            if(list.size()>0){
                response.setHeader("Content-Type", "application/octet-stream");
                //设置Http响应头告诉浏览器下载这个附件
                response.setHeader("Content-Disposition", "attachment;Filename=" + component_id + ".zip");
                byte[] data =(byte[])list.get(0).get("component_project");
                outputStream = response.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
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



    /*http://10.11.0.87:8087/cbc/GetPageComponentPackmjs.cbc?component_id=EchartMap*/
    @GetMapping("/cbc/GetPageComponentPackmjs.cbc")
    public void getMjsFile(@RequestParam("component_id") String component_id,  HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行findFile操作!");
        System.out.println("请求文件的component_id值：" + component_id);
        SqlSession sqlSession=null;
        OutputStream outputStream = null;

        try {
            Map map =new HashMap();
            map.put("component_id",component_id);
            sqlSession=sqlSessionFactory.openSession();
            List<Map<String, Object>> list=sqlSession.selectList("page_component_pack.findMjs", map);
            System.out.println(list);
            if(list.size()>0){
                response.setHeader("Content-Type", "application/javascript");
                byte[] data =(byte[])list.get(0).get("component_mjs");
                outputStream = response.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
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

    /*http://10.11.0.87:8087/cbc/GetPageComponentPackumdjs.cbc?component_id=EchartMap*/
    @GetMapping("/cbc/GetPageComponentPackumdjs.cbc")
    public void getUmdjsFile(@RequestParam("component_id") String component_id,  HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行findFile操作!");
        System.out.println("请求文件的component_id值：" + component_id);
        SqlSession sqlSession=null;
        OutputStream outputStream = null;

        try {
            Map map =new HashMap();
            map.put("component_id",component_id);
            sqlSession=sqlSessionFactory.openSession();
            List<Map<String, Object>> list=sqlSession.selectList("page_component_pack.findUmdjs", map);
            System.out.println(list);
            if(list.size()>0){
                response.setHeader("Content-Type", "application/json");
                byte[] data =(byte[])list.get(0).get("component_umdjs");
                outputStream = response.getOutputStream();
                outputStream.write(data);
                outputStream.flush();
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



    @PostMapping("/cbc/DeletePageComponentPackumd.cbc")
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
