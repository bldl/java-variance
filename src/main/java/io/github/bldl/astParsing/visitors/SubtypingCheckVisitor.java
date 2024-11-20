package io.github.bldl.astParsing.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import io.github.bldl.astParsing.util.ClassData;
import io.github.bldl.astParsing.util.ParamData;
import io.github.bldl.graph.ClassHierarchyGraph;
import io.github.bldl.util.Pair;

public class SubtypingCheckVisitor extends VoidVisitorAdapter<Void> {
    private final Map<String, Map<Integer, Type>> methodParams;
    private Map<String, Type> methodTypes;
    private final Messager messager;
    private final Map<String, ClassOrInterfaceType> varsToWatchMap = new HashMap<>();
    private final ClassData classData;
    private final ClassHierarchyGraph<String> classHierarchy;

    public SubtypingCheckVisitor(Map<String, Map<Integer, Type>> methodParams, Messager messager,
            Set<Pair<String, ClassOrInterfaceType>> varsToWatch, ClassData classData,
            ClassHierarchyGraph<String> classHierarchy) {
        this.methodParams = methodParams;
        this.messager = messager;
        this.classData = classData;
        this.classHierarchy = classHierarchy;
        varsToWatch.forEach(p -> {
            varsToWatchMap.put(p.first, p.second);
        });
    }

    public void visit(MethodCallExpr methodCall, Void arg) {
        super.visit(methodCall, arg);
        messager.printMessage(Kind.NOTE, methodCall.getNameAsString());
        if (!methodParams.containsKey(methodCall.getNameAsString()))
            return;
        for (Entry<Integer, Type> param : methodParams.get(methodCall.getNameAsString()).entrySet()) {
            Expression e = methodCall.getArgument(param.getKey());
            ClassOrInterfaceType argumentType = resolveType(e);
            if (argumentType == null) {
                messager.printMessage(Kind.WARNING, "Cannot resolve type for expression: " + e.toString());
                continue;
            }
            boolean valid = isValidSubtype((ClassOrInterfaceType) param.getValue(),
                    argumentType,
                    classData.params());
            if (!valid)
                messager.printMessage(Kind.ERROR,
                        String.format("Invalid subtype for method call: %s", methodCall.toString()));
        }
    }

    public void visit(AssignExpr assignExpr, Void arg) {
        super.visit(assignExpr, arg);
        ClassOrInterfaceType assignedType = resolveType(assignExpr.getValue()),
                assigneeType = resolveType(assignExpr.getTarget());
    }

    public void visit(ForEachStmt n, Void arg) {
        super.visit(n, arg);
    }

    public void visit(VariableDeclarator declaration, Void arg) {
        super.visit(declaration, arg);
        Type assigneeType = declaration.getType();
        if (!(assigneeType instanceof ClassOrInterfaceType))
            return;
        Optional<Expression> initializer = declaration.getInitializer();
        if (initializer.isEmpty())
            return;
        ClassOrInterfaceType assignedType = resolveType(initializer.get());
        boolean valid = isValidSubtype((ClassOrInterfaceType) assigneeType,
                assignedType,
                classData.params());
        if (!valid)
            messager.printMessage(Kind.ERROR,
                    String.format("Invalid subtype for variable declaration: %s", declaration.toString()));
    }

    private ClassOrInterfaceType resolveType(Expression e) {
        if (e instanceof EnclosedExpr)
            return resolveType(((EnclosedExpr) e).getInner());
        if (e instanceof UnaryExpr)
            return resolveType(((UnaryExpr) e).getExpression());
        if (e instanceof BinaryExpr) {
            BinaryExpr binExp = (BinaryExpr) e;
            ClassOrInterfaceType t1 = resolveType(binExp.getLeft()), t2 = resolveType(binExp.getRight());
            if (t1.asString().equals(t2.asString()))
                return t1;
            return null;
        }
        if (e instanceof MethodCallExpr) {
            Type t = methodTypes.get(((MethodCallExpr) e).getNameAsString());
            return t instanceof ClassOrInterfaceType ? (ClassOrInterfaceType) t : null;
        }
        if (e instanceof NameExpr)
            return varsToWatchMap.getOrDefault(((NameExpr) e).getNameAsString(), null);
        if (e instanceof FieldAccessExpr)
            return varsToWatchMap.getOrDefault(((FieldAccessExpr) e).getNameAsString(), null);
        if (e instanceof CastExpr) {
            Type t = ((CastExpr) e).getType();
            return t instanceof ClassOrInterfaceType ? (ClassOrInterfaceType) t : null;
        }
        if (e instanceof ObjectCreationExpr)
            return ((ObjectCreationExpr) e).getType();
        if (e instanceof ClassExpr) {
            Type t = ((ClassExpr) e).getType();
            return t instanceof ClassOrInterfaceType ? (ClassOrInterfaceType) t : null;
        }
        if (e instanceof InstanceOfExpr)
            return new ClassOrInterfaceType(null, "Boolean");
        return null;
    }

    private boolean isValidSubtype(ClassOrInterfaceType assigneeType, ClassOrInterfaceType assignedType,
            Map<String, ParamData> params) {
        if (!classHierarchy.containsVertex(assigneeType.getNameAsString())) {
            messager.printMessage(Kind.WARNING,
                    String.format("%s is not a user defined type, so no subtyping checks can be made", assigneeType));
            return true;
        }
        if (!classHierarchy.containsVertex(assignedType.getNameAsString())) {
            messager.printMessage(Kind.WARNING,
                    String.format("%s is not a user defined type, so no subtyping checks can be made", assignedType));
            return true;
        }
        return true;
        // switch (annotation.variance()) {
        // case COVARIANT:
        // return classHierarchy.isDescendant(assignedType, assigneeType);
        // case CONTRAVARIANT:
        // return classHierarchy.isDescendant(assigneeType, assignedType);
        // default:
        // return false;
        // }
    }
}
