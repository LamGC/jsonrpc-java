package net.lamgc.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

/**
 * JsonRpc 请求对象.
 * <p>
 * 请务必通过 Gson 使用 {@link net.lamgc.jsonrpc.serializer.json.JsonRpcRequestSerializer} 进行序列化, 否则将不保证报文的正确性.
 */
public final class JsonRpcRequest {
    private final String method;
    private final JsonElement params;
    /**
     * 请求的标识符.
     * <p>
     * 如果不包含 id, 那么服务端将不会返回 RPC 响应.
     */
    private final JsonPrimitive id;

    /**
     * 构造一个 JsonRpc 请求对象.
     *
     * @param method 方法名称.
     * @param params 序列化后的参数列表.
     * @param id     请求的标识符, 如果请求不包含 id, 那么该请求将被服务端视为通知，且不会得到响应.
     * @throws NullPointerException 如果 method 为 null, 则抛出该异常.
     */
    public JsonRpcRequest(String method, JsonElement params, JsonPrimitive id) {
        this.method = Objects.requireNonNull(method);
        this.params = params;
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    /**
     * 返回结构化的参数列表.
     * <p>
     * param 只支持 {@linkplain  com.google.gson.JsonObject JsonObject}
     * 和 {@linkplain com.google.gson.JsonArray JsonArray}.
     * <p>
     * 根据请求方所掌握的调用信息, 请求方可能会以 JsonObject 传递参数列表 (请求方认为调用方支持命名参数);
     * 如果请求方以 JsonArray 传递参数列表, 则参数顺序需要与实际调用的参数列表对应.
     *
     * @return 返回序列化的参数列表.
     */
    public JsonElement getParams() {
        return params;
    }

    /**
     * 获取请求标识.
     * <p>
     * 如果请求是一个通知, 那么该请求将没有标识.
     *
     * @return 如果请求是一个通知, 则返回 null, 否则返回标识的 Json 对象.
     */
    public JsonPrimitive getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcRequest that = (JsonRpcRequest) o;
        return method.equals(that.method) && Objects.equals(params, that.params) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, params, id);
    }

    @Override
    public String toString() {
        return "JsonRpcRequest{" + "method='" + method + '\'' +
                ", params=" + params +
                ", id=" + id +
                '}';
    }
}
