package anthonisen.felix.example;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Herd<Animal> animalHerd = new Herd<>(Arrays.asList(new Animal("Mickey"), new Animal("Poseidon")));
        Herd<Cat> catHerd = new Herd<>(Arrays.asList(new Cat("Katty"), new Cat("Mons")));

        Cat katty = catHerd.get(0);

        Collection<Cat> allCats = catHerd.getAll();

        for (Cat cat : allCats) {
            cat.feed();
        }

        Herd.feedAll(animalHerd);
    }
}
