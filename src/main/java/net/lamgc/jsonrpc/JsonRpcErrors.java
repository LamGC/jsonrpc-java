package net.lamgc.jsonrpc;

/**
 * 一些与 JsonRpc 有关的错误集合.
 */
public enum JsonRpcErrors {

    // ------------------ JSON RPC 2.0 Standard Error ------------------
    /**
     * 当 JsonRpc 报文解析失败时, 将返回该错误.
     * <p>
     * 一般来讲, 如果无法解析 JsonRpc 报文, 那么错误响应将不会包含 id (但 id 字段还是会存在的).
     */
    PARSE_ERROR(-32700, "Parse request error"),
    /**
     * 无效请求.
     * <p>
     * 如果 JsonRpc 报文不符合规范, 将返回该错误.
     */
    INVALID_REQUEST(-32600, "Invalid request"),
    /**
     * 请求的方法不存在.
     * <p>
     * JsonRpc 请求所指定的方法不存在时, 将返回该异常.
     */
    METHOD_NOT_FOUND(-32601, "Method not found"),
    /**
     * 无效的参数列表.
     * <p>
     * 如果参数列表无法通过参数转换器转换为方法所需的参数类型, 将返回该错误.
     * 解析过程中出现异常也会返回该错误.
     */
    INVALID_PARAMS(-32602, "Invalid params"),
    /**
     * 内部错误.
     * <p>
     * 如果由于服务端处理原因导致无法处理请求, 将返回该错误.
     */
    INTERNAL_ERROR(-32603, "Internal error"),

    // ------------------ JSON RPC IMPL Extension Error ------------------
    /**
     * 请求失败.
     * <p>
     * 仅限客户端.
     * <p>
     * 如果客户端发出请求失败, 将返回该错误.
     */
    REQUEST_FAILURE(-32901, "Request failure"),
    /**
     * 参数列表转换失败.
     * <p>
     * 如果客户端或服务端在序列化/反序列化参数列表时出现异常, 将返回该错误.
     */
    CONVERT_PARAMS_FAILURE(-32902, "Convert parameter failure"),
    /**
     * 返回值转换失败.
     * <p>
     * 如果客户端或服务端在序列化/反序列化返回值时出现异常, 将返回该错误.
     */
    CONVERT_RETURN_VALUE_FAILURE(-32903, "Convert return value failure"),

    ;
    public final int code;
    public final String message;

    JsonRpcErrors(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonRpcError toRpcError() {
        return new JsonRpcError(code, message, null);
    }

    public JsonRpcError toRpcError(Object data) {
        return new JsonRpcError(code, message, data);
    }

    public JsonRpcError toRpcError(String message, Object data) {
        return new JsonRpcError(code, message, data);
    }

}
