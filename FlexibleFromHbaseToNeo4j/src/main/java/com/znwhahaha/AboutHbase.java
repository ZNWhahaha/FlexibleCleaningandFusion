package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.Str;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AboutHbase {
    //与HBase数据库的连接对象
    public static Connection connection;

    //数据库元数操作对象
    public static Admin admin;

    public static Session session;
    public static Driver driver;
    /**
     * @ClassName : FromHbase
     * @Description : hbase 链接设置
     * @param

     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/6/1
     */
    public static void wakeupHbase(String filepath) throws IOException {
        //取得一个数据库配置参数对象
        Configuration conf = HBaseConfiguration.create();
        //获取hbaseip与端口号
//        String hbaseip = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[0];
//        String hbaseport = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[1];
//        conf.set("hbase.zookeeper.quorum",hbaseip);
//        conf.set("hbase.zookeeper.property.clientPort",hbaseport);
        connection = ConnectionFactory.createConnection(conf);
        admin = connection.getAdmin();
        driver = GraphDatabase.driver(ConfigInit.url, AuthTokens.basic(ConfigInit.username, ConfigInit.passwd));
        session = Controller.wakeupSession(driver);
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
        Controller.closeSession(driver,session);
    }

    public static void SetNeo4jNode(String tablename) throws IOException {
        //获取数据表对象
        Table table = connection.getTable(TableName.valueOf(tablename));

        //获取表中的数据
        ResultScanner scanner = table.getScanner(new Scan());

        //循环输出表中的数据
        for (Result result : scanner){
            List<Cell> listCells = result.listCells();
            HashMap<String,String> nodevalues = new HashMap<String, String>();
            for (Cell cell : listCells){
                String key = Bytes.toString(CellUtil.cloneQualifier(cell));
                String value = Bytes.toString(CellUtil.cloneValue(cell));
                nodevalues.put(key,value);
            }
            for (String kv:ConfigInit.globalkeyvalue){
                String key_g = kv.split("<=>")[0];
                String value_g = kv.split("<=>")[1];
                if(value_g.equals("null")){
                    value_g = "";
                }
                nodevalues.put(key_g,value_g);
            }
            Controller.CreateOrUpdateNode(session,tablename.replaceAll("Profile",""),nodevalues);
        }
    }

    public static void SetNeo4jRel(String tablenameA,String index,String tablenameB, String module) throws IOException {

        String nodeAtype = tablenameA.replaceAll("Profile","");
        String nodeBtype = tablenameB.replaceAll("Profile","");
        String nodeAtypeID = nodeAtype + "ID";
        //获取数据表对象
        Table table = connection.getTable(TableName.valueOf(tablenameA));
        //获取表中的数据
        ResultScanner scanner = table.getScanner(new Scan());

        //循环输出表中的数据
        for (Result result : scanner){
            List<Cell> listCells = result.listCells();
            String thisid = "";
            String idstr = "";
            for (Cell cell : listCells){
                if (index.equals(Bytes.toString(CellUtil.cloneQualifier(cell)))){
                    idstr = Bytes.toString(CellUtil.cloneValue(cell));
                }
                else if (nodeAtypeID.equals(Bytes.toString(CellUtil.cloneQualifier(cell)))){
                    thisid = Bytes.toString(CellUtil.cloneValue(cell));
                }
            }
            String[] ids = idstr.split(",");
            if (module.equals("module1")){
                for (int i = 0; i < ids.length; i++) {
                    Controller.CreateRel(session,nodeAtype,nodeBtype,thisid,ids[i],"Link");
                }
            }
            else if (module.equals("module2")){
                for (int i = 0; i < ids.length; i++) {
                    Controller.CreateRel(session,nodeAtype,nodeBtype,ids[i],thisid,"Behind");
                }
            }

        }
    }
}
