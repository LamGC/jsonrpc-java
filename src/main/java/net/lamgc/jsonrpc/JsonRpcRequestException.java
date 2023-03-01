package net.lamgc.jsonrpc;

public class JsonRpcRequestException extends JsonRpcException {

    private final JsonRpcError error;

    public JsonRpcRequestException(JsonRpcError error) {
        super("[" + error.getCode() + "] " + error.getMessage());
        this.error = error;
    }

    public JsonRpcRequestException(JsonRpcError error, Throwable cause) {
        super("[" + error.getCode() + "] " + error.getMessage(), cause);
        this.error = error;
    }

    public JsonRpcError getError() {
        return error;
    }
}
