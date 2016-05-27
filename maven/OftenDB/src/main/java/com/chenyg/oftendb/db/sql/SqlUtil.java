package com.chenyg.oftendb.db.sql;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.QuerySettings;
import org.json.JSONArray;
import org.json.JSONObject;

import com.chenyg.oftendb.db.BaseEasier;

public class SqlUtil
{
    /**
     * 把字段field的值value转换为字符串,对String会进行sql防注入处理。
     *
     * @param typeName
     * @param value
     * @return
     */
    public static String checkStr(String typeName, Object value)
    {
        if (value == null)
        {
            return null;
        }
        String s;
        String cName = typeName;

        if (value instanceof String)
        {
            value = ((String) value).replaceAll(".*([';]+|(--)+).*", "_");
        }

        // /
        if (cName.equals(String.class.getName()) || cName.equals("char")
                || cName.equals(Character.class.getName())
                || cName.equals(JSONObject.class.getName())
                || cName.equals(JSONArray.class.getName()))
        {
            s = "'" + value + "'";
        } else if (cName.equals("boolean") || cName.equals(Boolean.class.getName()))
        {
            s = ((Boolean) value ? 1 : 0) + "";
        } else
        {
            s = value.toString();
        }

        return s;
    }

    /**
     * 对String会进行sql防注入处理。
     *
     * @param value
     * @return
     */
    public static String checkStr(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String s;
        String cName = value.getClass().getName();

        if (value instanceof String)
        {
            value = ((String) value).replaceAll(".*([';]+|(--)+).*", "_");
        }

        // /
        if (cName.equals(String.class.getName()) || cName.equals("char")
                || cName.equals(Character.class.getName())
                || cName.equals(JSONObject.class.getName())
                || cName.equals(JSONArray.class.getName()))
        {
            s = "'" + value + "'";
        } else if (cName.equals("boolean") || cName.equals(Boolean.class.getName()))
        {
            s = ((Boolean) value ? 1 : 0) + "";
        } else
        {
            s = value.toString();
        }

        return s;
    }

    /**
     * 转换成insert或replace的sql句，参数值用?表示
     *
     * @param isInsert
     * @param tableName
     * @param names
     * @param withSemicolon 是否以分号结尾
     * @return
     */
    public static String toInsertOrReplace(boolean isInsert, String tableName, String[] names, boolean withSemicolon)
    {

        StringBuilder nameBuilder = new StringBuilder(), valueBuilder = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            nameBuilder.append("`").append(names[i]).append("`,");
            valueBuilder.append("?,");
        }

        if (nameBuilder.length() > 0)
        {
            BaseEasier.removeEndChar(nameBuilder, ',');
            BaseEasier.removeEndChar(valueBuilder, ',');
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(isInsert ? "INSERT" : "REPLACE").append(" INTO `").append(tableName).append('`');

        stringBuilder.append(" (").append(nameBuilder).append(") VALUES(").append(valueBuilder).append(")");
        if (withSemicolon)
        {
            stringBuilder.append(';');
        }

        return stringBuilder.toString();

    }

    /**
     * set的sql语句，参数值用?表示
     *
     * @param tableName
     * @param names
     * @param basicCondition
     * @return
     */
    public static String toSetValues(String tableName, String[] names, Condition basicCondition, boolean withSemicolon)
    {

        StringBuilder setValues = new StringBuilder();
        for (int i = 0; i < names.length; i++)
        {
            String name = names[i];
            setValues.append('`').append(name).append("`=").append("?,");
        }

        if (setValues.length() > 0)
        {
            BaseEasier.removeEndChar(setValues, ',');
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE `").append(tableName).append("` SET ").append(setValues);
        if (basicCondition != null)
        {
            stringBuilder.append(" WHERE ").append(basicCondition.toFinalObject());
        }
        if (withSemicolon)
            stringBuilder.append(";");

        return stringBuilder.toString();
    }

    public static String toSelect(String tableName, Condition basicCondition, QuerySettings querySettings,
            boolean withSemicolon,
            String... keys)
    {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        if (keys == null || keys.length == 0)
        {
            stringBuilder.append("*");
        } else
        {
            for (String key : keys)
            {
                stringBuilder.append('`').append(key).append("`,");
            }
        }

        if (stringBuilder.charAt(stringBuilder.length() - 1) == ',')
        {
            BaseEasier.removeEndChar(stringBuilder, ',');
        }
        stringBuilder.append(" FROM `").append(tableName).append('`');

        if (basicCondition != null)
        {
            stringBuilder.append(" WHERE ").append(basicCondition.toFinalObject());
        }

        if (querySettings != null)
        {
            Object object = querySettings.toFinalObject();
            if (object != null)
            {
                stringBuilder.append(" ORDER BY ").append(object);
            }
            if (querySettings.getLimit() != null)
            {
                int offset = querySettings.getSkip() == null ? 0
                        : querySettings.getSkip();
                int count = querySettings.getLimit();
                stringBuilder.append(" LIMIT ").append(offset).append(",").append(count);
            }
        }

        if (withSemicolon)
            stringBuilder.append(";");

        return stringBuilder.toString();
    }

}