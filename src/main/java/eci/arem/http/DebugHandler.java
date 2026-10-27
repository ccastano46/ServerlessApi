package eci.arem.http;

import java.util.Map;

public class DebugHandler {
    public static Map<String, Object> echo(Map<String, Object> event) {
        System.out.println("EVENT_DUMP: " + event);

        return Map.of(
                "statusCode", 200,
                "headers", Map.of("Content-Type", "application/json"),
                "body", "{\"ok\": true}"
        );
    }
}