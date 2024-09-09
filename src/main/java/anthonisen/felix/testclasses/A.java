package anthonisen.felix.testclasses;

import java.util.List;

public class A<T> {
    List<T> ls;

    public A(List<T> ls) {
        this.ls = ls;
    }

    void printAll() {
        for (T t : ls) {
            System.out.println(t);
        }
    }
}