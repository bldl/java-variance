# Java Variance

![Build Status](https://img.shields.io/github/workflow/status/bldl/java-variance/CI) ![License](https://img.shields.io/github/license/bldl/java-variance)

## Description

This is a project that incorporates Java annotations in order to allow specification of different generic parameter variances.

## Table of Contents

- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [For maven users](#for-maven-users)
  - [For grade users](#for-gradle-users)
  - [For other users](#for-other-users)
- [Usage](#usage)
  - [Subsection](#subsection)
- [Contributing](#contributing)

## Installation

### Prerequisites

### For maven users

<!-- TODO: Add when project is published to maven central -->

### For gradle users

<!-- TODO: Add when project is published to maven central -->

### For other users

```bash
$ git submodule add https://github.com/bldl/java-variance
```

## What is variance

# Variance in Generic Programming

**Variance** in Generic Programming refers to how subtyping between more complex generic types (like `List<T>`) relates to subtyping between their type parameters (like `T`). It determines whether type relationships are preserved, reversed, or invariant when generic types are involved.

## Types of Variance

### 1. **Covariance**

Covariance allows a generic type to be substituted with another generic type that has a more specific (derived) type parameter.

- Example: `List<Animal>` can accept `List<Dog>` if `Dog` is a subtype of `Animal`.
- Used when a generic type is only **producing** (out) values.
- Marked with **`out`** in Kotlin or achieved with `? extends` in Java.

### 2. **Contravariance**

Contravariance allows a generic type to be substituted with another generic type that has a more general (base) type parameter.

- Example: `List<Dog>` can accept `List<Animal>` if `Animal` is a supertype of `Dog`.
- Used when a generic type is only **consuming** (in) values.
- Marked with **`in`** in Kotlin or achieved with `? super` in Java.

### 3. **Invariance**

Invariance means no substitution is allowed between different generic types, even if their type parameters have a subtype relationship.

- Example: `List<Animal>` and `List<Dog>` are entirely distinct and incompatible.
- This is the default in many languages, like Java's generics.

---

## Key Insight

Covariance and contravariance depend on how the generic type uses its type parameter: **producing outputs** (covariance) or **consuming inputs** (contravariance). Invariant types neither allow flexibility.

### Annotations

There are currently three annotations provided by this project: MyVariance, Covariant and Contravariant. With these you are able to annotate type parameters for classes in order to

#### MyVariance

MyVariance is the most customizable one, and allows you to experiment with different types of variance. With this one there are several parameters you can provide to specify what variance rules should apply:

| Parameter  | Description                               | Possible values                     |
| ---------- | ----------------------------------------- | ----------------------------------- |
| `variance` | Specifies which variance to use           | COVARIANT, CONTRAVARIANT, INVARIANT |
| `depth`    | How deep subtyping goes                   | Integer value ≥ 0                   |
| `strict`   | Whether strict checks should be performed | `true`, `false`                     |

#### Covariant

Covariant is a specific instance of MyVariance. It's intended to inline with the semantics of traditional covariance. It acts as MyVariance with `variance` set to `COVARIANT`, `depth` as infinite and `strict` to `true`.

#### Contravariant

Contravariant, similarly to Covariant, aims to inline with the semantics of traditional contravariance. It acts as MyVariance with `variance` set to `CONTRAVARIANT`, `depth` as infinite and `strict` to `true`.

### Subsection

## Contributing

Pull requests and issues that aim to better the project are greatly appreciated.
