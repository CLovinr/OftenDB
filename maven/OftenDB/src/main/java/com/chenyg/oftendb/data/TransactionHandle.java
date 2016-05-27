package com.chenyg.oftendb.data;

import java.io.Closeable;
import java.io.IOException;

import com.chenyg.oftendb.db.DBException;

/**
 * 事务操作
 * 
 * @author ZhuiFeng
 *
 */
public abstract class TransactionHandle<T> implements Closeable
{
    public abstract T common();

    
    /**
     * 开启事务
     * @throws DBException
     */
    public abstract void startTransaction() throws DBException;

    /**
     * 提交事务
     * @throws DBException
     */
    public abstract void commitTransaction() throws DBException;
    
    /**
     * 回滚
     * @throws DBException
     */
    public abstract void rollback()throws DBException;
    
    /**
     * 关闭
     */
    public abstract void close()throws IOException;
}
