package com.mm.timesheet.Service;

import com.mm.timesheet.Dto.LocationRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface GoogleSheetsService {
    StringBuilder saveCheckInOutToGoogleSheet(LocationRequestDto locationInfoDTO) throws Exception;
}

