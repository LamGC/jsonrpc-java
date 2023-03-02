package net.lamgc.jsonrpc;

/**
 * 面向客户端的 JsonRpc 请求异常.
 */
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
