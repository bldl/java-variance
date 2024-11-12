package io.github.bldl.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ReturnTypeCollector extends VoidVisitorAdapter<Set<Type>> {
    @Override
    public void visit(MethodDeclaration n, Set<Type> arg) {
        super.visit(n, arg);
        arg.add(n.getType());
    }
}
