package com.chenyg.oftendb.data;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.chenyg.oftendb.db.*;
import org.json.JSONObject;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.annotation.DBAnnotation.Key;
import com.chenyg.wporter.util.MyJsonTool;

/**
 * 父类的相关注解变量(public)在子类中仍然有效。
 *
 * @author ZhuiFeng
 */
public abstract class Data extends DataAble
{


    /**
     * 转换成json对象.(使用Key.class注解标记的)
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJsonObject()
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = MyJsonTool.toJsonObject(this, null, Key.class);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return jsonObject;
    }


    private HashMap<String, Field> setValue(HashMap<String, Field> map,String[] names ,Object[] values) throws
            IllegalAccessException
    {
        if (names != null && names.length > 0)
        {
            if(map==null)
            map = getFieldMap();
            for (int i = 0; i < names.length; i++)
            {

                if (values[i] != null)
                {
                    Field field = map.get(names[i]);
                    if (field == null || !field.isAnnotationPresent(Key.class))
                    {
                        continue;
                    }
                    field.setAccessible(true);
                    field.set(this, values[i]);
                }
            }
        }
        return map;
    }

    /**
     * 设置值.
     *
     * @param neceFields
     * @param nvalues      为null的不会设置.
     * @param unneceFields
     * @param uvalues      非必需参数，为null的不会设置.
     * @throws Exception
     */
    @Override
    protected final void setParams(String[] neceFields, Object[] nvalues, String[] unneceFields,
            Object[] uvalues,String[] innerNames,Object[] inners) throws Exception
    {
        HashMap<String, Field> fieldMap = setValue(null,neceFields,nvalues);
        fieldMap=setValue(fieldMap,unneceFields,uvalues);
        setValue(fieldMap,innerNames,inners);
    }

    private HashMap<String, Field> getFieldMap()
    {
        Field[] fields = getClass().getFields();
        HashMap<String, Field> map = new HashMap<String, Field>(fields.length);
        for (Field field : fields)
        {
            map.put(field.getName(), field);
        }
        return map;
    }

    /**
     * 得到所属集合(或表)名称。
     *
     * @return
     */
    public abstract String getCollectionName();


    private Condition queryCondition;
    private KeysSelection keysSelection;

    public void setForKeys(KeysSelection keysSelection)
    {
        this.keysSelection = keysSelection;
    }

    public void setForQuery(Condition queryCondition)
    {
        this.queryCondition = queryCondition;
    }

    /**
     * 用于数据库修改、查询或删除时的寻找条件
     *
     * @return
     */
    public Condition forQuery()
    {
        return queryCondition;
    }

    public KeysSelection keys()
    {
        return keysSelection;
    }

    /**
     * <pre>
     * 设置类变量完成时、且在进行数据库调用前调用此函数.
     * 对于{@linkplain SetType#QUERY}和{@linkplain SetType#DELETE}不会设置值({@linkplain #setParams(String[], Object[], String[], Object[], String[], Object[])})
     * ，但会调用本函数。
     * </pre>
     *
     * @param setType
     * @param optionCode     可选code
     * @param wpObject
     * @param dbHandleAccess
     * @throws DataException 若抛出异常，则向客户端响应失败。
     */
    public abstract void whenSetDataFinished(SetType setType, int optionCode, WPObject wpObject,
            DBHandleAccess dbHandleAccess) throws DataException;


    @Override
    protected final NameValues toNameValues(ParamsGetter.Params params) throws Exception
    {
        Class<? extends Annotation> _key = params.getKeyClass();
        Field[] fields = getClass().getDeclaredFields();
        NameValues nameValues = new NameValues();
        Object value;
        if (_key.equals(Key.class))
        {
            for (Field field : fields)
            {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Key.class))
                {
                    Key key = field.getAnnotation(Key.class);
                    value = field.get(this);
                    if (value != null || key.nullSetOrAdd())
                    {
                        nameValues.put(key.value().equals("") ? field.getName()
                                : key.value(), value);
                    }

                }
            }
        } else
        {
            for (Field field : fields)
            {
                field.setAccessible(true);
                if (field.isAnnotationPresent(_key) && (value = field.get(this)) != null)
                {
                    nameValues.put(field.getName(), value);
                }
            }
        }

        return nameValues;
    }

    @Override
    protected String[] getFinalKeys(KeysSelection keysSelection,
            ParamsGetter.Params params)
    {
        Class<? extends Annotation> keyClass = params.getKeyClass();
        String[] keys = null;
        if (keysSelection != null)
        {
            if (keysSelection.isSelect)
            {
                keys = keysSelection.keys;
                // 对于@Key注解进行替换,转换成数据库的名称
                for (int i = 0; i < keys.length; i++)// 对于@Key注解进行替换,转换成数据库的名称
                {

                    String key = keys[i];
                    String name = getDbKeyName(key, params);
                    keys[i] = name;
                }

            } else
            {
                String[] unKeys = keysSelection.keys;
                for (int i = 0; i < unKeys.length; i++)// 对于@Key注解进行替换,转换成数据库的名称
                {
                    String unKey = unKeys[i];
                    String name = getDbKeyName(unKey, params);
                    unKeys[i] = name;
                }

                Arrays.sort(unKeys);
                List<String> list = new ArrayList<String>();
                Field[] fields = getClass().getFields();
                for (Field field : fields)
                {
                    String name = getFName(field, keyClass);
                    if (name != null && Arrays.binarySearch(unKeys, name) < 0)
                    {
                        list.add(name);
                    }
                }
                keys = list.toArray(new String[0]);
            }

        }
        return keys;
    }


    /**
     * 得到数据库字段对应的名称
     *
     * @param field
     * @param keyClass
     * @return
     */
    private static String getFName(Field field, Class<? extends Annotation> keyClass)
    {
        if (field.isAnnotationPresent(keyClass))
        {
            String name;
            if (keyClass == Key.class)
            {
                Key key = field.getAnnotation(Key.class);
                name = key.value().equals("") ? field.getName() : key.value();
            } else
            {
                name = field.getName();
            }
            return name;
        }
        return null;
    }


    /**
     * 得到数据库对应的键名（或列名等）.对于@Key注解进行替换,转换成数据库的名称.
     *
     * @param fieldName
     * @param params
     * @return 返回值要么为fieldName，要么为@Key的value值。不为空。
     */
    private static String getDbKeyName(String fieldName, ParamsGetter.Params params)
    {
        Field field = null;
        Class<? extends Annotation> keyClass = params.getKeyClass();
        try
        {
            field = params.getDataAble().getClass().getField(fieldName);
        } catch (NoSuchFieldException e)
        {
        } catch (SecurityException e)
        {
        }
        if (field != null && field.isAnnotationPresent(keyClass))
        {
            String name;
            if (keyClass == Key.class)
            {
                Key key = field.getAnnotation(Key.class);
                name = key.value().equals("") ? field.getName() : key.value();
            } else
            {
                name = field.getName();
            }
            return name;
        }
        return fieldName;
    }

    @Override
    protected final void dealNames(Condition condition)
    {
        condition.dealNames(getClass());
    }

    @Override
    protected final void dealNames(QuerySettings querySettings)
    {
        querySettings._dealNames(getClass());
    }

    protected final Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection selection, WPObject wpObject,
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
                        String sname = getDbKeyName(cnNames[index], params);

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
                        String sname = getDbKeyName(cuNames[index], params);
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

    @Override
    protected DataAble cloneData()
    {
        try
        {
            DataAble dataAble = (DataAble) clone();
            return dataAble;
        } catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e);
        }
    }
}
