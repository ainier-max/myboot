package cbc.boot.myboot.tools.map.bd09;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownMap {
	public static void main(String[] args)  
    throws Exception  
{  
    String link =  
        "http://online3.map.bdimg.com/onlinelabel/?qt=tile&x={x}&y={y}&z={z}&styles=pl&udt=20170712&scaler=1&p=1";  
    
    int z = 13;
    double xmax_jw=118.20;
	double xmin_jw=118.06;
	double ymax_jw=24.56;
	double ymin_jw=24.42;
    
    
    int xmin = 1552;//x��Сֵ  
    int xmax = 1671;//x���ֵ  
    int ymin = 492;//y��Сֵ  
    int ymax = 569;//y���ֵ  
    int c = 0;//�ɹ���  
    int fail = 0;//ʧ������  
    for (int i = xmin; i <= xmax; i++)  
    {  
        for (int j = ymin; j <= ymax; j++)  
        {  
            try  
            {  
                URL url = new URL(link.replace("{x}", i + "").replace("{y}", j + "").replace("{z}", z + ""));  
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
                conn.setConnectTimeout(100);  
                conn.connect();  
                InputStream in = conn.getInputStream();  
                File dir = new File("d:/baiduMap/tiles/" + z + "/" + i);  
                if (!dir.exists())  
                {  
                    dir.mkdirs();  
                }  
                File file = new File("d:/baiduMap/tiles/" + z + "/" + i + "/" + j + ".jpg");  
                if (!file.exists())  
                {  
                    file.createNewFile();  
                }  
                OutputStream out = new FileOutputStream(file);  
                byte[] bytes = new byte[1024 * 20];  
                int len = 0;  
                while ((len = in.read(bytes)) != -1)  
                {  
                    out.write(bytes, 0, len);  
                }  
                out.close();  
                in.close();  
                System.out.println("已成功下载:" + z + "_" + i + "_" + j
						+ ".png"); 
                c++;  
            }  
            catch (Exception e)  
            {  
                System.out.println(e.getMessage());  
                fail++;  
            }  
        }  
    }  
    System.out.println("共下载:   " + c + "   张");
	System.out.println("失败:   " + fail + "   张");
}  

}
