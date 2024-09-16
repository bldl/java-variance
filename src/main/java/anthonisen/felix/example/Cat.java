package anthonisen.felix.example;

public class Cat extends Animal {

    public Cat(String name) {
        super(name);
    }

    @Override
    public void feed() {
        System.out.println("Feeding the cat " + name);
    }

    public void cleanLitter() {
        System.out.println("Cleaning the cat litter");
    }
}
