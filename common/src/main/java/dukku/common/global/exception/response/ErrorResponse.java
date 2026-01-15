package dukku.common.global.exception.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private String code;
    private String message;
    private int status;
    private String details;
    private String timestamp;

    public ErrorResponse(String code, String message, int status, String details) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.details = details;
        this.timestamp = LocalDateTime.now().toString();
    }

    public ErrorResponse(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now().toString();
    }
}
