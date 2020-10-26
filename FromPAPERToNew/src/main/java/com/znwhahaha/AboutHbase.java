package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AboutHbase {

    //存储配置文件路径
    public static String conffilepath = "";
    //数据库元数操作对象
    public static Admin admin;
    //与HBase数据库的连接对象
    public static Connection connection;
    /**
     * @ClassName : SetHbase
     * @Description : 定义配置对象HBaseConfiguration
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
     */
    public void initHbase() throws IOException {
        Configuration configuration = HBaseConfiguration.create();
        connection = ConnectionFactory.createConnection(configuration);
        admin = connection.getAdmin();
    }



    /**
     * @ClassName : XMLAnalysis
     * @Description : 对于所存入的HbaseItem内的数据进行处理，放入到hbase数据库中
     * @Return : boolean
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
     */
    public boolean oldTonew(String inputtableName,String outputtablename) throws IOException {
        Table table_input = connection.getTable(TableName.valueOf(inputtableName));
        Table table_output = connection.getTable(TableName.valueOf(outputtablename));
        //添加

        ResultScanner scanner_input = table_input.getScanner(new Scan());
        for (Result result : scanner_input){
            Cell rawCell[] = result.rawCells();
            List<Put> puts = new ArrayList<Put>();
            for (Cell cell : rawCell){
                if (cell.getFamilyArray().equals("authors")){
                    Put put = new Put(result.getRow());
                    put.addColumn(Bytes.toBytes("inf"),Bytes.toBytes("autors"),cell.getValueArray());
                    puts.add(put);
                    //核实数据库具体内容，查看authors的列是否是机构名！！！
                }
                else {
                    Put put = new Put(result.getRow());
                    put.addColumn(Bytes.toBytes("inf"),cell.getQualifierArray(),cell.getValueArray());
                    puts.add(put);
                }
            }
            table_output.put(puts);
        }
        table_input.close();
        table_output.close();
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

    public  void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        initHbase();
        String inputname = args[0];
        String outputname = args[1];
        oldTonew(inputname,outputname);
        close();
    }

}
