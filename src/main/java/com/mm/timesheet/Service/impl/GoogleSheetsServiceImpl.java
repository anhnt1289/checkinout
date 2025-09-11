package com.mm.timesheet.Service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.mm.timesheet.Dto.CheckInAreaDto;
import com.mm.timesheet.Dto.LocationRequestDto;
import com.mm.timesheet.Service.GoogleSheetsService;
import com.mm.timesheet.Utility.GoogleSheetsCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static com.mm.timesheet.Utility.GoogleSheetsUtils.calculateDistance;
import static com.mm.timesheet.Utility.GoogleSheetsUtils.getSheetsService;
@Service
public class GoogleSheetsServiceImpl implements GoogleSheetsService {

    private static final Logger logger = LogManager.getLogger(GoogleSheetsServiceImpl.class);
    @Override
    public StringBuilder saveCheckInOutToGoogleSheet(LocationRequestDto locationInfoDTO) throws Exception {
        StringBuilder message = new StringBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        // Lấy dữ liệu từ cache hoặc từ Google Sheets nếu không có cache
        List<Map<String, String>> sheetData = GoogleSheetsCache.getSheetData();
        for (Map<String, String> data : sheetData) {
            // Lấy giá trị user từ Map
            String user = data.get("user");
            String spreadsheetId = data.get("spreadsheetId");
            String range = data.get("range");

            // Kiểm tra xem user
            if (locationInfoDTO.getUser().equals(user)) {
                String checkInAreasJson = data.get("checkInAreasJson");
                // Đổi kiểu JSON string -> List<CheckInAreaDto>
                List<CheckInAreaDto> checkInAreas = objectMapper.readValue(
                        checkInAreasJson,
                        new TypeReference<>() {
                        }
                );
                // Kiểm tra vị trí người dùng
                if (checkUserLocation(locationInfoDTO.getLat(), locationInfoDTO.getLon(),
                        locationInfoDTO.getAction(), checkInAreas, message)) {
                    saveGoogleSheet(locationInfoDTO, spreadsheetId, range, message.toString());
                }
                break;
            }
        }
        return message;
    }
    private void saveGoogleSheet(LocationRequestDto locationInfoDTO,
            String spreadsheetId, String range, String message) throws Exception {
        Sheets service = getSheetsService();
        // Lấy thời gian hiện tại (ngày tháng năm giờ phút)
        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Dữ liệu bạn muốn ghi vào Google Sheets
        List<List<Object>> values = List.of(
                Arrays.asList(locationInfoDTO.getLat(), locationInfoDTO.getLon(),
                        locationInfoDTO.getAction(), locationInfoDTO.getUser(), formattedDateTime, message)
        );

        ValueRange body = new ValueRange().setValues(values);
        // Ghi dữ liệu vào Google Sheets
        try {
        AppendValuesResponse appendResult = service.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .setIncludeValuesInResponse(true)
                .execute();
            logger.info("Data saved successfully to Google Sheets for user: {}", locationInfoDTO.getUser());
        } catch (Exception e) {
            logger.error("Error saving data to Google Sheets for user: {}. Error: {}", locationInfoDTO.getUser(), e.getMessage());
            throw new Exception("Failed to save data to Google Sheets", e);
        }
    }
    // Kiểm tra người dùng có nằm trong phạm vi check-in không
    private static boolean checkUserLocation(double userLat, double userLon, String action,
                                         List<CheckInAreaDto> checkinAreaDto, StringBuilder message) {
        boolean isInArea = false;

        for (CheckInAreaDto area : checkinAreaDto) {
            double distance = calculateDistance(userLat, userLon, area.getLat(), area.getLon());
            logger.info("Distance to {}: {} m", area.getName(), distance);
            // Kiểm tra nếu người dùng nằm trong phạm vi check-in
            if (distance <= area.getRadius()) {
                message.append(String.format("You are within the %s area: %s. Distance: %.2f m", action, area.getName(), distance));
                isInArea = true;
                break;
            }
        }

        if (!isInArea) {
            message.append(String.format("You are outside the %s area: Lat: %.2f Lon: %.2f ", action, userLat, userLon));
        }
        return isInArea;
    }
}

