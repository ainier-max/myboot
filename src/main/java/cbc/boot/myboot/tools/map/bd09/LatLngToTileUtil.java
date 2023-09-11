package cbc.boot.myboot.tools.map.bd09;

/**
 * 通过计算坐标得到行列值范围，供爬取的URL中的x,y值替换
 */
public class LatLngToTileUtil {
	public static void main(String[] args) {
		int z=13;
		double minx=118.06;
		double maxx=118.20;
		LatLngToTileUtil latLngToTileUtil=new LatLngToTileUtil();
		int tile_minx=latLngToTileUtil.minxtotile(minx,z);
		int tile_maxx=latLngToTileUtil.maxxtotile(maxx,z);
		System.out.println(tile_minx+":"+tile_maxx);
		double miny=24.42;
		double maxy=24.56;
		int tyle_miny=latLngToTileUtil.minytotile(miny,z);
		int tyle_maxy=latLngToTileUtil.maxytotile(maxy,z);
		System.out.println(tyle_miny+":"+tyle_maxy);
		
	}
	/**
	 * 计算最小的y所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int minytotile(double miny,int z){
		double R=6378137.0;
		double tt1=Math.tan((Math.PI*miny)/180);
		double yy1=1/Math.cos((Math.PI*miny)/180);
		//double tt1=Math.tan((Math.toRadians(miny));
		int y=(int) Math.floor(Math.pow(2,z-26)*R*Math.log(tt1+yy1))-1;
		return y;
	}
	/**
	 * 计算最大的y所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int maxytotile(double maxy,int z){
		double R=6378137.0;
		double tt1=Math.tan((Math.PI*maxy)/180);
		double yy1=1/Math.cos((Math.PI*maxy)/180);
		//double tt1=Math.tan((Math.toRadians(miny));
		int y=(int) Math.floor(Math.pow(2,z-26)*R*Math.log(tt1+yy1))+1;
		return y;
	}

	/**
	 * 计算最小的x所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int minxtotile(double minx,int z){
		double R=6378137.0;
		int x1=(int) Math.floor(Math.pow(2,z-26)*(Math.PI*minx*R/180))-1;
		return x1;
	}
	/**
	 * 计算最大的x所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int maxxtotile(double maxx,int z){
		double R=6378137.0;
		int x1=(int) Math.ceil(Math.pow(2,z-26)*(Math.PI*maxx*R/180))+1;
		return x1;
	}
}
