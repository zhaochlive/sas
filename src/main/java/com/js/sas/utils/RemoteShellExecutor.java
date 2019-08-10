package com.js.sas.utils;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @ClassName RemoteShellExecutor
 * @Description 远程Shell
 * @Author zc
 * @Date 2019/8/3 10:45
 **/
public class RemoteShellExecutor {
    private Connection conn;

    private String ip;

    private int port;

    private String userName;

    private String password;

    private String shell;

    private String charset = Charset.defaultCharset().toString();
    /**
     * 超时时间
     */
    private static final int TIME_OUT = 1000 * 5 * 60;

    /**
     *
     * @param ip ip
     * @param port 端口
     * @param userName 用户
     * @param password 密码
     * @param shell shell路径和文件名
     */
    public RemoteShellExecutor(String ip, int port, String userName, String password, String shell) {
        this.ip = ip;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.shell = shell;
    }

    /**
     * 执行脚本
     *
     * @return message
     */
    public String exec() {
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outMessage;
        try {
            conn = new Connection(ip, port);
            conn.connect();
            conn.authenticateWithPassword(userName, password);

            Session session = conn.openSession();
            session.execCommand(shell);
            stdOut = new StreamGobbler(session.getStdout());
            //outStr = processStream(stdOut, charset);
            stdErr = new StreamGobbler(session.getStderr());
            outMessage = processStream(stdErr, charset);
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
        } catch (IOException e) {
            outMessage = e.getMessage();
            e.printStackTrace();
        } catch (Exception e) {
            outMessage = e.getMessage();
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
            IOUtils.closeQuietly(stdOut);
            IOUtils.closeQuietly(stdErr);
        }
        //返回执行结果
        return outMessage;
    }

    private String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }
        return sb.toString();
    }

}
