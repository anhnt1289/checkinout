package com.mm.timesheet.Dto;

import com.google.gson.Gson;
import java.util.List;

public class SheetData {
    private String user;
    private String spreadsheetId;
    private String range;
    private List<CheckInAreaDto> checkInAreaDtos;  // Mới thêm

    // Constructor
    public SheetData(String user, String spreadsheetId, String range, String checkInAreasJson) {
        this.user = user;
        this.spreadsheetId = spreadsheetId;
        this.range = range;

        // Chuyển đổi chuỗi JSON thành danh sách CheckInArea
        Gson gson = new Gson();
        this.checkInAreaDtos = gson.fromJson(checkInAreasJson, List.class); // Convert JSON string to List of CheckInArea
    }

    // Getters and Setters
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public List<CheckInAreaDto> getCheckInAreas() {
        return checkInAreaDtos;
    }

    public void setCheckInAreas(List<CheckInAreaDto> checkInAreaDtos) {
        this.checkInAreaDtos = checkInAreaDtos;
    }

    @Override
    public String toString() {
        return "SheetData{" +
                "user='" + user + '\'' +
                ", spreadsheetId='" + spreadsheetId + '\'' +
                ", range='" + range + '\'' +
                ", checkInAreas=" + checkInAreaDtos +
                '}';
    }
}
