# Java Variance

![Build Status](https://img.shields.io/github/actions/workflow/status/bldl/java-variance/maven.yml) ![License](https://img.shields.io/github/license/bldl/java-variance)

## Description

This project provides Java annotations that enable fine-grained specification of variance for class generics, improving flexibility in generic programming.

> **DISCLAIMER:**
> This project is not intended for production use. It's an incomplete implementation with limited support for certain use cases. It should be viewed as a tool for experimenting with variance in Java.

## Table of Contents

- [Installation](#installation)
  - [For maven users](#for-maven-users)
  - [For other users](#for-other-users)
- [Background](#background)
  - [What is variance](#what-is-variance)
  - [Types of variance](#types-of-variance)
- [Usage](#usage)
  - [Annotations](#annotations)
  - [Output](#output)
- [Contributing](#contributing)

## Installation

### For Maven users

Add the following dependency to your pom.xml

```xml
<dependency>
      <groupId>io.github.bldl</groupId>
      <artifactId>java-variance</artifactId>
      <version>LATEST</version>
</dependency>
```

### For other users

Using Git Submodules
If your project does not use Maven or Gradle, but you still want to use the annotations provided by this library, you can include it as a Git submodule. Here's how:

**1. Add the repository as a submodule:**

```sh
git submodule add https://github.com/bldl/java-variance
```

**2. Initialize and update the submodule:**

```sh
git submodule update --init
```

**3. Add the submodule to your classpath in your project setup. For example:**

- If using an IDE, configure the submodule to be a source directory.
- If using a custom build script, ensure the submodule's output is included in the compilation step.

## Background

### What is variance

**Variance** in Generic Programming refers to how subtyping between more complex generic types (like `List<T>`) relates to subtyping between their type parameters (like `T`). It determines whether type relationships are preserved, reversed, or discarded for each type parameter.

### Types of Variance

**1. Covariance**

Covariance allows a generic type to be substituted with another generic type that has a more specific (derived) type parameter.

- Example: `List<Animal>` is a subtype of `List<Dog>` if `Dog` is a subtype of `Animal`.
- Used when a generic type is only **producing** (out) values. That is the generic type is not part of any non-private variables or method parameters.

**2. Contravariance**

Contravariance allows a generic type to be substituted with another generic type that has a more general (base) type parameter.

- Example: `List<Dog>` is a sybtype of `List<Animal>` if `Animal` is a supertype of `Dog`.
- Used when a generic type is only **consuming** (in) values. That is the generic type is not part of any non-private variables or method return types.

**3. Bivariance**

Bivariance allows a generic type to be substituted with another generic type regardless of the relationship between their type parameters. This means a generic type can accept both more specific (derived) and more general (base) type parameters interchangeably.

- Example: Both `List<Dog>` is a sybtype of `List<Animal>` and `List<Animal>` is a subtype of of `List<Dog>` if Dog is a subtype of Animal.
- Practical Use: Bivariance is generally unsafe in strongly typed systems because it disregards type safety. It might be allowed in certain scenarios where type constraints are relaxed for specific purposes, such as event handlers or certain dynamic language constructs.
- Drawbacks: Since it permits type substitution in both directions, it can lead to runtime errors if the system tries to enforce incompatible operations.

**4. Invariance**

Invariance means no substitution is allowed between different generic types, even if their type parameters have a subtype relationship.

- Example: `List<Animal>` and `List<Dog>` are entirely distinct and incompatible.
- This is the default in many languages, like Java's generics.

### New variance notions introduced in this project

In addition to the traditional variance constructs that exist, some novel constructs are also provided here. These are experimental variances that may or may not make sense in a functional manner, but that can be interesting to experiment with.

#### Depth

Depth refers to the extent to which subtyping relationships are valid within a type hierarchy.

- For **covariance**, depth determines how many levels you can move **down** in the hierarchy to find a valid subtype.
- For **contravariance**, depth specifies how many levels you can move **up** for a valid subtype.

If we define a depth of 2:

- For **covariance**, starting at `Animal`, valid subtypes would include `Mammal`, `Bird`, `Dog`, `Cat`, `Sparrow`, and `Penguin` (two levels down).
- For **contravariance**, starting at `Cat`, valid supertypes would include `Mammal` and `Animal` (two levels up).

For example, consider the following type hierarchy:

`Animal` ├── `Mammal` ├── `Dog`

If we define a depth of 1:

- For **covariance**, starting at `Animal`, a valid sutype would be `Mammal`, while `Dog` would be invalid since it's 2 levels down.

#### Side Variance

Side variance introduces a new concept in type relationships, where subtyping operates **sideways** rather than in the traditional **upward** or **downward** directions within a type hierarchy. This means that any classes on the same level in the hierarchy are considered valid subtypes of one another.

For example, onsider the types `Animal`, `Dog`, and `Cat`, where `Dog` and `Cat` are direct subtypes of `Animal`. With a side-variant `List` type:

- `List<Dog>` would be a subtype of `List<Cat>`.
- Similarly, `List<Cat>` would be a subtype of `List<Dog>`.

## Usage

To specify variance for classes, annotate the relevant type parameters with one of the provided annotations (details on these annotations will follow). Once annotated, you can use your classes as though they conform to the specified variance. Note that your IDE's linter may flag errors if the classes are used in ways that Java does not natively support. These warnings are expected and can be safely ignored.

When you compile the project, a new output directory named `output_javavariance` will be created. In this directory, type arguments are erased, and the necessary casts are inserted to ensure the project runs correctly. At this stage, any previously flagged errors should no longer appear.

### Annotations

There are currently three annotations provided by this project: `MyVariance`, `Covariant` and `Contravariant`. With these you are able to annotate type parameters for classes in order to specify fine grained variance.

#### MyVariance

MyVariance is the most customizable one, and allows you to experiment with different types of variance. There are several parameters you can provide to specify what variance rules should apply:

| Parameter  | Description                                                   | Possible values                                             |
| ---------- | ------------------------------------------------------------- | ----------------------------------------------------------- |
| `variance` | Specifies which variance type to use                          | COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT, SIDEVARIANT |
| `depth`    | How deep subtyping goes                                       | Integer value ≥ 0                                           |
| `strict`   | Whether compilation should fail if any errors are encountered | `true`, `false`                                             |

#### Covariant

Covariant is a specific instance of MyVariance. It's intended to inline with the semantics of traditional covariance. It acts as MyVariance with `variance` set to `COVARIANT`, `depth` as infinite and `strict` to `true`.

<details>
  <summary>
  Code example
  </summary>

```java
public class ImmutableList<@Covariant T> {

    private List<T> underlyingList = new ArrayList<>();

    public ImmutableList(Iterable<T> initial) {
        initial.forEach(e -> underlyingList.add(e));
    }

    public int size() {
        return underlyingList.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean contains(Object o) {
        return underlyingList.contains(o);
    }

    public Iterator<T> iterator() {
        return underlyingList.iterator();
    }

    public Object[] toArray() {
        return underlyingList.toArray();
    }

    public T get(int i) {
        return underlyingList.get(i);
    }

}
```

</details>

#### Contravariant

Contravariant, similarly to Covariant, aims to inline with the semantics of traditional contravariance. It acts as MyVariance with `variance` set to `CONTRAVARIANT`, `depth` as infinite and `strict` to `true`.

<details>
  <summary>
  Code example
  </summary>

```java
class Pair<@Contravariant X, @Contravariant Y> {
    private X first;
    private Y second;

    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    public void setFirstElement(X first) {
        this.first = first;
    }

    public Y setSecondElement(Y second) {
        this.second = second;
    }
}
```

</details>

### Building and running your project

If you are using Maven, you can compile your project with the following command:

```sh
mvn clean compile
```

If you are not using Maven, simply compile the project as you normally would.

This process will generate a new output folder named `output_javavariance`. The code within this folder is the code you should run. To execute your program, run the main file from this directory as you typically would—whether through your IDE, by using `mvn exec`, or any other method you prefer for running Java applications.

## Contributing

Pull requests and issues that aim to better the project are greatly appreciated.
