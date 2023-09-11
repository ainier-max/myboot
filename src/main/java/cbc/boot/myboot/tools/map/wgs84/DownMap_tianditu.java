//请改为自己所属的包路径
package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 申明：本代码仅用于技术分享，不可用于非法爬取。如有违规定，概不负责。谢谢。
 */
public class DownMap_tianditu {
	public static void main(String[] args) throws Exception {
		//矢量图层，该地址可通过访问天地图的地图服务，之后F12--》network可拿到
		String link = "http://t0.tianditu.gov.cn/vec_c/wmts?tk=9024bb5d2e154746bb513878231cc0cf&service=WMTS&request=GetTile&version=1.0.0&layer=vec&style=default&tilematrixSet=c&format=tiles&height=256&width=256&minZoom=1&maxZoom=18&tilematrix={z}&tilerow={y}&tilecol={x}";
		//标注图层
		//String link = "http://t0.tianditu.gov.cn/cva_c/wmts?tk=9024bb5d2e154746bb513878231cc0cf&service=WMTS&request=GetTile&version=1.0.0&layer=cva&style=default&tilematrixSet=c&format=tiles&height=256&width=256&minZoom=1&maxZoom=18&tilematrix={z}&tilerow={y}&tilecol={x}";
		//天地图的分辨率设置
		double[] resolutions={
				0.14,
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
		//爬取地图级别
		int z=0;
		//分辨率
		double resolution=0.0;
		//爬取地图范围设置
		double xmax_jw=180;
		double xmin_jw=-180;
		double ymax_jw=90;
		double ymin_jw=-90;
		for(int k=1;k<10;k++){
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
					System.out.println();
					String urlstr=link.replace("{x}", i + "").replace(
							"{y}", j + "").replace("{z}", z + "");
					System.out.println("urlstr:"+urlstr);
					URL url = new URL(urlstr);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
					conn.setConnectTimeout(10000);
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
