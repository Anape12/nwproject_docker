package jp.nw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.Test;

class ReverseEngineeringLogicTest {
    @Test
    void scansDeployedClassesWithoutRunningClassInitializers() {
        ServletContext context = (ServletContext) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] { ServletContext.class }, (proxy, method, arguments) -> {
                    if ("getClassLoader".equals(method.getName())) {
                        return getClass().getClassLoader();
                    }
                    if ("getResourcePaths".equals(method.getName())) {
                        String path = (String) arguments[0];
                        if ("/WEB-INF/classes/jp/nw/".equals(path)) {
                            return Set.of("/WEB-INF/classes/jp/nw/model/");
                        }
                        if ("/WEB-INF/classes/jp/nw/model/".equals(path)) {
                            return Set.of("/WEB-INF/classes/jp/nw/model/ReverseEngineeringLogic.class");
                        }
                    }
                    return null;
                });

        Map<String, Object> result = new ReverseEngineeringLogic().javaClasses(context);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> classes = (List<Map<String, Object>>) result.get("classes");
        assertEquals(1, classes.size());
        assertEquals("jp.nw.model.ReverseEngineeringLogic", classes.get(0).get("name"));
        assertTrue(classes.get(0).containsKey("methods"));
    }
}
