package anthonisen.felix.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import anthonisen.felix.astParsing.util.ClassData;
import anthonisen.felix.util.Pair;

import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

public class VariableCollector extends VoidVisitorAdapter<Set<Pair<String, String>>> {
    private final ClassData classData;

    public VariableCollector(ClassData classData) {
        this.classData = classData;
    }

    @Override
    public void visit(VariableDeclarator n, Set<Pair<String, String>> arg) {
        super.visit(n, arg);
        handleType(n.getNameAsString(), n.getType(), arg);
    }

    @Override
    public void visit(Parameter n, Set<Pair<String, String>> arg) {
        super.visit(n, arg);
        handleType(n.getNameAsString(), n.getType(), arg);
    }

    private void handleType(String varName, Type type, Set<Pair<String, String>> arg) {
        if (!(type instanceof ClassOrInterfaceType))
            return;

        ClassOrInterfaceType classType = (ClassOrInterfaceType) type;

        if (!classData.className().equals(classType.getNameAsString()))
            return; // visit typeparams, if present
        arg.add(new Pair<>(varName, classType.getTypeArguments().get().get(classData.indexOfParam()).toString()));
    }
}
