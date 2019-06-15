package com.twinking.network.utils;

import javax.swing.*;
import java.awt.*;

/**
 * @Description: 窗体工具类
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 10:09
 */
public class FormUtils {

    /**
     * 创建窗体
     * @param title 窗体名称
     * @param width 宽度
     * @param height 高度
     * @return 窗体
     */
    public static JFrame createNewForm(String title, int width, int height){
        // 创建及设置窗口
        JFrame frame = new JFrame(title);
        //设置好宽高
        frame.setSize(width, height);
        //窗体居中显示
        showCenter(frame);
        return frame;
    }

    /**
     * 创建窗体不带关闭按钮
     * @param title 窗体名称
     * @param width 宽度
     * @param height 高度
     * @return 窗体
     */
    public static JFrame createNewFormNoMaxBtn(String title, int width, int height){
        JFrame frame = createNewForm(title,width,height);
        //关闭最大化
        frame.setResizable(false);
        return frame;
    }

    /**
     * 居中显示
     * @param frame 窗体
     */
    public static void showCenter(JFrame frame) {
        //获得窗口宽
        int windowWidth = frame.getWidth();
        //获得窗口高
        int windowHeight = frame.getHeight();
        //定义工具包
        Toolkit kit = Toolkit.getDefaultToolkit();
        //获取屏幕的尺寸
        Dimension screenSize = kit.getScreenSize();
        //获取屏幕的宽
        int screenWidth = screenSize.width;
        //获取屏幕的高
        int screenHeight = screenSize.height;
        //设置窗口居中显示
        frame.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
    }

}
