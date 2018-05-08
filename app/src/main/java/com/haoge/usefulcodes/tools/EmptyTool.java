package com.haoge.usefulcodes.tools;

import java.util.List;
import java.util.Map;

public class EmptyTool {

    public static boolean isEmpty(Object data) {
        if (data == null) {
            return true;
        } else if (data instanceof CharSequence) {
            return ((CharSequence) data).length() == 0;
        } else if (data instanceof List) {
            return ((List) data).isEmpty();
        } else if (data instanceof Map) {
            return ((Map) data).isEmpty();
        } else if (data.getClass().isArray()) {
            return ((Object[]) data).length == 0;
        }
        return false;
    }
}
