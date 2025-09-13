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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
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

    @Override
    public List<List<Object>> loadCheckInOutHistory(String userLogin) throws Exception {
        List<Map<String, String>> sheetData = GoogleSheetsCache.getSheetData();
        for (Map<String, String> data : sheetData) {
            // Lấy giá trị user từ Map
            String user = data.get("user");
            String spreadsheetId = data.get("spreadsheetId");
            String range = data.get("range");

            // Kiểm tra xem user
            if (userLogin.equals(user)) {
                List<List<Object>> rows = getSheetData(spreadsheetId, range);
                // Lọc và xử lý dữ liệu theo yêu cầu
                List<List<Object>> filteredData = filterAndProcessData(rows, userLogin);
                return filteredData;
            }
        }
        return null;
    }
    // Lấy dữ liệu từ Google Sheets
    private List<List<Object>> getSheetData(String spreadsheetId, String range) throws Exception {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        return response.getValues();
    }
    // Hàm xử lý lọc và nhóm dữ liệu
    private List<List<Object>> filterAndProcessData(List<List<Object>> rows, String email) {
        // Lọc dữ liệu theo email
        List<List<Object>> filteredData = rows.stream()
                .filter(row -> row.size() > 3 && row.get(3).equals(email)) // Cột 4 (index 3) là email
                .collect(Collectors.toList());

        // Nhóm dữ liệu theo ngày và lọc check-in/check-out
        Map<String, Map<String, List<List<Object>>>> groupedData = new HashMap<>();

        for (List<Object> row : filteredData) {
            String date = row.get(4).toString().split(" ")[0]; // Lấy phần ngày từ cột thời gian
            String action = row.get(2).toString();

            groupedData.putIfAbsent(date, new HashMap<>());
            Map<String, List<List<Object>>> dayData = groupedData.get(date);
            dayData.putIfAbsent(action, new ArrayList<>());
            dayData.get(action).add(row);
        }

        // Lọc check-in và check-out
        List<List<Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<List<Object>>>> entry : groupedData.entrySet()) {
            String date = entry.getKey();
            Map<String, List<List<Object>>> actions = entry.getValue();

            // Lọc check-in (lấy check-in sớm nhất)
            List<List<Object>> checkinList = actions.get("checkin");
            if (checkinList != null && !checkinList.isEmpty()) {
                List<Object> earliestCheckin = checkinList.stream()
                        .min(Comparator.comparing(o -> o.get(4).toString())) // Lấy check-in sớm nhất
                        .orElse(null);
                if (earliestCheckin != null) result.add(earliestCheckin);
            }

            // Lọc check-out (lấy check-out muộn nhất)
            List<List<Object>> checkoutList = actions.get("checkout");
            if (checkoutList != null && !checkoutList.isEmpty()) {
                List<Object> latestCheckout = checkoutList.stream()
                        .max(Comparator.comparing(o -> o.get(4).toString())) // Lấy checkout muộn nhất
                        .orElse(null);
                if (latestCheckout != null) result.add(latestCheckout);
            }
        }

        // Sắp xếp kết quả theo ngày giảm dần
        result.sort((a, b) -> b.get(4).toString().compareTo(a.get(4).toString()));

        return result;
    }

    private void saveGoogleSheet(LocationRequestDto locationInfoDTO,
            String spreadsheetId, String range, String message) throws Exception {
        Sheets service = getSheetsService();
        // Lấy thời gian hiện tại (ngày tháng năm giờ phút)
//        String formattedDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        // Lấy thời gian theo múi giờ Việt Nam (GMT+7)
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Định dạng thời gian theo kiểu yyyy-MM-dd HH:mm
        String formattedDateTime = vietnamTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
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

