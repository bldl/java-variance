package anthonisen.felix.annotationProcessing;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.Type;
import java.util.HashSet;
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
import anthonisen.felix.astParsing.Covariancer;
import anthonisen.felix.astParsing.util.TypeHandler;
import anthonisen.felix.astParsing.visitors.ParameterTypeCollector;
import anthonisen.felix.astParsing.visitors.ReturnTypeCollector;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("anthonisen.felix.annotationProcessing.MyVariance")
public class VarianceProcessor extends AbstractProcessor {
    private Messager messager;
    private Covariancer covariancer;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager = processingEnv.getMessager();
        covariancer = new Covariancer(messager,
                System.getProperty("user.dir") + "/src/main/java");
        messager.printMessage(Kind.NOTE, "Processing annotations:\n");
        for (Element e : roundEnv.getElementsAnnotatedWith(MyVariance.class)) {
            MyVariance annotation = e.getAnnotation(MyVariance.class);
            String packageName = processingEnv.getElementUtils().getPackageOf(e).toString();
            if (packageName.contains("output"))
                continue;
            messager.printMessage(Kind.NOTE, e.getEnclosingElement().getKind().name());

            // should not process method declarations
            if (!isClassParameter(e))
                continue;

            TypeParameterElement tE = (TypeParameterElement) e;
            String className = tE.getEnclosingElement().getSimpleName().toString();
            if (annotation.variance() == VarianceType.INVARIANT) {
                messager.printMessage(Kind.NOTE,
                        String.format(
                                "Invariant type parameter detected in class: %s\nWill not proceed with AST manipulation",
                                className));
            }

            checkVariance(className, annotation.variance(), packageName);
            covariancer.makeCovariant(className + ".java", packageName);

        }
        covariancer.applyChanges();
        return true;
    }

    private void checkVariance(String className, VarianceType variance, String packageName) {
        Set<Type> types = new HashSet<>();
        CompilationUnit cu = covariancer.getSourceRoot().parse(packageName, className
                + ".java");
        if (variance == VarianceType.CONTRAVARIANT)
            cu.accept(new ReturnTypeCollector(), types);
        else
            cu.accept(new ParameterTypeCollector(), types);

        for (Type type : types) {
            if (TypeHandler.containsType(type, "T")) {
                messager.printMessage(
                        Kind.WARNING,
                        String.format(
                                "%s is declared as %s, but does not conform to constraints: contains T in %s position",
                                className,
                                variance,
                                variance == VarianceType.COVARIANT ? "IN" : "OUT"));
                break;
            }
        }
    }

    private static boolean isClassParameter(Element e) {
        return e.getEnclosingElement().getKind().name().equals("CLASS");
    }
}
