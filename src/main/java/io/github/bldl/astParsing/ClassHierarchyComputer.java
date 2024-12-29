package io.github.bldl.astParsing;

import java.io.File;
import java.nio.file.Paths;

import javax.annotation.processing.Messager;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.utils.SourceRoot;

import io.github.bldl.graph.ClassHierarchyGraph;

public class ClassHierarchyComputer {

    private SourceRoot sourceRoot;
    private Messager messager;
    private String sourceFolder;

    public ClassHierarchyComputer(SourceRoot sourceRoot, Messager messager, String sourceFolder) {
        this.sourceRoot = sourceRoot;
        this.messager = messager;
        this.sourceFolder = sourceFolder;
    }

    public ClassHierarchyGraph<String> computeClassHierarchy() {
        ClassHierarchyGraph<String> g = new ClassHierarchyGraph<>();
        g.addVertex("Object");
        computeClassHierarchyRec(g, Paths.get(sourceFolder).toFile(), "");
        return g;
    }

    private void computeClassHierarchyRec(ClassHierarchyGraph<String> g, File dir, String packageName) {
        for (File file : dir.listFiles()) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                if (!fileName.equals(AstManipulator.OUTPUT_NAME))
                    computeClassHierarchyRec(g, file, AstManipulator.appendPackageDeclaration(packageName, fileName));
                continue;
            }
            if (!AstManipulator.isJavaFile(file))
                continue;

            CompilationUnit cu = sourceRoot.parse(packageName, fileName);

            cu.findAll(RecordDeclaration.class).forEach(record -> {
                NodeList<ClassOrInterfaceType> supertypes = record.getImplementedTypes();
                handleDeclaration(packageName, g, supertypes, record);
            });

            cu.findAll(EnumDeclaration.class).forEach(enm -> {
                NodeList<ClassOrInterfaceType> supertypes = enm.getImplementedTypes();
                handleDeclaration(packageName, g, supertypes, enm);
            });

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                NodeList<ClassOrInterfaceType> supertypes = cls.getExtendedTypes();
                supertypes.addAll(cls.getImplementedTypes());
                handleDeclaration(packageName, g, supertypes, cls);
            });
        }
    }

    private void handleDeclaration(String packageName, ClassHierarchyGraph<String> g,
            NodeList<ClassOrInterfaceType> supertypes,
            TypeDeclaration<?> declaration) {
        g.addVertex(getQualifiedName(packageName, declaration.getNameAsString()));
        for (ClassOrInterfaceType supertype : supertypes) {
            if (!g.containsVertex(supertype.getNameAsString()))
                g.addVertex(supertype.getNameAsString());
            g.addEdge(supertype.getNameAsString(), declaration.getNameAsString());
        }
        if (supertypes.isEmpty())
            g.addEdge("Object", declaration.getNameAsString());
    }

    private String getQualifiedName(String packageName, String typeName) {
        return packageName + "." + typeName;
    }
}
