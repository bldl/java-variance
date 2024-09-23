package anthonisen.felix.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class VariableCollector extends VoidVisitorAdapter<Set<String>> {
    Set<String> classesToWatch;

    public VariableCollector(Set<String> classesToWatch) {
        this.classesToWatch = classesToWatch;
    }

    @Override
    public void visit(VariableDeclarator n, Set<String> arg) {
        super.visit(n, arg);
        handleType(n.getNameAsString(), n.getType(), arg);
    }

    @Override
    public void visit(Parameter n, Set<String> arg) {
        super.visit(n, arg);
        handleType(n.getNameAsString(), n.getType(), arg);
    }

    private void handleType(String varName, Type type, Set<String> arg) {
        if (!(type instanceof ClassOrInterfaceType))
            return;

        ClassOrInterfaceType classType = (ClassOrInterfaceType) type;

        if (!classesToWatch.contains(classType.getNameAsString()))
            return; // visit typeparams, if present
        arg.add(varName);
    }
}
