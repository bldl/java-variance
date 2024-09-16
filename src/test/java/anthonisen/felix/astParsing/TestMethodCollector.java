package anthonisen.felix.astParsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.type.ClassOrInterfaceType;

public class TestMethodCollector {
    @Test
    public void sanityTestReplaceTypes() {
        // Declare type List<Integer>
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, "List");
        type.setTypeArguments(new ClassOrInterfaceType(null, "Integer"));

        assertTrue(MethodCollector.replaceTypes(type, "Integer", "Object"));
        assertEquals("List<Object>", type.asString());
    }

    @Test
    public void testMultipleTypeParams() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, "Map");
        type.setTypeArguments(new ClassOrInterfaceType(null, "Integer"), new ClassOrInterfaceType(null, "Integer"));

        assertTrue(MethodCollector.replaceTypes(type, "Integer", "Double"));
        assertEquals("Map<Double,Double>", type.asString());
    }
}
