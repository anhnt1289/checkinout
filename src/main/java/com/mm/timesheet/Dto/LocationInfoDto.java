package com.mm.timesheet.Dto;

public class LocationInfoDto {

    private double lat;
    private double lon;
    private String action;
    private String areaId;
    private String user;

    // Constructor
    public LocationInfoDto(double lat, double lon, String action, String areaId, String user) {
        this.lat = lat;
        this.lon = lon;
        this.action = action;
        this.areaId = areaId;
        this.user = user;
    }

    // Getters and Setters
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    // To String Method
    @Override
    public String toString() {
        return "LocationInfo{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", action='" + action + '\'' +
                ", areaId='" + areaId + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
