package com.mm.timesheet.Dto;

public class ResponseDto {
    private String status;
    private String message;

    // Constructor
    public ResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters và Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
