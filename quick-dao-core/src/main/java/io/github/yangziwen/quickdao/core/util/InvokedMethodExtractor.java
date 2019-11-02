package io.github.yangziwen.quickdao.core.util;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class InvokedMethodExtractor<T>  implements MethodInterceptor {

    private final T proxyInstance;

    private final Class<T> classType;

    @Getter
    private Method latestInvokedMethod;

    @SuppressWarnings("unchecked")
    public InvokedMethodExtractor(Class<T> classType) {
        this.classType = classType;
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(classType);
        enhancer.setCallback(this);
        this.proxyInstance = (T) enhancer.create();
    }

    public InvokedMethodExtractor<T> invokeMethod(Function<T, ?> lambda) {
        lambda.apply(proxyInstance);
        return this;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        this.latestInvokedMethod = method;
        return null;
    }

    private String getLatestInvokedMethodName() {
        if (latestInvokedMethod == null) {
            return null;
        }
        return latestInvokedMethod.getName();
    }

    public String extractFieldNameFromGetter(Function<T, ?> getter) {
        if (getter == null) {
            throw new IllegalArgumentException("getter method cannot be null!");
        }
        String getterName = invokeMethod(getter).getLatestInvokedMethodName();
        if (!StringUtils.startsWith(getterName, "get")) {
            throw new IllegalArgumentException(getter + " is not a valid getter method for instance of type " + classType.getName());
        }
        String fieldName = StringUtils.replaceOnce(getterName, "get", "");
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

}
