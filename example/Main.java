package anthonisen.felix.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Main {

    private Herd<Cat> fieldHerd = new Herd<>();

    public static void main(String[] args) {
        Herd<Animal> animalHerd = new Herd<>(Arrays.asList(new Animal("Mickey"), new Animal("Poseidon")));
        Herd<Cat> catHerd = new Herd<>(Arrays.asList(new Cat("Katty"), new Cat("Mons")));

        Cat katty = catHerd.get(0);
        katty.cleanLitter();

        List<Cat> allCats = catHerd.getAll();

        for (Cat cat : allCats) {
            cat.feed();
        }

        Herd.feedAll(animalHerd);
    }

    private static void cleanAllLitters(Herd<Cat> herd) {
        for (Cat cat : herd.getAll()) {
            cat.cleanLitter();
        }
    }
}
