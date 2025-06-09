package com.staybnb.config;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;

public class JasyptConfigTest {

    @Test
    public void test() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("password");

        String encrypted = encryptor.encrypt("input");
        System.out.println("ENC(" + encrypted + ")");
    }
}
