package io.github.yangziwen.quickdao.sql2o;

import java.util.Objects;

import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;
import org.sql2o.converters.EnumConverterFactory;

import io.github.yangziwen.quickdao.core.IEnum;

public class CustomEnumConverterFactory implements EnumConverterFactory {

    @Override
    public <E extends Enum> Converter<E> newConverter(Class<E> enumClass) {
        return new Converter<E>() {

            @Override
            public E convert(Object val) throws ConverterException {
                if (val == null) {
                    return null;
                }
                try {
                    if (IEnum.class.isAssignableFrom(enumClass)) {
                        for (E enumObj : enumClass.getEnumConstants()) {
                            Object enumValue = ((IEnum<?, ?>) enumObj).getValue();
                            if (Objects.equals(enumValue, val)) {
                                return enumObj;
                            }
                        }
                    } else if (val instanceof String){
                        return (E) Enum.valueOf(enumClass, val.toString());
                    } else if (val instanceof Number){
                        return enumClass.getEnumConstants()[((Number) val).intValue()];
                    }
                } catch (Throwable t) {
                    throw new ConverterException("Error converting value '" + val.toString() + "' to " + enumClass.getName(), t);
                }
                throw new ConverterException("Cannot convert type '" + val.getClass().getName() + "' to an Enum");
            }

            @Override
            public Object toDatabaseParam(Enum val) {
                if (val instanceof IEnum) {
                    return ((IEnum<?, ?>) val).getValue();
                } else {
                    return val.toString();
                }
            }
        };
    }

}
