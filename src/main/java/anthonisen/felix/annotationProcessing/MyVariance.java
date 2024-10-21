package anthonisen.felix.annotationProcessing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_PARAMETER)
public @interface MyVariance {
    public VarianceType variance() default VarianceType.INVARIANT;

    public int depth() default Integer.MAX_VALUE;

    public boolean strict() default false;

}
