package net.lamgc.jsonrpc.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.lamgc.jsonrpc.JsonRpcRequest;
import net.lamgc.jsonrpc.JsonRpcUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParameterDeserializer implements ParameterDeserializer {

    @Override
    public final Object[] deserializer(Method method, JsonRpcRequest request) throws Exception {
        Parameter[] methodParams = method.getParameters();
        JsonElement params = request.getParams();
        List<Object> paramList = new ArrayList<>(methodParams.length);
        if (params.isJsonObject()) {
            if (!JsonRpcUtils.canUseNamedParameter(method)) {
                throw new IllegalArgumentException("Method does not support named parameters.");
            }
            JsonObject paramsObject = params.getAsJsonObject();
            if (paramsObject.size() != methodParams.length) {
                throw new ParameterCountMismatchException(methodParams.length, paramsObject.size());
            }
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter parameter = methodParams[i];
                JsonElement param = paramsObject.get(parameter.getName());
                paramList.add(toParameterObject(method, parameter, param));
            }
        } else if (params.isJsonArray()) {
            JsonArray paramsArray = params.getAsJsonArray();
            if (paramsArray.size() != methodParams.length) {
                throw new ParameterCountMismatchException(methodParams.length, paramsArray.size());
            }
            for (int i = 0; i < method.getParameters().length; i++) {
                Parameter parameter = methodParams[i];
                JsonElement param = paramsArray.get(i);
                paramList.add(toParameterObject(method, parameter, param));
            }
        }
        return paramList.toArray();
    }

    /**
     * 将 Json 形式的参数值转换成对应的参数类型.
     *
     * @param method    与转换相关的方法.
     * @param parameter 目标参数对象.
     * @param paramJson 从 RPC 请求传入的 JSON 形式参数.
     * @return 返回对应类型的参数对象.
     * @throws JsonParseException 如果转换失败, 可抛出异常.
     */
    protected abstract Object toParameterObject(Method method, Parameter parameter, JsonElement paramJson) throws Exception;

}
