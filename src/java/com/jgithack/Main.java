/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.jgithack.lang.StringUtil;
import com.jgithack.log.LogUtils;

/**
 * Main
 * 
 * @author Albert Wang
 * @version $Id: GitHack.java, V0.1 2016-10-29 20:10:00, jawangwen@qq.com $
 */
public class Main {
    /**
     * main
     * 
     * @param args
     * @return
     * @throws Exception 
     */
    public static void main(String[] args) {
        String rootUrl = args.length > 2 ? args[1] : null;
        try {
            if (StringUtil.isBlank(rootUrl)) {
                LogUtils.warn("请输入链接：");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                rootUrl = reader.readLine();
            }
            GitHack hack = new GitHack().withRootUrl(rootUrl).nativeFirst(true).skipError(true)
                .build();
            hack.checkout();
            LogUtils.info("Complete!");
        } catch (Exception exception) {
            LogUtils.error(exception, "GitHack run failed!");
        }
    }
}
