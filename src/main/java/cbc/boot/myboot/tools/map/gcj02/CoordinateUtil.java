package cbc.boot.myboot.tools.map.gcj02;


public class CoordinateUtil {

	/**
	 * 
	 * 功能描述：<br>
	 * 将tile坐标系转换为lnglat坐标系
	 * 
	 * @param tile
	 * @return
	 * @return Lnglat
	 * 
	 * 修改记录:
	 */
	public static Lnglat tileToLnglat(Tile tile){
		double n = Math.pow(2, tile.getZoom());
		double lng = tile.getX() / n * 360.0 - 180.0;
		double lat = Math.atan(Math.sinh(Math.PI * (1 - 2 * tile.getY() / n)));
		lat = lat * 180.0 / Math.PI;
		return new Lnglat(lng, lat);
	}
	
	/**
	 * 
	 * 功能描述：<br>
	 * 将lnglat坐标系转换为tile坐标系
	 * 
	 * @param zoom 缩放级别
	 * @param lnglat
	 * @return
	 * @return Tile
	 * 
	 * 修改记录:
	 */
	public static Tile lnglatToTile(int zoom, Lnglat lnglat){
		double n = Math.pow(2, zoom);
		double tileX = ((lnglat.getLng() + 180) / 360) * n;
		double tileY = (1 - (Math.log(Math.tan(Math.toRadians(lnglat.getLat())) + (1 / Math.cos(Math.toRadians(lnglat.getLat())))) / Math.PI)) / 2 * n;
		return new Tile(new Double(tileX).intValue(), new Double(tileY).intValue(), zoom);
	}
	
	public static void main(String[] args) {
		Lnglat lnglat = new Lnglat(106.171875, 29.84064389983442);
		System.out.println(lnglat);
		System.out.println(lnglatToTile(10, lnglat));
		
		System.out.println();
		Tile tile = new Tile(814, 423, 10);
		System.out.println(tile);
		System.out.println(tileToLnglat(tile));
		System.out.println("complete...");
	}
}
