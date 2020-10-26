package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class AboutHbase {
    //与HBase数据库的连接对象
    public static Connection connection;

    //数据库元数操作对象
    public static Admin admin;
    
    /**
     * @ClassName : FromHbase
     * @Description : hbase 链接设置
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/6/1
     */
    public static void wakeupHbase() throws IOException {
        //取得一个数据库配置参数对象
        Configuration conf = HBaseConfiguration.create();
        //获取hbaseip与端口号
//        String hbaseip = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[0];
//        String hbaseport = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[1];
//        conf.set("hbase.zookeeper.quorum",hbaseip);
//        conf.set("hbase.zookeeper.property.clientPort",hbaseport);
        connection = ConnectionFactory.createConnection(conf);
        admin = connection.getAdmin();

    }

    /**
     * @ClassName : FromHbase
     * @Description : hbase关闭
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/6/1
     */
    public static void close() throws IOException {
        admin.close();
        connection.close();
    }

    /**
     * @ClassName : SetHbase
     * @Description : 获取Hbase表中RowKey的最大值
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
     */
    public static int GetMaxHbaseRowKey(String tablename) throws IOException {
        int maxid = 0;
        //获取数据表对象
        Table table = connection.getTable(TableName.valueOf(tablename));
        //获取表中的数据
        ResultScanner scanner = table.getScanner(new Scan());
        for (Result result : scanner) {
            maxid += 1;
        }
        return maxid;
    }

    public static void SetHbaseID(String tablename) throws IOException {

        //获取数据表对象
        Table table = connection.getTable(TableName.valueOf(tablename));
        //获取表中的数据
        ResultScanner scanner = table.getScanner(new Scan());
        int nowid = GetMaxHbaseRowKey(tablename);
        //循环输出表中的数据
        for (Result result : scanner){
            Cell rawCell[] = result.rawCells();
            for (Cell cell : rawCell) {
                if (cell.getQualifierArray().equals(tablename+":ID")){
                    return;
                }
            }
            nowid += 1;
            String key = Bytes.toString(result.getRow());
            Put put = new Put(key.getBytes());
            put.addColumn(Bytes.toBytes("inf"),Bytes.toBytes(tablename+":ID"),Bytes.toBytes(nowid));
        }
    }
}
