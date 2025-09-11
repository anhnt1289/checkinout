package com.mm.timesheet.Controller;

import com.mm.timesheet.Dto.LocationInfoDto;
import com.mm.timesheet.Dto.ResponseDto;
import com.mm.timesheet.Service.GoogleSheetsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import static com.mm.timesheet.Utility.GoogleSheetsConst.CACHE_FILE;

@RestController
@RequestMapping("/api/location")
public class LocationController {
    private static final Logger logger = LogManager.getLogger(LocationController.class);
    private final GoogleSheetsService googleSheetsService;

    public LocationController(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @PostMapping("/send")
    public ResponseEntity<ResponseDto> sendLocation(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam String action,
            @RequestParam String areaId,
            @RequestParam String user) {

        logger.info("Received location data: lat={}, lon={}, action={}, areaId={}, user={}", lat, lon, action, areaId, user);

        ResponseDto responseDto;
        try {
            // Khởi tạo đối tượng LocationInfoDto
            LocationInfoDto locationInfoDTO = new LocationInfoDto(lat, lon, action, areaId, user);

            // Lưu dữ liệu vào Google Sheets và nhận thông điệp phản hồi
            StringBuilder message = googleSheetsService.saveCheckInOutToGoogleSheet(locationInfoDTO);

            // Kiểm tra và xử lý thông điệp trả về từ việc lưu vào Google Sheets
            String status = StringUtils.isEmpty(message.toString()) ? "error" : "success";
            String responseMessage = StringUtils.isEmpty(message.toString()) ?
                    String.format("User not exist: %s", user) : message.toString();

            // Tạo đối tượng ResponseDto với thông tin phản hồi
            responseDto = createResponse(status, responseMessage);

            logger.info("Response sent: status={}, message={}", responseDto.getStatus(), responseDto.getMessage());

            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            // Log lỗi khi xảy ra ngoại lệ
            logger.error("Error saving location data for user {}: {}", user, e.getMessage(), e);

            // Tạo đối tượng ResponseDto cho kết quả lỗi
            responseDto = createResponse("error", "Failed to save location data: " + e.getMessage());

            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/refreshCache")
    public ResponseEntity<ResponseDto> refreshCache() {
        logger.info("Refreshing cache...");
        ResponseDto responseDto;
        try {
            // Xóa cache cũ
            File cacheFile = new File(CACHE_FILE);
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            responseDto = createResponse("success", "Cache delete successfully.");
            logger.info("Cache delete successfully.");
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to refresh cache: {}", e.getMessage(), e);
            // Tạo đối tượng ResponseDto cho kết quả lỗi
            responseDto = createResponse("error", "Failed to refresh cache: " + e.getMessage());
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Hàm tạo ResponseDto
    private ResponseDto createResponse(String status, String message) {
        return new ResponseDto(status, message);
    }
}
