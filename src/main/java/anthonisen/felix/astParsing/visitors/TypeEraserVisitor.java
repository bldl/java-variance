package anthonisen.felix.astParsing.visitors;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import anthonisen.felix.astParsing.util.ClassData;
import anthonisen.felix.astParsing.util.TypeReplacer;

public class TypeEraserVisitor extends ModifierVisitor<Void> {

    Map<String, String> classCasts = new HashMap<>();

    public TypeEraserVisitor(Set<ClassData> classData) {
        classData
                .forEach(data -> classCasts.put(data.className(), data.className() + "<" + data.leftmostBound() + ">"));
    }

    @Override
    public Visitable visit(VariableDeclarationExpr n, Void arg) {
        TypeReplacer.replaceTypes(n.getElementType(), classCasts);
        return super.visit(n, arg);
    }

    public Visitable visit(Parameter n, Void arg) {
        TypeReplacer.replaceTypes(n.getType(), classCasts);
        return super.visit(n, arg);
    }
}
