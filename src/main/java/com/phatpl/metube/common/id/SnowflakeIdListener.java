package com.phatpl.metube.common.id;

import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class SnowflakeIdListener {

  private static IdGenerator idGenerator;

  @Autowired
  public void setIdGenerator(IdGenerator idGenerator) {
    SnowflakeIdListener.idGenerator = idGenerator;
  }

  @PrePersist
  public void generateId(Object entity) {
    try {
      Field idField = findIdField(entity.getClass());

      if (idField != null) {
        idField.setAccessible(true);
        Object currentId = idField.get(entity);

        if (currentId == null) {
          Long newId = idGenerator.nextLongId();
          idField.set(entity, newId);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate Snowflake ID", e);
    }
  }


  private Field findIdField(Class<?> clazz) {
    Class<?> currentClass = clazz;

    while (currentClass != null) {
      for (Field field : currentClass.getDeclaredFields()) {
        if (field.isAnnotationPresent(Id.class) && field.getType() == Long.class) {
          return field;
        }
      }
      currentClass = currentClass.getSuperclass();
    }

    return null;
  }
}
