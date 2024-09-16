package anthonisen.felix.example;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Herd<T extends Animal> {
    private List<T> animals = new ArrayList<>();

    public Herd(Collection<T> animals) {
        this.animals.addAll(animals);
    }

    public int getSizeOfHerd() {
        return animals.size();
    }

    public List<Map<T, T>> somemethod() {
        return null;
    }

    public T get(int i) {
        return animals.get(i);
    }

    public static void feedAll(Herd<Animal> herd) {
        for (int i = 0; i < herd.getSizeOfHerd(); ++i)
            herd.get(i).feed();
    }
}
