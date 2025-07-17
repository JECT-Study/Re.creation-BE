package org.ject.recreation.core.support.error;

import java.util.LinkedHashMap;

public class ErrorData extends LinkedHashMap<String, Object> {

    public static ErrorData of(String key, Object value) {
        ErrorData ed = new ErrorData();
        ed.put(key, value);
        return ed;
    }

    public ErrorData and(String key, Object value) {
        this.put(key, value);
        return this;
    }
}

