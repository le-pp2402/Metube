package com.phatpl.metube.common.aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.phatpl.metube.common.annotation.ValidateSchema;
import com.phatpl.metube.common.service.SchemaValidatorService;

@Aspect
@Component
public class SchemaValidationAspect {

  private final SchemaValidatorService schemaValidatorService;

  public SchemaValidationAspect(SchemaValidatorService schemaValidatorService) {
    this.schemaValidatorService = schemaValidatorService;
  }

  @Before("within(@org.springframework.web.bind.annotation.RestController *)")
  public void validateClasspathFile(JoinPoint joinPoint) {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    Object[] args = joinPoint.getArgs();
    java.lang.annotation.Annotation[][] paramAnotations = method.getParameterAnnotations();

    for (int i = 0; i < args.length; i++) {
      for (Annotation anno : paramAnotations[i]) {
        if (anno instanceof ValidateSchema) {
          ValidateSchema validateSchema = (ValidateSchema) anno;
          String classpathFile = "classpath:" + validateSchema.value();

          ClassPathResource resource = new ClassPathResource(classpathFile);

          if (!resource.exists()) {
            throw new IllegalArgumentException("Not found:" + validateSchema.value());
          }

          schemaValidatorService.validate(args[i], classpathFile);
        }
      }
    }
  }
}
