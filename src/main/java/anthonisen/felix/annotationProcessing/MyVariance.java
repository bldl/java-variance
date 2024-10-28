package anthonisen.felix.annotationProcessing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the variance characteristics of a type parameter.
 * <p>
 * This annotation can be used to specify whether a type parameter is
 * covariant, contravariant, or invariant, along with optional depth
 * and strictness settings.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * 
 * <pre>
 * {@code
 * public class ImmutableList<@MyVariance(variance = VarianceType.COVARIANT, depth = 100, strict = true) T> {
 *  ...
 * }
 * </pre>
 *
 * @see VarianceType
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE_PARAMETER)
public @interface MyVariance {
    /**
     * Specifies the variance type of the annotated type parameter.
     * 
     * @return the variance type
     */
    public VarianceType variance() default VarianceType.INVARIANT;

    /**
     * Specifies the depth of variance. This can indicate how deep
     * the variance should apply within type hierarchies.
     * 
     * @return the depth of variance
     */
    public int depth() default Integer.MAX_VALUE;

    /**
     * Indicates whether strict variance checking is enforced.
     * 
     * <p>
     * If set to true, the compiler will enforce strict variance rules. 
     * This means that compilation will fail if the specified variance
     * is not adhered to. If set to false, warnings are logged, but program will still compile.
     * </p>
     * 
     * @return true if strict checking is enforced; false otherwise
     */
    public boolean strict() default false;

}
