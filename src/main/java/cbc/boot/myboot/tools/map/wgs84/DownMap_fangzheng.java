package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownMap_fangzheng {
	/**
	 * 厦门本岛
		ymax="24.56"
		ymin="24.42"
		xmax="118.20"
		xmin="118.06"
	 */


	public static void main(String[] args) throws Exception {
		//国土局矢量2017
		//String link = "http://120.27.10.99:8080/EzServer7/Maps/sltdt/EzMap?Service=getImage&Type=RGB&ZoomOffset=0&Col={x}&Row={y}&Zoom={z}&V=0.3&key=";
		String link = "http://77.1.37.159:8090/MapTileService/wmts?REQUEST=GetTile&STORETYPE=merged-dat&PROJECTION=4326&layer=wmts_4326_500000&style=default&tilematrixset=c&Service=WMTS&Request=GetTile&Version=1.0.0&Format=image%2Fpng&TileMatrix={z}&TileCol={x}&TileRow={y}";
		double[] resolutions={
				1.40625,
				0.703125,
				0.3515625,
				0.17578125,
				0.087890625,
				0.0439453125,
				0.02197265625,
				0.010986328125,
				0.0054931640625,
				0.00274658203125,
				0.001373291015625,
				6.866455078125E-4,
				3.4332275390625E-4,
				1.71661376953125E-4,
				8.58306884765625E-5,
				4.291534423828125E-5,
				2.1457672119140625E-5,
				1.0728836059570312E-5,
				5.364418029785156E-6,
		        2.682209014892578E-6,
		        1.341104507446289E-6
		};
		int z=0;
		double resolution=0.0;

		/**
		 * 福州
		 *
119.16114807128906,25.917563438415527,119.45365905761719,26.168875694274902
		 */


		/**
		 * 厦门
		 * */
		//double xmax_jw=118.40;
		//double xmin_jw=117.86;
		//double ymax_jw=24.76;
		//double ymin_jw=24.22;

		//重庆
		double xmax_jw=107.13867187500001;
		double xmin_jw=105.99609375;
		double ymax_jw=30.498046875;
		double ymin_jw=28.564453125;

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
		for(int k=5;k<14;k++){
			z=k;
			resolution=resolutions[k];
			createMap(link,z,resolution,xmax_jw,xmin_jw,ymax_jw,ymin_jw);
		}
		Date date2=new Date();
		System.out.println("执行时间："+(date2.getTime()-date1.getTime())+" MS");

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
