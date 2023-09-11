package cbc.boot.myboot.controller.gis.util;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.util.Assert;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class WKTWriter
        extends com.vividsolutions.jts.io.WKTWriter {
    public static String toPoint(Coordinate p0) {
        return "POINT ( " + p0.x + " " + p0.y + " )";
    }

    public static String toLineString(CoordinateSequence seq) {
        StringBuffer buf = new StringBuffer();
        buf.append("LINESTRING ");
        if (seq.size() == 0) {
            buf.append(" EMPTY");
        } else {
            buf.append("(");
            for (int i = 0; i < seq.size(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(seq.getX(i) + " " + seq.getY(i));
            }
            buf.append(")");
        }
        return buf.toString();
    }

    public static String toLineString(Coordinate p0, Coordinate p1) {
        return "LINESTRING ( " + p0.x + " " + p0.y + ", " + p1.x + " " + p1.y + " )";
    }

    private static int INDENT = 2;

    private static DecimalFormat createFormatter(PrecisionModel precisionModel) {
        int decimalPlaces = precisionModel.getMaximumSignificantDigits();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0" + (decimalPlaces > 0 ? "." : "") +
                stringOfChar('#', decimalPlaces), symbols);
    }

    public static String stringOfChar(char ch, int count) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < count; i++) {
            buf.append(ch);
        }
        return buf.toString();
    }

    private int outputDimension = 2;
    private DecimalFormat formatter;
    private boolean isFormatted = false;
    private boolean useFormatting = false;
    private int level = 0;
    private int coordsPerLine = -1;
    private String indentTabStr = "  ";

    public WKTWriter() {
    }

    public WKTWriter(int outputDimension) {
        this.outputDimension = outputDimension;
        if ((outputDimension < 2) || (outputDimension > 3)) {
            throw new IllegalArgumentException("Invalid output dimension (must be 2 or 3)");
        }
    }

    @Override
    public void setFormatted(boolean isFormatted) {
        this.isFormatted = isFormatted;
    }

    @Override
    public void setMaxCoordinatesPerLine(int coordsPerLine) {
        this.coordsPerLine = coordsPerLine;
    }

    @Override
    public void setTab(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Tab count must be positive");
        }
        this.indentTabStr = stringOfChar(' ', size);
    }

    @Override
    public String write(Geometry geometry) {
        if (geometry == null) {
            return "EMPTY";
        }
        Writer sw = new StringWriter();
        try {
            writeFormatted(geometry, this.isFormatted, sw);
        } catch (IOException ex) {
            Assert.shouldNeverReachHere();
        }
        return sw.toString();
    }

    @Override
    public void write(Geometry geometry, Writer writer)
            throws IOException {
        writeFormatted(geometry, false, writer);
    }

    @Override
    public String writeFormatted(Geometry geometry) {
        Writer sw = new StringWriter();
        try {
            writeFormatted(geometry, true, sw);
        } catch (IOException ex) {
            Assert.shouldNeverReachHere();
        }
        return sw.toString();
    }

    @Override
    public void writeFormatted(Geometry geometry, Writer writer)
            throws IOException {
        writeFormatted(geometry, true, writer);
    }

    private void writeFormatted(Geometry geometry, boolean useFormatting, Writer writer)
            throws IOException {
        this.useFormatting = useFormatting;
        this.formatter = createFormatter(geometry.getPrecisionModel());
        appendGeometryTaggedText(geometry, 0, writer);
    }

    private void appendGeometryTaggedText(Geometry geometry, int level, Writer writer)
            throws IOException {
        indent(level, writer);
        if ((geometry instanceof Point)) {
            Point point = (Point) geometry;
            appendPointTaggedText(point.getCoordinate(), level, writer, point.getPrecisionModel());
        } else if ((geometry instanceof LinearRing)) {
            appendLinearRingTaggedText((LinearRing) geometry, level, writer);
        } else if ((geometry instanceof LineString)) {
            appendLineStringTaggedText((LineString) geometry, level, writer);
        } else if ((geometry instanceof Polygon)) {
            appendPolygonTaggedText((Polygon) geometry, level, writer);
        } else if ((geometry instanceof MultiPoint)) {
            appendMultiPointTaggedText((MultiPoint) geometry, level, writer);
        } else if ((geometry instanceof MultiLineString)) {
            appendMultiLineStringTaggedText((MultiLineString) geometry, level, writer);
        } else if ((geometry instanceof MultiPolygon)) {
            appendMultiPolygonTaggedText((MultiPolygon) geometry, level, writer);
        } else if ((geometry instanceof GeometryCollection)) {
            appendGeometryCollectionTaggedText((GeometryCollection) geometry, level, writer);
        } else {
            Assert.shouldNeverReachHere(
                    "Unsupported Geometry implementation:" + geometry.getClass());
        }
    }

    private void appendPointTaggedText(Coordinate coordinate, int level, Writer writer, PrecisionModel precisionModel)
            throws IOException {
        writer.write("POINT ");
        appendPointText(coordinate, level, writer, precisionModel);
    }

    private void appendLineStringTaggedText(LineString lineString, int level, Writer writer)
            throws IOException {
        writer.write("LINESTRING ");
        appendLineStringText(lineString, level, false, writer);
    }

    private void appendLinearRingTaggedText(LinearRing linearRing, int level, Writer writer)
            throws IOException {
        writer.write("LINEARRING ");
        appendLineStringText(linearRing, level, false, writer);
    }

    private void appendPolygonTaggedText(Polygon polygon, int level, Writer writer)
            throws IOException {
        writer.write("POLYGON ");
        appendPolygonText(polygon, level, false, writer);
    }

    private void appendMultiPointTaggedText(MultiPoint multipoint, int level, Writer writer)
            throws IOException {
        writer.write("MULTIPOINT ");
        appendMultiPointText(multipoint, level, writer);
    }

    private void appendMultiLineStringTaggedText(MultiLineString multiLineString, int level, Writer writer)
            throws IOException {
        writer.write("MULTILINESTRING ");
        appendMultiLineStringText(multiLineString, level, false, writer);
    }

    private void appendMultiPolygonTaggedText(MultiPolygon multiPolygon, int level, Writer writer)
            throws IOException {
        writer.write("MULTIPOLYGON ");
        appendMultiPolygonText(multiPolygon, level, writer);
    }

    private void appendGeometryCollectionTaggedText(GeometryCollection geometryCollection, int level, Writer writer)
            throws IOException {
        writer.write("GEOMETRYCOLLECTION ");
        appendGeometryCollectionText(geometryCollection, level, writer);
    }

    private void appendPointText(Coordinate coordinate, int level, Writer writer, PrecisionModel precisionModel)
            throws IOException {
        if (coordinate == null) {
            writer.write("EMPTY");
        } else {
            writer.write("(");
            appendCoordinate(coordinate, writer);
            writer.write(")");
        }
    }

    private void appendCoordinate(CoordinateSequence seq, int i, Writer writer)
            throws IOException {
        writer.write(writeNumber(seq.getX(i)) + " " + writeNumber(seq.getY(i)));
        if ((this.outputDimension >= 3) && (seq.getDimension() >= 3)) {
            double z = seq.getOrdinate(i, 3);
            if (!Double.isNaN(z)) {
                writer.write(" ");
                writer.write(writeNumber(z));
            }
        }
    }

    private void appendCoordinate(Coordinate coordinate, Writer writer)
            throws IOException {
        writer.write(writeNumber(coordinate.x) + " " + writeNumber(coordinate.y));
        if ((this.outputDimension >= 3) && (!Double.isNaN(coordinate.z))) {
            writer.write(" ");
            writer.write(writeNumber(coordinate.z));
        }
    }

    private String writeNumber(double d) {

        return this.formatter.format(d);
    }

    private void appendSequenceText(CoordinateSequence seq, int level, boolean doIndent, Writer writer)
            throws IOException {
        if (seq.size() == 0) {
            writer.write("EMPTY");
        } else {
            if (doIndent) {
                indent(level, writer);
            }
            writer.write("(");
            for (int i = 0; i < seq.size(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    if ((this.coordsPerLine > 0) &&
                            (i % this.coordsPerLine == 0)) {
                        indent(level + 1, writer);
                    }
                }
                appendCoordinate(seq, i, writer);
            }
            writer.write(")");
        }
    }

    private void appendLineStringText(LineString lineString, int level, boolean doIndent, Writer writer)
            throws IOException {
        if (lineString.isEmpty()) {
            writer.write("EMPTY");
        } else {
            if (doIndent) {
                indent(level, writer);
            }
            writer.write("(");
            for (int i = 0; i < lineString.getNumPoints(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    if ((this.coordsPerLine > 0) &&
                            (i % this.coordsPerLine == 0)) {
                        indent(level + 1, writer);
                    }
                }
                appendCoordinate(lineString.getCoordinateN(i), writer);
            }
            writer.write(")");
        }
    }

    private void appendPolygonText(Polygon polygon, int level, boolean indentFirst, Writer writer)
            throws IOException {
        if (polygon.isEmpty()) {
            writer.write("EMPTY");
        } else {
            if (indentFirst) {
                indent(level, writer);
            }
            writer.write("(");
            appendLineStringText(polygon.getExteriorRing(), level, false, writer);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                writer.write(", ");
                appendLineStringText(polygon.getInteriorRingN(i), level + 1, true, writer);
            }
            writer.write(")");
        }
    }

    private void appendMultiPointText(MultiPoint multiPoint, int level, Writer writer)
            throws IOException {
        if (multiPoint.isEmpty()) {
            writer.write("EMPTY");
        } else {
            writer.write("(");
            for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    indentCoords(i, level + 1, writer);
                }
                writer.write("(");
                appendCoordinate(((Point) multiPoint.getGeometryN(i)).getCoordinate(), writer);
                writer.write(")");
            }
            writer.write(")");
        }
    }

    private void appendMultiLineStringText(MultiLineString multiLineString, int level, boolean indentFirst, Writer writer)
            throws IOException {
        if (multiLineString.isEmpty()) {
            writer.write("EMPTY");
        } else {
            int level2 = level;
            boolean doIndent = indentFirst;
            writer.write("(");
            for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    level2 = level + 1;
                    doIndent = true;
                }
                appendLineStringText((LineString) multiLineString.getGeometryN(i), level2, doIndent, writer);
            }
            writer.write(")");
        }
    }

    private void appendMultiPolygonText(MultiPolygon multiPolygon, int level, Writer writer)
            throws IOException {
        if (multiPolygon.isEmpty()) {
            writer.write("EMPTY");
        } else {
            int level2 = level;
            boolean doIndent = false;
            writer.write("(");
            for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    level2 = level + 1;
                    doIndent = true;
                }
                appendPolygonText((Polygon) multiPolygon.getGeometryN(i), level2, doIndent, writer);
            }
            writer.write(")");
        }
    }

    private void appendGeometryCollectionText(GeometryCollection geometryCollection, int level, Writer writer)
            throws IOException {
        if (geometryCollection.isEmpty()) {
            writer.write("EMPTY");
        } else {
            int level2 = level;
            writer.write("(");
            for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
                if (i > 0) {
                    writer.write(", ");
                    level2 = level + 1;
                }
                appendGeometryTaggedText(geometryCollection.getGeometryN(i), level2, writer);
            }
            writer.write(")");
        }
    }

    private void indentCoords(int coordIndex, int level, Writer writer)
            throws IOException {
        if ((this.coordsPerLine <= 0) ||
                (coordIndex % this.coordsPerLine != 0)) {
            return;
        }
        indent(level, writer);
    }

    private void indent(int level, Writer writer)
            throws IOException {
        if ((!this.useFormatting) || (level <= 0)) {
            return;
        }
        writer.write("\n");
        for (int i = 0; i < level; i++) {
            writer.write(this.indentTabStr);
        }
    }
}

