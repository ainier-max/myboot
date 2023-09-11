package cbc.boot.myboot.controller.gis.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.ParseException;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;


public class WKTReader extends com.vividsolutions.jts.io.WKTReader {
    private static final String EMPTY = "EMPTY";
    private static final String COMMA = ",";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";
    private GeometryFactory geometryFactory;
    private PrecisionModel precisionModel;
    private StreamTokenizer tokenizer;

    public WKTReader() {
        this(new GeometryFactory());
    }

    public WKTReader(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
        this.precisionModel = geometryFactory.getPrecisionModel();
    }

    @Override
    public Geometry read(String wellKnownText)
            throws ParseException {
        StringReader reader = new StringReader(wellKnownText);
        try {
            return read(reader);
        } finally {
            reader.close();
        }
    }

    @Override
    public Geometry read(java.io.Reader reader)
            throws ParseException {
        this.tokenizer = new StreamTokenizer(reader);
        this.tokenizer.resetSyntax();
        this.tokenizer.wordChars(97, 122);
        this.tokenizer.wordChars(65, 90);
        this.tokenizer.wordChars(160, 255);
        this.tokenizer.wordChars(48, 57);
        this.tokenizer.wordChars(45, 45);
        this.tokenizer.wordChars(43, 43);
        this.tokenizer.wordChars(46, 46);
        this.tokenizer.whitespaceChars(0, 32);
        this.tokenizer.commentChar(35);
        try {
            return readGeometryTaggedText();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }

    private Coordinate[] getCoordinates()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return new Coordinate[0];
        }
        ArrayList coordinates = new ArrayList();
        coordinates.add(getPreciseCoordinate());
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            coordinates.add(getPreciseCoordinate());
            nextToken = getNextCloserOrComma();
        }
        Coordinate[] array = new Coordinate[coordinates.size()];
        return (Coordinate[]) coordinates.toArray(array);
    }

    private Coordinate getPreciseCoordinate()
            throws IOException, ParseException {
        Coordinate coord = new Coordinate();
        coord.x = getNextNumber();
        coord.y = getNextNumber();
        if (isNumberNext()) {
            coord.z = getNextNumber();
        }
        this.precisionModel.makePrecise(coord);
        return coord;
    }

    private boolean isNumberNext() throws IOException {
        int type = this.tokenizer.nextToken();
        this.tokenizer.pushBack();
        return type == -3;
    }

    private double getNextNumber()
            throws IOException, ParseException {
        int type = this.tokenizer.nextToken();
        switch (type) {
            case -3:
                try {
                    return Double.parseDouble(this.tokenizer.sval);
                } catch (NumberFormatException ex) {
                    throw new ParseException("Invalid number: " + this.tokenizer.sval);
                }
            default:
                System.out.println("default");
                break;

        }
        parseError("number");
        return 0.0D;
    }

    private String getNextEmptyOrOpener()
            throws IOException, ParseException {
        String nextWord = getNextWord();
        if ((EMPTY.equals(nextWord)) || ("(".equals(nextWord))) {
            return nextWord;
        }
        parseError("EMPTY or (");
        return null;
    }

    private String getNextCloserOrComma()
            throws IOException, ParseException {
        String nextWord = getNextWord();
        if ((",".equals(nextWord)) || ")".equals(nextWord)) {
            return nextWord;
        }
        parseError(", or )");
        return null;
    }

    private String getNextCloser()
            throws IOException, ParseException {
        String nextWord = getNextWord();
        if (")".equals(nextWord)) {
            return nextWord;
        }
        parseError(")");
        return null;
    }


    private String getNextWord()
            throws IOException, ParseException {
        int type = this.tokenizer.nextToken();
        switch (type) {
            case -3:
                String word = this.tokenizer.sval;
                if ("EMPTY".equalsIgnoreCase(word)) {
                    return "EMPTY";
                }
                return word;
            case 40:
                return "(";
            case 41:
                return ")";
            case 44:
                return ",";
            default:
                System.out.println("default");
        }
        parseError("word");
        return null;
    }

    private void parseError(String expected)
            throws ParseException {
        if (this.tokenizer.ttype == -2) {
            com.vividsolutions.jts.util.Assert.shouldNeverReachHere("Unexpected NUMBER token");
        }
        if (this.tokenizer.ttype == 10) {
            com.vividsolutions.jts.util.Assert.shouldNeverReachHere("Unexpected EOL token");
        }
        String tokenStr = tokenString();
        throw new ParseException("Expected " + expected + " but found " + tokenStr);
    }

    private String tokenString() {
        switch (this.tokenizer.ttype) {
            case -2:
                return "<NUMBER>";
            case 10:
                return "End-of-Line";
            case -1:
                return "End-of-Stream";
            case -3:
                return "'" + this.tokenizer.sval + "'";
            default:
                System.out.println("default");
        }
        return "'" + (char) this.tokenizer.ttype + "'";
    }

    private Geometry readGeometryTaggedText()
            throws IOException, ParseException {
        String type = null;
        try {
            type = getNextWord();
        } catch (IOException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
        if ("EMPTY".equals(type)) {
            return null;
        }
        if ("POINT".equalsIgnoreCase(type)) {
            return readPointText();
        }
        if (("LINESTRING".equalsIgnoreCase(type)) || ("LINESEGMENT".equalsIgnoreCase(type))) {
            return readLineStringText();
        }
        if ("LINEARRING".equalsIgnoreCase(type)) {
            return readLinearRingText();
        }
        if ("POLYGON".equalsIgnoreCase(type)) {
            return readPolygonText();
        }
        if ("MULTIPOINT".equalsIgnoreCase(type)) {
            return readMultiPointText();
        }
        if ("MULTILINESTRING".equalsIgnoreCase(type)) {
            return readMultiLineStringText();
        }
        if ("MULTIPOLYGON".equalsIgnoreCase(type)) {
            return readMultiPolygonText();
        }
        if ("GEOMETRYCOLLECTION".equalsIgnoreCase(type)) {
            return readGeometryCollectionText();
        }
        throw new ParseException("Unknown geometry type: " + type);
    }

    private Point readPointText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            System.out.println("cbc111111111111");
            // return this.geometryFactory.createPoint(null);
        }
        Point point = this.geometryFactory.createPoint(getPreciseCoordinate());
        getNextCloser();
        return point;
    }

    private LineString readLineStringText()
            throws IOException, ParseException {
        return this.geometryFactory.createLineString(getCoordinates());
    }

    private LinearRing readLinearRingText()
            throws IOException, ParseException {
        return this.geometryFactory.createLinearRing(getCoordinates());
    }

    private MultiPoint readMultiPointText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return this.geometryFactory.createMultiPoint(new Point[0]);
        }
        ArrayList pointStrings = new ArrayList();
        Point pointString = readPointText();
        pointStrings.add(pointString);
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            pointString = readPointText();
            pointStrings.add(pointString);
            nextToken = getNextCloserOrComma();
        }
        Point[] array = new Point[pointStrings.size()];
        return this.geometryFactory.createMultiPoint((Point[]) pointStrings.toArray(array));
    }

    private Point[] toPoints(Coordinate[] coordinates) {
        ArrayList points = new ArrayList();
        for (int i = 0; i < coordinates.length; i++) {
            points.add(this.geometryFactory.createPoint(coordinates[i]));
        }
        return (Point[]) points.toArray(new Point[0]);
    }

    private Polygon readPolygonText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return this.geometryFactory.createPolygon(this.geometryFactory
                    .createLinearRing(new Coordinate[0]), new LinearRing[0]);
        }
        ArrayList holes = new ArrayList();
        LinearRing shell = readLinearRingText();
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            LinearRing hole = readLinearRingText();
            holes.add(hole);
            nextToken = getNextCloserOrComma();
        }
        LinearRing[] array = new LinearRing[holes.size()];
        return this.geometryFactory.createPolygon(shell, (LinearRing[]) holes.toArray(array));
    }

    private MultiLineString readMultiLineStringText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return this.geometryFactory.createMultiLineString(new LineString[0]);
        }
        ArrayList lineStrings = new ArrayList();
        LineString lineString = readLineStringText();
        lineStrings.add(lineString);
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            lineString = readLineStringText();
            lineStrings.add(lineString);
            nextToken = getNextCloserOrComma();
        }
        LineString[] array = new LineString[lineStrings.size()];
        return this.geometryFactory.createMultiLineString((LineString[]) lineStrings.toArray(array));
    }

    private MultiPolygon readMultiPolygonText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return this.geometryFactory.createMultiPolygon(new Polygon[0]);
        }
        ArrayList polygons = new ArrayList();
        Polygon polygon = readPolygonText();
        polygons.add(polygon);
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            polygon = readPolygonText();
            polygons.add(polygon);
            nextToken = getNextCloserOrComma();
        }
        Polygon[] array = new Polygon[polygons.size()];
        return this.geometryFactory.createMultiPolygon((Polygon[]) polygons.toArray(array));
    }

    private GeometryCollection readGeometryCollectionText()
            throws IOException, ParseException {
        String nextToken = getNextEmptyOrOpener();
        if ("EMPTY".equals(nextToken)) {
            return this.geometryFactory.createGeometryCollection(new Geometry[0]);
        }
        ArrayList geometries = new ArrayList();
        Geometry geometry = readGeometryTaggedText();
        geometries.add(geometry);
        nextToken = getNextCloserOrComma();
        while (",".equals(nextToken)) {
            geometry = readGeometryTaggedText();
            geometries.add(geometry);
            nextToken = getNextCloserOrComma();
        }
        Geometry[] array = new Geometry[geometries.size()];
        return this.geometryFactory.createGeometryCollection((Geometry[]) geometries.toArray(array));
    }
}
