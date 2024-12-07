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
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;
import io.github.bldl.annotationProcessing.annotations.MyVariance;
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
        sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(AstManipulator.class).resolve(sourceFolder));
        classHierarchy = computeClassHierarchy();
    }

    public void applyChanges() {
        this.sourceRoot.getCompilationUnits().forEach(cu -> {
            changePackageDeclaration(cu);
        });
        String outputPath = sourceFolder + "/" + OUTPUT_NAME;
        messager.printMessage(Kind.NOTE, outputPath);
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

    public ClassHierarchyGraph<String> computeClassHierarchy() {
        ClassHierarchyGraph<String> g = new ClassHierarchyGraph<>();
        g.addVertex("Object");
        computeClassHierarchyRec(g, Paths.get(sourceFolder).toFile(), "");
        return g;
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
        return new ClassData(cls.replaceFirst("\\.java$", ""), indexAndBound);
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
                    new SubtypingCheckVisitor(collectMethodParams(cu, classData), collectMethodTypes(cu), messager,
                            varsToWatch, classData,
                            classHierarchy),
                    null);
            cu.accept(new TypeEraserVisitor(classData), null);
            for (Pair<String, ClassOrInterfaceType> var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);
            }
        }
    }

    private void computeClassHierarchyRec(ClassHierarchyGraph<String> g, File dir, String packageName) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (!fileName.equals(OUTPUT_NAME))
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
        }
    }

    private boolean isJavaFile(File file) {
        return file.getName().endsWith(".java");
    }

    private void changePackageDeclaration(CompilationUnit cu) {
        String newPackageName = OUTPUT_NAME + "." + cu.getPackageDeclaration().get().getNameAsString();
        cu.setPackageDeclaration(new PackageDeclaration(new Name(newPackageName)));
    }

    private String appendPackageDeclaration(String existing, String toAppend) {
        if (existing.equals(""))
            return toAppend;
        return existing + "." + toAppend;
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
                    mp.get(methodName).put(i,
                            type);
                }
            }
        });
        return mp;
    }

    private Map<String, Type> collectMethodTypes(CompilationUnit cu) {
        Map<String, Type> mp = new HashMap<>();
        cu.findAll(MethodDeclaration.class).forEach(dec -> {
            String methodName = dec.getNameAsString();
            mp.put(methodName, dec.getType());
        });
        return mp;
    }
}