package anthonisen.felix.example;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Herd<Animal> animalHerd = new Herd<>(Arrays.asList(new Animal("Mickey"), new Animal("Poseidon")));
        Herd<Animal> catHerd = new Herd<Animal>(Arrays.asList(new Cat("Katty"), new Cat("Mons")));
        catHerd.get(0).feed();
        Collection<Animal> allCats = catHerd.getAll();
        Herd.feedAll(animalHerd);
    }
}
