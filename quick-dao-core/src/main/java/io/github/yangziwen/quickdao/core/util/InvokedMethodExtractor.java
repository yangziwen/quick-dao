package io.github.yangziwen.quickdao.core.util;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.Getter;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class InvokedMethodExtractor<T>  implements MethodInterceptor {

    @Getter
    private Supplier<T> supplier;

    @Getter
    private Method latestInvokedMethod;

    public InvokedMethodExtractor(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public InvokedMethodExtractor<T> invokeMethod(Function<T, ?> lambda) {
        lambda.apply(supplier.get());
        return this;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        this.latestInvokedMethod = method;
        return null;
    }

    public String getLatestInvokedMethodName() {
        if (latestInvokedMethod == null) {
            return null;
        }
        return latestInvokedMethod.getName();
    }

}
