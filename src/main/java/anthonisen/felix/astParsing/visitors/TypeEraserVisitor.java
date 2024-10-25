package anthonisen.felix.astParsing.visitors;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import anthonisen.felix.astParsing.util.ClassData;
import anthonisen.felix.astParsing.util.TypeHandler;

public class TypeEraserVisitor extends ModifierVisitor<Void> {

    private ClassData classData;

    public TypeEraserVisitor(ClassData classData) {
        this.classData = classData;
    }

    @Override
    public Visitable visit(VariableDeclarationExpr n, Void arg) {
        TypeHandler.replaceTypeArgument(n.getElementType(), classData.className(), classData.indexOfParam(),
                classData.leftmostBound());
        return super.visit(n, arg);
    }

    public Visitable visit(Parameter n, Void arg) {
        TypeHandler.replaceTypeArgument(n.getType(), classData.className(), classData.indexOfParam(),
                classData.leftmostBound());
        return super.visit(n, arg);
    }
}
