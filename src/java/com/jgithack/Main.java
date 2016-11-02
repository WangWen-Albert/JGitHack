/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack;

import java.awt.EventQueue;

import com.jgithack.hack.GitHack;
import com.jgithack.lang.StringUtil;
import com.jgithack.log.LogUtils;
import com.jgithack.ui.HomeFrame;

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
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        HomeFrame homeFrame = new HomeFrame();
                        homeFrame.setLocationRelativeTo(null);
                        homeFrame.setVisible(true);
                    }
                });
            } else {
                GitHack hack = new GitHack().withRootUrl(rootUrl).nativeFirst(true).skipError(true)
                    .build();
                hack.checkout();
                LogUtils.info("Complete!");
            }
        } catch (Exception exception) {
            LogUtils.error(exception, "GitHack run failed!");
        }
    }
}
