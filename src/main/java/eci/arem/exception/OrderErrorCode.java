package eci.arem.exception;

public enum OrderErrorCode {

    NEGATIVE_QUANTITY("Quantity must be greater than 0", HttpStatusCode.BAD_REQUEST),
    BLANK_NULL("Value cannot be null or blank", HttpStatusCode.BAD_REQUEST),
    ORDER_NOT_FOUND("Order not found", HttpStatusCode.NOT_FOUND);

    private final String message;
    private final HttpStatusCode error;

    OrderErrorCode(String defaultMessage, HttpStatusCode httpStatus) {
        this.message = defaultMessage;
        this.error = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatusCode getError() {
        return error;
    }
}