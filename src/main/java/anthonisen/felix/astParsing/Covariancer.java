package anthonisen.felix.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import anthonisen.felix.astParsing.util.MethodData;
import anthonisen.felix.astParsing.visitors.CastInsertionVisitor;
import anthonisen.felix.astParsing.visitors.MethodCollector;
import anthonisen.felix.astParsing.visitors.TypeEraserVisitor;
import anthonisen.felix.astParsing.visitors.VariableCollector;
import anthonisen.felix.astParsing.visitors.ClassCollector;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Covariancer {

    public void makeCovariant() {
        String sourceFolder = "example";
        SourceRoot sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(sourceFolder));
        MethodCollector collector = new MethodCollector(Arrays.asList("T"));
        Map<String, MethodData> methodMap = new HashMap<>();
        Set<String> classesToWatch = new HashSet<String>();
        sourceRoot.parse("", "Herd.java").accept(collector, methodMap);

        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();
        for (File file : dir.listFiles()) {
            CompilationUnit cu = sourceRoot.parse("", file.getName());
            cu.accept(new ClassCollector(), classesToWatch);
        }
        for (File file : dir.listFiles()) {
            CompilationUnit cu = sourceRoot.parse("", file.getName());
            Set<String> varsToWatch = new HashSet<>();
            cu.accept(new VariableCollector(classesToWatch), varsToWatch);
            cu.accept(new TypeEraserVisitor(), null);
            for (String var : varsToWatch) {
                CastInsertionVisitor castInsertionVisitor = new CastInsertionVisitor(var, methodMap);
                cu.accept(castInsertionVisitor, null);

            }
        }
        sourceRoot.saveAll(CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(Paths.get("output")));
    }
}
