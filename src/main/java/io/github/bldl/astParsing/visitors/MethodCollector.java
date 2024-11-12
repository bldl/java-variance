package io.github.bldl.astParsing.visitors;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import io.github.bldl.astParsing.util.MethodData;
import io.github.bldl.astParsing.util.TypeHandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

public class MethodCollector extends VoidVisitorAdapter<Map<String, MethodData>> {
    private final Set<String> typeParameters = new HashSet<>();

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
