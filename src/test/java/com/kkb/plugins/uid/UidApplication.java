/*
 * Copyright
 */
package com.kkb.plugins.uid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 类说明
 *
 * @author ztkool
 * @since
 */
@SpringBootApplication
@EnableUidGenerator
public class UidApplication {

    public static void main(String[] args) {
        SpringApplication.run(UidApplication.class, args);
    }
}
