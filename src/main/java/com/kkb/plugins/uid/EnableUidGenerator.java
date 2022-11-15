/*
 * Copyright
 */
package com.kkb.plugins.uid;

import com.kkb.plugins.uid.config.UidConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({UidConfig.class})
public @interface EnableUidGenerator {
}
