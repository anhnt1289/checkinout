package com.mm.timesheet.Service;

import com.mm.timesheet.Dto.LocationInfoDto;
import org.springframework.stereotype.Service;

@Service
public interface GoogleSheetsService {
    StringBuilder saveCheckInOutToGoogleSheet(LocationInfoDto locationInfoDTO) throws Exception;
}

