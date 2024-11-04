package anthonisen.felix.annotationProcessing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.Type;

import java.lang.annotation.Annotation;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
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

import anthonisen.felix.annotationProcessing.annotations.Contravariant;
import anthonisen.felix.annotationProcessing.annotations.Covariant;
import anthonisen.felix.annotationProcessing.annotations.MyVariance;
import anthonisen.felix.astParsing.AstManipulator;
import anthonisen.felix.astParsing.util.TypeHandler;
import anthonisen.felix.astParsing.visitors.ParameterTypeCollector;
import anthonisen.felix.astParsing.visitors.ReturnTypeCollector;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({
        "anthonisen.felix.annotationProcessing.MyVariance",
        "anthonisen.felix.annotationProcessing.Covariant",
        "anthonisen.felix.annotationProcessing.Contravariant",
})
public class VarianceProcessor extends AbstractProcessor {
    private Messager messager;
    private AstManipulator astManipulator;
    private final ImmutableList<Class<? extends Annotation>> supportedAnnotations = ImmutableList.of(MyVariance.class,
            Covariant.class, Contravariant.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager = processingEnv.getMessager();
        astManipulator = new AstManipulator(messager,
                System.getProperty("user.dir") + "/src/main/java");
        messager.printMessage(Kind.NOTE, "Processing annotations:\n");
        for (Class<? extends Annotation> annotationType : supportedAnnotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(annotationType)) {
                MyVariance annotation = e.getAnnotation(MyVariance.class);
                try {
                    if (annotationType.equals(Covariant.class))
                        annotation = TypeFactory.annotation(MyVariance.class,
                                Map.of("variance", VarianceType.COVARIANT, "strict", true));
                    else if (annotationType.equals(Contravariant.class))
                        annotation = TypeFactory.annotation(MyVariance.class,
                                Map.of("variance", VarianceType.CONTRAVARIANT, "strict", true));

                } catch (AnnotationFormatException ex) {
                    // catch this later
                }
                if (annotation != null)
                    processElement(annotation, e);
                else
                    messager.printMessage(Kind.WARNING, "Could not parse annotation for element: " + e);
            }
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

        checkVariance(className, annotation, packageName, tE.getSimpleName().toString());
        astManipulator.eraseTypesAndInsertCasts(className + ".java", packageName,
                tE.getSimpleName().toString());
    }

    private void checkVariance(String className, MyVariance annotation, String packageName, String typeOfInterest) {
        Set<Type> types = new HashSet<>();
        CompilationUnit cu = astManipulator.getSourceRoot().parse(packageName, className
                + ".java");
        if (annotation.variance() == VarianceType.CONTRAVARIANT)
            cu.accept(new ReturnTypeCollector(), types);
        else
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
                break;
            }
        }
    }

    private static boolean isClassParameter(Element e) {
        return e.getEnclosingElement().getKind().name().equals("CLASS");
    }
}
