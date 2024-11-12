package io.github.bldl.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
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

    public AstManipulator(Messager messager, String sourceFolder) {
        this.messager = messager;
        this.sourceFolder = sourceFolder;
        sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(sourceFolder));
    }

    public void eraseTypesAndInsertCasts(String cls, String packageName, String typeOfInterest) {
        messager.printMessage(Kind.NOTE, "Now parsing AST's");

        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();

        ClassData classData = computeClassData(cls, packageName, typeOfInterest);
        Map<String, MethodData> methodMap = new HashMap<>();

        sourceRoot.parse(packageName, cls).accept(new MethodCollector(Arrays.asList(typeOfInterest)),
                methodMap);

        changeAST(dir, classData, methodMap, "");

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
            cu.accept(new TypeEraserVisitor(classData), null);
            for (Pair<String, String> var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);
            }
        }
    }

    public ClassHierarchyGraph<String> computeClassHierarchy() {
        ClassHierarchyGraph<String> g = new ClassHierarchyGraph<>();
        computeClassHierarchyRec(g, Paths.get(sourceFolder).toFile(), "");
        return g;
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

    public void applyChanges() {
        this.sourceRoot.getCompilationUnits().forEach(cu -> changePackageDeclaration(cu));
        this.sourceRoot.saveAll(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(Paths.get(sourceFolder + "/output")));
    }

    public SourceRoot getSourceRoot() {
        return sourceRoot;
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
}