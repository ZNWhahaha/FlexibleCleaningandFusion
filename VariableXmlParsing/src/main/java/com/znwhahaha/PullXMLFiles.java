package com.znwhahaha;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;

import java.io.IOException;

public class PullXMLFiles {

    public boolean login(String ip,String usr,String psword){
        //创建远程连接，默认连连接端口为22，如果不使用默认，可以使用方法
        //new Connection(ip, port)创建对象
        Connection conn = new Connection(ip);
        try {
            //连接远程服务器
            conn.connect();
            //使用用户名和密码登录
            return conn.authenticateWithPassword(usr, psword);
        } catch (IOException e) {
            System.err.printf("用户%s密码%s登录服务器%s失败！", usr, psword, ip);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载服务器文件到本地目录
     */
    public void copyFile(Connection conn,String filename){
        SCPClient sc = new SCPClient(conn);
        try {
            sc.get(filename);
            s
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
