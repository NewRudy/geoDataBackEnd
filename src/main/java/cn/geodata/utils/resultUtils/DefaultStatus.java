package cn.geodata.utils.resultUtils;

public enum DefaultStatus implements StatusCode {

    SUCCESS(0, "Operation Success"),
    FAILURE(1, "Application internal error"),
    NO_PARAM(2, "no parameters"),
    PARAM_ERROR(3, "Invalid parameters"),
    NOT_FOUND(4, "Resource not found");


    private final int code;
    private final String message;

    DefaultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return String.valueOf(this.code);
    }
}
