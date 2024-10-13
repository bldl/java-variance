package anthonisen.felix;

import anthonisen.felix.astParsing.Covariancer;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

class Main {
    public static void main(String[] args) {
        Covariancer manip = new Covariancer(new StdoutMessager(), "example");
        manip.makeCovariant("Herd.java", "");
        manip.applyChanges();
    }
}

class StdoutMessager implements Messager {
    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
        System.out.println("[" + kind + "]: " + msg);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
        System.out.println("[" + kind + "]: " + msg + " Element: " + e);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
        System.out.println("[" + kind + "]: " + msg + " Element: " + e + " AnnotationMirror: " + a);
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
            AnnotationValue v) {
        System.out.println(
                "[" + kind + "]: " + msg + " Element: " + e + " AnnotationMirror: " + a + " AnnotationValue: " + v);
    }
}