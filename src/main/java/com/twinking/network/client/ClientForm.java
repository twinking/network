package com.twinking.network.client;

import com.twinking.network.common.AbstractWinForm;
import com.twinking.network.utils.MessageUtils;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description: 客户端界面
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 10:08
 */
public class ClientForm extends AbstractWinForm {

    private SocketChannel clientChannel;

    public ClientForm() {
        super();
    }

    @Override
    public void startConnect() throws IOException {
        clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(false);
        Selector selector = Selector.open();
        clientChannel.register(selector, SelectionKey.OP_CONNECT);
        clientChannel.connect(new InetSocketAddress(this.getIp(), this.getPort()));
        this.getStartBtn().setEnabled(false);
        this.getStopBtn().setEnabled(true);
        this.setWorking(true);
        for(;;){
            if(!this.getWorking()){
                this.getMsgArea().append(MessageUtils.createMsg(STOP_CONNECT_MESSAGE));
                break;
            }
            // 这里select()方法返回的是当前监听得到的事件数目，为0表示当前没有任何事件到达
            if (selector.select(1000) == 0) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (key.isConnectable()) {
                    connect(key, selector);
                }
                if (key.isWritable()) {
                    write(key, selector);
                }
                if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        this.getMsgArea().append(MessageUtils.createMsg(readFromChannel(clientChannel)));
    }

    private void write(SelectionKey key, Selector selector) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void connect(SelectionKey key, Selector selector) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        channel.register(selector, SelectionKey.OP_WRITE);
    }

    @Override
    public void stopConnect() throws IOException {
        if(clientChannel != null){
            clientChannel.close();
            clientChannel = null;
        }
        this.getStartBtn().setEnabled(true);
        this.getStopBtn().setEnabled(false);
        this.setWorking(false);
    }

    @Override
    public void disconnect(SocketChannel channel) throws IOException {
        this.getMsgArea().append(MessageUtils.createMsg("与服务端断开连接！"));
        channel.close();
    }

    @Override
    public void sendMsg() throws IOException {
        String message = this.getSendArea().getText();
        if (null == message || EMPTY_STRING.equals(message.trim())) {
            JOptionPane.showMessageDialog(this.getFrame(), "发送消息不能为空！", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        clientChannel.write(ByteBuffer.wrap(message.trim().getBytes()));
        this.getSendArea().setText(EMPTY_STRING);
    }

}
