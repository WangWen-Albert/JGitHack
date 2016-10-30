/**
 * JHacker.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack;

import org.testng.annotations.Test;

import com.jgithack.log.LogUtils;

/**
 * GitHackTest
 * 
 * @author Albert Wang
 * @version $Id: GitHackTest.java, v 0.1 Oct 20, 2016 1:34:03 PM jawangwen Exp $
 */
public class GitHackTest {
    final String testUrl = "http://10969825b5674ba6b0f0dd5b9742d5677aa2c9ad31314ff7.game.ichunqiu.com/Challenges";

    @Test
    public void testCheckout() {
        LogUtils.info("测试开始");
        try {
            GitHack hack = new GitHack().withRootUrl(testUrl).nativeFirst(true).skipError(true)
                .build();
            hack.checkout();
            LogUtils.info("测试成功");
        } catch (Exception exception) {
            LogUtils.error(exception, "测试失败");
        }
    }
}
