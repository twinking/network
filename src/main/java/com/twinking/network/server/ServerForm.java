package com.twinking.network.server;

import com.twinking.network.common.AbstractWinForm;
import com.twinking.network.utils.MessageUtils;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Description: 服务端界面
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 10:08
 */
public class ServerForm extends AbstractWinForm {

    private ServerSocketChannel serverSocketChannel;
    private Map<String, SocketChannel> clients = new HashMap<>();
    private JComboBox clientsComboBox;

    public ServerForm() {
        super();
        clientsComboBox = new JComboBox();
        clientsComboBox.setSize(120, 25);
        this.getSendBtnPanel().add(clientsComboBox);
    }

    @Override
    public void startConnect() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(this.getIp(), this.getPort()));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.getMsgArea().append(MessageUtils.createMsg("开始监听客户端连接...."));
        this.getStartBtn().setEnabled(false);
        this.getStopBtn().setEnabled(true);
        this.setWorking(true);
        for(;;){
            if(!this.getWorking()){
                this.getMsgArea().append(MessageUtils.createMsg(STOP_CONNECT_MESSAGE));
                break;
            }
            // 这里select()方法返回的是当前监  听得到的事件数目，为0表示当前没有任何事件到达
            if (selector.select(1000) == 0) {
                continue;
            }
            // 对监听到的事件进行遍历
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // Selector在为每个有事件到达的Channel建立SelectionKey对象，其并不自动移除
                iterator.remove();
                if (key.isAcceptable()) {
                    accept(key, selector);
                }
                if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        String message = readFromChannel(clientChannel);
        if(message == null){
            return;
        }
        String sender = clientChannel.getRemoteAddress().toString().substring(1);
        this.getMsgArea().append(MessageUtils.createMsg(message, sender));
        // 已客户端数据，因而这里将感兴趣的事件修改为SelectionKey.OP_READ和OP_WRITE，
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    @SuppressWarnings("unchecked")
    private void accept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        // 将客户端连接Channel注册到Selector中，并且监听该Channel的OP_READ事件，
        socketChannel.register(selector, SelectionKey.OP_READ);
        String remoteAddress = socketChannel.getRemoteAddress().toString().substring(1);
        clients.put(remoteAddress, socketChannel);
        clientsComboBox.addItem(remoteAddress);
        this.getMsgArea().append(MessageUtils.createMsg(SUCCESS_CONNECT_MESSAGE, remoteAddress));
        writeToChannel(SUCCESS_CONNECT_MESSAGE, socketChannel);
    }

    @Override
    public void stopConnect() throws IOException {
        if(serverSocketChannel != null){
            serverSocketChannel.close();
            serverSocketChannel = null;
        }
        this.setWorking(false);
        this.getStartBtn().setEnabled(true);
        this.getStopBtn().setEnabled(false);
    }

    @Override
    public void disconnect(SocketChannel channel) throws IOException {
        String host = channel.getRemoteAddress().toString().substring(1);
        clients.remove(host);
        clientsComboBox.removeItem(host);
        this.getMsgArea().append(MessageUtils.createMsg("与客户端断开连接！", host));
    }

    @Override
    public void sendMsg() throws IOException {
        String message = this.getSendArea().getText();
        if (null == message || EMPTY_STRING.equals(message.trim())
                || clientsComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this.getFrame(), "发送消息或对象不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String client = clientsComboBox.getSelectedItem().toString();
        SocketChannel channel = clients.get(client);
        writeToChannel(message, channel);
        this.getSendArea().setText(EMPTY_STRING);
    }

}
