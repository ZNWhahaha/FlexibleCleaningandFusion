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
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.ArrayList;

public class MR extends Configured implements Tool {
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
        protected void setup(Context context) throws IOException{
            Configuration conf = context.getConfiguration();
            String filePath = conf.get("filePath");
            String Hbasetablename = conf.get("Hbasetablename");
            ConfigInit.initIndexRule(filePath,Hbasetablename);
        }


        public void map(ImmutableBytesWritable row, Result values, Context context)
                throws IOException, InterruptedException {

            ArrayList<ImmutableBytesWritable> valueSet = new ArrayList<ImmutableBytesWritable>();
            //添加主键
            String[] rowkey = null;
            //添加主键区分符
            String compare = "";
            Cell rawCell[] = values.rawCells();
            for (Cell cell : rawCell) {
                //提取特定属性进行融合
                String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                String cellvalue = "";
                String valueStr = "";
                //为合并项添加内容
                String fusionvalue = "";
                
                //为主键添加内容
                if (ConfigInit.Rowkey.equals(qualifier)){
                    rowkey = Bytes.toString(CellUtil.cloneValue(cell)).split(",|，|；|;|、");
                }
                //添加区分符内容
                if (ConfigInit.Compareindex.contains(qualifier)){
                    compare += Bytes.toString(CellUtil.cloneValue(cell));
                }
                if (ConfigInit.Fusionindex.equals(qualifier)){
                    fusionvalue = Bytes.toString(CellUtil.cloneValue(cell));
                }
                else{
                    cellvalue = Bytes.toString(CellUtil.cloneValue(cell));
                }
                if (cellvalue.length() > 0){
                    valueStr = qualifier + "<=>" +cellvalue ;
                }
                else if(fusionvalue.length() > 0){
                    for (int i = 0; i < rowkey.length; i++) {
                        valueStr = "fusionvalue<=>" + fusionvalue;
                    }
                }
                ImmutableBytesWritable value = new ImmutableBytesWritable(Bytes.toBytes(valueStr));
                valueSet.add(value);
            }
            for (int i = 0; i < rowkey.length; i++) {
                String keyStr = rowkey[i] + "<=>" + compare;
                ImmutableBytesWritable key = new ImmutableBytesWritable(Bytes.toBytes(keyStr));
                for (ImmutableBytesWritable va:valueSet){
                    context.write(key,va);
                }
            }

        }
    }

    // MyReduce函数
    public static class MyReducer
            extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {

        public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
                throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            ConfigInit.Outputtable = conf.get("outputtable");
            String Rowkey = Bytes.toString(key.get());
            ArrayList<String> qualifierSet = new ArrayList<String>();
            ArrayList<String> valueSet = new ArrayList<String>();
            String fusionvalue = "";
            for (ImmutableBytesWritable val : values) {
                String valueStr = Bytes.toString(val.get());
                String qualifier = valueStr.split("<=>")[0];
                String value = valueStr.split("<=>")[1];
                if (qualifier.equals("fusionvalue")){
                    fusionvalue += value + ",";
                }
                if (!qualifierSet.contains(qualifier) && ConfigInit.Outputtableindex.contains(qualifier)) {
                    qualifierSet.add(qualifier);
                    valueSet.add(value);
                }
            }

            Rowkey = ConfigInit.Outputtable + ":" + Rowkey;
            //添加主键
            Put put = new Put(Bytes.toBytes(Rowkey));
            context.write(null,put);
            //添加融合后数据
            put.addColumn(Bytes.toBytes("inf"), Bytes.toBytes(ConfigInit.Fusionindex), Bytes.toBytes(fusionvalue));
            context.write(null,put);
            for (int i = 0; i < qualifierSet.size(); i++) {
                put.addColumn(Bytes.toBytes("inf"), Bytes.toBytes(qualifierSet.get(i)), Bytes.toBytes(valueSet.get(i)));
                context.write(null, put);
            }
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        boolean b = true;
        String filepath = args[0];
        ArrayList<String> tables = ReadConfigFile.ReadConfigItem(filepath,"融合Hbase表名=");
        for (String table : tables){
            String inputtable = table.split("<=>")[0];
            String outputtable = table.split("<=>")[1];

            Configuration hbaseConf = HBaseConfiguration.create();
            hbaseConf.set("filePath", filepath);
            hbaseConf.set("Hbasetablename",inputtable);
            hbaseConf.set("outputtable",outputtable);


            Job job = Job.getInstance(hbaseConf, "fusion_"+table);
            job.setJarByClass(Main.class);

            ConfigInit.initIndexRule(filepath,inputtable);

            Scan scan = new Scan();
            scan.setCaching(500);
            scan.setCacheBlocks(false);

            TableMapReduceUtil.initTableMapperJob(inputtable, scan, MR.MyMapper.class, ImmutableBytesWritable.class,
                    ImmutableBytesWritable.class, job);

            TableMapReduceUtil.initTableReducerJob(outputtable, MR.MyReducer.class, job);

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
