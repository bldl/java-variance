package anthonisen.felix.astParsing.visitors;

import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import anthonisen.felix.astParsing.util.TypeReplacer;

public class TypeEraserVisitor extends ModifierVisitor<Void> {
    @Override
    public Visitable visit(VariableDeclarationExpr n, Void arg) {
        TypeReplacer.replaceTypes(n.getElementType(), "Herd<Cat>", "Herd<Animal>");
        return super.visit(n, arg);
    }
}
