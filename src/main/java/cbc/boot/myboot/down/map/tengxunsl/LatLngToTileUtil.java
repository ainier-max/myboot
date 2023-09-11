package cbc.boot.myboot.down.map.tengxunsl;

/**
 * 通过计算坐标得到行列值范围，供爬取的URL中的x,y值替换
 */
public class LatLngToTileUtil {
	public static void main(String[] args) {
		int z=17;
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
	public int minytotile(double miny,int zoom){
		double n = Math.pow(2, zoom);
		double tileY = (1 - (Math.log(Math.tan(Math.toRadians(miny)) + (1 / Math.cos(Math.toRadians(miny)))) / Math.PI)) / 2 * n;
		int y=(int) Math.ceil(tileY);
		return y;
	}
	/**
	 * 计算最大的y所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int maxytotile(double maxy,int zoom){
		double n = Math.pow(2, zoom);
		double tileY = (1 - (Math.log(Math.tan(Math.toRadians(maxy)) + (1 / Math.cos(Math.toRadians(maxy)))) / Math.PI)) / 2 * n;
		int y=(int) Math.floor(tileY);
		return y;
	}

	/**
	 * 计算最小的x所对应的行列值
	 * @param minx
	 * @param z
	 * @return
	 */
	public int minxtotile(double minx,int zoom){
		double n = Math.pow(2, zoom);
		double tileX = ((minx + 180) / 360) * n;
		int x=(int) Math.floor(tileX);
		return x;
	}
	/**
	 * 计算最大的x所对应的行列值
	 * @param maxx
	 * @param z
	 * @return
	 */
	public int maxxtotile(double maxx,int zoom){
		double n = Math.pow(2, zoom);
		double tileX = ((maxx + 180) / 360) * n;
		int x=(int) Math.ceil(tileX);
		return x;
	}
}
