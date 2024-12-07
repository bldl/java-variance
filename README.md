# Java Variance

![Build Status](https://img.shields.io/github/workflow/status/bldl/java-variance/maven) ![License](https://img.shields.io/github/license/bldl/java-variance)

## Description

This project provides Java annotations that enable fine-grained specification of variance for class generics, improving flexibility in generic programming.

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

## Usage

### Annotations

There are currently three annotations provided by this project: MyVariance, Covariant and Contravariant. With these you are able to annotate type parameters for classes in order to specify fine grained variance.

### MyVariance

MyVariance is the most customizable one, and allows you to experiment with different types of variance. With this one there are several parameters you can provide to specify what variance rules should apply:

INVARIANT,
COVARIANT,
CONTRAVARIANT,
BIVARIANT,
SIDEVARIANT

| Parameter  | Description                     | Possible values                                             |
| ---------- | ------------------------------- | ----------------------------------------------------------- |
| `variance` | Specifies which variance to use | COVARIANT, CONTRAVARIANT, INVARIANT, BIVARIANT, SIDEVARIANT |
| `depth`    | How deep subtyping goes         | Integer value ≥ 0                                           |
| `strict`   | Whether                         | `true`, `false`                                             |

#### Covariant

Covariant is a specific instance of MyVariance. It's intended to inline with the semantics of traditional covariance. It acts as MyVariance with `variance` set to `COVARIANT`, `depth` as infinite and `strict` to `true`.

<details>
  <summary>
  Code example
  </summary>

```java
public class ImmutableList<@Covariant T> {
List<T> underlyingList = new ArrayList<>();

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

## Contributing

Pull requests and issues that aim to better the project are greatly appreciated.
