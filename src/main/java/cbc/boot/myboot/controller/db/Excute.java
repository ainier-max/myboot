package cbc.boot.myboot.controller.db;

import cbc.boot.myboot.controller.db.util.DynamicDataSourceHolder;
import cbc.boot.myboot.util.GetIPUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cbc on 2018/7/9.
 */
@RestController
public class Excute {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @PostMapping("/cbc/excute.cbc")
    public List<Object> excute(@RequestBody String param, HttpServletRequest request,
                               HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行excute操作!");
        System.out.println("传入参数:");
        System.out.println(param);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        Date date1 = new Date();
        String realIP = GetIPUtil.getIpAddr(request);
        System.out.println("请求客户端IP地址：" + realIP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        String nowTime = dateFormat.format(date1);
        System.out.println("请求时间：" + nowTime);
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);
            //设置数据源
            String db=(String) map.get("db");
            if(db!=null && !"".equals(db)){
                DynamicDataSourceHolder.setDataSourceType(db);
            }
            sqlSession=sqlSessionFactory.openSession();
            int insert_flag = sqlSession.insert((String) map.get("sql"),
                    map);
            sqlSession.commit();
//            if(map.get("id")!=null && map.get("id")!=""){
//                System.out.println("返回主键:" + map.get("id"));
//            }
//          System.out.println("执行记录数:" + insert_flag);
            Date date2 = new Date();
            hashMap.put("state", "success");
            if(map.get("id")!=null && map.get("id")!=""){
                hashMap.put("id", map.get("id"));
            }
            String durationTime=(date2.getTime() - date1.getTime()) + "MS";
            hashMap.put("time", durationTime);
            System.out.println("执行时间："+durationTime);
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        } finally {
            if (sqlSession != null) {
                sqlSession.rollback();
                sqlSession.close();
                sqlSession = null;
            }
            returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }
    }

}
