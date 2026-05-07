package com.phatpl.metube.common.id;

public interface IdGenerator {
    SnowflakeId nextId();

    default Long nextLongId() {
        return nextId().value();
    }
}
