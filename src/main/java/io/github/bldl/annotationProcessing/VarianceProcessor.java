package io.github.bldl.annotationProcessing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.Type;

import java.lang.annotation.Annotation;

import io.github.bldl.annotationProcessing.annotations.Contravariant;
import io.github.bldl.annotationProcessing.annotations.Covariant;
import io.github.bldl.annotationProcessing.annotations.MyVariance;
import io.github.bldl.astParsing.AstManipulator;
import io.github.bldl.astParsing.util.TypeHandler;
import io.github.bldl.astParsing.visitors.ParameterTypeCollector;
import io.github.bldl.astParsing.visitors.ReturnTypeCollector;
import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.Diagnostic.Kind;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "io.github.bldl.annotationProcessing.annotations.MyVariance",
        "io.github.bldl.annotationProcessing.annotations.Covariant",
        "io.github.bldl.annotationProcessing.annotations.Contravariant",
})
public class VarianceProcessor extends AbstractProcessor {
    private Messager messager;
    private AstManipulator astManipulator;
    private Map<String, Map<String, MyVariance>> classes = new HashMap<>();
    private Map<String, String> packages = new HashMap<>();
    private final ImmutableList<Class<? extends Annotation>> supportedAnnotations = ImmutableList.of(MyVariance.class,
            Covariant.class, Contravariant.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.getElementsAnnotatedWithAny(Set.of(MyVariance.class,
                Covariant.class, Contravariant.class)).isEmpty())
            return false;
        boolean workHasBeenDone = false;
        messager = processingEnv.getMessager();
        astManipulator = new AstManipulator(messager,
                System.getProperty("user.dir") + "/src/main/java");
        messager.printMessage(Kind.NOTE, "Processing annotations:\n");
        for (Class<? extends Annotation> annotationType : supportedAnnotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(annotationType)) {
                workHasBeenDone = true;
                MyVariance annotation = e.getAnnotation(MyVariance.class);
                try {
                    if (annotationType.equals(Covariant.class))
                        annotation = TypeFactory.annotation(MyVariance.class,
                                Map.of("variance", VarianceType.COVARIANT, "strict", true));
                    else if (annotationType.equals(Contravariant.class))
                        annotation = TypeFactory.annotation(MyVariance.class,
                                Map.of("variance", VarianceType.CONTRAVARIANT, "strict", true));

                } catch (AnnotationFormatException ex) {
                }
                if (annotation != null)
                    processElement(annotation, e);
                else
                    messager.printMessage(Kind.WARNING, "Could not parse annotation for element: " + e);
            }
        }
        if (!workHasBeenDone) {
            messager.printMessage(Kind.NOTE, "No changes made. Not saving.");
            return false;
        }

        for (String className : classes.keySet()) {
            astManipulator.eraseTypesAndInsertCasts(className + ".java", packages.get(className),
                    classes.get(className));
        }

        astManipulator.applyChanges();
        return true;
    }

    private void processElement(MyVariance annotation, Element e) {
        // should not process method declarations
        if (!isClassParameter(e))
            return;

        TypeParameterElement tE = (TypeParameterElement) e;
        String className = tE.getEnclosingElement().getSimpleName().toString();
        String packageName = processingEnv.getElementUtils().getPackageOf(tE.getEnclosingElement()).toString();

        if (packageName.contains("output"))
            return;

        if (annotation.variance() == VarianceType.INVARIANT) {
            messager.printMessage(Kind.NOTE,
                    String.format(
                            "Invariant type parameter detected in class: %s\nWill not proceed with AST manipulation",
                            className));
        }

        packages.putIfAbsent(className, packageName);
        classes.putIfAbsent(className, new HashMap<>());
        classes.get(className).put(tE.getSimpleName().toString(), annotation);
        checkVariance(className, annotation, packageName, tE.getSimpleName().toString());
        // astManipulator.eraseTypesAndInsertCasts(className + ".java", packageName,
        // tE.getSimpleName().toString(), annotation);
    }

    private void checkVariance(String className, MyVariance annotation, String packageName, String typeOfInterest) {
        Set<Type> types = new HashSet<>();
        CompilationUnit cu = astManipulator.getSourceRoot().parse(packageName, className
                + ".java");
        if (annotation.variance() == VarianceType.CONTRAVARIANT)
            cu.accept(new ReturnTypeCollector(), types);
        else if (annotation.variance() == VarianceType.COVARIANT)
            cu.accept(new ParameterTypeCollector(), types);

        for (Type type : types) {
            if (TypeHandler.containsType(type, typeOfInterest)) {
                messager.printMessage(
                        annotation.strict() ? Kind.ERROR : Kind.WARNING,
                        String.format(
                                "%s is declared as %s, but does not conform to constraints: contains T in %s position",
                                className,
                                annotation.variance(),
                                annotation.variance() == VarianceType.COVARIANT ? "IN" : "OUT"));
            }
        }
    }

    private static boolean isClassParameter(Element e) {
        return e.getEnclosingElement().getKind().name().equals("CLASS");
    }
}
