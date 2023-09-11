package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownMap_jingyi {

	
	public static void main(String[] args) throws Exception {
		//国土局矢量2017
		String link = "http://68.26.21.71/images/GetImage.do?method=showImageRedisBytable&table=jingyi_admin:MA_PGISSLDT&version=v1&l={z}&x={y}&y={x}";
		double[] resolutions={
                2.0000081722216954,
                1.0000040861108477,
                0.50000204305542384,
                0.25000102152771203,
                0.12500051076385602,
                0.062500255381928008,
                0.031250127690964004,
                0.015625063845482002,
                0.007812531922741001,
                0.0039062659613704467,
                0.0019531329806852234,
                0.00097656649034265201,
                0.00048828324517127884,
                0.00024414162258569,
                0.000122070811292845,
                6.10354056464225e-005,
                3.0517702823161505e-005,
                7.6294257058400141e-006,
                3.8147128528703159e-006,
                1.9073515436177414e-006
		};
		int z=0;
		double resolution=0.0;
		
		
		double xmax_jw=113.5667271417531;
		double xmin_jw=113.1667271417531;
		double ymax_jw=23.325439823073137;
		double ymin_jw=22.925439823073137;
		
		/**
		 107.5341796875,23.51898193359375,123.8818359375,41.97601318359375
		 */
		
		/**全国
		double xmax_jw=123.8818359375;
		double xmin_jw=107.5341796875;
		double ymax_jw=41.97601318359375;
		double ymin_jw=23.51898193359375;
		*/
		Date date1=new Date();
		for(int k=15;k<16;k++){
			z=k;
			resolution=resolutions[k];
			createMap(link,z,resolution,xmax_jw,xmin_jw,ymax_jw,ymin_jw);
		}
		Date date2=new Date();
		System.out.println("执行时间："+(date2.getTime()-date1.getTime())+" MS");
		
	}
	
	public static void createMap(String link,int z,double resolution,double xmax_jw,double xmin_jw,double ymax_jw,double ymin_jw){
		//原点【109.5,27】
		int xmin=(int) Math.floor((xmin_jw - 109.5)/(256*resolution));
		int xmax=(int) Math.floor((xmax_jw - 109.5)/(256*resolution));
		int ymin=(int) Math.floor((27 - ymax_jw)/(256*resolution));
		int ymax=(int) Math.floor((27 -ymin_jw)/(256*resolution));
		System.out.println(xmin+"-"+xmax);
		System.out.println(ymin+"-"+ymax);
		int c = 0;// 成功数
		int fail = 0;// 失败数量
		for (int i = xmin; i <= xmax; i++) {
			for (int j = ymin; j <= ymax; j++) {
				try {
					URL url = new URL(link.replace("{x}", i + "").replace(
							"{y}", j + "").replace("{z}", z + ""));
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(100);
					conn.connect();
					InputStream in = conn.getInputStream();
					File dir = new File("d:/地图下载/tiles/" + z
							+ "/" + i);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File("d:/地图下载/tiles/" + z
							+ "/" + i + "/" + j + ".png");
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
					System.out.println("已成功下载:" + z + "_" + i + "_" + j
							+ ".png");
					c++;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					fail++;
				}
			}
		}
		System.out.println("共下载:   " + c + "   张");
		System.out.println("失败:   " + fail + "   张");
	}

}
