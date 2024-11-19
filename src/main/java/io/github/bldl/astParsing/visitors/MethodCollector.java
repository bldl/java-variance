package io.github.bldl.astParsing.visitors;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import io.github.bldl.astParsing.util.MethodData;
import io.github.bldl.astParsing.util.TypeHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;

public class MethodCollector extends VoidVisitorAdapter<Map<String, MethodData>> {
    private final List<String> typeParameters = new ArrayList<>();

    public MethodCollector(Collection<String> typeParameters) {
        this.typeParameters.addAll(typeParameters);
    }

    @Override
    public void visit(MethodDeclaration n, Map<String, MethodData> arg) {
        super.visit(n, arg);
        String methodName = n.getNameAsString();
        Type type = n.getType().clone();
        boolean shouldReplace = false;
        for (int i = 0; i < typeParameters.size(); ++i) {
            String typeParameter = typeParameters.get(i);
            shouldReplace = shouldReplace || TypeHandler.replaceTypes(type, typeParameter, Integer.toString(i));
        }
        arg.put(methodName, new MethodData(shouldReplace, type.asString()));
    }

}
