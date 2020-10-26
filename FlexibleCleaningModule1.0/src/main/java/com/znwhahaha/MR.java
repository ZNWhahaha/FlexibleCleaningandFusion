package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper.Context;

import java.io.IOException;
import java.util.ArrayList;

public class MR {
    // MyMapper函数
    public static class MyMapper extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {

        /**
         * @ClassName : MyMapper
         * @Description : 在开始前获取到对应表属性的清洗规则
         * @param context

         * @Return : void
         * @Author : ZNWhahaha
         * @Date : 2020/10/12
        */
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String filePath = conf.get("filePath");
            String Hbasetablename = conf.get("Hbasetablename");
            ConfigInit.initIndexRule(filePath,Hbasetablename);
            ConfigInit.ID = 0;
        }


        public void map(ImmutableBytesWritable row, Result values, Context context)
                throws IOException, InterruptedException {

            ArrayList<ImmutableBytesWritable> valueSet = new ArrayList<ImmutableBytesWritable>();
            //添加ID
            String rowkey = Bytes.toString(row.get());

            Cell rawCell[] = values.rawCells();
            for (Cell cell : rawCell) {
                //相同模块提取属性值与清洗规则后进行清洗
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                for (String index : ConfigInit.tableindex){
                    if (index.equals(Bytes.toString(CellUtil.cloneQualifier(cell)))){
                        String cellvalue = Bytes.toString(CellUtil.cloneValue(cell));
                        for (String rule:ConfigInit.indexcleanrule){
                            if (rule.contains(index)){
                                rule = rule.substring(rule.indexOf("\"")+1,rule.lastIndexOf("\""));
                                //根据配置文件所给信息对相应的hbase值按照对应清洗规则进行清洗
                                DataCleaning cleanData=new DataCleaning(cellvalue,rule);
                                if(cleanData.cleanDataByRule()){
                                    cellvalue=cleanData.getCleanResult();
                                }
                            }
                        }
                        if(cellvalue.length() > 0){
                            String valueStr = qualifier + "<=>" +cellvalue ;
                            ImmutableBytesWritable value = new ImmutableBytesWritable(Bytes.toBytes(valueStr));
                            valueSet.add(value);
                        }
                    }
                }
            }
            String keyStr = rowkey;
            ImmutableBytesWritable key = new ImmutableBytesWritable(Bytes.toBytes(keyStr));
            for (ImmutableBytesWritable va:valueSet){
                context.write(key,va);
            }
        }
    }

    // MyReduce函数
    public static class MyReducer
            extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {


        public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
                throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            ConfigInit.outputtable = conf.get("Outputtable");

            String Rowkey = ConfigInit.outputtable+":"+ Bytes.toString(key.get());

            ArrayList<String> qualifierSet = new ArrayList<String>();
            ArrayList<String> valueSet = new ArrayList<String>();

            for (ImmutableBytesWritable val : values) {
                String valueStr = Bytes.toString(val.get());
                String qualifier = valueStr.split("<=>")[0];
                String value = valueStr.split("<=>")[1];
                if (!qualifierSet.contains(qualifier)) {
                    qualifierSet.add(qualifier);
                    valueSet.add(value);
                }
            }
            qualifierSet.add(ConfigInit.outputtable + ":ID");
            ConfigInit.ID += 1;
            valueSet.add(String.valueOf(ConfigInit.ID));
            Put put = new Put(Bytes.toBytes(Rowkey));
            context.write(null,put);
            for (int i = 0; i < qualifierSet.size(); i++) {
                put.addColumn(Bytes.toBytes("inf"), Bytes.toBytes(qualifierSet.get(i)), Bytes.toBytes(valueSet.get(i)));
                context.write(null, put);
            }
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        String filepath = args[0];
//        String filepath = "/Users/znw_mac/GitRepository/FlexibleCleaningAndFusion/FlexibleCleaningandFusion/FlexibleCleaningModule1.0/xml.conf";
        ArrayList<String> tables = ReadConfigFile.ReadConfigItem(filepath,"清洗Hbase表名=");
        for (String table : tables){
            String inputtable = table.split("<=>")[0];
            String outputtable = table.split("<=>")[1];

            Configuration hbaseConf = HBaseConfiguration.create();
            hbaseConf.set("filePath", filepath);
            hbaseConf.set("Hbasetablename",inputtable);
            hbaseConf.set("Outputtable",outputtable);

            //获取hbaseip与端口号
//            String hbaseip = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[0];
//            String hbaseport = ReadConfigFile.ReadConfigItem(filepath,"HbaseIP地址与端口=").get(0).split("<=>")[1];
//            hbaseConf.set("hbase.zookeeper.quorum",hbaseip);
//            hbaseConf.set("hbase.zookeeper.property.clientPort",hbaseport);

            Job job = Job.getInstance(hbaseConf, "clean_"+table);
            job.setJarByClass(MR.class);
            
            
            ConfigInit.initIndexRule(filepath,inputtable);

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);

            TableMapReduceUtil.initTableMapperJob(inputtable, scan, MR.MyMapper.class, ImmutableBytesWritable.class, ImmutableBytesWritable.class, job);

            TableMapReduceUtil.initTableReducerJob(outputtable, MR.MyReducer.class, job);

            job.waitForCompletion(true);
        }

    }
}
