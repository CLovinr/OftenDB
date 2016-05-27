package com.chenyg.oftendb.data;

import java.lang.annotation.Annotation;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.DBHandle;
import com.chenyg.oftendb.db.QuerySettings;
import com.chenyg.wporter.InCheckParser;
import com.chenyg.wporter.WebPorter;
import com.chenyg.wporter.annotation.DBAnnotation.Key;

/**
 * @author ZhuiFeng
 */
public class DataPorter extends WebPorter implements SourceAndPGetter
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    protected DBHandleSource dbHandleSource;
    private Params params;


    /**
     * @param inCheckParser
     * @param dataClass
     * @param dbHandleSource
     */
    public DataPorter(InCheckParser inCheckParser, Class<? extends Data> dataClass, DBHandleSource dbHandleSource)
    {
        this(inCheckParser, Key.class, dataClass, dbHandleSource);
    }


    /**
     * @param inCheckParser
     * @param key
     * @param dataClass
     * @param dbHandleSource
     */
    public DataPorter(InCheckParser inCheckParser, Class<? extends Annotation> key, Class<? extends Data> dataClass,
            DBHandleSource dbHandleSource)
    {
        this(inCheckParser, dataClass == null ? null : new Params(key == null ? Key.class : key, dataClass),
                dbHandleSource);
    }

    public DataPorter(InCheckParser inCheckParser, DataAble dataAble,
            DBHandleSource dbHandleSource)
    {
        this(inCheckParser, new Params(dataAble), dbHandleSource);
    }

    public DataPorter(InCheckParser inCheckParser, Params params,
            DBHandleSource dbHandleSource)
    {
        setInCheckParser(inCheckParser);
        this.params = params;
        this.dbHandleSource = dbHandleSource;
    }

    /**
     * 构造一个Condition
     *
     * @return
     */
    public Condition newCondition()
    {
        return dbHandleSource.newCondition();
    }

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

    @Override
    public Params getParams()
    {
        return params;
    }

}
