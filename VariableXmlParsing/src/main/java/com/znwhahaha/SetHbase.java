package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SetHbase {

    //存储配置文件路径
    public static String conffilepath = "";
    //数据库元数操作对象
    public static Admin admin;
    //与HBase数据库的连接对象
    public static Connection connection;
    /**
     * @ClassName : SetHbase
     * @Description : 定义配置对象HBaseConfiguration
     * @param ip
     * @param port
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
    */
    public void initHbase(String ip,String port) throws IOException{
        Configuration configuration = HBaseConfiguration.create();
        //hbase服务地址
//        configuration.set("hbase.zookeeper.quorum",ip);
//        //端口号
//        configuration.set("hbase.zookeeper.property.clientPort",port);
        connection = ConnectionFactory.createConnection(configuration);
        admin = connection.getAdmin();
    }

    

    /**
     * @ClassName : XMLAnalysis
     * @Description : 对于所存入的HbaseItem内的数据进行处理，放入到hbase数据库中
     * @param tableName

     * @Return : boolean
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
     */
    public boolean FileToHbase(String tableName,Item item) throws IOException {
        if (item.itemmap.isEmpty()){
            return false;
        }
        ArrayList<String> hbaseindex = ReadConfigFile.ReadConfigItem(conffilepath,tableName+"列信息=");
        String rowkeyindex = ReadConfigFile.ReadConfigItem(conffilepath,tableName+"的RowKey=").get(0);
        Table table = connection.getTable(TableName.valueOf(tableName));
        String key = (String)item.itemmap.get(rowkeyindex);
        if (key == null || key.equals("")) {
            return  false;
        }
        //添加
        List<Put> puts = new ArrayList<Put>();
        for (String hb:hbaseindex){
            Put put = new Put(key.getBytes());
            System.out.println(hb +" ==== "+ item.itemmap.get(hb));
            if ( item.itemmap.get(hb) == null){
                put.addColumn(Bytes.toBytes("inf"),Bytes.toBytes(hb),Bytes.toBytes(""));
            }
            else {
                put.addColumn(Bytes.toBytes("inf"),Bytes.toBytes(hb),Bytes.toBytes((String) item.itemmap.get(hb)));
            }
            puts.add(put);
            
        }
        table.put(puts);
        table.close();
        return true;
    }

    
    /**
     * @ClassName : SetHbase
     * @Description : 关闭Hbase数据库
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
    */
    public void close() throws IOException {
        admin.close();
        connection.close();
    }

}
