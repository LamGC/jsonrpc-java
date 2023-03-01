package net.lamgc.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.util.Objects;

/**
 * JsonRpc 响应对象.
 * <p>
 * 请务必通过 Gson 使用 {@link net.lamgc.jsonrpc.serializer.json.JsonRpcResponseSerializer} 进行序列化, 否则将不保证报文的正确性.
 */
public final class JsonRpcResponse {

    /**
     * 响应的结果,
     * 只支持 {@linkplain JsonElement JsonElement} / {@linkplain JsonRpcError JsonRpcError} 和 null
     */
    private final Object result;
    /**
     * 响应的标识符.
     * <p> 与对应请求的 id 相同, 如果由于解析失败导致无法得到请求的 id, 那么 id 将为 JsonNull. </p>
     */
    private final JsonElement id;

    /**
     * 构造一个 Json RPC 响应对象.
     *
     * @param result 响应的结果,
     *               只支持 {@linkplain JsonElement JsonElement} / {@linkplain JsonRpcError JsonRpcError} 和 null
     * @param id     响应对应请求的标识, 如果无法从请求获取 id, 则传入 {@linkplain JsonNull JsonNull};
     *               如果针对通知请求构造响应对象, 则只需传递 null;
     *               如果请求处理成功, 则传入请求的标识.
     */
    public JsonRpcResponse(Object result, JsonElement id) {
        if (result instanceof JsonElement || result instanceof JsonRpcError || result == null) {
            this.result = result;
        } else {
            throw new IllegalArgumentException("Unsupported result type: " + result.getClass().getName());
        }
        if (id instanceof JsonNull || id instanceof JsonPrimitive || id == null) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("Unsupported id type: " + id.getClass().getName());
        }
    }

    /**
     * 响应的结果.
     *
     * @return 如果发生错误, 那么将返回 {@link JsonRpcError}, 否则返回返回值的 JSON 序列化形式.
     * 如果调用的方法或者 Rpc 请求的执行者没有返回值, 那么将返回 null.
     */
    public Object getResult() {
        return result;
    }

    /**
     * 获取该响应所对应的请求 Id.
     * <p>
     * 如果请求解析失败, 那么该 Id 为 {@link com.google.gson.JsonNull};
     * 如果请求成功但没有 Id (请求是一个通知), 那么该 Id 为 null.
     *
     * @return 根据情况返回对应的信息, 该返回值可能为 null.
     */
    public JsonElement getId() {
        return id;
    }

    /**
     * 结果是否为 {@link JsonRpcError}.
     *
     * @return 如果响应结果是一个 {@link JsonRpcError}, 那么将返回 true.
     */
    public boolean isError() {
        return result instanceof JsonRpcError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcResponse that = (JsonRpcResponse) o;
        return Objects.equals(result, that.result) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, id);
    }

    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "result=" + result +
                ", id=" + id +
                '}';
    }
}
