package cbc.boot.myboot.controller.map.util;

import java.io.*;
import java.net.URL;

public class ReadMapFile {
    /**
     * 生成混淆加密文件
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception{
        String classPathFile= ReadMapFile.class.getResource("/").getFile();
        String classPath=java.net.URLDecoder.decode(classPathFile,"utf-8");
        String prePath=classPath.split("target/classes")[0];
        prePath=prePath+"/src/main/resources/static/map/vue-map-component/";
        //System.out.println("classPath:"+classPath);
        System.out.println("prePath:"+prePath);
        ReadMapFile readMapFile=new ReadMapFile();
        File file=new File(prePath);
        //加密
        readMapFile.listDirectory(file);
        //解密
        String minUrl="E:\\workspace\\myspace2022\\myboot\\src\\main\\resources\\static\\map\\vue-map-component-min\\leaflet\\map-component.vue";
        String content123=readMapFile.readMinContent(minUrl);
        DesUtils desUtils=new DesUtils();
        System.out.println("解密后数据：");
        System.out.println(desUtils.decrypt(content123));




    }

    /**
     * 读取混淆文件
     * @param fileName
     * @return
     */
    public String readMinContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            reader = new BufferedReader(isr);
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                //sbf.append(tempStr+"\\n");
                sbf.append(tempStr);
            }
            reader.close();
            //System.out.println("读取内容："+sbf.toString());
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sbf.toString();
    }

    /**
     * 读取未混淆文件
     * @param fileName
     * @return
     */
    public String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            reader = new BufferedReader(isr);
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                if(tempStr.indexOf("console.log(")<0){
                    //去掉“//”注释
                    for(int i = 0; i < tempStr.length(); i++){
                        if(i + 1 < tempStr.length() && tempStr.charAt(i) == '/'){
                            if(tempStr.charAt(i + 1) == '/'){//以 // 开始的注释代码
                                i=tempStr.length();// 直接 行结束了
                            }else{
                                sbf.append(tempStr.charAt(i));//添加代码
                            }
                        }else{
                            sbf.append(tempStr.charAt(i));//添加代码
                        }
                    }
                    //sbf.append(tempStr+"\\n");
                    //sbf.append("");
                }else{
                    //System.out.println("含有console的行："+tempStr);
                }
            }
            reader.close();
            //System.out.println("读取内容123："+sbf.toString());
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        //System.out.println(sbf.toString());
        return sbf.toString();
    }



    /*
     * 列出指定目录下（包括其子目录）的所有文件
     */
    public static void listDirectory(File dir)throws Exception {
        if (!dir.exists())
            throw new IllegalArgumentException("目录：" + dir + "不存在.");
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir + "不是目录。");
        }
        //如果要遍历子目录下的内容就需要构造File对象做递归操作，File提供了直接返回File对象的API
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isDirectory()) {
                    //递归
                    listDirectory(file);
                } else {
                    //System.out.println("文件路径：" + file.getAbsolutePath());
                    ReadMapFile readMapFile=new ReadMapFile();
                    String content = readMapFile.readFileContent(file.getAbsolutePath());
                    //System.out.println("读取内容："+content);
                    //Des加密
                    DesUtils desUtils=new DesUtils();
                    String resultStr=desUtils.encrypt(content);
                    //System.out.println("加密内容："+resultStr);
                    //System.out.println("创建文件路径："+file.getAbsolutePath().replaceAll("vue-map-component","vue-map-component-min"));
                    System.out.println("未加密文件路径："+file.getAbsolutePath());
                    File createfile = new File(file.getAbsolutePath().replaceAll("vue-map-component", "vue-map-component-min"));
                    System.out.println("加密文件路径:" + createfile.getAbsolutePath());
                    if (!createfile.exists()) {
                        createfile.getParentFile().mkdirs();
                    }else{
                        createfile.delete();
                    }
                    createfile.createNewFile();
                    // write
                    FileWriter fw = new FileWriter(createfile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(resultStr);
                    bw.flush();
                    bw.close();

                    System.out.println("createfile.getAbsolutePath()："+createfile.getAbsolutePath());
                    String content123=readMapFile.readMinContent(createfile.getAbsolutePath());
                    createfile.delete();
                    createfile.createNewFile();
                    // write
                    FileWriter fw1 = new FileWriter(createfile, true);
                    BufferedWriter bw1 = new BufferedWriter(fw1);
                    bw1.write(content123);
                    bw1.flush();
                    bw1.close();
                }
            }
        }
    }


}
