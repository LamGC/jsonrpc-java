package net.lamgc.jsonrpc.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lamgc.jsonrpc.JsonRpcUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public abstract class AbstractParameterSerializer implements ParameterSerializer {

    private final boolean enableNamedParameter;

    public AbstractParameterSerializer(boolean enableNamedParameter) {
        this.enableNamedParameter = enableNamedParameter;
    }

    @Override
    public final JsonElement serializer(Method method, String methodName, Object[] parameters) {
        if (enableNamedParameter && method != null && JsonRpcUtils.canUseNamedParameter(method)) {
            JsonObject paramsObj = new JsonObject();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = method.getParameters()[i];
                paramsObj.add(parameter.getName(), serializerParameter(parameter.getType(), parameters[i]));
            }
            return paramsObj;
        } else if (method != null) {
            JsonArray paramsArray = new JsonArray();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = method.getParameters()[i];
                paramsArray.add(serializerParameter(parameter.getType(), parameters[i]));
            }
            return paramsArray;
        } else {
            JsonArray paramsArray = new JsonArray();
            for (Object parameter : parameters) {
                paramsArray.add(serializerParameter(parameter.getClass(), parameter));
            }
            return paramsArray;
        }
    }


    /**
     * 序列化参数
     *
     * @param expectType 参数的预期类型, 如果 {@linkplain AbstractParameterSerializer} 被调用时获得了 {@link Method},
     *                   那么 expectType 为对应参数的参数类型, 如果没有获得 Method 对象, 那么将传入 value 的类型.
     * @param value      传入的参数值.
     * @return 返回参数值的 JSON 序列化对象.
     */
    protected abstract JsonElement serializerParameter(Type expectType, Object value);

}
