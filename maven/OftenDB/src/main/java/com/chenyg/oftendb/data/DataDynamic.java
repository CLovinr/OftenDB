package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.NameValues;
import com.chenyg.oftendb.db.QuerySettings;
import com.chenyg.oftendb.db.Unit;
import com.chenyg.wporter.WPObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;

/**
 * 用于动态的、无需声明类的情况。
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class DataDynamic extends DataAble
{
    private String collectionName;
    private Condition forQuery;
    private KeysSelection keys;
    private JSONObject jsonObject;
    private String[] keyNames;

    /**
     * 构建一个动态的。其字段是根据必须参数与非必须参数中不为空的来动态确定。
     *
     * @param collectionName
     */
    public DataDynamic(String collectionName)
    {
        this.collectionName = collectionName;
    }

    /**
     * 构建一个指定了字段的。
     *
     * @param collectionName
     * @param keyNames
     */
    public DataDynamic(String collectionName, String... keyNames)
    {
        this(collectionName);
        this.keyNames = keyNames;
        Arrays.sort(keyNames);
    }


    @Override
    public String getCollectionName()
    {
        return collectionName;
    }


    public void setForQuery(Condition forQuery)
    {
        this.forQuery = forQuery;
    }

    @Override
    public Condition forQuery()
    {
        return forQuery;
    }

    public void setKeys(KeysSelection keys)
    {
        this.keys = keys;
    }

    @Override
    public KeysSelection keys()
    {
        return keys;
    }

    @Override
    public JSONObject toJsonObject()
    {
        return jsonObject;
    }

    @Override
    protected NameValues toNameValues(ParamsGetter.Params params) throws Exception
    {
        NameValues nameValues = new NameValues();
        if (jsonObject != null)
        {
            Iterator<String> names = jsonObject.keys();
            while (names.hasNext())
            {
                String name = names.next();
                nameValues.put(name, jsonObject.get(name));
            }
        }
        return nameValues;
    }

    @Override
    public void whenSetDataFinished(SetType setType, int optionCode, WPObject wpObject,
            DBHandleAccess dbHandleAccess) throws DataException
    {

    }

    private boolean isInKeyNames(String name)
    {
        if (keyNames == null || Arrays.binarySearch(keyNames, name) >= 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    private void setValue(JSONObject jsonObject,String[] names,Object[] values) throws JSONException
    {
        if (names != null && names.length > 0)
        {
            for (int i = 0; i < names.length; i++)
            {
                String name = names[i];
                if (values[i] != null && isInKeyNames(name))
                {
                    jsonObject.put(name, values[i]);
                }
            }
        }
    }

    @Override
    protected void setParams(String[] neceFields, Object[] nvalues, String[] unneceFields,
            Object[] uvalues,String[] innerNames,Object[] inners) throws Exception
    {
        jsonObject = new JSONObject();
        setValue(jsonObject,neceFields,nvalues);
        setValue(jsonObject,unneceFields,uvalues);
        setValue(jsonObject,innerNames,inners);
    }

    @Override
    protected String[] getFinalKeys(KeysSelection keysSelection, ParamsGetter.Params params)
    {
        String[] keys = null;
        if (keysSelection != null)
        {
            keys = keysSelection.keys;
        }
        return keys;
    }

    @Override
    protected void dealNames(Condition condition)
    {

    }

    @Override
    protected void dealNames(QuerySettings querySettings)
    {

    }

    @Override
    protected DataAble cloneData()
    {
        try
        {
            DataDynamic dataAble = (DataDynamic) clone();
            dataAble.jsonObject = null;
            dataAble.forQuery = null;
            dataAble.keys = null;
            return dataAble;
        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WPObject wpObject,
            ParamsGetter.Params params)
    {
        Condition condition = null;

        if (selection != null)
        {
            condition = dbHandleSource.newCondition();
            boolean toNull;
            int[] nIndexes = selection.nIndexes;

            if (nIndexes != null)
            {
                String[] cnNames = wpObject.inNames.cnNames;
                Object[] cns = wpObject.cns;

                for (int i = 0; i < nIndexes.length; i++)
                {
                    int index = nIndexes[i];
                    if (index < 0)
                    {
                        index = -(index + 1);
                        toNull = true;
                    } else
                    {
                        toNull = false;
                    }
                    if (cns[index] != null)
                    {
                        String sname = cnNames[index];

                        condition.put(Condition.EQ, new Unit(sname, cns[index]));
                        if (toNull)
                        {
                            cns[index] = null;
                        }
                    }

                }

            }

            int[] uIndexes = selection.uIndexes;

            if (uIndexes != null)
            {
                String[] cuNames = wpObject.inNames.cuNames;
                Object[] cus = wpObject.cus;

                for (int i = 0; i < uIndexes.length; i++)
                {
                    int index = uIndexes[i];
                    if (index < 0)
                    {
                        index = -(index + 1);
                        toNull = true;
                    } else
                    {
                        toNull = false;
                    }
                    if (cus[index] != null)
                    {
                        String sname = cuNames[index];
                        condition.put(Condition.EQ, new Unit(sname, cus[index]));
                        if (toNull)
                        {
                            cus[index] = null;
                        }
                    }

                }

            }

        }
        return condition;
    }
}
