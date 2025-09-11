package com.mm.timesheet.Dto;

public class CheckInAreaDto {
    private int id;
    private String name;
    private double lat;
    private double lon;
    private int radius;
    // ✅ Constructor mặc định (rất quan trọng cho Jackson)
    public CheckInAreaDto() {

    }

    // Constructor
    public CheckInAreaDto(int id, String name, double lat, double lon, int radius) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public String toString() {
        return "CheckInArea{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", radius=" + radius +
                '}';
    }
}
