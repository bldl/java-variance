package anthonisen.felix.testclasses;

import java.util.Arrays;

public class B {
    private static A<Integer> someClass;

    public B(A<String> stringClass) {
        A<Number> numberClass = new A<Number>(Arrays.asList(1, 2, 3));
        numberClass.printAll();
    }
}
