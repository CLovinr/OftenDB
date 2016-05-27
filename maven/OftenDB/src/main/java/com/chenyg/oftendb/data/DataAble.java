package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.CloneableObj;
import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.NameValues;
import com.chenyg.oftendb.db.QuerySettings;
import com.chenyg.wporter.WPObject;
import org.json.JSONObject;

/**
 * Created by 宇宙之灵 on 2016/4/8.
 */
public abstract class DataAble extends CloneableObj
{
    public static final int OPTION_CODE_DEFAULT = -1;
    public static final int OPTION_CODE_EXISTS = -2;
    public static final int OPTION_CODE_LOGIN = -3;

    /**
     * 得到所属集合(或表)名称。
     *
     * @return
     */
    public abstract String getCollectionName();

    public abstract Condition forQuery();

    public abstract KeysSelection keys();

    public abstract JSONObject toJsonObject();

    protected abstract NameValues toNameValues(ParamsGetter.Params params) throws Exception;

    public abstract void whenSetDataFinished(SetType setType, int optionCode, WPObject wpObject,
            DBHandleAccess dbHandleAccess) throws DataException;

    protected abstract void setParams(String[] neceFields, Object[] nvalues, String[] unneceFields,
            Object[] uvalues,String[] innerNames,Object[] innerValues) throws Exception;

    /**
     * 得到数据库选择的键。
     * @return
     */
    protected abstract String[] getFinalKeys(KeysSelection keysSelection, ParamsGetter.Params params);

    protected abstract void dealNames(Condition condition);

    protected abstract void dealNames(QuerySettings querySettings);

    protected abstract DataAble cloneData();

    /**
     * 转换ParamsSelection为Condition
     *
     * @param dbHandleSource
     * @param selection
     * @param wpObject
     * @param params
     * @return
     */
    protected abstract Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WPObject wpObject,
            ParamsGetter.Params params);
}
