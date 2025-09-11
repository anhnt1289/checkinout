package com.mm.timesheet.Utility;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.mm.timesheet.Utility.GoogleSheetsConst.*;
import static com.mm.timesheet.Utility.GoogleSheetsUtils.getSheetsService;

public class GoogleSheetsCache {

    // Lưu dữ liệu vào file JSON
    public static void saveToCache(List<Map<String, String>> data) {
        try (FileWriter writer = new FileWriter(CACHE_FILE)) {
            Gson gson = new Gson();
            gson.toJson(data, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Đọc dữ liệu từ file JSON
    public static List<Map<String, String>> loadFromCache() {
        try (FileReader reader = new FileReader(CACHE_FILE)) {
            Gson gson = new Gson();
            return gson.fromJson(reader, List.class);
        } catch (Exception e) {
            System.out.println("✅ File cache not exists.");
            return null;
        }
    }

    // Tải dữ liệu từ Google Sheets nếu cache không có
    public static List<Map<String, String>> getSheetDataFromGoogleSheets() throws Exception {
        // Thực hiện gọi Google Sheets API và lấy dữ liệu
        // Lưu dữ liệu vào cache sau khi lấy
        List<Map<String, String>> data = fetchDataFromGoogleSheets();
        saveToCache(data);
        return data;
    }

    // Fetch dữ liệu từ Google Sheets (đây là ví dụ, bạn cần thay thế bằng thực tế)
    private static List<Map<String, String>> fetchDataFromGoogleSheets() throws Exception {
        // Sử dụng Google Sheets API để lấy dữ liệu
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values()
                .get(SPREADSHEET_ID, RANGE_INFO)
                .execute();

        List<List<Object>> readValues = response.getValues();
        // Chuyển dữ liệu thành danh sách SheetData, bỏ qua dòng tiêu đề
        List<Map<String, String>> resultList = readValues.stream()
                // Bỏ qua dòng tiêu đề
                .skip(1)
                // Chuyển đổi mỗi dòng thành đối tượng SheetData
                .map(row -> {
                    if (row.size() >= 4) {
                        Map<String, String> map = new HashMap<>();
                        map.put("user", row.get(0).toString());
                        map.put("spreadsheetId", row.get(1).toString());
                        map.put("range", row.get(2).toString());
                        map.put("checkInAreasJson", row.get(3).toString());
                        return map;
                    }
                    return null;
                })
                // Lọc các giá trị null (nếu có dòng không hợp lệ)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // Sau khi lấy được dữ liệu, bạn trả về dưới dạng List<Map<String, String>>
        return resultList;
    }

    // Kiểm tra nếu cache còn hợp lệ
    public static List<Map<String, String>> getSheetData() throws Exception {
        // Kiểm tra xem có cache hay không và có hợp lệ không
        List<Map<String, String>> cachedData = loadFromCache();
        if (cachedData != null) {
            // Nếu cache có, trả về dữ liệu từ cache
            return cachedData;
        } else {
            // Nếu không có cache hoặc cache không hợp lệ, tải dữ liệu mới từ Google Sheets
            return getSheetDataFromGoogleSheets();
        }
    }
}
