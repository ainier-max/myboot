package cbc.boot.myboot.controller.gis.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;

import java.io.*;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Created by admin on 2019/7/9.
 */
public class WktRemove {

    //测试类编写
    public static void main(String[] args) throws Exception{
        String fileNameStr="newwktnewwkt441971510020";
        getData(fileNameStr);
    }

    public static void getData(String fileNameStr){
        StringBuilder stringBuilder=new StringBuilder();
        try {
            //定义文件
            File file =new File("C:\\Users\\Administrator\\Desktop\\失败地图\\失败地图\\"+fileNameStr+".txt");
            //字节读入流，读取文件
            FileInputStream fis = new FileInputStream(file);
            //创建字节数组，提高效率
            byte[] b = new byte[1024];
            int l = 0;
            while ((l = fis.read(b)) != -1) {
                //使用字符串构造方法
                //第一个参数为要连接元素的数组 第二个参数为起始下标 第三个参数为连接长度
                stringBuilder.append(new String(b, 0, l));
            }
            //注意关闭流，这样IO流才能从内存写到磁盘，不关不会执行io操作
            String wkt=stringBuilder.toString();
            //System.out.println("wkt："+wkt);

            WKTReader reader = new WKTReader();
            Geometry geometry = reader.read(wkt);
            /**
             * 重点逻辑处理
             */
            //处理重复数据
            geometry = dealData(geometry);
            //处理自相交
            geometry= validate(geometry);

            /**
             * WKT输出器，将Geometry对象写出为WKT文本
             */
            WKTWriter wktWriter = new WKTWriter();
            //System.out.println("更新后数据");
            //System.out.println(wktWriter.write(geometry));

            //创建新的wkt文件
            String fileName = "C:\\Users\\Administrator\\Desktop\\失败地图\\失败地图\\newwkt"+fileNameStr+".txt";
            File newFile = new File(fileName);
            if (newFile.createNewFile()) {
                System.out.println("创建文件成功！");
            } else {
                System.out.println("文件已经存在不需要重复创建");
            }
            // 使用FileWriter写文件
            try (FileWriter writer = new FileWriter(newFile)) {
                writer.write(wktWriter.write(geometry));
            }

            //创建新的geojson文件
            String newJsonFileName = "C:\\Users\\Administrator\\Desktop\\失败地图\\失败地图\\newjson"+fileNameStr+".json";
            File newJsonFile = new File(newJsonFileName);
            if (newJsonFile.createNewFile()) {
                System.out.println("创建文件成功！");
            } else {
                System.out.println("文件已经存在不需要重复创建");
            }
            // 使用FileWriter写文件
            try (FileWriter writerJson = new FileWriter(newJsonFile)) {
                WktUtil wktUtil=new WktUtil();
                String geojson=wktUtil.wktToJson(wktWriter.write(geometry));
                //System.out.println("geojson:"+geojson);
                writerJson.write(geojson);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Geometry dealData(Geometry geometry){
        UniqueCoordinateArrayFilter filter = new UniqueCoordinateArrayFilter();
        filter.reset();
        geometry.apply(filter);
        Coordinate[] CoordinateArr=geometry.getCoordinates();
        System.out.println("遍历处理后的坐标数据看是否重复");
        int flag=0;
        for (int i = 0; i < CoordinateArr.length-1; i++) {
            //循环遍历数组
            if(CoordinateArr[i].x==CoordinateArr[i+1].x && CoordinateArr[i].y==CoordinateArr[i+1].y){
                flag=flag+1;
                System.out.println("相同坐标,其位置是："+i+","+(i+1));
                System.out.println("更新的当前坐标为："+CoordinateArr[i].x+","+CoordinateArr[i].y);
                System.out.println("更新的后一个坐标为："+CoordinateArr[i+1].x+","+CoordinateArr[i+1].y);
            }
        }
        if(flag==0){
            System.out.println("无坐标重复");
        }else{
            geometry=dealData(geometry);

        }
        return geometry;
    }








    /**
     * Get / create a valid version of the geometry given. If the geometry is a polygon or multi polygon, self intersections /
     * inconsistencies are fixed. Otherwise the geometry is returned.
     *
     * @param geom
     * @return a geometry
     */
    public static Geometry validate(Geometry geom){
        if(geom instanceof Polygon){
            if(geom.isValid()){
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the polygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((Polygon)geom, polygonizer);
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        }else if(geom instanceof MultiPolygon){
            if(geom.isValid()){
                geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
                return geom; // If the multipolygon is valid just return it
            }
            Polygonizer polygonizer = new Polygonizer();
            for(int n = geom.getNumGeometries(); n-- > 0;){
                addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
            }
            return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        }else{
            return geom; // In my case, I only care about polygon / multipolygon geometries
        }
    }

    /**
     * Add all line strings from the polygon given to the polygonizer given
     *
     * @param polygon polygon from which to extract line strings
     * @param polygonizer polygonizer
     */
    static void addPolygon(Polygon polygon, Polygonizer polygonizer){
        addLineString(polygon.getExteriorRing(), polygonizer);
        for(int n = polygon.getNumInteriorRing(); n-- > 0;){
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }


    static void addLineString(LineString lineString, Polygonizer polygonizer){

        if(lineString instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
        }

        // unioning the linestring with the point makes any self intersections explicit.
        Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
        Geometry toAdd = lineString.union(point);

        //Add result to polygonizer
        polygonizer.add(toAdd);
    }

    /**
     * Get a geometry from a collection of polygons.
     *
     * @param polygons collection
     * @param factory factory to generate MultiPolygon if required
     * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
     */
    static Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory){
        switch(polygons.size()){
            case 0:
                return null; // No valid polygons!
            case 1:
                return polygons.iterator().next(); // single polygon - no need to wrap
            default:
                return factory.createMultiPolygon(polygons.toArray(new Polygon[polygons.size()])); // multiple polygons - wrap them
        }
    }



}









class UniqueCoordinateArrayFilter implements CoordinateFilter {
    private Coordinate preCoordinate = null;
    //坐标微调值0.000000000001
    BigDecimal fixValue = BigDecimal.valueOf(0.000000000001);
    //每次处理过滤时，重新初始化
    public void reset(){
        preCoordinate = null;
    }
    public void filter(Coordinate coord) {
        //System.out.println(coord);
        if (coord.equals(preCoordinate)) {//如果与前一坐标相同，进行微调
            System.out.println("前一个坐标："+preCoordinate.x+" "+preCoordinate.y);
            System.out.println("当前坐标："+coord.x+" "+coord.y);
            coord.x = BigDecimal.valueOf(coord.x).add(fixValue).doubleValue();
            //coord.y = BigDecimal.valueOf(coord.y).add(fixValue).doubleValue();
            //preCoordinate.x=BigDecimal.valueOf(preCoordinate.x).add(fixValue).doubleValue();
            //preCoordinate.y=BigDecimal.valueOf(preCoordinate.y).add(fixValue).doubleValue();
        }
        preCoordinate = coord;
    }
}

