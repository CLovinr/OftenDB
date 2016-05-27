package com.chenyg.oftendb.db;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

/**
 * clone函数公共的
 *
 * @author Administrator
 */
public class CloneableObj implements Cloneable
{

    public static class ValueSetException extends Exception
    {
        public ValueSetException(String msg)
        {
            super(msg);
        }
    }

    /**
     * 浅复制,只需浅复制即可.
     */
    @Override
    public CloneableObj clone() throws CloneNotSupportedException
    {
        return (CloneableObj) super.clone();
    }

    /**
     * 支持jsonStr转JSONObject,jsonArrayStr转JSONArray
     * @param thisField
     * @param value
     * @throws ValueSetException
     */
    public void setField(Field thisField, Object value) throws ValueSetException
    {
        try
        {
            if (thisField.getClass().equals(JSONObject.class))
            {
                if (!(value instanceof JSONObject))
                {
                    try
                    {
                        value = new JSONObject(value.toString());
                    } catch (JSONException e)
                    {
                        throw new ValueSetException(e.getMessage());
                    }
                }
            } else if (thisField.getClass().equals(JSONArray.class))
            {
                if (!(value instanceof JSONArray))
                {
                    try
                    {
                        value = new JSONArray(value.toString());
                    } catch (JSONException e)
                    {
                        throw new ValueSetException(e.getMessage());
                    }
                }
            }

            thisField.set(this, value);
        } catch (IllegalAccessException e)
        {
            throw new ValueSetException(e.getMessage());
        }

    }
}
