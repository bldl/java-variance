package anthonisen.felix.example;

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

    public Collection<T> getAll() {
        return Collections.unmodifiableCollection(animals);
    }

    public T get(int i) {
        return animals.get(i);
    }

    public static void feedAll(Herd<Animal> herd) {
        for (int i = 0; i < herd.getSizeOfHerd(); ++i)
            herd.get(i).feed();
    }
}
