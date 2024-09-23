package anthonisen.felix.astParsing.util;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class TypeReplacer {
    public static boolean replaceTypes(Type type, String targetTypeName, String newTypeName) {
        boolean found = false;
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) type;
            if (classType.asString().equals(targetTypeName)) {
                found = true;
                classType.replace(new ClassOrInterfaceType(null, newTypeName));
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
}
