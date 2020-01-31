package io.github.yangziwen.quickdao.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public interface IEnum<E extends Enum<E>, V> {

    V getValue();

    static Object extractEnumValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof IEnum) {
            return IEnum.class.cast(value).getValue();
        }
        else if (value instanceof Collection) {
            Collection<?> collection = Collection.class.cast(value);
            if (collection.isEmpty()) {
                return value;
            }
            if (collection.iterator().next() instanceof IEnum) {
                return collection.stream()
                        .map(IEnum.class::cast)
                        .map(IEnum::getValue)
                        .collect(Collectors.toList());
            }
        }
        else if (value instanceof IEnum[]) {
            IEnum<?, ?>[] array = (IEnum[]) value;
            return Arrays.stream(array)
                    .map(IEnum::getValue)
                    .toArray();
        }
        return null;
    }

}
