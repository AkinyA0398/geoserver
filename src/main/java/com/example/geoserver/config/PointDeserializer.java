package com.example.geoserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.IOException;

public class PointDeserializer extends StdDeserializer<Point> {

    private static final GeometryFactory gf = new GeometryFactory();

    public PointDeserializer() {
        super(Point.class);
    }

    @Override
    public Point deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectNode node = p.getCodec().readTree(p);
        ArrayNode coords = (ArrayNode) node.get("coordinates");
        if (coords == null || coords.size() < 2) {
            throw new IOException("Coordinates manquantes pour le point");
        }
        double lng = coords.get(0).asDouble();
        double lat = coords.get(1).asDouble();
        return gf.createPoint(new Coordinate(lng, lat));
    }

}
