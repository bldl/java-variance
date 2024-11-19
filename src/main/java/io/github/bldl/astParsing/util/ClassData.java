package io.github.bldl.astParsing.util;

import java.util.Map;

public record ClassData(String className, Map<String, ParamData> params) {

}
