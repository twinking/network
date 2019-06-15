package com.twinking.network;

import com.twinking.network.client.ClientForm;
import com.twinking.network.server.ServerForm;
import com.twinking.network.utils.FormUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @Description: 程序入口
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 09:11
 */
public class App {

    private JFrame frame;
    private static final String CLIENT_BUTTON_TEXT = "客户端";
    private static final String SERVER_BUTTON_TEXT = "服务端";
    private ActionListener actionListener;


    public App() {
        // 创建及设置窗口
        frame = FormUtils.createNewFormNoMaxBtn("网络调试助手",400,300);
        //注册事件
        addListener();
        //添加按钮 创建横向Box容器
        Box btnBox=Box.createHorizontalBox();
        //将外层横向Box添加进窗体
        frame.add(btnBox);
        //添加高度为200的垂直框架
        btnBox.add(Box.createVerticalStrut(200));
        //添加服务端按钮
        JButton serverBtn = new JButton(SERVER_BUTTON_TEXT);
        serverBtn.addActionListener(actionListener);
        btnBox.add(serverBtn);
        //添加长度为40的水平框架
        btnBox.add(Box.createHorizontalStrut(40));
        //添加客户端按钮
        JButton clientBtn = new JButton(CLIENT_BUTTON_TEXT);
        clientBtn.addActionListener(actionListener);
        btnBox.add(clientBtn);
        //添加水平胶水
        btnBox.add(Box.createHorizontalGlue());

        //关闭按钮退出程序
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    public void addListener() {
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()){
                    case CLIENT_BUTTON_TEXT:
                        new ClientForm().start();
                        break;
                    case SERVER_BUTTON_TEXT:
                        new ServerForm().start();
                        break;
                    default:
                        System.out.println(e.getActionCommand());
                        break;
                }
            }
        };
    }

    public void start() {
        // 显示窗口
        frame.setVisible(true);
    }

    public void close() {

    }

    public static void main(String[] args) {
        new App().start();
    }
}
