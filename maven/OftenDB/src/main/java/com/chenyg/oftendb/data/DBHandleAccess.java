package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.DBHandle;
import com.chenyg.oftendb.db.QuerySettings;

/**
 * Created by 宇宙之灵 on 2015/10/9.
 */
public class DBHandleAccess implements DBHandleSource
{
    private DBHandleSource dbHandleSource;
    private DBHandle dbHandle;


    DBHandleAccess(DBHandleSource dbHandleSource, DBHandle dbHandle)
    {
        this.dbHandleSource = dbHandleSource;
        this.dbHandle = dbHandle;
    }

    /**
     * 得到当前的(与common当前的为同一个)
     *
     * @return
     */
    public DBHandle getCurrentDBHandle()
    {
        return dbHandle;
    }

    @Override
    public Condition newCondition()
    {
        return dbHandleSource.newCondition();
    }

    @Override
    public QuerySettings newQuerySettings()
    {
        return dbHandleSource.newQuerySettings();
    }

    @Override
    public DBHandle getDbHandle(ParamsGetter paramsGetter, DBHandle dbHandle) throws DBException
    {
        return dbHandleSource.getDbHandle(paramsGetter, dbHandle);
    }

    @Override
    public void afterClose(DBHandle dbHandle)
    {

    }
}
