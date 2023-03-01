package net.lamgc.jsonrpc;

import com.google.gson.Gson;
import net.lamgc.jsonrpc.serializer.GsonParameterDeserializer;
import net.lamgc.jsonrpc.serializer.GsonReturnValueSerializer;
import net.lamgc.jsonrpc.serializer.ParameterDeserializer;
import net.lamgc.jsonrpc.serializer.ReturnValueSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 简单的 Rpc 执行器实现.
 * <p>
 * 要求 handler 中的方法命名不能有重复, 不允许使用方法重载.
 */
public final class SimpleJsonRpcExecutor extends JsonRpcExecutor {

    private final Map<String, Method> nameToMethod = new HashMap<>();

    public SimpleJsonRpcExecutor(Object handler, Gson gson) {
        this(handler, new GsonParameterDeserializer(gson), new GsonReturnValueSerializer(gson));
    }

    public SimpleJsonRpcExecutor(Object handler, ParameterDeserializer parameterDeserializer, ReturnValueSerializer returnValueSerializer) {
        super(handler, parameterDeserializer, returnValueSerializer);
        for (Method method : getHandler().getClass().getDeclaredMethods()) {
            if (nameToMethod.containsKey(method.getName())) {
                throw new IllegalArgumentException("The use of overloaded methods is not supported: " + method.getName());
            }

            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            } else if (!method.canAccess(handler)) {
                continue;
            }

            nameToMethod.put(method.getName(), method);
        }
    }

    @Override
    protected Method findMethod(JsonRpcRequest request) {
        Method method = nameToMethod.get(request.getMethod());
        if (method == null) {
            throw new NoSuchElementException(request.getMethod());
        }
        return method;
    }
}
