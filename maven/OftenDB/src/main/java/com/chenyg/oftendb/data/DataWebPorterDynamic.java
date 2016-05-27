package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.DBHandle;
import com.chenyg.oftendb.db.QuerySettings;
import com.chenyg.wporter.InCheckParser;
import com.chenyg.wporter.WebPorterDynamic;
import com.chenyg.wporter.annotation.DBAnnotation;

import java.lang.annotation.Annotation;

/**
 * Created by 宇宙之灵 on 2015/12/17.
 */
public class DataWebPorterDynamic extends WebPorterDynamic implements ParamsGetter, DBHandleSource
{
    private DBHandleSource dbHandleSource;
    private Params params;


    /**
     * @param inCheckParser
     * @param dataClass
     * @param dbHandleSource
     */
    public DataWebPorterDynamic(InCheckParser inCheckParser, Class<? extends DataAble> dataClass,
            DBHandleSource dbHandleSource)
    {
        this(inCheckParser, DBAnnotation.Key.class, dataClass, dbHandleSource);
    }


    /**
     * @param inCheckParser
     * @param key
     * @param dataClass
     * @param dbHandleSource
     */
    public DataWebPorterDynamic(InCheckParser inCheckParser, Class<? extends Annotation> key,
            Class<? extends DataAble> dataClass,
            DBHandleSource dbHandleSource)
    {
        setInCheckParser(inCheckParser);
        this.params = new Params(key, dataClass);
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

    /**
     * @param dataClass 如果为null，则表示使用构造函数传过来的data类
     * @param excepts
     * @return
     */
    public static String[] getPramsFromData(Class<? extends DataAble> dataClass, String... excepts)
    {
        String[] keys = DataUtil
                .getKeys(new KeysSelection(false).names(excepts), DataUtil.newParamsGetter(dataClass));
        return keys;
    }


}
