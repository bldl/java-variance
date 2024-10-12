package anthonisen.felix.astParsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.type.ClassOrInterfaceType;

import anthonisen.felix.astParsing.util.TypeHandler;

public class TestTypeHandler {
    @Test
    public void sanityTestReplaceTypes() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, "List");
        type.setTypeArguments(new ClassOrInterfaceType(null, "Integer"));

        assertTrue(TypeHandler.replaceTypes(type, "Integer", "Object"));
        assertEquals("List<Object>", type.asString());
    }

    @Test
    public void testMultipleTypeParams() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, "Map");
        type.setTypeArguments(new ClassOrInterfaceType(null, "Integer"), new ClassOrInterfaceType(null, "Integer"));

        assertTrue(TypeHandler.replaceTypes(type, "Integer", "Double"));
        assertEquals("Map<Double,Double>", type.asString());
    }

    @Test
    public void testContainsType() {
        ClassOrInterfaceType type = new ClassOrInterfaceType(null, "Map");
        type.setTypeArguments(new ClassOrInterfaceType(null, "List<Cat>"), new ClassOrInterfaceType(null, "Integer"));

        assertTrue(TypeHandler.containsType(type, "Map"));
        assertTrue(TypeHandler.containsType(type, "List<Cat>"));
        assertTrue(TypeHandler.containsType(type, "Integer"));
        assertFalse(TypeHandler.containsType(type, "Double"));
    }

}
