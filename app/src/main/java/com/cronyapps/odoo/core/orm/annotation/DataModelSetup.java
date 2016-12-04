package com.cronyapps.odoo.core.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.cronyapps.odoo.core.orm.annotation.ModelSetup.NONE;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataModelSetup {
    ModelSetup value() default NONE;
}