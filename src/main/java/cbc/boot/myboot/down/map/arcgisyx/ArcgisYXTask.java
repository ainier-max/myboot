package cbc.boot.myboot.down.map.arcgisyx;


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
class ArcgisYXTask implements Runnable {
    String link;
    int i;  //x坐标
    int j;  //y坐标
    int z;  //缩放级别

    static volatile Integer c = 0;//成功数
    static volatile Integer fail = 0;//失败数量

    public ArcgisYXTask(String link, int i, int j, int z) {
        this.link = link;
        this.i = i;
        this.j = j;
        this.z = z;

    }

    public static void main(String[] args)
            throws Exception {
        //Arcgis影像地图瓦片下载路径
        String link = "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}";
        //爬取地图级别
        for (int z = 5; z < 18; z++) {
            //int z = 12;  //层级
            //double xmax_jw=118.20;
            //double xmin_jw=118.06;
            //double ymax_jw=24.56;
            //double ymin_jw=24.42;
            //118.03073125352506
            //north
            //:
            //24.566393462737455
            //south
            //:
            //24.559289735109576
            //west
            //:
            //118.02286867915468

            //爬取地图范围
            double xmax_jw = 118.03073125352506;
            double xmin_jw = 118.02286867915468;
            double ymax_jw = 24.576393462737455;
            double ymin_jw = 24.559289735109576;
            LatLngToTileUtil latLngToTileUtil = new LatLngToTileUtil();
            int xmin = latLngToTileUtil.minxtotile(xmin_jw, z);
            int xmax = latLngToTileUtil.maxxtotile(xmax_jw, z);
            int ymin = latLngToTileUtil.minytotile(ymax_jw, z);
            int ymax = latLngToTileUtil.maxytotile(ymin_jw, z);

            int temp=0;
            if(ymin>ymax){
                temp=ymin;
                ymin=ymax;
                ymax=temp;
            }
            System.out.println("x:" + xmin + "-" + xmax);
            System.out.println("y:" + ymin + "-" + ymax);
            //创建线程池,corePoolSize两条线程,最大存在四条线程,大于corePoolSize小于MaxmumPoolSize的线程等待空闲时间为500毫秒,任务队列LinkBlockingQueue不写时的默认值为Integer默认值.
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
            for (int i = xmin; i <= xmax; i++) {   //循环X
                for (int j = ymin; j <= ymax; j++) {    //循环Y
                    threadPoolExecutor.execute(new ArcgisYXTask(link, i, j, z));
                    //new Thread(new GCJTask(link,i,j,z)).start();    //此种方法会一直创建线程导致死机
                }    //循环Y结束
            }   //循环X结束
            threadPoolExecutor.shutdown();   //关闭线程池
            while (!threadPoolExecutor.isTerminated()) {
            }     //一直循环等到所有任务被执行完毕时继续往下执行
            System.out.println("共下载:   " + c + "   张");
            System.out.println("失败:   " + fail + "   张");
        }


    }

    public void run() {
        try {
            URL url = new URL(link.replace("{x}", i + "").replace("{y}", j + "").replace("{z}", z + ""));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            conn.setConnectTimeout(10000);
            conn.connect();
            InputStream in = conn.getInputStream();
            File dir = new File("d:/gcj/tiles/" + z + "/" + i);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File("d:/gcj/tiles/" + z + "/" + i + "/" + j + ".png");
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
