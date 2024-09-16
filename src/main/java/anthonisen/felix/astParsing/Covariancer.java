package anthonisen.felix.astParsing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.SourceRoot;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Covariancer {

    public void makeCovariant() {
        String sourceFolder = "src/main/java/anthonisen/felix/example";
        SourceRoot sourceRoot = new SourceRoot(
                CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(sourceFolder));
        MethodCollector collector = new MethodCollector(Arrays.asList("T"));
        Map<String, MethodData> methodMap = new HashMap<>();
        sourceRoot.parse("", "Herd.java").accept(collector, methodMap);

        File dir = Paths.get(sourceFolder).toFile();
        assert dir.exists();
        assert dir.isDirectory();

        for (File file : dir.listFiles()) {
            CompilationUnit cu = sourceRoot.parse("", file.getName());
            CastInsertionVisitor vis = new CastInsertionVisitor("catHerd", methodMap);
            cu.accept(vis, null);
            System.out.println(cu);
        }

        sourceRoot.saveAll(CodeGenerationUtils.mavenModuleRoot(Covariancer.class).resolve(Paths.get("output")));

    }
}
