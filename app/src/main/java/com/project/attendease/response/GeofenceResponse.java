package com.project.attendease.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeofenceResponse {

    @SerializedName("location")
    private Location location;

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("radius")
    private double radius;

    @SerializedName("__v")
    private int version;

    public GeofenceResponse(Location location, String id, String name, double radius, int version) {
        this.location = location;
        this.id = id;
        this.name = name;
        this.radius = radius;
        this.version = version;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public static class Location {
        @SerializedName("type")
        private String type;

        @SerializedName("coordinates")
        private List<Double> coordinates;

        public Location(String type, List<Double> coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }
    }
}
