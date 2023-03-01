package net.lamgc.jsonrpc.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.lamgc.jsonrpc.JsonRpcResponse;

import java.lang.reflect.Method;

/**
 * 使用 Gson 将 Rpc Response 中的 ReturnValue 转换为对应类型.
 * <p>
 * 如需支持某些复杂类型的转换, 建议实现类型的 {@link com.google.gson.JsonSerializer}
 * 和 {@link com.google.gson.JsonDeserializer}, 并创建自定义的 Gson 对象后传递到本转换器中.
 * <p>
 * 在没有特殊需求的情况下建议不要手动实现 ReturnValueDeserializer, 自定义 Gson 已经足够大部分使用场景了.
 */
public class GsonReturnValueDeserializer implements ReturnValueDeserializer {

    private final Gson gson;

    public GsonReturnValueDeserializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object deserializer(Method method, JsonRpcResponse response) {
        return gson.fromJson((JsonElement) response.getResult(), method.getReturnType());
    }
}
