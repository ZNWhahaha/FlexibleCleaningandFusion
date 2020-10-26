package com.znwhahaha;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.*;

public class MR {
    public static class MyMapper extends TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {

        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String filePath = conf.get("filePath");
            String Hbasetableinputname = conf.get("Hbasetableinputname");
            String Hbasetableoutputname = conf.get("Hbasetableoutputname");
            ConfigInit.initIndexRule(filePath,Hbasetableinputname,Hbasetableoutputname);
        }

        public void map(ImmutableBytesWritable row, Result values, Context context)
                throws IOException, InterruptedException {
            String rowkey = Bytes.toString(row.get());
            String compare = "";
            ArrayList<ImmutableBytesWritable> valueSet = new ArrayList<ImmutableBytesWritable>();
            //Targettable相当于要修改id的表，Bullettable为所修改id对应的表
            if (rowkey.contains(ConfigInit.Targettable)){
                //如果为目标表时，rowkey为自身的rowkey
                rowkey = Bytes.toString(row.get()).split(":")[1];
                Cell rawCell[] = values.rawCells();
                for (Cell cell : rawCell){
                    String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                    String cellvalue = "";
                    String valueStr = "";
                    if (ConfigInit.Compare.contains(qualifier)){
                        compare += Bytes.toString(CellUtil.cloneValue(cell));
                    }
                    if (cellvalue.length() > 0){
                        valueStr = qualifier + "<=>" +cellvalue ;
                    }
                    ImmutableBytesWritable value = new ImmutableBytesWritable(Bytes.toBytes(valueStr));
                    valueSet.add(value);
                }
            }
            else if(rowkey.contains(ConfigInit.Bullettable)){
                //存储ID对应的值
                String valuetoID = "";
                String valueStr = "";
                Cell rawCell[] = values.rawCells();
                for (Cell cell : rawCell){
                    String qualifier = Bytes.toString(CellUtil.cloneQualifier(cell));
                    String cellvalue = "";

                    if (ConfigInit.Compare.contains(qualifier)){
                        compare += Bytes.toString(CellUtil.cloneValue(cell));
                    }
                    else if(ConfigInit.Key.equals(qualifier)){
                        //如果为关联副表时，rowkey为与目标表相同的qualifier值
                        rowkey = Bytes.toString(CellUtil.cloneValue(cell));
                    }
                    else if (ConfigInit.Bullet.equals(qualifier)){
                        valuetoID = Bytes.toString(CellUtil.cloneValue(cell));
                    }
                    if (qualifier.contains("ID")){
                        valueStr = cellvalue ;
                    }
                }
                valueStr = valuetoID +":ID" + "<=>" +valueStr ;
                ImmutableBytesWritable value = new ImmutableBytesWritable(Bytes.toBytes(valueStr));
                valueSet.add(value);
            }

            String keyStr = rowkey + "<=>" + compare;
            ImmutableBytesWritable key = new ImmutableBytesWritable(Bytes.toBytes(keyStr));
            for (ImmutableBytesWritable va:valueSet){
                context.write(key,va);
            }

        }
    }

    public static class MyReducer
            extends TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {

        public void reduce(ImmutableBytesWritable key, Iterable<ImmutableBytesWritable> values, Context context)
                throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            ConfigInit.Outputtable = conf.get("outputtable");
            //获取主键
            String k1 = Bytes.toString(key.get());
            String Rowkey = k1.split("<=>")[0];
            ArrayList<String> qualifierSet = new ArrayList<String>();
            ArrayList<String> valueSet = new ArrayList<String>();
            //存储关联附表信息对应id的键值对
            HashMap<String,String> bullet = new HashMap<String, String>();
            //设置标志位判断目标表中是否有附表属性
            boolean flag = false;
            for (ImmutableBytesWritable val : values){
                String valueStr = Bytes.toString(val.get());
                String qualifier = valueStr.split("<=>")[0];
                String value = valueStr.split("<=>")[1];
                if (qualifier.contains("ID")){
                    //此时bullet的key为关联附表的名字
                    bullet.put(qualifier.split(":")[0],value);
                }
                else if (!qualifierSet.contains(qualifier)){
                    qualifierSet.add(qualifier);
                    valueSet.add(value);
                }
            }
            String bulletToqualifiersetValue = "";
            for (int i = 0; i < qualifierSet.size(); i++){
                if (qualifierSet.get(i).equals(ConfigInit.Bullet)){
                    bulletToqualifiersetValue = valueSet.get(i);
                    Set<String> set = bullet.keySet();
                    Iterator<String> iterator = set.iterator();
                    //替换主关联表中的关联内容所对应的id
                    while (iterator.hasNext()){
                        String str = iterator.next();
                        bulletToqualifiersetValue.replaceAll(str,bullet.get(str));
                    }
                    flag = true;
                }
            }
            //当flag等于true时，表明关联内容已替换为id，则添加
            if (flag == true){
                qualifierSet.add(ConfigInit.Bullet);
                valueSet.add(bulletToqualifiersetValue);
            }

            Rowkey = ConfigInit.Outputtable + ":" + Rowkey;
            //添加主键
            Put put = new Put(Bytes.toBytes(Rowkey));
            context.write(null,put);
            for (int i = 0; i < qualifierSet.size(); i++) {
                put.addColumn(Bytes.toBytes("inf"), Bytes.toBytes(qualifierSet.get(i)), Bytes.toBytes(valueSet.get(i)));
                context.write(null, put);

            }

        }
    }
}
