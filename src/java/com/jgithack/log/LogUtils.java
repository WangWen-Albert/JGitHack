/**
 * JHack.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 简单的日志工具。。
 * 
 * @author Albert Wang
 * @version $Id: LogUtils.java, V0.1 2016-10-29 20:02:00, jawangwen@qq.com $
 */
public class LogUtils {
    public static final int       DEBUG   = 0;
    public static final int       INFO    = 1;
    public static final int       WARNING = 2;
    public static final int       ERROR   = 3;

    private static volatile int   level   = DEBUG;

    private static volatile Print print   = new Print() {
                                              @Override
                                              public void toOut(String log) {
                                                  System.out.println(log);
                                              }

                                              @Override
                                              public void toErr(String log) {
                                                  System.err.println(log);
                                              }
                                          };

    /**
     * 打印方案
     * 
     * @author Albert Wang
     * @version $Id: LogUtils.java, V0.1 Nov 2, 2016 12:30:54 AM jawangwen@qq.com $
     */
    public static interface Print {
        /**
         * 打印一般信息
         */
        public void toOut(String log);

        /**
         * 打印错误信息
         */
        public void toErr(String log);
    }

    /**
     * 以slf4j的格式来格式化日志输出
     * 
     * @param message
     * @param arguments
     * @return
     */
    public static String formatMessage(String message, Object... arguments) {
        if (arguments == null || arguments.length == 0) {
            return message;
        }
        return String.format(message, arguments);
    }

    /**
     * debug级别日志输出
     * 
     * @param logger
     * @param message
     * @param arguments
     */
    public static void debug(String message, Object... arguments) {
        if (level <= DEBUG) {
            print.toOut(formatMessage(message, arguments));
        }
    }

    /**
     * info级别日志输出
     * 
     * @param logger
     * @param message
     * @param arguments
     */
    public static void info(String message, Object... arguments) {
        if (level <= INFO) {
            print.toOut(formatMessage(message, arguments));
        }
    }

    /**
     * warn级别日志输出
     * 
     * @param logger
     * @param message
     * @param arguments
     */
    public static void warn(String message, Object... arguments) {
        if (level <= WARNING) {
            print.toErr(formatMessage(message, arguments));
        }
    }

    /**
     * error级别日志输出
     * 
     * @param logger
     * @param message
     * @param arguments
     */
    public static void error(String message, Object... arguments) {
        print.toErr(formatMessage(message, arguments));
    }

    /**
     * warn级别带异常堆栈日志输出
     * 
     * @param logger
     * @param e
     * @param message
     * @param arguments
     */
    public static void warn(Throwable e, String message, Object... arguments) {
        if (level <= WARNING) {
            print.toErr(formatMessage(message, arguments) + getStackTrace(e));
        }

    }

    /**
     * error级别带异常堆栈日志输出
     * 
     * @param logger
     * @param e
     * @param message
     * @param arguments
     */
    public static void error(Throwable e, String message, Object... arguments) {
        print.toErr(formatMessage(message, arguments));
        print.toErr(getStackTrace(e));
    }

    /**
     * 异常堆栈转String
     * 
     * @param throwable
     * @return
     */
    public static String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        Writer sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Setter method for property <tt>print</tt>.
     * 
     * @param print value to be assigned to property print
     */
    public static void setPrint(Print print) {
        LogUtils.print = print;
    }
}
