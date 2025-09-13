package com.mm.timesheet.Service;

import com.mm.timesheet.Dto.LocationRequestDto;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface GoogleSheetsService {
    StringBuilder saveCheckInOutToGoogleSheet(LocationRequestDto locationInfoDTO) throws Exception;
    List<List<Object>> loadCheckInOutHistory(String userLogin) throws Exception;

}

