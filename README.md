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
- [License](#license)

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

## Usage

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

## License
