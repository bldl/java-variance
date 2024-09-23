package anthonisen.felix.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ClassCollector extends VoidVisitorAdapter<Set<String>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Set<String> arg) {
        super.visit(n, arg);
        if (n.getTypeParameters().isNonEmpty()) {
            arg.add(n.getNameAsString());
        }
    }
}
