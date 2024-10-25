package anthonisen.felix.astParsing.util;

import java.util.Map;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class TypeHandler {
    public static boolean replaceTypes(Type type, String targetTypeName, String newTypeName) {
        boolean found = false;
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
            if (classType.getNameAsString().equals(targetTypeName)) {
                found = true;
                classType.setName(newTypeName);
            }

            if (classType.getTypeArguments().isPresent()) {
                for (Type arg : classType.getTypeArguments().get()) {
                    found = replaceTypes(arg, targetTypeName, newTypeName) || found;
                }
            }
        }
        return found;
    }

    public static boolean replaceTypes(Type type, Map<String, String> classes) {
        boolean found = false;
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
            if (classes.containsKey(classType.getNameAsString())) {
                found = true;
                classType.replace(new ClassOrInterfaceType(null, classes.get(classType.getNameAsString())));
                classType.setName(classes.get(classType.getNameAsString()));
            }

            if (classType.getTypeArguments().isPresent()) {
                for (Type arg : classType.getTypeArguments().get()) {
                    found = replaceTypes(arg, classes) || found;
                }
            }
        }
        return found;
    }

    public static boolean containsType(Type sourceType, String targetType) {
        if (!(sourceType instanceof ClassOrInterfaceType))
            return sourceType.asString().equals(targetType);
        ClassOrInterfaceType type = (ClassOrInterfaceType) sourceType;
        boolean b = false;
        if (type.getTypeArguments().isPresent())
            for (Type arg : type.getTypeArguments().get())
                b = b || containsType(arg, targetType);

        return type.getNameAsString().equals(targetType) || b;

    }
}
