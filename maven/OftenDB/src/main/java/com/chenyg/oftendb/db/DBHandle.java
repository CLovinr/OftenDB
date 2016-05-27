package com.chenyg.oftendb.db;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

public interface DBHandle extends Closeable
{
    /**
     * 添加。
     *
     * @param nameValues
     * @return
     * @throws DBException
     */
    boolean add(NameValues nameValues) throws DBException;

    /**
     * 批量添加.
     *
     * @param multiNameValues
     * @return 返回每一条的结果.或者返回一个大小为0的数组表示全部成功
     * @throws DBException
     */
    int[] add(MultiNameValues multiNameValues) throws DBException;

    /**
     * 替换。若不存在相关记录，则添加；否则替换。相当于删除已经存在的（若存在），再添加。
     *
     * @param nameValues
     * @return
     * @throws DBException
     */
    boolean replace(Condition query, NameValues nameValues) throws DBException;

    /**
     * 删除操作。
     *
     * @param query
     * @return 删除的条数
     * @throws DBException
     */
    int del(Condition query) throws DBException;

    /**
     * 高级查询，相对简单查询来数，通用性更小。
     *
     * @param advancedQuery
     * @return
     * @throws DBException
     */
    List<JSONObject> advancedQuery(AdvancedQuery advancedQuery) throws DBException;

    Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException;

//    /**
//     * 简单查询
//     *
//     * @param temp          用于克隆
//     * @param query
//     * @param querySettings
//     * @param keys          为空表示选择所有
//     * @return
//     * @throws DBException
//     */
//    <T extends CloneableObj> List<T> get(T temp, Condition query, QuerySettings querySettings,
//            String... keys) throws DBException;

    /**
     * 简单查询
     *
     * @param query
     * @param querySettings
     * @param keys          为空表示选择所有
     * @return
     * @throws DBException
     */
    List<JSONObject> getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException;

//    /**
//     * 简单查询
//     *
//     * @param c
//     * @param query
//     * @param keys  为空表示选择所有
//     * @return
//     * @throws DBException
//     */
//    <T extends CloneableObj> T getOne(Class<? extends T> c, Condition query, String... keys) throws DBException;

    JSONObject getOne(Condition query, String... keys) throws DBException;

//    /**
//     * 简单查询
//     *
//     * @param query
//     * @param keys  为空表示选择所有
//     * @return
//     * @throws DBException
//     */
//    JSONObject getJSON(Condition query, String... keys) throws DBException;

    /**
     * 简单查询
     *
     * @param query
     * @param querySettings
     * @param key
     * @return
     * @throws DBException
     */
    List<Object> get(Condition query, QuerySettings querySettings, String key) throws DBException;

    /**
     * 修改数据。修改记录，若未找到匹配的，则什么都不做。
     *
     * @param query
     * @param nameValues
     * @return
     * @throws DBException
     */
    int update(Condition query, NameValues nameValues) throws DBException;

    /**
     * 统计记录条数。
     *
     * @param query
     * @return
     * @throws DBException
     */
    long exists(Condition query) throws DBException;

    /**
     * 保存二进制数据
     *
     * @param query
     * @param name
     * @param data
     * @param offset
     * @param length
     * @return
     * @throws DBException
     */
    boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException;

    /**
     * 得到二进制数据
     *
     * @param query
     * @param name
     * @return
     * @throws DBException
     */
    byte[] getBinary(Condition query, String name) throws DBException;

    /**
     * 关闭
     *
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * 是否支持事务
     *
     * @return
     * @throws DBException
     */
    boolean supportTransaction() throws DBException;

    /**
     * 是否已经开启了事务
     *
     * @return
     */
    boolean isTransaction();

    /**
     * 开始事务
     *
     * @throws DBException
     */
    void startTransaction() throws DBException;

    /**
     * 提交事务
     *
     * @throws DBException
     */
    void commitTransaction() throws DBException;

    /**
     * 事务回滚
     *
     * @throws DBException
     */
    void rollback() throws DBException;

    /**
     * 用于设置或得到临时对象
     *
     * @param tempObject 要设置的临时对象。
     * @return 返回上次设置的值
     */
    Object tempObject(Object tempObject);

}
