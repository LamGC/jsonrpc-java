package net.lamgc.jsonrpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lamgc.jsonrpc.serializer.ParameterCountMismatchException;
import net.lamgc.jsonrpc.serializer.ParameterDeserializer;
import net.lamgc.jsonrpc.serializer.ParameterTypeMismatchException;
import net.lamgc.jsonrpc.serializer.ReturnValueSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

public abstract class JsonRpcExecutor {

    private final static Logger logger = LoggerFactory.getLogger(JsonRpcExecutor.class);

    private final Object handler;

    private final ParameterDeserializer parameterDeserializer;

    private final ReturnValueSerializer returnValueSerializer;

    /**
     * 构造一个 Json RPC 执行器.
     *
     * @param handler               实际执行的方法.
     * @param parameterDeserializer 参数反序列化器.
     * @param returnValueSerializer 返回值序列化器.
     */
    public JsonRpcExecutor(Object handler, ParameterDeserializer parameterDeserializer, ReturnValueSerializer returnValueSerializer) {
        this.handler = handler;
        this.parameterDeserializer = parameterDeserializer;
        this.returnValueSerializer = returnValueSerializer;
    }

    /**
     * 执行 JsonRpc 请求.
     *
     * @param request JsonRpc 请求对象.
     * @return 返回对应的 Json RPC 响应, 即使 Json RPC 请求不包含 id,
     * 也会返回一个 Json RPC 响应 (因为 JsonRpcExecutor 只负责执行请求).
     */
    public final JsonRpcResponse execute(JsonRpcRequest request) {
        try {
            JsonRpcUtils.validateRequest(request);
            logger.debug("Request validated: " + request);
        } catch (Exception e) {
            logger.error("Invalid request: " + request, e);
            return new JsonRpcResponse(JsonRpcErrors.INVALID_REQUEST
                    .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, false, false)),
                    request.getId());
        }

        Method method;
        try {
            method = findMethod(request);
            if (method == null) {
                throw new NoSuchElementException("Cannot find a method to process the request.");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Method found: " + method);
            }
        } catch (Exception e) {
            if (e instanceof NoSuchElementException) {
                logger.error("Method not found: " + request.getMethod(), e);
                return new JsonRpcResponse(JsonRpcErrors.METHOD_NOT_FOUND
                        .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, false, true)), request.getId());
            } else {
                logger.error("An exception occurred while finding the method: " + request.getMethod(), e);
                return new JsonRpcResponse(JsonRpcErrors.INTERNAL_ERROR
                        .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, false, true)), request.getId());
            }
        }

        Object[] params = new Object[0];
        if (method.getParameterCount() != 0) {
            try {
                params = parameterDeserializer.deserializer(method, request);
                if (params.length != method.getParameterCount()) {
                    throw new ParameterCountMismatchException(method.getParameterCount(), params.length);
                }
            } catch (Exception e) {
                if (e instanceof ParameterTypeMismatchException || e instanceof ParameterCountMismatchException) {
                    return new JsonRpcResponse(JsonRpcErrors.INVALID_PARAMS.toRpcError(), request.getId());
                }

                logger.error("An exception occurred while deserializing the parameters.", e);
                return new JsonRpcResponse(JsonRpcErrors.CONVERT_PARAMS_FAILURE
                        .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, true, true)),
                        request.getId());
            }
        } else if (request.getParams() != null) {
            JsonElement paramsJson = request.getParams();
            if (paramsJson instanceof JsonArray && ((JsonArray) paramsJson).size() != 0) {
                return new JsonRpcResponse(JsonRpcErrors.INVALID_PARAMS.toRpcError(), request.getId());
            } else if (paramsJson instanceof JsonObject && ((JsonObject) paramsJson).size() != 0) {
                return new JsonRpcResponse(JsonRpcErrors.INVALID_PARAMS.toRpcError(), request.getId());
            }
        }

        Object result;
        try {
            logger.debug("Invoking method: " + method.getDeclaringClass().getName() + "." + method.getName());
            result = method.invoke(handler, params);
            logger.debug("Method invoked.");
        } catch (Exception e) {
            logger.error("An exception occurred while invoking the method. (Method: " +
                    method.getDeclaringClass().getName() + "." + method.getName() + ")", e);
            if (e instanceof InvocationTargetException) {
                if (e.getCause() instanceof JsonRpcExecuteException) {
                    return new JsonRpcResponse(((JsonRpcExecuteException) e.getCause()).getError(), request.getId());
                }
                return new JsonRpcResponse(JsonRpcErrors.INTERNAL_ERROR
                        .toRpcError(JsonRpcUtils.exceptionToJsonObject(e.getCause(), true, true)),
                        request.getId());
            }
            return new JsonRpcResponse(JsonRpcErrors.INTERNAL_ERROR
                    .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, false, true)),
                    request.getId());
        }

        try {
            JsonElement resultJson = returnValueSerializer.serializer(method, result);
            logger.debug("Request processed successfully.");
            return new JsonRpcResponse(resultJson, request.getId());
        } catch (Exception e) {
            logger.error("An exception occurred while serializing the return value.", e);
            return new JsonRpcResponse(
                    JsonRpcErrors.CONVERT_RETURN_VALUE_FAILURE
                            .toRpcError(JsonRpcUtils.exceptionToJsonObject(e, false, true)),
                    request.getId());
        }
    }

    /**
     * 根据 JsonRpcRequest 从 {@link #handler} 寻找对应的 {@link Method}.
     *
     * @param request 需要寻找方法的 JsonRpc 请求.
     * @return 返回对应的 {@link Method}.
     * @throws NoSuchElementException 当找不到合适的方法时抛出该异常. 抛出该异常后,
     *                                执行器将返回 {@link JsonRpcErrors#METHOD_NOT_FOUND} 错误响应.
     * @throws Exception              如果抛出除 {@link NoSuchElementException} 外的其他异常,
     *                                执行器将以 {@link JsonRpcErrors#INTERNAL_ERROR} 错误返回 RPC 响应.
     */
    protected abstract Method findMethod(JsonRpcRequest request) throws Exception;

    /**
     * 获取当前执行器所使用的 handler 对象.
     *
     * @return 返回 handler 对象.
     */
    public final Object getHandler() {
        return handler;
    }

}
