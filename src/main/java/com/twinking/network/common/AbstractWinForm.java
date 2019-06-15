package com.twinking.network.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.twinking.network.utils.FormUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 界面
 * @Author: 苏展林（zhanlin.su@luckincoffee.com）
 * @Date: 2019-06-14 10:08
 */
public abstract class AbstractWinForm {

    private static final String[] CONNECT_TYPE = {"TCP"};
    private static final String START_BUTTON_TEXT = "Start";
    private static final String STOP_BUTTON_TEXT = "Stop";
    private static final String CLEAR_BUTTON_TEXT = "清空";
    private static final String SEND_BUTTON_TEXT = "发送";
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final String DEFAULT_PORT = "8888";
    private static final String FONT_STYLE = "Serif";

    protected static final String EMPTY_STRING = "";
    protected static final String SUCCESS_CONNECT_MESSAGE = "连接成功！";
    protected static final String STOP_CONNECT_MESSAGE = "停止连接！";

    private ActionListener actionListener;
    private KeyAdapter keyAdapter;
    private ExecutorService threadPool;
    private JFrame frame;
    private JTextArea msgArea;
    private JTextArea sendArea;
    private JTextField ipText;
    private JTextField portText;
    private String ip;
    private Integer port;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel downPanel;
    private JPanel sendBtnPanel;
    private JButton startBtn;
    private JButton stopBtn;
    private Boolean working = false;

    public AbstractWinForm() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        threadPool = new ThreadPoolExecutor(2, 5
                , 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024)
                , namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        frame = FormUtils.createNewFormNoMaxBtn("客户端",615,400);
        addListener();
        Container con = frame.getContentPane();
        con.setBackground(Color.white);
        frame.setLayout(null);

        leftPanel = new JPanel();
        placeLeftComponents(leftPanel);
        con.add(leftPanel);

        centerPanel = new JPanel();
        placeCenterComponents(centerPanel);
        con.add(centerPanel);

        downPanel = new JPanel();
        placeDownComponents(downPanel);
        con.add(downPanel);

        frame.setVisible(true);
    }

    private void placeDownComponents(JPanel panel) {
        panel.setBorder(BorderFactory.createTitledBorder("消息区"));
        panel.setBounds(210,5,400,250);
        panel.setBackground(Color.white);
        panel.setLayout(new BorderLayout());
        msgArea = new JTextArea();
        msgArea.setFont(new Font(FONT_STYLE,0,11));
        msgArea.setBorder(BorderFactory.createEmptyBorder());
        msgArea.setLineWrap(true);
        msgArea.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(msgArea);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(jScrollPane);
    }

    private void placeCenterComponents(JPanel panel) {
        panel.setBorder(BorderFactory.createTitledBorder("发送消息"));
        panel.setBounds(210,260,400,110);
        panel.setBackground(Color.white);
        panel.setLayout(new BorderLayout());
        sendArea = new JTextArea();
        sendArea.setFont(new Font(FONT_STYLE,0,11));
        sendArea.setLineWrap(true);
        sendArea.addKeyListener(keyAdapter);
        JScrollPane sendMsgPanel = new JScrollPane(sendArea);

        sendBtnPanel = new JPanel();
        sendBtnPanel.setBackground(Color.white);
        sendBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton clearBtn = new JButton(CLEAR_BUTTON_TEXT);
        JButton sendBtn = new JButton(SEND_BUTTON_TEXT);
        clearBtn.addActionListener(actionListener);
        sendBtn.addActionListener(actionListener);
        sendBtnPanel.add(clearBtn);
        sendBtnPanel.add(sendBtn);

        panel.add(sendMsgPanel, BorderLayout.CENTER);
        panel.add(sendBtnPanel, BorderLayout.SOUTH);

    }

    /**
     * 画左边控件
     * @param panel 左边面板
     */
    @SuppressWarnings("unchecked")
    private void placeLeftComponents(JPanel panel) {
        panel.setBorder(BorderFactory.createTitledBorder("参数设置"));
        panel.setLayout(null);
        panel.setBackground(Color.white);
        panel.setBounds(5,5,200,365);
        JLabel ipLabel = new JLabel("地址:");
        ipLabel.setBounds(10,20,80,25);
        panel.add(ipLabel);
        ipText = new JTextField(DEFAULT_HOST);
        ipText.setBounds(50,20,120,25);
        panel.add(ipText);

        JLabel portLabel = new JLabel("端口:");
        portLabel.setBounds(10,50,80,25);
        panel.add(portLabel);
        portText = new JTextField(DEFAULT_PORT);
        portText.setBounds(50, 50, 120, 25);
        panel.add(portText);

        JLabel typeLabel = new JLabel("类型:");
        typeLabel.setBounds(10,80,80,25);
        panel.add(typeLabel);
        JComboBox typeComboBox = new JComboBox(CONNECT_TYPE);
        typeComboBox.setBounds(50, 80, 120, 25);
        panel.add(typeComboBox);


        startBtn = new JButton(START_BUTTON_TEXT);
        startBtn.setBounds(40,115,60,25);
        startBtn.addActionListener(actionListener);
        panel.add(startBtn);
        stopBtn = new JButton(STOP_BUTTON_TEXT);
        stopBtn.setBounds(95,115,60,25);
        stopBtn.addActionListener(actionListener);
        stopBtn.setEnabled(false);
        panel.add(stopBtn);

    }

    private void addListener() {
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()){
                    case START_BUTTON_TEXT:
                        verifyAndConnect();
                        break;
                    case STOP_BUTTON_TEXT:
                        try {
                            stopConnect();
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog( frame, "连接失败！", "警告", JOptionPane.WARNING_MESSAGE );
                            e1.printStackTrace();
                        }
                        break;
                    case CLEAR_BUTTON_TEXT:
                        msgArea.setText(EMPTY_STRING);
                        sendArea.setText(EMPTY_STRING);
                        break;
                    case SEND_BUTTON_TEXT:
                        try {
                            sendMsg();
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(frame, "发送失败！", "警告", JOptionPane.WARNING_MESSAGE);
                            e1.printStackTrace();
                        }
                        break;
                    default:
                        System.out.println(e.getActionCommand());
                        break;
                }
            }
        };

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER)
                {
                    try {
                        sendMsg();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };

        //关闭窗体事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    stopConnect();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * 验证ip和端口并开始连接
     */
    private void verifyAndConnect(){
        boolean legal = ipText.getText() == null || EMPTY_STRING.equals(ipText.getText().trim()) ||
                portText.getText() == null || EMPTY_STRING.equals(portText.getText());
        if(legal){
            JOptionPane.showMessageDialog(frame, "参数错误！", "警告", JOptionPane.WARNING_MESSAGE);
        }
        ip = ipText.getText();
        port = Integer.parseInt(portText.getText().trim());
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    startConnect();
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(frame, "连接失败！", "警告", JOptionPane.WARNING_MESSAGE);
                    e1.printStackTrace();
                }
            }
        });
    }

    /**
     * 开始连接
     * @throws IOException io异常
     */
    protected abstract void startConnect() throws IOException;

    /**
     * 发送消息
     * @throws IOException io异常
     */
    public abstract void sendMsg() throws IOException;

    /**
     * 停止连接
     * @throws IOException io异常
     */
    public abstract void stopConnect() throws IOException;

    /**
     * 断开连接
     */
    public abstract void disconnect(SocketChannel channel) throws IOException ;

    /**
     * 从管道中读取数据写入显示区
     * @param channel SocketChannel
     */
    protected String readFromChannel(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);
        // 如果为-1，则表示客户端断开连接了
        int len = channel.read(buffer);
        if (len == -1) {
            disconnect(channel);
            channel.close();
            return null;
        }
        return new String(buffer.array(), 0, len);
    }

    /**
     * 往客户端Channel写入数据
     * @param message 消息
     * @param channel 通道
     * @throws IOException 写入异常
     */
    protected void writeToChannel(String message, SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.trim().getBytes());
        if (channel.isOpen()) {
            channel.write(buffer);
        }
        buffer.compact();
    }

    public void start() {
        // 显示窗口
        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public JTextArea getMsgArea() {
        return msgArea;
    }

    public void setMsgArea(JTextArea msgArea) {
        this.msgArea = msgArea;
    }

    public JTextArea getSendArea() {
        return sendArea;
    }

    public void setSendArea(JTextArea sendArea) {
        this.sendArea = sendArea;
    }

    public JTextField getIpText() {
        return ipText;
    }

    public void setIpText(JTextField ipText) {
        this.ipText = ipText;
    }

    public JTextField getPortText() {
        return portText;
    }

    public void setPortText(JTextField portText) {
        this.portText = portText;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public JPanel getLeftPanel() {
        return leftPanel;
    }

    public void setLeftPanel(JPanel leftPanel) {
        this.leftPanel = leftPanel;
    }

    public JPanel getCenterPanel() {
        return centerPanel;
    }

    public void setCenterPanel(JPanel centerPanel) {
        this.centerPanel = centerPanel;
    }

    public JPanel getDownPanel() {
        return downPanel;
    }

    public void setDownPanel(JPanel downPanel) {
        this.downPanel = downPanel;
    }

    public JPanel getSendBtnPanel() {
        return sendBtnPanel;
    }

    public void setSendBtnPanel(JPanel sendBtnPanel) {
        this.sendBtnPanel = sendBtnPanel;
    }

    public JButton getStartBtn() {
        return startBtn;
    }

    public void setStartBtn(JButton startBtn) {
        this.startBtn = startBtn;
    }

    public JButton getStopBtn() {
        return stopBtn;
    }

    public void setStopBtn(JButton stopBtn) {
        this.stopBtn = stopBtn;
    }

    public Boolean getWorking() {
        return working;
    }

    public void setWorking(Boolean working) {
        this.working = working;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }
}
