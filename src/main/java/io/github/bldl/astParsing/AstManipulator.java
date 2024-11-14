package io.github.bldl.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import io.github.bldl.astParsing.util.ClassData;
import io.github.bldl.astParsing.util.MethodData;
import io.github.bldl.astParsing.visitors.CastInsertionVisitor;
import io.github.bldl.astParsing.visitors.MethodCollector;
import io.github.bldl.astParsing.visitors.TypeEraserVisitor;
import io.github.bldl.astParsing.visitors.VariableCollector;
import io.github.bldl.graph.ClassHierarchyGraph;
import io.github.bldl.util.Pair;
import com.github.javaparser.ast.body.Parameter;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

public class AstManipulator {
    private final Messager messager;
    private final String sourceFolder;
    private final SourceRoot sourceRoot;
    private final ClassHierarchyGraph<String> classHierarchy;

    public AstManipulator(Messager messager, String sourceFolder) {
        this.messager = messager;
        this.sourceFolder = sourceFolder;
        sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(sourceFolder));
        classHierarchy = computeClassHierarchy();
    }

    public void applyChanges() {
        this.sourceRoot.getCompilationUnits().forEach(cu -> {
            // messager.printMessage(Kind.NOTE, "Saving cu: " + cu.toString());
            changePackageDeclaration(cu);
        });
        this.sourceRoot.saveAll(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(Paths.get(sourceFolder + "/output")));
    }

    public SourceRoot getSourceRoot() {
        return sourceRoot;
    }

    public void eraseTypesAndInsertCasts(String cls, String packageName, String typeOfInterest) {
        messager.printMessage(Kind.NOTE,
                String.format("Now parsing AST's for class %s and type param %s", cls, typeOfInterest));
        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();

        ClassData classData = computeClassData(cls, packageName, typeOfInterest);
        messager.printMessage(Kind.NOTE, "Collected class data:\n" + classData);
        Map<String, MethodData> methodMap = new HashMap<>();

        sourceRoot.parse(packageName, cls).accept(new MethodCollector(Arrays.asList(typeOfInterest)),
                methodMap);

        messager.printMessage(Kind.NOTE, "Collected methods:\n" + methodMap.toString());
        changeAST(dir, classData, methodMap, "");
    }

    public ClassHierarchyGraph<String> computeClassHierarchy() {
        ClassHierarchyGraph<String> g = new ClassHierarchyGraph<>();
        g.addVertex("Object");
        computeClassHierarchyRec(g, Paths.get(sourceFolder).toFile(), "");
        return g;
    }

    private ClassData computeClassData(String cls, String packageName, String typeOfInterest) {
        CompilationUnit cu = sourceRoot.parse(packageName, cls);
        var a = cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getTypeParameters();
        for (int i = 0; i < a.size(); ++i) {
            TypeParameter type = a.get(i);
            NodeList<ClassOrInterfaceType> boundList = type.getTypeBound();
            String leftMostBound = boundList == null || boundList.size() == 0 ? "Object" : boundList.get(0).asString();
            if (type.getNameAsString().equals(typeOfInterest)) {
                a.get(i);
                return new ClassData(cls.replaceFirst("\\.java$", ""), leftMostBound, i);
            }

        }
        return null;
    }

    private void changeAST(File dir, ClassData classData, Map<String, MethodData> methodMap,
            String packageName) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (!fileName.equals("output"))
                    changeAST(file, classData, methodMap, appendPackageDeclaration(packageName, fileName));
                continue;
            }
            if (!isJavaFile(file))
                continue;

            CompilationUnit cu = sourceRoot.parse(packageName, fileName);

            Set<Pair<String, String>> varsToWatch = new HashSet<>();
            cu.accept(new VariableCollector(classData), varsToWatch);
            messager.printMessage(Kind.NOTE, "Collected variables to watch:\n" + varsToWatch);
            performSubtypingChecks(cu, classData, methodMap, varsToWatch);
            cu.accept(new TypeEraserVisitor(classData), null);
            for (Pair<String, String> var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);
            }
        }
    }

    private void computeClassHierarchyRec(ClassHierarchyGraph<String> g, File dir, String packageName) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (!fileName.equals("output"))
                    computeClassHierarchyRec(g, file, appendPackageDeclaration(packageName, fileName));
                continue;
            }
            if (!isJavaFile(file))
                continue;

            CompilationUnit cu = sourceRoot.parse(packageName, fileName);

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                NodeList<ClassOrInterfaceType> supertypes = cls.getExtendedTypes();
                supertypes.addAll(cls.getImplementedTypes());
                g.addVertex(cls.getNameAsString());
                for (ClassOrInterfaceType supertype : supertypes) {
                    if (!g.containsVertex(supertype.getNameAsString()))
                        g.addVertex(supertype.getNameAsString());
                    g.addEdge(supertype.getNameAsString(), cls.getNameAsString());
                }
                if (supertypes.isEmpty())
                    g.addEdge("Object", cls.getNameAsString());
            });
            ;
        }
    }

    private boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }

    private void changePackageDeclaration(CompilationUnit cu) {
        String newPackageName = "output." + cu.getPackageDeclaration().get().getNameAsString();
        cu.setPackageDeclaration(new PackageDeclaration(new Name(newPackageName)));
    }

    private String appendPackageDeclaration(String existing, String toAppend) {
        if (existing.equals(""))
            return toAppend;
        return existing + "." + toAppend;
    }

    private void performSubtypingChecks(CompilationUnit cu, ClassData classData,
            Map<String, MethodData> methodMap,
            Set<Pair<String, String>> varsToWatch) {
        Map<String, Map<Integer, Type>> methodParams = collectMethodParams(cu, classData);
        cu.findAll(MethodCallExpr.class).forEach(methodCall -> {
            if (!methodParams.containsKey(methodCall.getNameAsString()))
                return;
            for (Integer paramIndex : methodParams.get(methodCall.getNameAsString()).keySet()) {
                Expression e = methodCall.getArgument(paramIndex);
                if (!(e instanceof NameExpr)) {
                    messager.printMessage(Kind.WARNING, "Cannot resolve type for expression: " + e.toString());
                    continue;
                }
                String name = ((NameExpr) e).getNameAsString();
                varsToWatch.forEach(p -> {
                    if (p.first.equals(name)) {
                        // check subtyping
                    }
                });
            }

        });
        cu.findAll(AssignExpr.class).forEach(assignExpr -> {

            messager.printMessage(Kind.NOTE, assignExpr.toString());
            messager.printMessage(Kind.NOTE, assignExpr.getTarget().getClass().toString());
            messager.printMessage(Kind.NOTE, assignExpr.getValue().getClass().toString());
        });
        // cu.findAll(ForEachStmt.class).forEach(stmt -> {

        // });
    }

    private Map<String, Map<Integer, Type>> collectMethodParams(CompilationUnit cu, ClassData classData) {
        Map<String, Map<Integer, Type>> mp = new HashMap<>();
        cu.findAll(MethodDeclaration.class).forEach(dec -> {
            NodeList<Parameter> params = dec.getParameters();
            for (int i = 0; i < params.size(); ++i) {
                Parameter param = params.get(i);
                if (!(param.getType() instanceof ClassOrInterfaceType))
                    continue;
                ClassOrInterfaceType type = ((ClassOrInterfaceType) param.getType());
                String methodName = dec.getNameAsString();
                if (type.getNameAsString().equals(classData.className())) {
                    mp.putIfAbsent(methodName, new HashMap<>());
                    mp.get(methodName).put(i, type.getTypeArguments().get().get(classData.indexOfParam()));
                }
            }
        });
        return mp;
    }

    private String resolveType() {
        return null;
    }

}