package com.twinking.network.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 消息工具
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 17:46
 */
public class MessageUtils {

    public static String createMsg(String message){
        StringBuffer sb = createMessageHead();
        sb.append(" ：");
        sb.append(message);
        sb.append('\n');
        return sb.toString();
    }

    public static String createMsg(String message, String sender){
        StringBuffer sb = createMessageHead();
        sb.append(" [");
        sb.append(sender);
        sb.append("] ：");
        sb.append(message);
        sb.append('\n');
        return sb.toString();
    }

    private static StringBuffer createMessageHead() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer();
        String dateNowStr = sdf.format(new Date());
        sb.append(dateNowStr);
        return sb;
    }
}
