package eci.arem.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eci.arem.exception.OrderErrorCode;
import eci.arem.exception.OrderException;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    static final ObjectMapper MAPPER = new ObjectMapper();

    private HttpResponse() {}

    /** Respuesta exitosa con un body serializado desde el objeto dado. */
    public static Map<String, Object> ok(int statusCode, Object body) {
        return build(statusCode, serialize(body));
    }

    /** Respuesta de error construida a partir de una OrderException. */
    public static Map<String, Object> fromException(OrderException ex) {
        OrderErrorCode code = ex.getErrorCode();
        Map<String, String> errorBody = Map.of(
                "error", code.name(),
                "message", code.getMessage()
        );
        return build(code.getError().getCode(), serialize(errorBody));
    }

    /** Respuesta de error genérica (para errores inesperados). */
    public static Map<String, Object> internalError(String message) {
        Map<String, String> errorBody = Map.of(
                "error", "INTERNAL_ERROR",
                "message", message
        );
        return build(500, serialize(errorBody));
    }

    private static Map<String, Object> build(int statusCode, String body) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("headers", Map.of("Content-Type", "application/json"));
        response.put("body", body);
        return response;
    }

    private static String serialize(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"SERIALIZATION_FAILED\"}";
        }
    }

    public static Map<String, Object> created(Object body, String locationPath) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 201);
        response.put("headers", Map.of(
                "Content-Type", "application/json",
                "Location", locationPath
        ));
        response.put("body", serialize(body));
        return response;
    }


}