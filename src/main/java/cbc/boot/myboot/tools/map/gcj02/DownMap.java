package cbc.boot.myboot.tools.map.gcj02;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DownMap {

	static ExecutorService pool;
	static String downloadDir;
	static int[] zoom;
	static Lnglat leftTopLnglat;
	static Lnglat rightBottomLnglat;
	static int roundCount;
	static int totalSize;
	static int currentIndex = 0;
	static int failedCount = 0;
	static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	static ReentrantReadWriteLock lockFailCount = new ReentrantReadWriteLock();
	static String mapUrl="";
	static int mapType=1;
	static int mapUrlEnable=0;
	
	public static int addCurrentIndex(){
		synchronized (lock.writeLock()) {
			try{
				lock.writeLock().lock();
				currentIndex++;
			}finally{
				lock.writeLock().unlock();
			}
		}
		return currentIndex;
	}
	
	public static void addFailedCount(){
		synchronized (lockFailCount.writeLock()) {
			try{
				lockFailCount.writeLock().lock();
				failedCount++;
			}finally{
				lockFailCount.writeLock().unlock();
			}
		}
	}
	public static int getFailedCount(){
		int count = 0;
		synchronized (lockFailCount.readLock()) {
			try{
				lockFailCount.readLock().lock();
				count = failedCount;
			}finally{
				lockFailCount.readLock().unlock();
			}
		}
		return count;
	}
	
	
	public static void main(String[] args) {
		init();
		printCalculatSize(zoom, leftTopLnglat, rightBottomLnglat);
		startDownload(zoom, leftTopLnglat, rightBottomLnglat);
	}

	private static void init() {
		try{
			boolean isProxy = false;
			isProxy = Boolean.valueOf(ConfigUtil.get("isProxy"));
			if(isProxy){
				DownloadUtil.setProxy(isProxy);
				DownloadUtil.setProxyAddress(ConfigUtil.get("proxyAddress"));
				try{DownloadUtil.setProxyPort(Integer.parseInt(ConfigUtil.get("proxyPort")));}catch (Exception e) {}
			}
		}catch (Exception e) {}
		downloadDir = ConfigUtil.get("downloadDir");
		mapUrl= ConfigUtil.get("mapUrl");
		mapType=Integer.valueOf(ConfigUtil.get("mapType"));
		mapUrlEnable=Integer.valueOf(ConfigUtil.get("mapUrlEnable"));
		pool = Executors.newFixedThreadPool(Integer.parseInt(ConfigUtil.get("threadCount")));
		roundCount = Integer.parseInt(ConfigUtil.get("roundCount").trim());
		String[] tmpZoom = ConfigUtil.get("zoom").split(",");
		zoom = new int[tmpZoom.length];
		for(int i = 0, len = zoom.length; i < len; i++){
			zoom[i] = Integer.parseInt(tmpZoom[i].trim());
		}
		
		String[] tmpLngLat = ConfigUtil.get("leftTopLnglat").split(",");
		leftTopLnglat = new Lnglat(Double.valueOf(tmpLngLat[0].trim()), Double.valueOf(tmpLngLat[1].trim()));
		tmpLngLat = ConfigUtil.get("rightBottomLnglat").split(",");
		rightBottomLnglat = new Lnglat(Double.valueOf(tmpLngLat[0].trim()), Double.valueOf(tmpLngLat[1].trim()));;
	}

	private static void printCalculatSize(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		int size = calculateDownloadSize(zoom, leftTopLnglat, rightBottomLnglat);
		System.out.println("文件总数:" + size);
		System.out.println("文件大小:" + size * 22 + "k, " + size * 22 / 1024 + "M, " + size * 22 / 1024 / 1024 + "G");
	}

	private static int calculateDownloadSize(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		int size = 0;
		for(int i = 0, len = zoom.length; i < len; i++){
			Tile leftTopTile = CoordinateUtil.lnglatToTile(zoom[i], leftTopLnglat);
			Tile rightBottomTile = CoordinateUtil.lnglatToTile(zoom[i], rightBottomLnglat);
			
			size += (rightBottomTile.getX() - leftTopTile.getX() + 1) * (rightBottomTile.getY() - leftTopTile.getY() + 1);
		}
		return size;
	}

	private static void startDownload(int[] zoom, Lnglat leftTopLnglat, Lnglat rightBottomLnglat) {
		totalSize = calculateDownloadSize(zoom, leftTopLnglat, rightBottomLnglat);
		
		List<Tile> tmpTileList = new ArrayList<Tile>();
		int tmpTileListSize = 0;
		for(int i = 0, len = zoom.length; i < len; i++){
			Tile leftTopTile = CoordinateUtil.lnglatToTile(zoom[i], leftTopLnglat);
			Tile rightBottomTile = CoordinateUtil.lnglatToTile(zoom[i], rightBottomLnglat);
			
			for(int x = leftTopTile.getX();x <= rightBottomTile.getX(); x++){
				for(int y = leftTopTile.getY();y <= rightBottomTile.getY(); y++){
					tmpTileList.add(new Tile(x, y, zoom[i]));
					tmpTileListSize++;
					if(tmpTileListSize >= roundCount){
						tmpTileListSize = 0;
						startDownloadThread(tmpTileList);
					}
				}
			}
		}
		if(tmpTileListSize != 0){
			tmpTileListSize = 0;
			startDownloadThread(tmpTileList);
		}
	}

	private static void startDownloadThread(List<Tile> tmpTileList) {
		final Tile[] threadTaskTiles = tmpTileList.toArray(new Tile[0]);
		tmpTileList.clear();
		pool.execute(new Runnable() {
			public void run() {
				for(int i = 0, len = threadTaskTiles.length; i < len; i++){
					try{
						System.out.println("当前文件 " + addCurrentIndex() + " 文件总数 " + totalSize + "下载失败 " + DownMap.getFailedCount() + "张");
						downloadXYZ(threadTaskTiles[i]);
					}catch (Exception e) {
						DownMap.addFailedCount();
						e.printStackTrace();
						System.out.println("下载文件:x(" + threadTaskTiles[i].getX() + "),y(" + threadTaskTiles[i].getY() + "),z(" + threadTaskTiles[i].getZoom() + ")");
					}
				}
			}
		});
	}
	
	
	private static void downloadXYZ(Tile tile) throws Exception{
		int x=tile.getX();
		int y=tile.getY();
		int Zoom=tile.getZoom();
		//高德
		String url="http://webrd04.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x=" + x + "&y=" + y + "&z=" + Zoom;
		//国家天地图矢量
		//String url="http://t2.tianditu.cn/DataServer?T=vec_w&X=" + x + "&Y=" + y + "&L=" + Zoom;
		//国家天地图注记
		//String url="http://t3.tianditu.cn/DataServer?T=cva_w&X=" + x + "&Y=" + y + "&L=" + Zoom;
		//google矢量
		//String url="http://www.google.cn/maps/vt?lyrs=m@189&gl=cn&x=" + x + "&y=" + y + "&z=" + Zoom;
		//String url="https://c.tile.openstreetmap.org/"+Zoom+"/"+x+"/"+y+".png";
		//国家天地图影像
		//String url="http://t7.tianditu.gov.cn/img_w/wmts?service=WMTS&version=1.0.0&request=GetTile&tilematrix="+Zoom+"&layer=img&style=default&tilerow="+y+"&tilecol="+x+"&tilematrixset=w&format=tiles&tk=83b36ded6b43b9bc81fbf617c40b83b5";
		//国家天地图注记
		//String url="http://t5.tianditu.gov.cn/cia_w/wmts?service=WMTS&version=1.0.0&request=GetTile&tilematrix="+Zoom+"&layer=cia&style=default&tilerow="+y+"&tilecol="+x+"&tilematrixset=w&format=tiles&tk=56b81006f361f6406d0e940d2f89a39c";


				
		String storePath = downloadDir + "/tiles/" + tile.getZoom() + "/" + tile.getX() + "/" + tile.getY() + ".png";
		storePath = storePath.replace("//", "/");
		DownloadUtil.download(url, storePath, true);
	}
}
