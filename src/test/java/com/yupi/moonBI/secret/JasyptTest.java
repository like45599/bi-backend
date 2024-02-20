package com.yupi.moonBI.secret;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JasyptTest {
    @Test
    public void testEncrypt() throws Exception {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        // 设置密码
        config.setPassword("MoonBI");
        // 设置算法
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        // 设置密钥获取迭代次数
        config.setKeyObtentionIterations("1000");
        // 设置池大小
        config.setPoolSize("1");
        // 设置提供者名称
        config.setProviderName("SunJCE");
        // 设置盐生成器类名
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        // 设置IV生成器类名
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        // 设置输出类型
        config.setStringOutputType("base64");
        encryptor.setConfig(config);

        System.out.println("加密"+encryptor.encrypt("Yjk1YmEzNzM4NzIxYTE5NGI4ZmYzMjAz"));
        System.out.println("解密"+encryptor.encrypt("e402227bdcdc92f623a4dd4499b22b61"));
        System.out.println("解密"+encryptor.encrypt("192.168.88.130"));
        System.out.println("解密"+encryptor.encrypt("123456"));
    }
}
