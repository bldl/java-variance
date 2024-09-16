package anthonisen.felix.misc;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class AstManipulator {

    /**
     * Replaces all declarations of {@link anthonisen.felix.testclasses.A} in
     * {@link anthonisen.felix.testclasses.B} with wildcard definitions. Then writes
     * the output to a new file
     */
    public void manipulateB() {
        String sourceFilePath = "src/main/java/anthonisen/felix/testclasses/B.java";
        String targetFilePath = "src/main/java/anthonisen/felix/testclasses/ManipulatedB.java";
        String sourceCode = "";

        try {
            sourceCode = Files.readString(Paths.get(sourceFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        CompilationUnit cu = StaticJavaParser.parse(sourceCode);

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            cls.setName("Manipulated" + cls.getName());
        });

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            cls.findAll(ConstructorDeclaration.class).forEach(constructor -> {
                if (constructor.getNameAsString().equals("B")) {
                    constructor.setName("ManipulatedB");
                }
            });
        });

        cu.findAll(VariableDeclarator.class)
                .forEach(variableDeclaration -> replace_with_wildcard(variableDeclaration.getType()));
        cu.findAll(Parameter.class).forEach(parameter -> replace_with_wildcard(parameter.getType()));

        String modifiedSourceCode = cu.toString();

        try {
            Files.write(Paths.get(targetFilePath), modifiedSourceCode.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void replace_with_wildcard(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType cIType = type.asClassOrInterfaceType();
            if (cIType.getNameAsString().equals("A")
                    && cIType.getTypeArguments().isPresent()) {
                cIType.getTypeArguments().get()
                        .replaceAll(arg -> new WildcardType(arg.asReferenceType()));
            }
        }
    }
}
