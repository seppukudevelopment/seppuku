package me.rigamortis.seppuku.api.patch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author Seth
 * 4/4/2019 @ 11:24 PM.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodPatch {

    String mcpName() default "";

    String notchName() default "";

    String mcpDesc() default "";

    String notchDesc() default "";

}
