package cbc.boot.myboot.controller.gis.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.operation.distance.DistanceOp;

/**
 * Created by chenbc on 2019/3/13.
 */
public class GeometryOperation {
    public static boolean equals(Geometry gSource, Geometry gDestination) {
        return gSource.equals(gDestination);
    }

    public static boolean disjoint(Geometry gSource, Geometry gDestination) {
        return gSource.disjoint(gDestination);
    }

    public static boolean intersects(Geometry gSource, Geometry gDestination) {
        return gSource.intersects(gDestination);
    }

    public static boolean touches(Geometry gSource, Geometry gDestination) {
        return gSource.touches(gDestination);
    }

    public static boolean crosses(Geometry gSource, Geometry gDestination) {
        return gSource.crosses(gDestination);
    }

    public static boolean within(Geometry gSource, Geometry gDestination) {
        return gSource.within(gDestination);
    }

    public static boolean contains(Geometry gSource, Geometry gDestination) {
        return gSource.contains(gDestination);
    }

    public static boolean overlaps(Geometry gSource, Geometry gDestination) {
        return gSource.overlaps(gDestination);
    }

    public static IntersectionMatrix relate(Geometry gSource, Geometry gDestination) {
        IntersectionMatrix matrix = gSource.relate(gDestination);
        return matrix;
    }

    public static double distance(Geometry gSource, Geometry gDestination) {
        return new DistanceOp(gSource, gDestination).distance();
    }

    public static Geometry buffer(Geometry gSource, double distance) {
        return gSource.buffer(distance);
    }

    public static Geometry convexHull(Geometry gSource) {
        return gSource.convexHull();
    }

    public static Geometry intersection(Geometry gSource, Geometry gDestination) {
        return gSource.intersection(gDestination);
    }

    public static Geometry union(Geometry gSource, Geometry gDestination) {
        return gSource.union(gDestination);
    }

    public static Geometry difference(Geometry gSource, Geometry gDestination) {
        return gSource.difference(gDestination);
    }

    public static Geometry symDifference(Geometry gSource, Geometry gDestination) {
        return gSource.symDifference(gDestination);
    }
}


