package falgout.utils.reflection;

public interface TypeConversion {
    /**
     * Determines if a conversion {@code from} the given class {@code to} the
     * other class is allowed via a set of rules. <br/>
     * <br/>
     * Example:<br/>
     * If this class represented an assignment conversion:
     * 
     * <pre>
     * From f = ...
     * To t = f;
     * </pre>
     * 
     * If this conversion would result in a compiler error, the method would
     * return {@code false}. If it is allowed, the method would return
     * {@code true}. <br/>
     * <br/>
     * <b>Note:</b> {@code from} may be {@code null} to represent the
     * {@code null type}, but {@code to} should <i>never</i> be {@code null}.
     * Providing {@code null} as the {@code to} parameter will result in an
     * {@code IllegalArgumentException}.
     * 
     * @param from The {@code Class} to convert from
     * @param to The {@code Class} to convert to
     * @return Whether or not the conversion is allowed.
     * 
     * @throws IllegalArgumentException If {@code to} is {@code null} or either
     *         {@code Class} is {@code void.class}.
     */
    public boolean convert(Class<?> from, Class<?> to);
}
