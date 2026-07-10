package jp.nw.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

    public static String getValue(HttpServletRequest request, String parameterName, String attributeName) {

        String value = request.getParameter(parameterName);

        if (value == null || value.isBlank()) {
            value = (String) request.getAttribute(attributeName);
        }

        return value;
    }
}
