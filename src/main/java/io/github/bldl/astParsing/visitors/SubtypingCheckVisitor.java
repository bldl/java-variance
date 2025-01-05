package io.github.bldl.astParsing.visitors;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;

import io.github.bldl.annotationProcessing.annotations.MyVariance;
import io.github.bldl.astParsing.util.ClassData;
import io.github.bldl.graph.ClassHierarchyGraph;

public class SubtypingCheckVisitor extends VoidVisitorAdapter<Void> {
    private final Map<String, Map<Integer, Type>> methodParams;
    private final Messager messager;
    private final ClassData classData;
    private final ClassHierarchyGraph<String> classHierarchy;

    public SubtypingCheckVisitor(Map<String, Map<Integer, Type>> methodParams,
            Messager messager,
            ClassData classData,
            ClassHierarchyGraph<String> classHierarchy) {
        this.methodParams = methodParams;
        this.messager = messager;
        this.classData = classData;
        this.classHierarchy = classHierarchy;
    }

    public void visit(MethodCallExpr methodCall, Void arg) {
        super.visit(methodCall, arg);
        if (!methodParams.containsKey(methodCall.getNameAsString()))
            return;
        // ResolvedMethodDeclaration methodDeclaration = methodCall.resolve();
        for (int i = 0; i < methodCall.getArguments().size(); ++i) {
            ResolvedType argumentType = methodCall.getArgument(i).calculateResolvedType(),
                    parameterType = methodParams.get(methodCall.getNameAsString()).get(i).resolve();
            boolean valid = isValidSubtype(parameterType, argumentType);
            if (!valid)
                messager.printMessage(Kind.ERROR,
                        String.format("Invalid subtype for method call: %s", methodCall.toString()));
        }
    }

    public void visit(AssignExpr assignExpr, Void arg) {
        super.visit(assignExpr, arg);
        ResolvedType assignedType = assignExpr.getValue().calculateResolvedType(),
                assigneeType = assignExpr.getTarget().calculateResolvedType();
        boolean valid = isValidSubtype(assigneeType, assignedType);
        if (!valid)
            messager.printMessage(Kind.ERROR,
                    String.format("Invalid subtype for assignment expression call: %s\n%s is not a subtype of %s",
                            assignExpr.toString(),
                            assignedType.toString(), assigneeType.toString()));
    }

    public void visit(ForEachStmt n, Void arg) {
        super.visit(n, arg);
    }

    public void visit(VariableDeclarator declaration, Void arg) {
        super.visit(declaration, arg);
        ResolvedType assigneeType = declaration.getType().resolve();
        Optional<Expression> initializer = declaration.getInitializer();
        if (initializer.isEmpty())
            return;
        ResolvedType assignedType = initializer.get().calculateResolvedType();
        boolean valid = isValidSubtype(assigneeType,
                assignedType);
        if (!valid)
            messager.printMessage(Kind.ERROR,
                    String.format("Invalid subtype for variable declaration: %s\n %s is not a subtype of %s",
                            declaration.toString(),
                            assignedType.toString(), assigneeType.toString()));
    }

    private boolean isValidSubtype(ResolvedType aType, ResolvedType assignType) {
        if (!aType.isReferenceType() || !assignType.isReferenceType())
            return true;

        ResolvedReferenceType assigneeType = aType.asReferenceType(), assignedType = assignType.asReferenceType();
        if (!classHierarchy.containsVertex(assigneeType.getQualifiedName())) {
            messager.printMessage(Kind.WARNING,
                    String.format("%s is not a user defined type, so no subtyping checks can be made", assigneeType));
            return true;
        }
        if (!classHierarchy.containsVertex(assignedType.getQualifiedName())) {
            messager.printMessage(Kind.WARNING,
                    String.format("%s is not a user defined type, so no subtyping checks can be made", assignedType));
            return true;
        }
        if (!assigneeType.typeParametersValues().isEmpty() || !assignedType.typeParametersValues().isEmpty())
            return true;

        var assigneeArgs = assigneeType.typeParametersValues();
        var assignedArgs = assignedType.typeParametersValues();
        boolean isSubtype = true;
        // TODO update classData to use qualified name
        if (assigneeType.getQualifiedName().equals(classData.className())) {
            Map<Integer, MyVariance> paramVariance = new HashMap<>();
            for (var param : classData.params().values())
                paramVariance.put(param.index(), param.variance());
            for (int i = 0; i < assigneeArgs.size(); ++i) {
                isSubtype = isSubtype && isValidSubtype(assigneeArgs.get(i), assignedArgs.get(i));
                ResolvedType assigneeArgType = assigneeArgs.get(i);
                ResolvedType assignedArgType = assignedArgs.get(i);
                if (!paramVariance.containsKey(i) || !assigneeArgType.isReferenceType()
                        || !assignedArgType.isReferenceType())
                    continue;

                switch (paramVariance.get(i).variance()) {
                    case COVARIANT:
                        isSubtype = isSubtype && classHierarchy.isDescendant(
                                assigneeArgType.asReferenceType().getQualifiedName(),
                                assignedArgType.asReferenceType().getQualifiedName(), paramVariance.get(i).depth());
                        break;
                    case CONTRAVARIANT:
                        isSubtype = isSubtype && classHierarchy.isDescendant(
                                assignedArgType.asReferenceType().getQualifiedName(),
                                assigneeArgType.asReferenceType().getQualifiedName(), paramVariance.get(i).depth());
                        break;
                    case BIVARIANT:
                        isSubtype = isSubtype && classHierarchy.isDescendant(
                                assigneeArgType.asReferenceType().getQualifiedName(),
                                assignedArgType.asReferenceType().getQualifiedName(), paramVariance.get(i).depth())
                                || classHierarchy.isDescendant(
                                        assignedArgType.asReferenceType().getQualifiedName(),
                                        assigneeArgType.asReferenceType().getQualifiedName(),
                                        paramVariance.get(i).depth());
                        break;
                    case SIDEVARIANT:
                        isSubtype = isSubtype && classHierarchy.sameLevel(
                                assignedArgType.asReferenceType().getQualifiedName(),
                                assigneeArgType.asReferenceType().getQualifiedName());
                        break;
                    default:
                        return true;
                }
            }
            return isSubtype;
        }

        for (int i = 0; i < assigneeArgs.size(); ++i)
            isSubtype = isSubtype && isValidSubtype(assigneeArgs.get(i), assignedArgs.get(i));
        return classHierarchy.isDescendant(assigneeType.getQualifiedName(),
                assignedType.getQualifiedName(), -1);
    }
}
