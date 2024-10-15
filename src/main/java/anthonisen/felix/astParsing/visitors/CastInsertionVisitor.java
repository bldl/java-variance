package anthonisen.felix.astParsing.visitors;

import java.util.Map;
import java.util.Optional;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

import anthonisen.felix.astParsing.util.MethodData;
import anthonisen.felix.util.Pair;

public class CastInsertionVisitor extends ModifierVisitor<Void> {
    private final Pair<String, String> ref;
    private final Map<String, MethodData> methodMap;

    public CastInsertionVisitor(Pair<String, String> ref, Map<String, MethodData> methodMap) {
        this.ref = ref;
        this.methodMap = methodMap;
    }

    @Override
    public Visitable visit(MethodCallExpr n, Void arg) {
        Optional<Expression> scope = n.getScope();
        if (scope.isEmpty() || !scope.get().getClass().equals(NameExpr.class))
            return super.visit(n, arg);
        NameExpr expr = (NameExpr) scope.get();
        if (expr.getNameAsString().equals(ref.first)) {
            MethodData data = methodMap.get(n.getNameAsString());
            if (data != null && data.shouldCast()) {
                String castString = data.castString().replace("*", ref.second);
                expr.setName("(" + castString + ") " + ref.first); // TODO use the correct methods to make a
                                                                   // castexpr
            }
        }
        return super.visit(n, arg);
    }
}
