package com.znwhahaha;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XMLAnalysis {

    Item item = new Item();

    private static Item FilexmlAnalysis(String filePath) throws SAXException, ParserConfigurationException,IOException {
        //存储重复元素类中重复的个数
        int num = 0;
        System.out.println("创建DOM解析器工厂");
        //创建DOM解析器工厂
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        System.out.println("获取解析器对象");
        //获取解析器对象
        DocumentBuilder db = dbf.newDocumentBuilder();
        //调用DOM解析器对象paerse（string uri）方法得到Document对象
        Document doc = db.parse(filePath);
        //获得NodeList对象
        NodeList nl = doc.getElementsByTagName("Document");
//            if (nl.getLength() == 0)
//                return false;
        System.out.println(nl.getLength());
        //遍历XML文件中的各个元素
        for (int i = 0; i < nl.getLength(); i++) {
            //得到Nodelist中的Node对象
            Node node = nl.item(i);
            //强制转化得到Element对象
            Element element = (Element) node;

            //获取各个元素的属性值
            System.out.println("开始传递数据");
        }
        return null;
    }

    //对于所存入的HbaseItem内的数据进行处理，放入到hbase数据库中
    private static boolean FileToHbase(String tableName){
        return false;
    }

    //通过给定文件夹路径，查找该文件夹下的所有文件及文件名，并存储至泛型中
    //input：文件夹路线
    //output：该文件夹下所有文件的绝对路径
    private static List<String> XMLFilePath(String localfilePath){
        return null;
    }


    //String keyword
    if(element.getElementsByTagName("keyword").getLength() != 0){
        num = element.getElementsByTagName("keyword").getLength();
        for (int j = 0; j < num; j++) {
            if (j == 0) {
                hItem.keyword = element.getElementsByTagName("keyword").item(j).getTextContent();
            }else {
                hItem.keyword += ","+element.getElementsByTagName("keyword").item(j).getTextContent();
            }

        }
        System.out.println("传递fundsproject成功  "+ "("+hItem.fundsproject+")" + element.getElementsByTagName("fundsproject").item(0).getTextContent());
    }
}
