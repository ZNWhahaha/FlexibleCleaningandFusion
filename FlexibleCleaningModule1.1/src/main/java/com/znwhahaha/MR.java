package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;

public class MR extends Configured implements Tool {
    // MyMapper函数
    public static class MyMapper extends TableMapper<Text, Put> {

        /**
         * @ClassName : MyMapper
         * @Description : 在开始前获取到对应表属性的清洗规则
         * @param context

         * @Return : void
         * @Author : ZNWhahaha
         * @Date : 2020/10/12
        */
        protected void setup(Context context) throws IOException{
            Configuration conf = context.getConfiguration();
            String filePath = conf.get("filePath");
            String Hbasetablename = conf.get("Hbasetablename");
            ConfigInit.initIndexRule(filePath,Hbasetablename);
        }


        public void map(ImmutableBytesWritable row, Result values, Context context)
                throws IOException, InterruptedException {
            
            //添加ID
            String rowkey = Bytes.toString(row.get());
            rowkey = ConfigInit.outputtable +":"+ rowkey;
            Put put = new Put(Bytes.toBytes(rowkey));
            Cell rawCell[] = values.rawCells();
            for (Cell cell : rawCell) {
                //相同模块提取属性值与清洗规则后进行清洗
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                if (ConfigInit.tableindex.contains(qualifier)){
                    String cellvalue = Bytes.toString(CellUtil.cloneValue(cell));
                    for (String rule:ConfigInit.indexcleanrule){
                        if (rule.contains(qualifier)){
                            rule = rule.substring(rule.indexOf("\"")+1,rule.lastIndexOf("\""));
                            //根据配置文件所给信息对相应的hbase值按照对应清洗规则进行清洗
                            DataCleaning cleanData=new DataCleaning(cellvalue,rule);
                            if(cleanData.cleanDataByRule()){
                                cellvalue=cleanData.getCleanResult();
                            }
                        }
                    }
                    if(cellvalue.length() > 0){
                        put.addColumn(Bytes.toBytes("inf"), Bytes.toBytes(ConfigInit.oldIndexToNew.get(qualifier)), Bytes.toBytes(cellvalue));

                    }
                }
                if(!put.isEmpty()){
                    context.write(new Text(rowkey),put);
                }

            }

        }
    }



    // MyReduce函数
    public static class MyReducer
            extends TableReducer<Text,Put,ImmutableBytesWritable> {


        public void reduce(Text key, Iterable<Put> values, Context context) throws IOException, InterruptedException {

            for (Put value : values) {
                
                context.write(null,value);

            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        boolean b = true;

        String filepath = args[0];

        ArrayList<String> tables = ReadConfigFile.ReadConfigItem(filepath,"清洗Hbase表名=");
        for (String table : tables){
            String inputtable = table.split("<=>")[0];
            String outputtable = table.split("<=>")[1];
            Configuration hbaseConf = HBaseConfiguration.create();
            hbaseConf.set("filePath", filepath);
            hbaseConf.set("Hbasetablename",inputtable);
            hbaseConf.set("Outputtable",outputtable);


            Job job = Job.getInstance(hbaseConf, "clean_"+table);
            job.setJarByClass(MR.class);

            ConfigInit.initIndexRule(filepath,inputtable);

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);

            TableMapReduceUtil.initTableMapperJob(inputtable, scan, MyMapper.class, Text.class, Put.class, job);

            TableMapReduceUtil.initTableReducerJob(outputtable, MyReducer.class, job);
       
            b = job.waitForCompletion(true);

        }
        return b?0:1;
    }

    public static void main(String[] args) throws Exception {

        //创建HBaseConfiguration配置
        Configuration configuration = HBaseConfiguration.create();
        int run = ToolRunner.run(configuration, new MR(), args);
        System.exit(run);

    }


}
