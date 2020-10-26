package com.znwhahaha;

import java.util.*;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;


public class Controller {


    /**
     * @ClassName : Controller
     * @Description : 执行CSQL语句，向Neo4j数据库中插入数据
     * @param nodetype
     * @param nodevalues
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/15
    */
    public static void CreateOrUpdateNode(Session session, String nodetype, HashMap<String,String> nodevalues){

        String runstr = makeRunStr("createnode",nodevalues);
        //判断neo4j内是否存在该节点，存在则更新信息，不存在则创建信息
        StatementResult result = session.run( "MATCH (a:"+nodetype+" {"+nodetype+"ID:\""+nodevalues.get(nodetype+"ID")+"\"}) RETURN a");
        if (result.hasNext()){
            UpdateNode(session,nodetype,nodevalues);
        }
        else {
            session.run( "CREATE (a:"+nodetype+" {"+runstr+"}) RETURN a");
        }
    }

    /**
     * @ClassName : Controller
     * @Description : 对两个节点建立关联
     * @param nodeAtype
     * @param nodeBtype
     * @param nodeAid
     * @param nodeBid
     * @param linktype
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/18
    */
    public static void CreateRel(Session session, String nodeAtype, String nodeBtype,String nodeAid, String nodeBid,String linktype){
        StatementResult result = session.run("MATCH p=(a:"+nodeAtype+" {"+nodeAtype+"ID:\""+nodeAid+"\"})-[]->(b:"+nodeBtype+"{"+nodeBtype+"ID:\""+nodeBid+"\"}) RETURN p");
        if (result.hasNext()){
            return;
        }
        else {
            session.run( "MATCH (a:"+nodeAtype+" {"+nodeAtype+"ID:\""+nodeAid+"\"}),(b:"+nodeBtype+" {"+nodeBtype+"ID:\""+nodeBid+"\"}) CREATE (a)-[r:"+linktype+"]->(b) RETURN r");
        }

    }

    /**
     * @ClassName : Controller
     * @Description : 更新node属性信息
     * @param nodetype
     * @param nodevalues
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/18
    */
    public static void UpdateNode(Session session, String nodetype,HashMap<String,String> nodevalues){
        String runstr = makeRunStr("updatenode",nodevalues);
        session.run("MATCH (n:"+nodetype+" {"+nodetype+"ID: \""+nodevalues.get(nodetype+"ID")+"\" })" + " SET " + runstr);
    }
    

    /**
     * @ClassName : Controller
     * @Description : 将字典中的属性与值拼接为字符串
     * @param nn

     * @Return : java.lang.String
     * @Author : ZNWhahaha
     * @Date : 2020/10/15
    */
    public static String makeRunStr(String index, HashMap<String,String> nn){
        String runs = "";
        Set<String> set = nn.keySet();
        Iterator<String> iterable = set.iterator();
        while (iterable.hasNext()){
            String key = iterable.next();
            String value = nn.get(key);
            if (index.equals("createnode")){
                runs += key +":\""+value+"\",";
            }
            else if (index.equals("updatenode")){
                runs += "n."+key+"=\""+value+"\",";
            }
        }
        runs = runs.substring(0,runs.length() - 1);
        return  runs;
    }

    /**
     * @ClassName : Controller
     * @Description : 建立Session连接
     * @Return : org.neo4j.driver.v1.Session
     * @Author : ZNWhahaha
     * @Date : 2020/10/18
    */
    public static Session wakeupSession(Driver driver){

        Session session = driver.session();
        return  session;
    }

    /**
     * @ClassName : Controller
     * @Description : 关闭driver与session的连接
     * @param driver
     * @param session
     * @Return : void
     * @Author : ZNWhahaha
     * @Date : 2020/10/15
    */
    public static void closeSession(Driver driver,Session session){
        session.close();
        driver.close();
    }

}
