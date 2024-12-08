package io.github.bldl.astParsing.visitors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import io.github.bldl.astParsing.util.ClassData;
import io.github.bldl.astParsing.util.TypeHandler;

public class TypeEraserVisitor extends ModifierVisitor<Void> {
    private final ClassData classData;

    public TypeEraserVisitor(ClassData classData) {
        this.classData = classData;
    }

    @Override
    public Visitable visit(VariableDeclarationExpr n, Void arg) {
        for (var param : classData.params().values())
            TypeHandler.replaceTypeArgument(n.getElementType(), classData.className(), param.index(),
                    param.leftmostBound());
        return super.visit(n, arg);
    }

    public Visitable visit(Parameter n, Void arg) {
        for (var param : classData.params().values())
            TypeHandler.replaceTypeArgument(n.getType(), classData.className(), param.index(),
                    param.leftmostBound());
        return super.visit(n, arg);
    }

    @Override
    public Visitable visit(ObjectCreationExpr n, Void arg) {
        if (n.getType().getNameAsString().equals(classData.className()))
            n.getType().setTypeArguments(new NodeList<>());
        return super.visit(n, arg);
    }
}
