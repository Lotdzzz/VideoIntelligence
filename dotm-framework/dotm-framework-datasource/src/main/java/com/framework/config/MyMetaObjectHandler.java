package com.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.framework.constants.AutoInjectColumnsConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author dotm
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        // 方式1：严格填充（推荐）- 只有字段值为 null 时才填充
        this.strictInsertFill(metaObject, AutoInjectColumnsConstants.CREATE_TIME, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, AutoInjectColumnsConstants.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时只填充 updateTime
        this.strictUpdateFill(metaObject, AutoInjectColumnsConstants.UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
    }
}
