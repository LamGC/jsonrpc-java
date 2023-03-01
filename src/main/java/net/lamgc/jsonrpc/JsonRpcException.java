package net.lamgc.jsonrpc;

/**
 * JsonRpc 相关异常的父类.
 */
public class JsonRpcException extends RuntimeException {

    public JsonRpcException(String message) {
        super(message);
    }

    public JsonRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonRpcException(Throwable cause) {
        super(cause);
    }

    public JsonRpcException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
