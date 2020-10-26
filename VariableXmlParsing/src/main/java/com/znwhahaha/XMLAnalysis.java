package com.znwhahaha;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;

public class XMLAnalysis {

    //存储配置文件路径
    public String conffilepath = "";



    /**
     * @ClassName : XMLAnalysis
     * @Description : xml数据解析
     * @param filePath

     * @Return : com.znwhahaha.Item
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
    */
    public Item FilexmlAnalysis(String filePath, String tablename) throws SAXException, ParserConfigurationException,IOException {
        ReadConfigFile rf = new ReadConfigFile();
        //从配置文件中获取属标志
        ArrayList<String> xmlindex = rf.ReadConfigItem(conffilepath,tablename+"表xml标签属性=");
        Item item = new Item();
        //存储重复元素类中重复的个数
        int num = 0;
        System.out.println("创建DOM解析器工厂");
        //创建DOM解析器工厂
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        System.out.println("获取解析器对象");
        //获取解析器对象
        DocumentBuilder db = dbf.newDocumentBuilder();
        //调用DOM解析器对象paerse（string uri）方法得到Document对象
//        System.out.println("调用DOM解析器   :" +filePath);
        Document doc = db.parse(filePath);
        //获得NodeList对象
        NodeList nl = doc.getElementsByTagName("Document");
        //遍历XML文件中的各个元素
        for (int i = 0; i < nl.getLength(); i++) {
            //得到Nodelist中的Node对象
            Node node = nl.item(i);
            //强制转化得到Element对象
            Element element = (Element) node;

            //获取各个元素的属性值
            System.out.println("开始传递数据");
            for (String xm:xmlindex){
                if(element.getElementsByTagName(xm).getLength() != 0){
                    num = element.getElementsByTagName(xm).getLength();
                    for (int j = 0; j < num; j++) {
                        if (j == 0) {
                            item.itemmap.put(xm,element.getElementsByTagName(xm).item(j).getTextContent());
                        }else {
                            String value = item.itemmap.get(xm) + ","+element.getElementsByTagName(xm).item(j).getTextContent();
                            item.itemmap.put(xm,value);
                        }

                    }
                    System.out.println("传递成功  "+xm+ "("+item.itemmap.get(xm)+")" + element.getElementsByTagName(xm).item(0).getTextContent());
                }
            }
            }

        return item;
    }
    
    /**
     * @ClassName : XMLAnalysis
     * @Description : 通过给定文件夹路径，查找该文件夹下的所有文件及文件名，并存储至泛型中
     * @param localfilePath  文件夹路径
     * @Return : java.lang.String[]
     * @Author : ZNWhahaha
     * @Date : 2020/10/10
    */
    public String[] XMLFilePath(String localfilePath){
        File file = new File(localfilePath);
        String[] array1 = file.list();
        return array1;
    }



}
