package anthonisen.felix.util;

public class Pair<T, U> {
    public T first;
    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Pair))
            return false;
        Pair<?, ?> p = (Pair<?, ?>) o;
        return first.equals(p.first) && second.equals(p.second);
    }

    @Override
    public String toString() {
        return "{" + first.toString() + ", " + second.toString() + "}";
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}