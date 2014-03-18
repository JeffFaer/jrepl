package falgout.jrepl.guice;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

@BindingAnnotation
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
// TODO when Java 8 comes out and Guice is updated, there might be a way to use
// Parameter.getName() instead of binding/named annotations. [Stdout and Stderr]
public @interface Stdout {}
