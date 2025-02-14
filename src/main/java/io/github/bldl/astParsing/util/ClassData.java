package io.github.bldl.astParsing.util;

import java.util.Map;

public record ClassData(String className, String packageName, Map<String, ParamData> params) {

}
