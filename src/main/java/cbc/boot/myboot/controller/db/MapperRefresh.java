package cbc.boot.myboot.controller.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.ehcache.CacheManager;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
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
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

/**
 * Created by cbc on 2018/7/5.
 */
@RestController
public class MapperRefresh {
    @Autowired
    private CacheManager cacheManager;

    private final static Logger logger = LoggerFactory.getLogger(MapperRefresh.class);
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    private SqlSession sqlSession;
    private volatile Configuration configuration;
    @Autowired
    private  ApplicationContext applicationContext;

    @PostMapping("/cbc/mapperRefreshByDeleteNameSpace.cbc")
    public List<Object>  mapperRefreshByDeleteNameSpace(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        List<Object> returnList = new ArrayList<Object>();
        configuration = sqlSessionFactory.getConfiguration();
        try {
            sqlSession=sqlSessionFactory.openSession();
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);

            //更新類型
            sqlSession.delete("page_data_model_tree.deleteByIDAndPID", map);
            sqlSession.commit();

            String deleteType=(String) map.get("deleteType");
            String pid=null;
            if(deleteType.equals("deleteNamespace")){
                pid=(String) map.get("id");
            }else{
                pid=(String) map.get("pid");
            }

            String namespace=(String) map.get("name_space");
            System.out.println("命名空间12："+namespace);

            //String classpath = System.getProperty("java.class.path");
            //System.out.println("classpath: " + classpath);

            URL resource = ClassLoader.getSystemResource("");
            if (resource != null) {
                String classpath = resource.getPath();
                File file=new File(classpath+"\\mapper\\lowcode\\"+namespace+".xml");
                //存在则删除
                if(file.exists()){
                    //删除文件夹
                    System.out.println("删除"+namespace+".xml");
                    file.delete();
                }
                if(deleteType.equals("deleteSQL")){
                    //创建文件
                    file.createNewFile();
                    //写入内容
                    FileWriter writer = new FileWriter(file, true);
                    String XmlContent=getXmlContent(namespace,pid);
                    writer.write(XmlContent);
                    writer.close();
                }
            }

            if(deleteType.equals("deleteSQL")){
                Resource template = applicationContext.getResource("classpath:/mapper/lowcode/"+namespace+".xml");
                //清除緩存
                cacheManager.removeCache(namespace);
                //清理
                clearMap(namespace);
                clearSet(template.toString());
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(template.getInputStream(), configuration,
                        template.toString(), configuration.getSqlFragments());
                xmlMapperBuilder.parse();
            }

            hashMap.put("state", "success");

        }catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        }finally {
            if (sqlSession != null) {
                sqlSession.close();
                sqlSession = null;
            }
            returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }
    }

    @PostMapping("/cbc/mapperRefreshByEditNameSpace.cbc")
    public List<Object>  mapperRefreshByEditNameSpace(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("----------Start(Author:陈斌才)----------");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        List<Object> returnList = new ArrayList<Object>();
        configuration = sqlSessionFactory.getConfiguration();
        try {
            sqlSession=sqlSessionFactory.openSession();
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);

            //更新類型
            sqlSession.insert("page_data_model_tree.updateNameSpace", map);
            String pid=(String) map.get("id");
            sqlSession.commit();


            String oldnamespace=(String) map.get("old_name_space");
            String namespace=(String) map.get("name_space");

            if(oldnamespace.equals(namespace)){
                System.out.println("命名空間未發生變化，直接返回");
                hashMap.put("state", "success");
            }else{
                System.out.println("命名空間发生变化");
                System.out.println("命名空间12："+namespace);

                //String classpath = System.getProperty("java.class.path");
                //System.out.println("classpath: " + classpath);

                URL resource = ClassLoader.getSystemResource("");
                if (resource != null) {
                    String classpath = resource.getPath();
                    File file=new File(classpath+"\\mapper\\lowcode\\"+oldnamespace+".xml");
                    //存在则删除
                    if(file.exists()){
                        //删除文件夹
                        file.delete();
                    }
                    File newFile=new File(classpath+"\\mapper\\lowcode\\"+namespace+".xml");
                    //创建文件
                    newFile.createNewFile();
                    //写入内容
                    FileWriter writer = new FileWriter(newFile, true);
                    String XmlContent=getXmlContent(namespace,pid);
                    writer.write(XmlContent);
                    writer.close();
                }
                //舊的
                Resource oldtemplate = applicationContext.getResource("classpath:/mapper/lowcode/"+oldnamespace+".xml");
                //清除緩存
                cacheManager.removeCache(oldnamespace);
                clearMap(oldnamespace);
                clearSet(oldtemplate.toString());

                //解析新文件
                Resource newtemplate = applicationContext.getResource("classpath:/mapper/lowcode/"+namespace+".xml");
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(newtemplate.getInputStream(), configuration,
                        newtemplate.toString(), configuration.getSqlFragments());
                xmlMapperBuilder.parse();

                hashMap.put("state", "success");

            }


        }catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        }finally {
            if (sqlSession != null) {
                sqlSession.close();
                sqlSession = null;
            }
            returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }

    }

        @PostMapping("/cbc/mapperRefresh.cbc")
    public List<Object>  mapperRefresh(@RequestBody String param, HttpServletRequest request, HttpServletResponse response) {

        System.out.println("----------Start(Author:陈斌才)----------");
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        List<Object> returnList = new ArrayList<Object>();
        configuration = sqlSessionFactory.getConfiguration();
        try {
            sqlSession=sqlSessionFactory.openSession();
            ObjectMapper mapper = new ObjectMapper();
            Map map=mapper.readValue(param, Map.class);

            //更新類型
            sqlSession.insert("page_data_model_tree.update", map);
            String pid=(String) map.get("pid");

            sqlSession.commit();

            String namespace=(String) map.get("name_space");
            System.out.println("命名空间12："+namespace);

            //String classpath = System.getProperty("java.class.path");
            //System.out.println("classpath: " + classpath);

            URL resource = ClassLoader.getSystemResource("");
            if (resource != null) {
                String classpath = resource.getPath();
                File file=new File(classpath+"\\mapper\\lowcode\\"+namespace+".xml");
                //存在则删除
                if(file.exists()){
                    //删除文件夹
                    file.delete();
                }
                //创建文件
                file.createNewFile();
                //写入内容
                FileWriter writer = new FileWriter(file, true);
                String XmlContent=getXmlContent(namespace,pid);
                writer.write(XmlContent);
                writer.close();
            }
            Resource template = applicationContext.getResource("classpath:/mapper/lowcode/"+namespace+".xml");
            //清除緩存
            cacheManager.removeCache(namespace);
            //cacheManager.clearAll();
            //sqlSession.clearCache();
            //sqlSession.commit();
            //清理
            clearMap(namespace);
            clearSet(template.toString());
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(template.getInputStream(), configuration,
                    template.toString(), configuration.getSqlFragments());
            xmlMapperBuilder.parse();

            hashMap.put("state", "success");

        }catch (Exception e) {
            e.printStackTrace();
            hashMap.put("state", "error");
            hashMap.put("message", e.getMessage());
        }finally {
            if (sqlSession != null) {
                sqlSession.close();
                sqlSession = null;
            }
            returnList.add(hashMap);
            System.out.println("----------End(Author:陈斌才)----------");
            return returnList;
        }

    }

    private List<Map<String, Object>> findDataModelByPid(String pid) {
        Map map=new HashMap();
        map.put("pid",pid);
        map.put("sql","findByPid");
        List<Map<String, Object>> list=sqlSession.selectList((String) map.get("sql"), map);
        return list;
    }


    private String getXmlContent(String namespace,String pid) {
        StringBuffer xmlContent = new StringBuffer();
        xmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        xmlContent.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"");
        xmlContent.append(" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");

        xmlContent.append("<mapper namespace='"+namespace+"'>");
        xmlContent.append("<cache type=\"org.mybatis.caches.ehcache.EhcacheCache\"/>");

        List<Map<String, Object>> list=findDataModelByPid(pid);

        System.out.println("返回结果21："+list.toString());

        for(int i=0;i<list.size();i++){
            String data_model_type=(String) list.get(i).get("data_model_type");
            String data_model_id=(String) list.get(i).get("data_model_id");
            String data_model_sql=(String) list.get(i).get("data_model_sql");
            String is_cache=(String) list.get(i).get("is_cache");
            if(data_model_type!=null && data_model_id!=null && data_model_sql!=null && is_cache!=null){
                if(data_model_type.equals("select")){
                    xmlContent.append("<select id=\""+data_model_id+"\" parameterType=\"java.util.HashMap\" resultType=\"java.util.HashMap\" useCache=\""+is_cache+"\">");
                    xmlContent.append(data_model_sql);
                    xmlContent.append("</select>");
                }
                if(data_model_type.equals("insert")){
                    xmlContent.append("<insert id=\""+data_model_id+"\" parameterType=\"java.util.HashMap\">");
                    xmlContent.append(data_model_sql);
                    xmlContent.append("</insert>");
                }
                if(data_model_type.equals("update")){
                    xmlContent.append("<update id=\""+data_model_id+"\" parameterType=\"java.util.HashMap\">");
                    xmlContent.append(data_model_sql);
                    xmlContent.append("</update>");
                }
                if(data_model_type.equals("delete")){
                    xmlContent.append("<delete id=\""+data_model_id+"\" parameterType=\"java.util.HashMap\">");
                    xmlContent.append(data_model_sql);
                    xmlContent.append("</delete>");
                }
            }

        }
        xmlContent.append("</mapper>");
        return xmlContent.toString();
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

                List<Object> list = map.keySet().stream().filter(o -> o.toString().startsWith(nameSpace)).collect(Collectors.toList());
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
