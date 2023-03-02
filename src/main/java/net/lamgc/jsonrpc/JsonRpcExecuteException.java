package net.lamgc.jsonrpc;

/**
 * 面向服务端实际处理者的 JsonRpc 执行异常.
 */
public class JsonRpcExecuteException extends JsonRpcException {

    private final JsonRpcError error;

    public JsonRpcExecuteException(JsonRpcError error) {
        super("JsonRpc execute error: [" + error.getCode() + "] " + error.getMessage());
        this.error = error;
    }

    public JsonRpcError getError() {
        return error;
    }

}
