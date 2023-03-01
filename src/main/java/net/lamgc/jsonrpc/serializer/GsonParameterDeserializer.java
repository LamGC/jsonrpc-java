package net.lamgc.jsonrpc.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class GsonParameterDeserializer extends AbstractParameterDeserializer {

    private final Gson gson;

    public GsonParameterDeserializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected Object toParameterObject(Method method, Parameter parameter, JsonElement paramJson) {
        try {
            return gson.fromJson(paramJson, parameter.getType());
        } catch (JsonSyntaxException e) {
            throw new ParameterTypeMismatchException(method.getName(), parameter.getName(), parameter.getType(), e);
        }
    }
}
