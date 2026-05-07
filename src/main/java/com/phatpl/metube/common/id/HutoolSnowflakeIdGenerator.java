package com.phatpl.metube.common.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

@Component
public class HutoolSnowflakeIdGenerator implements IdGenerator {
    private Snowflake snowflake;

    public HutoolSnowflakeIdGenerator(SnowflakeProperties properties) {
        this.snowflake = IdUtil.getSnowflake(properties.workerId(), properties.dataCenterId());
    }

    @Override
    public SnowflakeId nextId() {
        return new SnowflakeId(snowflake.nextId());
    }
}
