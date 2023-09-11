package cbc.boot.myboot.tools.map.bd09;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownMap1 {
	/**
	 * z=9: x[95,105]y[30,35] 
	 * z=10: x[190,215]y[58,75]  
	 * z=11: x[385,420]y[120,145]   
	 * z=12: x[775,836]y[243,285]  
	 * z=13: x[1552,1671]y[492,569] 
	 */
	
	/**
	int z = 19;//层级
    int xmin = 103514;//x最小值
    int xmax = 104292;//x最大值
    int ymin = 29400;//y最小值
    int ymax = 30700;//y最大值
	*/
	
	public static void main(String[] args) throws Exception {
		String link = "http://online3.map.bdimg.com/onlinelabel/?qt=tile&x={x}&y={y}&z={z}&styles=pl&udt=20170712&scaler=1&p=1";

		int z = 9;// �㼶
		int xmin = 95;// x��Сֵ
		int xmax = 105;// x���ֵ
		int ymin = 30;// y��Сֵ
		int ymax = 35;// y���ֵ
		int c = 0;// �ɹ���
		int fail = 0;// ʧ������
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
					File dir = new File("d:/mybaidumapdownload1/tiles/" + z
							+ "/" + i);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File file = new File("d:/mybaidumapdownload1/tiles/" + z
							+ "/" + i + "/" + j + ".jpg");
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
							+ ".jpg");
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
