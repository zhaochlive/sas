package com.js.sas;

import com.js.sas.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author daniel
 * @description:
 * @create: 2019-10-16 14:18
 */
@Slf4j
public class Md5Secret {
    @Test
    public void MD5Test(){
        String password = "admin";
        String md1 = MD5Util.MD5(password);
        String md2 = MD5Util.generate(password);
        System.out.println("加密前:"+password);
        System.out.println("普通MD5加密后:"+md1);
        System.out.println("加盐MD5加密后:"+md2);
        System.out.println("比较原文和加盐MD5加密之后是否一致:"+MD5Util.verify(password,md2));
    }

    @Test
    public void test(){
        String plaintext = "admin";
        //  plaintext = "123456";
        System.out.println("原始：" + plaintext);
        System.out.println("普通MD5后：" + MD5Util.MD5(plaintext));

        // 获取加盐后的MD5值
        String ciphertext = MD5Util.generate(plaintext);
        System.out.println("加盐后MD5：" + ciphertext);
        System.out.println("是否是同一字符串:" + MD5Util.verify(plaintext, ciphertext));
        /**
         * 其中某次DingSai字符串的MD5值
         */
        String[] tempSalt = { "a34555351d88554183016a91e5c69eb0f769c1638c89716f", "945570e6b25327e08d27ef9146a31d83d47956181081190f", "61a718e4c15d914504a41d95230087a51816632183732b5a" };

        for (String temp : tempSalt) {
            System.out.println("是否是同一字符串:" + MD5Util.verify(plaintext, temp));
        }

    }
}
