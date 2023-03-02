package net.lamgc.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.lamgc.jsonrpc.serializer.ParameterSerializationException;
import net.lamgc.jsonrpc.serializer.ParameterSerializer;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * JsonRpc 请求构建器.
 * <p>
 * 可通过该工具类构造一个符合规范的 JsonRpc 请求对象.
 */
public final class JsonRpcRequestBuilder {

    private final ParameterSerializer parameterSerializer;

    public JsonRpcRequestBuilder(ParameterSerializer parameterSerializer) {
        this.parameterSerializer = Objects.requireNonNull(parameterSerializer);
    }

    public JsonRpcRequest buildRequest(String method, JsonPrimitive id, Object... args) {
        Objects.requireNonNull(method);
        JsonElement serializedParams = null;
        if (args != null) {
            try {
                serializedParams = parameterSerializer.serializer(null, method, args);
            } catch (Exception e) {
                throw new ParameterSerializationException(e);
            }
        }
        return new JsonRpcRequest(method, serializedParams, id);
    }

    public JsonRpcRequest buildRequest(Method method, JsonPrimitive id, Object... args) {
        Objects.requireNonNull(method);
        JsonElement serializedParams = null;
        if (args != null) {
            try {
                serializedParams = parameterSerializer.serializer(method, method.getName(), args);
            } catch (Exception e) {
                throw new ParameterSerializationException(e);
            }
        }
        return new JsonRpcRequest(method.getName(), serializedParams, id);
    }

}
