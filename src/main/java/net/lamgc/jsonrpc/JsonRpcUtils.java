package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.lamgc.jsonrpc.serializer.json.JsonRpcRequestSerializer;
import net.lamgc.jsonrpc.serializer.json.JsonRpcResponseSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.regex.Pattern;

public final class JsonRpcUtils {

    private final static Pattern nonNamedParameterRegex = Pattern.compile("^arg\\d+$");

    private JsonRpcUtils() {
        throw new RuntimeException("The class does not allow instantiation.");
    }

    /**
     * 验证 JsonRpc 请求是否合法.
     *
     * @param request JsonRpc 请求对象.
     * @throws IllegalArgumentException 如果请求不合法, 则抛出异常.
     */
    public static void validateRequest(JsonRpcRequest request) {
        if (request.getMethod() == null || request.getMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Method cannot be null or empty.");
        } else if (request.getParams() != null &&
                (!request.getParams().isJsonArray() && !request.getParams().isJsonObject())) {
            throw new IllegalArgumentException("Parameter field type can only be JsonObject or JsonArray.");
        } else if (request.getId() != null) {
            if (!request.getId().isNumber() && !request.getId().isString()) {
                throw new IllegalArgumentException("Parameter field type can only be Number or String.");
            }
        }
    }

    /**
     * 将 Throwable 转换成 JsonObject.
     * <p>
     * 该对象无法直接反序列化为 Throwable, 仅用于调试.
     *
     * @param e              Throwable 对象.
     * @param withStackTrace 是否包括堆栈信息, 如果包括, 将会大大增加 JsonObject 的数据量.
     * @param withCause      是否包括异常原因, 如果为 true, 那么将会把每一层 cause 都加入到 JsonObject.
     * @return 返回带有 Throwable 对象的 JsonObject.
     */
    public static JsonObject exceptionToJsonObject(Throwable e, boolean withStackTrace, boolean withCause) {
        Objects.requireNonNull(e);
        JsonObject errorObject = new JsonObject();
        String message = e.getMessage();
        if (message == null) {
            message = "";
        } else {
            String localizedMsg = e.getLocalizedMessage();
            if (!message.equals(localizedMsg) && localizedMsg != null && !localizedMsg.isEmpty()) {
                errorObject.addProperty("localizedMessage", localizedMsg);
            }
        }
        errorObject.addProperty("message", message);
        errorObject.addProperty("exception", e.getClass().getName());

        if (withStackTrace) {
            JsonArray stackTrace = new JsonArray();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.add(element.toString());
            }
            errorObject.add("stackTrace", stackTrace);
        }

        if (withCause && e.getCause() != null) {
            errorObject.add("cause", exceptionToJsonObject(e.getCause(), withStackTrace, true));
        }
        return errorObject;
    }

    /**
     * 判断方法是否可以使用命名参数.
     *
     * <p>如果方法中全部参数的名称都符合 {@link #nonNamedParameterRegex} 的正则表达式, 则认为该方法不可以使用命名参数.
     *
     * @param method 要检查的方法.
     * @return 如果可用命名参数, 则返回 true.
     */
    public static boolean canUseNamedParameter(Method method) {
        for (Parameter parameter : method.getParameters()) {
            if (!nonNamedParameterRegex.matcher(parameter.getName()).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建一个用于 JsonRpc 的 Gson 对象.
     * <p>
     * 该对象将预先注册 {@link JsonRpcRequest} 和 {@link JsonRpcResponse} 的类型适配器,
     * 并启用 JsonNull 的序列化 ({@link GsonBuilder#serializeNulls()}), 以确保 JsonRpc 响应的 id 字段为 null 时可以被正确序列化.
     *
     * @return 返回一个可用于 JsonRpc 的 Gson 对象.
     */
    public static Gson createGsonForJsonRpc() {
        return new GsonBuilder()
                .registerTypeAdapter(JsonRpcRequest.class, new JsonRpcRequestSerializer())
                .registerTypeAdapter(JsonRpcResponse.class, new JsonRpcResponseSerializer())
                .serializeNulls()
                .create();
    }
}
