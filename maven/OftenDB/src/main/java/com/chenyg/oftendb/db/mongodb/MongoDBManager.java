package com.chenyg.oftendb.db.mongodb;

import java.net.UnknownHostException;
import java.util.Arrays;

import com.chenyg.wporter.log.LogUtil;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDBManager
{
    private static MongoClient mongoClient = null;

    private static String databaseName = "test";

    /**
     * 是否无认证
     */
    public static boolean noAuthority = false;

    private MongoDBManager()
    {

    }

    /**
     * 得到DB
     *
     * @return
     */
    public static DB getDB()
    {
        DB db = getDB(databaseName);
        return db;
    }

    /**
     * 根据名称获取DB
     *
     * @param dbName
     * @return
     */
    private static DB getDB(String dbName)
    {
        return mongoClient.getDB(dbName);
    }

    /**
     * 初始化
     */
    private static void init(String dbName, String host, int port, String userName, String psw, int connectionsPerHost)
    {
        databaseName = dbName;
        String serverName = host + ":" + port;

        /****** 2. 接着连接并选择数据库名为databaseName的服务器 ******/
        try
        {
            if (noAuthority)
            {
                mongoClient = new MongoClient();
            } else
            {
                Builder builder = new MongoClientOptions.Builder().cursorFinalizerEnabled(false);
                builder.connectionsPerHost(connectionsPerHost);

                mongoClient = new MongoClient(new ServerAddress(serverName), Arrays.asList(
                        MongoCredential.createMongoCRCredential(userName, databaseName, psw.toCharArray())),
                        builder.build());
            }
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

    }

    /**
     * 打开mongodb连接池
     *
     * @param dbName
     * @param host
     * @param port
     * @param userName
     * @param psw
     * @param connectionsPerHost
     */
    public static void start(String dbName, String host, int port, String userName, String psw, int connectionsPerHost)
    {
        LogUtil.printPosLn("启动mongodb。。。\n");
        init(dbName, host, port, userName, psw, connectionsPerHost);
    }


    /**
     * 关闭连接池
     */
    public static void stop()
    {
        LogUtil.printPosLn("关闭mongodb连接池！\n");
        mongoClient.close();
    }
}
