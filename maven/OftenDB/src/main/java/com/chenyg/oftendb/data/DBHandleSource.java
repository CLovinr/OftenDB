package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.DBHandle;
import com.chenyg.oftendb.db.QuerySettings;

/**
 * 用于获取数据库操作
 * Created by 宇宙之灵 on 2015/10/9.
 */
public interface DBHandleSource
{
    /**
     * 新建一个条件
     *
     * @return Condition
     */
    Condition newCondition();

    /**
     * 新建一个查询设置
     *
     * @return QuerySettings
     */
    QuerySettings newQuerySettings();

    /**
     * @param paramsGetter 用于获取一些参数
     * @param dbHandle    使用已经有的操作
     * @return DBHandle
     * @throws DBException
     */
    DBHandle getDbHandle(ParamsGetter paramsGetter, DBHandle dbHandle) throws DBException;

    void afterClose(DBHandle dbHandle);
}
