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
public class PortalColumnUpload {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostMapping("/cbc/PortalColumnUpload.cbc")
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
            map.put("portal_column_umdjs", byteArray);
            int insert_flag = sqlSession.insert("portal_column.insert", map);
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

    @GetMapping("/cbc/GetPortalColumn.cbc")
    public void getFile(@RequestParam("uuid") String uuid,  HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行findFile操作!");
        System.out.println("请求文件的UUID值：" + uuid);
        SqlSession sqlSession=null;
        OutputStream outputStream = null;

        try {
            Map map =new HashMap();
            map.put("uuid",uuid);
            sqlSession=sqlSessionFactory.openSession();
            List<Map<String, Object>> list=sqlSession.selectList("portal_column.findUmd", map);
            System.out.println(list);
            if(list.size()>0){
                response.setHeader("Content-Type", "application/json");
                byte[] data =(byte[])list.get(0).get("portal_column_umdjs");
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

    @PostMapping("/cbc/DeletePortalColumn.cbc")
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
