package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownMap_guotuju {
	/**
	 * 厦门岛内
		ymax="24.56"
		ymin="24.42"
		xmax="118.20"
		xmin="118.06"
	 */
	
	
	public static void main(String[] args) throws Exception {
		//国土局矢量2017
		//String link = "http://10.10.4.11:80/ssn/GAJ/arcgis/rest/services/GT/GT.LIMG.XMMAP_C2000/MapServer/tile/{z}/{y}/{x}";
		//国土局注记
		String link = "http://10.10.4.11:80/ssn/GAJ/arcgis/rest/services/GT/GT.LIMG.XMMAP_CVA_C2000/MapServer/tile/{z}/{y}/{x}";
		double[] resolutions={
		                    0.703125, // Level 0
		  			        0.3515625, // Level 1
		  			        0.17578125, // Level 2
		  			        0.087890625, // Level 3
		  			        0.0439453125, // Level 4
		  			        0.02197265625,// Level 5
		  			        0.010986328125,// Level 6
		  			        0.0054931640625,// Level 7
		  			        0.00274658203125,// Level 8
		  			        0.001373291015625,// Level 9
		  			        6.866455078125E-4,// Level 10
		  			        3.4332275390625E-4,// Level 11
		  			        1.71661376953125E-4,// Level 12
		  			        8.58306884765625E-5,// Level 13
		  			        4.291534423828125E-5,// Level 14
		  			        2.1457672119140625E-5,// Level 15
		  			        1.0728836059570312E-5,// Level 16
		  			        5.364418029785156E-6,// Level 17
		  			        2.682209014892578E-6,// Level 18
		  			        1.341104507446289E-6// Level 19
		};
		int z=0;
		double resolution=0.0;
		double xmax_jw=118.20;
		double xmin_jw=118.06;
		double ymax_jw=24.56;
		double ymin_jw=24.42;
		for(int k=10;k<16;k++){
			z=k;
			resolution=resolutions[k];
			createMap(link,z,resolution,xmax_jw,xmin_jw,ymax_jw,ymin_jw);
		}
		
	}
	
	public static void createMap(String link,int z,double resolution,double xmax_jw,double xmin_jw,double ymax_jw,double ymin_jw){
		
		int xmin=(int) Math.floor((xmin_jw + 180)/(256*resolution));
		int xmax=(int) Math.floor((xmax_jw + 180)/(256*resolution));
		int ymin=(int) Math.floor((90 - ymax_jw)/(256*resolution));
		int ymax=(int) Math.floor((90 -ymin_jw)/(256*resolution));
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
