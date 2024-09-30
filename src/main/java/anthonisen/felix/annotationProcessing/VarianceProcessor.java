package anthonisen.felix.annotationProcessing;

import java.util.HashMap;
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

import anthonisen.felix.astParsing.Covariancer;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("anthonisen.felix.annotationProcessing.MyVariance")
public class VarianceProcessor extends AbstractProcessor {

    Map<String, String> classHierarchy = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();
        Covariancer covariancer = new Covariancer(messager,
                System.getProperty("user.dir") + "/src/main/java/anthonisen/felix");
        messager.printMessage(Kind.NOTE, "Processing annotations:\n");
        for (Element e : roundEnv.getElementsAnnotatedWith(MyVariance.class)) {
            MyVariance annotation = e.getAnnotation(MyVariance.class);
            messager.printMessage(Kind.NOTE, e.getEnclosingElement().getKind().name());
            if (isClassParameter(e)) {
                messager.printMessage(Kind.NOTE, "Is part of a class declaration");
            } else
                messager.printMessage(Kind.NOTE, "Is part of a method declaration");
            messager.printMessage(Kind.NOTE, e.getEnclosingElement().getKind().name());
            switch (annotation.variance()) {
                case INVARIANT:
                    messager.printMessage(Kind.NOTE, "Invariant class detected");
                    break;
                case CONTRAVARIANT:
                    messager.printMessage(Kind.NOTE, "Contravariant class detected");
                    break;
                case COVARIANT:
                    messager.printMessage(Kind.NOTE, "Covariant class detected");
                    break;
            }
            TypeParameterElement tE = (TypeParameterElement) e;
            covariancer.makeCovariant(tE.getEnclosingElement().getSimpleName() + ".java");
            // Get the enclosing element (this could be a class, method, or constructor)

        }
        covariancer.applyChanges();
        return true;
    }

    // Check whether the type parameter is for a class or a method
    private static boolean isClassParameter(Element e) {
        return e.getEnclosingElement().getKind().name().equals("CLASS");
    }
}
