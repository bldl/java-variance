package anthonisen.felix.astParsing.visitors;

import java.util.Set;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ParameterTypeCollector extends VoidVisitorAdapter<Set<Type>> {
  @Override
  public void visit(Parameter n, Set<Type> arg) {
    super.visit(n, arg);
    arg.add(n.getType());
  }
}
