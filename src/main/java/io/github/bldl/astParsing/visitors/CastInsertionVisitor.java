package io.github.bldl.astParsing.visitors;

import java.util.Map;
import java.util.Optional;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.ast.type.Type;

import io.github.bldl.astParsing.util.MethodData;
import io.github.bldl.util.Pair;

public class CastInsertionVisitor extends ModifierVisitor<Void> {
    private final Pair<String, ClassOrInterfaceType> ref;
    private final Map<String, MethodData> methodMap;

    public CastInsertionVisitor(Pair<String, ClassOrInterfaceType> ref, Map<String, MethodData> methodMap) {
        this.ref = ref;
        this.methodMap = methodMap;
    }

    @Override
    public Visitable visit(MethodCallExpr n, Void arg) {
        Optional<Expression> scope = n.getScope();
        if (scope.isEmpty() || !(scope.get() instanceof NameExpr))
            return super.visit(n, arg);
        NameExpr expr = (NameExpr) scope.get();
        if (expr.getNameAsString().equals(ref.first)) {
            MethodData data = methodMap.get(n.getNameAsString());
            if (data != null && data.shouldCast()) {
                NodeList<Type> arguments = ref.second.getTypeArguments().get();
                String castString = data.castString();
                for (int i = 0; i < arguments.size(); ++i)
                    castString = data.castString().replace(Integer.toString(i), arguments.get(i).asString());
                ClassOrInterfaceType castType = new ClassOrInterfaceType(null, castString);
                CastExpr cast = new CastExpr(castType, n);
                EnclosedExpr enclosedCast = new EnclosedExpr(cast);
                return enclosedCast;
            }
        }
        return super.visit(n, arg);
    }
}
