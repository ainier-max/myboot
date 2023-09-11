//请改为自己所属的包路径
package cbc.boot.myboot.down.map.baidumidblue;

import cbc.boot.myboot.tools.map.bd09.LatLngToTileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 申明：本代码仅用于技术分享，不可用于非法爬取。如有违规定，概不负责。谢谢。
 */
class BaiduMidBlueTask implements Runnable{
    String link;
    int i;  //x坐标
    int j;  //y坐标
    int z;  //缩放级别

    static volatile Integer c = 0;//成功数
    static volatile Integer fail = 0;//失败数量

    public BaiduMidBlueTask(String link, int i, int j, int z) {
        this.link = link;
        this.i = i;
        this.j = j;
        this.z = z;

    }
    public static void main(String[] args)
            throws Exception {
        String link = "https://api.map.baidu.com/customimage/tile?&x={x}&y={y}&z={z}&udt=20210506&scale=1&ak=1XjLLEhZhQNUzd93EjU5nOGQ&customid=midnight";
        //爬取地图级别
        for(int z=10;z<16;z++){
        	//int z = 12;  //层级
            //double xmax_jw=118.20;
        	//double xmin_jw=118.06;
        	//double ymax_jw=24.56;
        	//double ymin_jw=24.42;
            //爬取地图范围
        	double xmax_jw=118.31122935456039;
        	double xmin_jw=118.07143998013917;
        	double ymax_jw=24.4634935737506;
        	double ymin_jw=24.25357021412386;
            LatLngToTileUtil latLngToTileUtil=new LatLngToTileUtil();
        	int xmin=latLngToTileUtil.minxtotile(xmin_jw,z);
    		int xmax=latLngToTileUtil.minxtotile(xmax_jw,z);
    		int ymin=latLngToTileUtil.minytotile(ymin_jw,z);
    		int ymax=latLngToTileUtil.maxytotile(ymax_jw,z);
            //创建线程池,corePoolSize两条线程,最大存在四条线程,大于corePoolSize小于MaxmumPoolSize的线程等待空闲时间为500毫秒,任务队列LinkBlockingQueue不写时的默认值为Integer默认值.
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,4,500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
            for (int i = xmin; i <= xmax; i++) {   //循环X
                for (int j = ymin; j <= ymax; j++) {    //循环Y
                    threadPoolExecutor.execute(new BaiduMidBlueTask(link,i,j,z));
                    //new Thread(new BDTask(link,i,j,z)).start();    //此种方法会一直创建线程导致死机
                }    //循环Y结束
            }   //循环X结束
            threadPoolExecutor.shutdown();   //关闭线程池
            while (!threadPoolExecutor.isTerminated()){}     //一直循环等到所有任务被执行完毕时继续往下执行
            System.out.println("共下载:   " + c + "   张");
            System.out.println("失败:   " + fail + "   张");
        }
        
        
    }

    public void run() {
        try {
            URL url = new URL(link.replace("{x}", i + "").replace("{y}", j + "").replace("{z}", z + ""));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(100);
            conn.connect();
            InputStream in = conn.getInputStream();
            File dir = new File("d:/bd09/tiles/" + z + "/" + i);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File("d:/bd09/tiles/" + z + "/" + i + "/" + j + ".png");
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream out = new FileOutputStream(file);
            byte[] bytes = new byte[1024 * 20];
            int len = 0;
            while ((len = in.read(bytes)) != -1) {
                out.write(bytes, 0, len);
            }
            out.close();
            in.close();
            synchronized (fail) {
            	System.out.println("成功下载" + z + "/" + i + "/" + j + ".png");
                c++;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            synchronized (c) {
                fail++;
            }
        }
    }
}