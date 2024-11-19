package io.github.bldl.astParsing.util;

import io.github.bldl.annotationProcessing.annotations.MyVariance;

public record ParamData(int index, String leftmostBound, MyVariance variance) {
}