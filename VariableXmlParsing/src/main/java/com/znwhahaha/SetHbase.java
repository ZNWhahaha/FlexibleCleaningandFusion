package com.znwhahaha;

import com.jcraft.jsch.IO;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import java.io.IOException;

public class SetHbase {

    // 定义配置对象HBaseConfiguration
    public static void initHbase(Configuration configuration,String ip,String port) throws IOException{
        configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum",ip);  //hbase 服务地址
        configuration.set("hbase.zookeeper.property.clientPort",port); //端口号
    }

}
