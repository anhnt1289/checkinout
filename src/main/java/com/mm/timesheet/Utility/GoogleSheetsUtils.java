package com.mm.timesheet.Utility;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static com.mm.timesheet.Utility.GoogleSheetsConst.APPLICATION_NAME;

@Component
public class GoogleSheetsUtils {
    private static String googleApiKey;

    @Value("${google.api.key}")
    private String googleApiKeyNonStatic;

    @PostConstruct
    private void init() {
        googleApiKey = googleApiKeyNonStatic;
    }

    public static Sheets getSheetsService() throws Exception {
//        FileInputStream serviceAccountStream = new FileInputStream(googleApiKey);
//        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream)
//                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(googleApiKey.getBytes(StandardCharsets.UTF_8)));
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Hàm tính khoảng cách giữa 2 điểm tọa độ
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Khoảng cách tính bằng mét
    }
}
