package anthonisen.felix.testclasses;

import java.util.Arrays;

public class ManipulatedB {

    private static A<? extends Integer> someClass;

    public ManipulatedB(A<? extends String> stringClass) {
        A<? extends Number> numberClass = new A<Number>(Arrays.asList(1, 2, 3));
        numberClass.printAll();
    }
}
