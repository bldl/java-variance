package anthonisen.felix.example;

class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void feed() {
        System.out.println("Feeding the animal " + name);
    }

    public String getName() {
        return name;
    }
}