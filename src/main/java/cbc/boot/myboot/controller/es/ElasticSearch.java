package cbc.boot.myboot.controller.es;

import cbc.boot.myboot.controller.db.util.DynamicDataSourceHolder;
import cbc.boot.myboot.util.GetIPUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class ElasticSearch {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private RestHighLevelClient highLevelClient;
    @SuppressWarnings("unchecked")
    @PostMapping("/cbc/database/to/es.cbc")
    public List<Object> esPoi(@RequestBody String param,
                              HttpServletRequest request, HttpServletResponse response){
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行数据导入ES库操作!");
        System.out.println("传入参数:" + param);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        try {
            Date date1 = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
            String nowTime = dateFormat.format(date1);
            System.out.println("请求时间：" + nowTime);
            String realIP = GetIPUtil.getIpAddr(request);
            System.out.println("请求客户端IP地址：" + realIP);
            ObjectMapper mapper = new ObjectMapper();
            Map paramMap = mapper.readValue(param, Map.class);
            //设置数据源
            String db = (String) paramMap.get("db");
            if (db != null && !"".equals(db)) {
                DynamicDataSourceHolder.setDataSourceType(db);
            }
            sqlSession = sqlSessionFactory.openSession();
            List<Map<String, Object>> list = sqlSession.selectList((String) paramMap.get("sql"), paramMap);
            System.out.println("查询到的记录数：" + list.size());
            sqlSession.close();
            sqlSession = null;
            //索引名
            String indexName = (String) paramMap.get("index");


            GetIndexRequest getIndexRequest=new GetIndexRequest(indexName);
            getIndexRequest.humanReadable(true);
            boolean isExists = highLevelClient.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
            System.out.println(indexName+"索引是否存在："+isExists);
            if(isExists==false){
                throw new Exception("索引不存在，请创建！");
            }
            //数据插入
            String type = "_doc";
            int size = list.size();
            BulkRequest bulkRequest = new BulkRequest();
            for (int i = 0; i < size; i++) {
                String id = list.get(i).get("indexID") + "";
                Map<String, Object> map = list.get(i);
                //这里必须每次都使用new IndexRequest(index,type),不然只会插入最后一条记录（这样插入不会覆盖已经存在的Id，也就是不能更新）
                bulkRequest.add(new IndexRequest(indexName, type, id).source(map, XContentType.JSON));
                highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            }
            Date date2 = new Date();
            hashMap.put("state", "success");
            hashMap.put("time", (date2.getTime() - date1.getTime()) + "MS");
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

}
