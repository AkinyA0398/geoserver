package com.example.geoserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.InetAddress;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.HttpClientErrorException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private static final String INTERNET_CHECK_HOST = "8.8.8.8";
    private static final int INTERNET_CHECK_TIMEOUT = 5000;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("error")
    private String error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    public static String extractFirebaseError(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(responseBody);
            return node.path("error").path("message").asText();
        } catch (Exception ex) {
            return "UNKNOWN_ERROR";
        }
    }

    public static boolean checkInternetConnectivity() {
        try {
            InetAddress address = InetAddress.getByName(INTERNET_CHECK_HOST);
            return address.isReachable(INTERNET_CHECK_TIMEOUT);
        } catch (Exception e) {
            System.out.println("Internet connectivity check failed: " + e.getMessage());
            return false;
        }
    }
}