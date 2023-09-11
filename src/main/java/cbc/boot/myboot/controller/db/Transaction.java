package cbc.boot.myboot.controller.db;

import cbc.boot.myboot.controller.db.util.DynamicDataSourceHolder;
import cbc.boot.myboot.util.GetIPUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
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
public class Transaction {
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private DataSourceTransactionManager transactionManager;
    /**
     * 事务管理（执行多个sql语句，）
     * (1)在MySQL中，只有InnoDB存储引擎类型的数据表才能支持事务处理。
     * (2)设置MySQL不进行自动提交
     * 将自动提交功能置为ON
     * SET AUTOCOMMIT=1;
     * 将自动提交功能置为OFF
     * SET AUTOCOMMIT=0;
     *
     */
    @PostMapping("/cbc/excuteOneByOne.cbc")
    public List<Object> excuteOneByOne(@RequestBody String param,
                                       HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        System.out.println("执行excuteOneByOne操作!");
        System.out.println("传入参数:");
        System.out.println(param);
        SqlSession sqlSession = null;
        Date date1 = new Date();
        String realIP = GetIPUtil.getIpAddr(request);
        System.out.println("请求客户端IP地址：" + realIP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        String nowTime = dateFormat.format(date1);
        System.out.println("请求时间：" + nowTime);
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        TransactionStatus transactionStatus = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);
            //设置数据源
            String db=(String) map.get("db");
            if(db!=null && !"".equals(db)){
                DynamicDataSourceHolder.setDataSourceType(db);
            }
            //设置属性的默认属性
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            //设置事务的传播行为，此处是设置为开启一个新事物
            definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            //设置事务的隔离级别，此处是读已经提交
            definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            //获取事务状态对象
            transactionStatus = (TransactionStatus) transactionManager.getTransaction(definition);
            //****开始对数据库操作
            sqlSession = sqlSessionFactory.openSession();
            sqlSession.getConnection().setAutoCommit(false);
            //System.out.println("开启sqlSession");
            //System.out.println("连接池：" + sqlSession.getConnection());
            String[] sqls = (String[]) map.get("sql").toString().split(";");
            for (int i = 0; i < sqls.length; i++) {
                  //异常测试
//                if(i==1){
//                    int j=19/0;
//                }
                sqlSession.insert(sqls[i], map);
            }
            sqlSession.commit();
            transactionManager.commit(transactionStatus);
            Date date2 = new Date();
            hashMap.put("state", "success");
            String durationTime=(date2.getTime() - date1.getTime()) + "MS";
            hashMap.put("time", durationTime);
            System.out.println("执行时间："+durationTime);
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
            if (transactionManager != null) {
                System.out.println("发生异常,执行回滚操作");
                transactionManager.rollback(transactionStatus);
            }
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

    @PostMapping("/cbc/excuteByBatch.cbc")
    public List<Object> excuteByBatch(@RequestBody String param,
                                      HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Author:陈斌才----------");
        System.out.println("执行excuteByBatch操作!");
        System.out.println("传入参数:");
        System.out.println(param);
        Date date1 = new Date();
        List<Object> returnList = new ArrayList<Object>();
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        SqlSession sqlSession = null;
        String realIP = GetIPUtil.getIpAddr(request);
        System.out.println("请求客户端IP地址：" + realIP);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        String nowTime = dateFormat.format(date1);
        System.out.println("请求时间：" + nowTime);
        TransactionStatus transactionStatus = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);
            //设置数据源
            String db=(String) map.get("db");
            if(db!=null && !"".equals(db)){
                DynamicDataSourceHolder.setDataSourceType(db);
            }
            //设置属性的默认属性
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            //设置事务的传播行为，此处是设置为开启一个新事物
            definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            //设置事务的隔离级别，此处是读已经提交
            definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
            //获取事务状态对象
            transactionStatus = (TransactionStatus) transactionManager.getTransaction(definition);
            //****开始对数据库操作
            sqlSession = sqlSessionFactory.openSession();
            sqlSession.getConnection().setAutoCommit(false);
            //System.out.println("开启sqlSession");
            //System.out.println("连接池：" + sqlSession.getConnection());
            sqlSession.insert((String) map.get("sql"), map);
            //异常测试
            //int j=19/0;
            sqlSession.commit();
            transactionManager.commit(transactionStatus);
            Date date2 = new Date();
            hashMap.put("state", "success");
            String durationTime=(date2.getTime() - date1.getTime()) + "MS";
            hashMap.put("time", durationTime);
            System.out.println("执行时间："+durationTime);
        } catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
            if (transactionManager != null) {
                transactionManager.rollback(transactionStatus);
                System.out.println("发生异常,执行回滚操作");
            }
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
