package anthonisen.felix.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import anthonisen.felix.astParsing.util.ClassData;

public class ClassCollector extends VoidVisitorAdapter<Set<ClassData>> {
    @Override
    public void visit(ClassOrInterfaceDeclaration n, Set<ClassData> arg) {
        super.visit(n, arg);
        if (n.getTypeParameters().isNonEmpty()) {
            NodeList<ClassOrInterfaceType> boundList = n.getTypeParameter(0).getTypeBound();
            arg.add(new ClassData(n.getNameAsString(),
                    boundList == null || boundList.size() == 0 ? "Object" : boundList.get(0).asString()));
        }
    }
}
