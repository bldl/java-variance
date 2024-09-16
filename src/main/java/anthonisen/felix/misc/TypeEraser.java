package anthonisen.felix.misc;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.TypeParameter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.github.javaparser.ast.type.Type;

public class TypeEraser {

    public void eraseA() {
        String sourceFilePath = "src/main/java/anthonisen/felix/testclasses/A.java";
        String sourceCode = "";

        try {
            sourceCode = Files.readString(Paths.get(sourceFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        CompilationUnit cu = StaticJavaParser.parse(sourceCode);

        cu.findAll(TypeParameter.class).forEach(typeParameter -> {
            System.out.println("Generic Type: " + typeParameter.getName() + typeParameter.getAnnotations());
        });

        cu.findAll(MethodDeclaration.class).forEach(method -> {
            method.getTypeParameters().forEach(typeParameter -> {
                System.out.println("Method Generic Type: " + typeParameter.getName());
            });
        });

        cu.findAll((ConstructorDeclaration.class)).forEach(constructor -> {
            constructor.getParameters().forEach(parameter -> {
                Type variableType = parameter.getType();
                // if (parameter.getType().isTypeParameter())
                // System.out.println("Constructor Generic Type: " + parameter.getName() + " "
                // + parameter.getType().);
            });
        });

        cu.findAll(VariableDeclarator.class).forEach(variable -> {
            Type variableType = variable.getType();
            if (variableType.isClassOrInterfaceType()) {
                // This handles cases like List<T>
                variableType.asClassOrInterfaceType().getTypeArguments().ifPresent(typeArguments -> {
                    typeArguments.forEach(typeArgument -> {
                        System.out.println(
                                "Variable/Field Generic Type: " + typeArgument + " " +
                                        variable.getNameAsString());
                    });
                });
            } else if (variableType.isTypeParameter()) {
                // This handles type parameters like T
                System.out
                        .println("Variable/Field Generic Type Parameter: " + variableType +
                                variable.getNameAsString());
            } else {
                System.out.println("Nothing found: " + variable.getNameAsString());
            }
        });
        // cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classOrInterfaceDeclaration
        // -> {
        // classOrInterfaceDeclaration.getTypeParameters().forEach(typeParameter -> {
        // if (typeParameter.getNameAsString().equals("T")) {
        // // Find all occurrences of T in the class
        // cu.findAll(SimpleName.class).forEach(name -> {
        // if (name.asString().equals("T")) {
        // System.out.println("Found 'T' at line " + name.getBegin().get().line + ": " +
        // name);
        // }
        // });
        // }
        // });
        // });
    }
}
