package io.github.bldl;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import io.github.bldl.astParsing.AstManipulator;

class Main {
    public static void main(String[] args) {
        AstManipulator manip = new AstManipulator(new StdoutMessager(), "example");
        manip.eraseTypesAndInsertCasts("Herd.java", "", "T");
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