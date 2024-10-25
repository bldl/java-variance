package anthonisen.felix.astParsing.visitors;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import anthonisen.felix.astParsing.util.MethodData;
import anthonisen.felix.astParsing.util.TypeHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

public class MethodCollector extends VoidVisitorAdapter<Map<String, MethodData>> {

    Set<String> typeParameters = new HashSet<>();

    public MethodCollector(Collection<String> typeParameters) {
        this.typeParameters.addAll(typeParameters);
    }

    @Override
    public void visit(MethodDeclaration n, Map<String, MethodData> arg) {
        super.visit(n, arg);
        String methodName = n.getNameAsString();
        Type type = n.getType().clone();
        boolean shouldReplace = false;
        for (String typeParameter : typeParameters) {
            shouldReplace = shouldReplace || TypeHandler.replaceTypes(type, typeParameter, "*");
        }
        arg.put(methodName, new MethodData(shouldReplace, type.asString()));
    }

}
