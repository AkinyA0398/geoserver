package com.example.geoserver.dto;

import lombok.Getter;
import org.locationtech.jts.geom.Point;

@Getter
public class PointDTO {

    private final String type = "Point";
    private double[] coordinates;

    public PointDTO(Point point) {
        if (point != null) {
            this.coordinates = new double[] {
                point.getX(), // longitude
                point.getY()  // latitude
            };
        }
    }
}
