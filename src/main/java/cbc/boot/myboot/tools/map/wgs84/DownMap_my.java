package cbc.boot.myboot.tools.map.wgs84;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DownMap_my {

	
	
	public static void main(String[] args) throws Exception {
		//国土局矢量2017
		String link = "https://192.168.104.72:6443/arcgis/rest/services/fujian/MapServer/tile/{z}/{y}/{x}";
		//String link = "https://localhost:6443/arcgis/rest/services/fujian/MapServer/tile/{z}/{y}/{x}";
		//No name matching localhost found
		//
		double[] resolutions={
				  0.7039144156840442,
				  0.3519572078420221,
				  0.17597860392101106,
				  0.08798930196050553,
				  0.043994650980252764,
				  0.021997325490126382,
				  0.010998662745063191,
				  0.0054993313725315955,
				  0.0027496656862657978,
				  0.0013748328431328989,
				  6.874164215664494E-4
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
		
		double xmax_jw=120.86841939120359;
		double xmin_jw=115.72104522651402;
		double ymax_jw=28.407488627646135;
		double ymin_jw=23.047091729622628;
		
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
		for(int k=1;k<11;k++){
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
					
					
					HttpsURLConnection.setDefaultHostnameVerifier(new DownMap_my().new NullHostNameVerifier());
			        SSLContext sc = SSLContext.getInstance("TLS");
			        sc.init(null, trustAllCerts, new SecureRandom());
			        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

					
					
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
	
	
	
	static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub
        }

        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    } };

	
	
	public class NullHostNameVerifier implements HostnameVerifier {
        /*
         * (non-Javadoc)
         * 
         * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String,
         * javax.net.ssl.SSLSession)
         */
        public boolean verify(String arg0, SSLSession arg1) {
            // TODO Auto-generated method stub
            return true;
        }
    }


}
