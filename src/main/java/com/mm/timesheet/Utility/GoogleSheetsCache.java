package com.mm.timesheet.Utility;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

import static com.mm.timesheet.Utility.GoogleSheetsConst.*;
import static com.mm.timesheet.Utility.GoogleSheetsUtils.getSheetsService;

public class GoogleSheetsCache {

    // Đặt đường dẫn file cache hợp lệ
    private static final Path CACHE_FILE = Paths.get(System.getProperty("java.io.tmpdir"), "location-cache.json");

    public static void saveToCache(List<Map<String, String>> data) {
        try (Writer writer = Files.newBufferedWriter(CACHE_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            new Gson().toJson(data, writer);
            System.out.println("✅ Cache saved to: " + CACHE_FILE.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Failed to save cache: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<Map<String, String>> loadFromCache() {
        if (!Files.exists(CACHE_FILE)) {
            System.out.println("ℹ️ Cache file does not exist: " + CACHE_FILE.toAbsolutePath());
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(CACHE_FILE, StandardCharsets.UTF_8)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> result = gson.fromJson(reader, listType);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Failed to load cache: " + e.getMessage());
            return new ArrayList<>();
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
        if (!cachedData.isEmpty()) {
            // Nếu cache có, trả về dữ liệu từ cache
            return cachedData;
        } else {
            // Nếu không có cache hoặc cache không hợp lệ, tải dữ liệu mới từ Google Sheets
            return getSheetDataFromGoogleSheets();
        }
    }
}
