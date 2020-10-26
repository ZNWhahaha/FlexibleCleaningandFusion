package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String filepath = args[0];
        ArrayList<String> tables = ReadConfigFile.ReadConfigItem(filepath,"关联Hbase表名=");
        for (String table : tables){
            String inputtable = table.split("<=>")[0];
            String outputtable = table.split("<=>")[1];

            Configuration hbaseConf = HBaseConfiguration.create();
            hbaseConf.set("filePath", filepath);
            hbaseConf.set("Hbasetablename",inputtable);
            hbaseConf.set("outputtable",inputtable);

//            //获取hbaseip与端口号
//            String hbaseip = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[0];
//            String hbaseport = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[1];
//            hbaseConf.set("hbase.zookeeper.quorum",hbaseip);
//            hbaseConf.set("hbase.zookeeper.property.clientPort",hbaseport);

            Job job = Job.getInstance(hbaseConf, "fusion_"+table);
            job.setJarByClass(Main.class);

            ConfigInit.initIndexRule(filepath,inputtable,outputtable);

            List scans = new ArrayList();
            Scan scan1 = new Scan();
            scan1.setCaching(500);
            scan1.setCacheBlocks(false);
            scan1.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(inputtable));
            scans.add(scan1);

            Scan scan2 = new Scan();
            scan2.setCaching(500);
            scan2.setCacheBlocks(false);
            scan2.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes(outputtable));
            scans.add(scan2);

            TableMapReduceUtil.initTableMapperJob(scans, MR.MyMapper.class, ImmutableBytesWritable.class,
                    ImmutableBytesWritable.class, job);

            TableMapReduceUtil.initTableReducerJob(inputtable, MR.MyReducer.class, job);

            job.waitForCompletion(true);
        }

    }
}
