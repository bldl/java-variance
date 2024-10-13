package anthonisen.felix.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import anthonisen.felix.astParsing.util.ClassData;
import anthonisen.felix.astParsing.util.MethodData;
import anthonisen.felix.astParsing.visitors.CastInsertionVisitor;
import anthonisen.felix.astParsing.visitors.MethodCollector;
import anthonisen.felix.astParsing.visitors.TypeEraserVisitor;
import anthonisen.felix.astParsing.visitors.VariableCollector;
import anthonisen.felix.util.Pair;
import anthonisen.felix.astParsing.visitors.ClassCollector;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

public class Covariancer {
    private Messager messager;
    private String sourceFolder;
    private SourceRoot sourceRoot;

    public Covariancer(Messager messager, String sourceFolder) {
        this.messager = messager;
        this.sourceFolder = sourceFolder;
        sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(sourceFolder));
    }

    public void makeCovariant(String cls, String packageName) {
        messager.printMessage(Kind.NOTE, "Now parsing AST's");

        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();

        Set<ClassData> classesToWatch = computeClassesToWatch(dir, "");
        Map<String, MethodData> methodMap = new HashMap<>();

        sourceRoot.parse(packageName, cls).accept(new MethodCollector(Arrays.asList("T")),
                methodMap);
        changeAST(dir, classesToWatch, methodMap, "");

    }

    private Set<ClassData> computeClassesToWatch(File dir, String packageName) {
        Set<ClassData> classesToWatch = new HashSet<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                classesToWatch
                        .addAll(computeClassesToWatch(file, appendPackageDeclaration(packageName, file.getName())));
                continue;
            }
            CompilationUnit cu = sourceRoot.parse(packageName, file.getName());
            cu.accept(new ClassCollector(), classesToWatch);
        }
        return classesToWatch;
    }

    private void changeAST(File dir, Set<ClassData> classesToWatch, Map<String, MethodData> methodMap,
            String packageName) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                changeAST(file, classesToWatch, methodMap, appendPackageDeclaration(packageName, file.getName()));
                continue;
            }
            if (!isJavaFile(file))
                continue;

            CompilationUnit cu = sourceRoot.parse(packageName, file.getName());
            changePackageDeclaration(cu);
            Set<Pair<String, String>> varsToWatch = new HashSet<>();
            cu.accept(new VariableCollector(classesToWatch), varsToWatch);
            cu.accept(new TypeEraserVisitor(classesToWatch), null);
            for (Pair<String, String> var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);
            }
        }
    }

    public void applyChanges() {
        this.sourceRoot.saveAll(
                CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(Paths.get(sourceFolder + "/output")));
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