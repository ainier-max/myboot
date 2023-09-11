package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class DownMap_super {

	
	public static void main(String[] args) throws Exception {
		String link = "http://53.1.238.17/iserver/services/map-bigdata/rest/maps/bigdata/tileImage.png?redirect=false&transparent=false&cacheEnabled=true&_cache=true&origin=%7B%22x%22:-180,%22y%22:90%7D&overlapDisplayed=false&x={x}&y={y}&width=256&height=256&scale={scale}";
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
				4.29153442382813E-5,
				2.14576721191406E-5,
				1.07288360595703E-5,
				5.36441802978516E-6,
				2.68220901489258E-6
		};
		int z=0;
		double resolution=0.0;
		
		//32.02,117.26
		double xmax_jw=118.26;
		double xmin_jw=116.261;
		double ymax_jw=33.02;
		double ymin_jw=31.02;
		
		/**
		 107.5341796875,23.51898193359375,123.8818359375,41.97601318359375
		 */
		
		/**ȫ��
				double xmax_jw=123.8818359375;
				double xmin_jw=107.5341796875;
				double ymax_jw=41.97601318359375;
				double ymin_jw=23.51898193359375;
		*/
		Date date1=new Date();
		for(int k=14;k<15;k++){
			z=k;
			resolution=resolutions[k];
			createMap(link,z,resolution,xmax_jw,xmin_jw,ymax_jw,ymin_jw);
		}
		Date date2=new Date();
		System.out.println("ִ��ʱ�䣺"+(date2.getTime()-date1.getTime())+" MS");
		
	}
	
	public static void createMap(String link,int z,double resolution,double xmax_jw,double xmin_jw,double ymax_jw,double ymin_jw){
		double[] scales={
				1,
				3.380327143205305e-9,
				6.76065428641061e-9,
				1.3521308572821245e-8,
				2.704261714564249e-8,
				5.40852342912848e-8,
				1.0817046858256987e-7,
				2.1634093716513982e-7,
				4.3268187433027964e-7,
				8.653637486605593e-7,
				0.0000017307274973211186,
				0.000003461454994642237,
				0.000006922909989284474,
				0.000013845819978568949,
				0.000027691639957137897,
				0.000055383279914275794,
				0.00011076655982855159,
				0.00022153311965710318,
				0.00044306623931420635,
				0.000886132478628411
		};
		
		//ԭ�㡾109.5,27��
		int xmin=(int) Math.floor((xmin_jw + 180)/(256*resolution));
		int xmax=(int) Math.floor((xmax_jw + 180)/(256*resolution));
		int ymin=(int) Math.floor((90 - ymax_jw)/(256*resolution));
		int ymax=(int) Math.floor((90 -ymin_jw)/(256*resolution));
		System.out.println(xmin+"-"+xmax);
		System.out.println(ymin+"-"+ymax);
		int c = 0;// �ɹ���
		int fail = 0;// ʧ������
		for (int i = xmin; i <= xmax; i++) {
			for (int j = ymin; j <= ymax; j++) {
				try {
					URL url = new URL(link.replace("{x}", i + "").replace(
							"{y}", j + "").replace("{scale}", scales[z] + ""));
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setConnectTimeout(100);
					conn.connect();
					InputStream in = conn.getInputStream();
					File dir = new File("d:/��ͼ����/tiles/" + z
							+ "/" + i);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File("d:/��ͼ����/tiles/" + z
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
					System.out.println("�ѳɹ�����:" + z + "_" + i + "_" + j
							+ ".png");
					c++;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					fail++;
				}
			}
		}
		System.out.println("������:   " + c + "   ��");
		System.out.println("ʧ��:   " + fail + "   ��");
	}

}
