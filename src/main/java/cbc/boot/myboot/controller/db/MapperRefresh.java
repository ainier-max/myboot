package cbc.boot.myboot.controller.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;


/**
 * Created by cbc on 2018/7/5.
 */
@RestController
public class MapperRefresh {
    private final static Logger logger = LoggerFactory.getLogger(MapperRefresh.class);
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    private volatile Configuration configuration;
    @Autowired
    private  ApplicationContext applicationContext;

    @PostMapping("/cbc/mapperRefresh.cbc")
    public List<Object>  select(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) {


        List<Object> returnList = new ArrayList<Object>();
        configuration = sqlSessionFactory.getConfiguration();
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);
            String namespace=(String) map.get("namespace");
            System.out.println("命名空间："+namespace);

            Resource template = applicationContext.getResource("classpath:/mapper/lowcode/"+namespace+".xml");

            //清理
            clearMap(namespace);
            clearSet(template.toString());
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(template.getInputStream(), configuration,
                    template.toString(), configuration.getSqlFragments());
            xmlMapperBuilder.parse();
        }catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行成功！");
        return returnList;
    }

    /**
     *  删除xml元素的节点缓存
     *  @param nameSpace xml中命名空间
     *  @date                    ：2018/12/19
     *  @author                  ：zc.ding@foxmail.com
     */
    private void clearMap(String nameSpace) {
        logger.info("清理Mybatis的namespace="+nameSpace+"在mappedStatements、caches、resultMaps、parameterMaps、keyGenerators、sqlFragments中的缓存");
        Arrays.asList("mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments").forEach(fieldName -> {
            Object value = getFieldValue(configuration, fieldName);
            if (value instanceof Map) {
                Map<?, ?> map = (Map)value;
                List<Object> list = map.keySet().stream().filter(o -> o.toString().startsWith(nameSpace + ".")).collect(Collectors.toList());
                logger.info("需要清理的元素: {}", list);
                list.forEach(k -> map.remove((Object)k));
            }
        });
    }

    /**
     *  清除文件记录缓存
     *  @param resource xml文件路径
     *  @date                    ：2018/12/19
     *  @author                  ：zc.ding@foxmail.com
     */
    private void clearSet(String resource) {
        Object value = getFieldValue(configuration, "loadedResources");
        if (value instanceof Set) {
            Set<?> set = (Set)value;
            set.remove(resource);
            set.remove("namespace:" + resource);
        }
    }

    /**
     *  获取对象指定属性
     *  @param obj 对象信息
     *  @param fieldName 属性名称
     *  @return java.lang.Object
     *  @date                    ：2018/12/19
     *  @author                  ：zc.ding@foxmail.com
     */
    private Object getFieldValue(Object obj, String fieldName){
        try{
            Field field = obj.getClass().getDeclaredField(fieldName);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object value = field.get(obj);
            field.setAccessible(accessible);
            return value;
        }catch(Exception e){
            logger.info("ERROR: 加载对象中[{}]", fieldName, e);
            throw new RuntimeException("ERROR: 加载对象中[" + fieldName + "]", e);
        }
    }






}
