package io.github.bldl.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import io.github.bldl.astParsing.util.ClassData;
import io.github.bldl.astParsing.util.MethodData;
import io.github.bldl.astParsing.util.ParamData;
import io.github.bldl.astParsing.visitors.CastInsertionVisitor;
import io.github.bldl.astParsing.visitors.MethodCollector;
import io.github.bldl.astParsing.visitors.SubtypingCheckVisitor;
import io.github.bldl.astParsing.visitors.TypeEraserVisitor;
import io.github.bldl.astParsing.visitors.VariableCollector;
import io.github.bldl.graph.ClassHierarchyGraph;
import io.github.bldl.util.Pair;
import io.github.bldl.variance.annotations.MyVariance;

import com.github.javaparser.ast.body.Parameter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;
import org.apache.commons.io.FileUtils;

public class AstManipulator {
    private final Messager messager;
    private final String sourceFolder;
    private final SourceRoot sourceRoot;
    private final ClassHierarchyGraph<String> classHierarchy;
    public static final String OUTPUT_NAME = "output_javavariance";

    public AstManipulator(Messager messager, String sourceFolder) {
        this.messager = messager;
        this.sourceFolder = sourceFolder;
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(Paths.get("src/main/java")));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(sourceFolder));
        sourceRoot.getParserConfiguration().setSymbolResolver(symbolSolver);
        classHierarchy = (new ClassHierarchyComputer(sourceRoot, messager, sourceFolder)).computeClassHierarchy();
    }

    public void applyChanges() {
        this.sourceRoot.getCompilationUnits().forEach(cu -> {
            changePackageDeclaration(cu);
        });
        String outputPath = sourceFolder + "/" + OUTPUT_NAME;
        File dir = new File(outputPath);
        if (dir.exists() && dir.isDirectory()) {
            try {
                messager.printMessage(Kind.NOTE, "Cleaning output directory: " + OUTPUT_NAME);
                FileUtils.cleanDirectory(dir);
                messager.printMessage(Kind.NOTE, "Successfully cleaned output directory");
            } catch (IOException e) {
                messager.printMessage(Kind.WARNING, "Could not clean output directory:\n" + e.toString());
            }
        }
        messager.printMessage(Kind.NOTE, "Saving modified AST's to output directory");
        this.sourceRoot.saveAll(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class)
                        .resolve(Paths.get(outputPath)));
    }

    public SourceRoot getSourceRoot() {
        return sourceRoot;
    }

    public void eraseTypesAndInsertCasts(String cls, String packageName, Map<String, MyVariance> mp) {
        messager.printMessage(Kind.NOTE,
                String.format("Now parsing AST's for class %s", cls));
        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();
        eraseAnnotations(cls, packageName);
        ClassData classData = computeClassData(cls, packageName, mp);
        messager.printMessage(Kind.NOTE, "Collected class data:\n" + classData);
        Map<String, MethodData> methodMap = new HashMap<>();
        sourceRoot.parse(packageName, cls).accept(new MethodCollector(mp.keySet()),
                methodMap);

        messager.printMessage(Kind.NOTE, "Collected methods:\n" + methodMap.toString());
        changeAST(dir, classData, methodMap, "");
    }

    public void eraseAnnotations(String cls, String packageName) {
        Set<String> annotations = Set.of("MyVariance", "Covariant", "Contravariant");
        CompilationUnit cu = sourceRoot.parse(packageName, cls);
        cu.accept(new ModifierVisitor<Void>() {
            @Override
            public Visitable visit(Parameter n, Void arg) {
                n.getAnnotations().removeIf(annotation -> annotations.contains(annotation.getNameAsString()));
                return super.visit(n, arg);
            }

            public Visitable visit(TypeParameter n, Void arg) {
                n.getAnnotations().removeIf(annotation -> annotations.contains(annotation.getNameAsString()));
                return super.visit(n, arg);
            }
        }, null);
    }

    private ClassData computeClassData(String cls, String packageName, Map<String, MyVariance> mp) {
        CompilationUnit cu = sourceRoot.parse(packageName, cls);
        Map<String, ParamData> indexAndBound = new HashMap<>();
        var a = cu.findAll(ClassOrInterfaceDeclaration.class).get(0).getTypeParameters();
        for (int i = 0; i < a.size(); ++i) {
            TypeParameter type = a.get(i);
            NodeList<ClassOrInterfaceType> boundList = type.getTypeBound();
            String leftMostBound = boundList == null || boundList.size() == 0 ? "Object" : boundList.get(0).asString();
            if (mp.keySet().contains(type.getNameAsString())) {
                indexAndBound.put(type.getNameAsString(),
                        new ParamData(i, leftMostBound, mp.get(type.getNameAsString())));
            }

        }
        return new ClassData(cls.replaceFirst("\\.java$", ""), packageName, indexAndBound);
    }

    private void changeAST(File dir, ClassData classData, Map<String, MethodData> methodMap,
            String packageName) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (!fileName.equals(OUTPUT_NAME))
                    changeAST(file, classData, methodMap, appendPackageDeclaration(packageName, fileName));
                continue;
            }
            if (!isJavaFile(file))
                continue;

            CompilationUnit cu = sourceRoot.parse(packageName, fileName);

            Set<Pair<String, ClassOrInterfaceType>> varsToWatch = new HashSet<>();
            cu.accept(new VariableCollector(classData), varsToWatch);
            cu.accept(
                    new SubtypingCheckVisitor(collectMethodParams(cu, classData), messager, classData, classHierarchy),
                    null);
            cu.accept(new TypeEraserVisitor(classData), null);
            for (Pair<String, ClassOrInterfaceType> var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);
            }
        }
    }

    public static boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }

    private void changePackageDeclaration(CompilationUnit cu) {
        String newPackageName = OUTPUT_NAME + "." + cu.getPackageDeclaration().get().getNameAsString();
        cu.setPackageDeclaration(new PackageDeclaration(new Name(newPackageName)));
    }

    public static String appendPackageDeclaration(String existing, String toAppend) {
        if (existing.equals(""))
            return toAppend;
        return existing + "." + toAppend;
    }

    private Map<String, Map<Integer, Type>> collectMethodParams(CompilationUnit cu, ClassData classData) {
        Map<String, Map<Integer, Type>> methodParams = new HashMap<>();
        cu.findAll(MethodDeclaration.class).forEach(dec -> {
            String methodName = dec.getNameAsString();
            if (methodParams.containsKey(methodName)) {
                messager.printMessage(Kind.ERROR, "Duplicate methods inside a class. Can't handle polymorphism.");
                return;
            }
            methodParams.put(methodName, new HashMap<>());
            NodeList<Parameter> params = dec.getParameters();
            for (int i = 0; i < params.size(); ++i) {
                Type type = params.get(i).getType();
                methodParams.get(methodName).put(i, type);
            }
        });
        return methodParams;
    }
}