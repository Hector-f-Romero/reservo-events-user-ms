package com.hector.eventuserms.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Creamos esta anotación personalizada para detectar de forma más simple los métodos de NATS y así poder aplicar AOP más fácil por medio de PointersCut.
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NatsHandler {

}
