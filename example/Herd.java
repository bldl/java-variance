package anthonisen.felix.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Herd<T extends Animal> {
    private List<T> animals = new ArrayList<>();

    public Herd(Collection<T> animals) {
        this.animals.addAll(animals);
    }

    public int getSizeOfHerd() {
        return animals.size();
    }

    public List<T> getAll() {
        return Collections.unmodifiableList(animals);
    }

    public T get(int i) {
        return animals.get(i);
    }

    public static void feedAll(Herd<Animal> herd) {
        for (int i = 0; i < herd.getSizeOfHerd(); ++i) {
            Animal Animal_herd_get_i = (Animal) herd.get(i);
            Animal_herd_get_i.feed();
            // herd.get(i).feed();
        }
    }
}
