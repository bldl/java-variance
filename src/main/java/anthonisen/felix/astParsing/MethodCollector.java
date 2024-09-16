package anthonisen.felix.astParsing;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class MethodCollector extends VoidVisitorAdapter<Map<String, MethodData>> {

    Set<String> typeParameters = new HashSet<>();

    public MethodCollector(Collection<String> typeParameters) {
        this.typeParameters.addAll(typeParameters);
    }

    @Override
    public void visit(MethodDeclaration n, Map<String, MethodData> arg) {
        super.visit(n, arg);
        String signature = n.getSignature().asString();
        Type type = n.getType().clone();
        boolean shouldReplace = replaceTypes(type, "T", "Object"); // TODO get typeparams and bound
        arg.put(signature, new MethodData(shouldReplace, type.asString()));
    }

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

}
