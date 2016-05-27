package com.chenyg.oftendb.db.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.chenyg.oftendb.db.*;
import org.json.JSONException;
import org.json.JSONObject;

import com.chenyg.wporter.util.FileTool;
import com.chenyg.wporter.util.WPTool;

public class SqlHandle implements DBHandle
{

    private Connection conn;
    private String tableName;// 表名

    private boolean isTransaction;

    /**
     * 创建一个DbRwMysql
     *
     * @param conn      数据库连接对象
     * @param tableName 要操作的表的名字
     */
    public SqlHandle(Connection conn, String tableName)
    {
        this.conn = conn;
        this.tableName = tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    private static final SqlCondition TRUE = new SqlCondition();

    public static SqlCondition checkCondition(Condition condition)
    {
        if (condition == null)
        {
            return TRUE;
        }
        if (condition instanceof SqlCondition)
        {
            return (SqlCondition) condition;
        } else
        {
            throw new DBException("the condition type of " + SqlCondition.class
                    + " is accept."
                    + "Current is "
                    + condition.getClass());
        }
    }

    /**
     *
     */
    @Override
    public boolean add(NameValues addFields) throws DBException
    {
        return DataBase.executePS(conn, true, tableName, addFields) == 1;
    }

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException
    {
        return DataBase.addPS(conn, isTransaction, tableName, multiNameValues);
    }

    /**
     * @param query 不会被使用
     */
    @Override
    public boolean replace(Condition query, NameValues updateFields) throws DBException
    {
        return DataBase.executePS(conn, false, tableName, updateFields) > 0;
    }

    @Override
    public int del(Condition query) throws DBException
    {
        String sql = "DELETE FROM `" + tableName
                + "` WHERE "
                + checkCondition(query).toFinalObject() + ";";

        return DataBase.execute(conn, sql);
    }

    public static QuerySettings checkQuerySettings(QuerySettings querySettings)
    {
        SqlQuerySettings settings = null;
        if (querySettings != null)
        {
            if (querySettings instanceof SqlQuerySettings)
            {
                settings = (SqlQuerySettings) querySettings;
            } else
            {
                throw new RuntimeException("the type of " + querySettings.getClass()
                        + " is not accept!");
            }
        }
        return settings;
    }

//    @Override
//    public JSONObject getJSON(Condition query, String... keys) throws DBException
//    {
//
//        String sql = SqlUtil.toSelect(tableName, checkCondition(query), SqlQuerySettings.FIND_ONE, true, keys);
//
//        PreparedStatement ps = null;
//        JSONObject jsonObject = null;
//        try
//        {
//
//            ps = conn.prepareStatement(sql);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next())
//            {
//                jsonObject = getJSONObject(rs, keys);
//            }
//        } catch (Exception e)
//        {
//            throw new DBException(e);
//        } finally
//        {
//            WPTool.close(ps);
//        }
//
//        return jsonObject;
//    }

    @Override
    public List<JSONObject> getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException
    {

        String sql = SqlUtil.toSelect(tableName, checkCondition(query), checkQuerySettings(querySettings), true, keys);
        return _getJSONS(sql, keys);
    }

    private List<JSONObject> _getJSONS(String sql, String[] keys)
    {
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        PreparedStatement ps = null;

        try
        {

            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                list.add(getJSONObject(rs, keys));
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }
        return list;
    }

    private JSONObject getJSONObject(ResultSet rs, String[] keys) throws JSONException, SQLException
    {
        JSONObject jsonObject = new JSONObject();
        if (keys == null || keys.length == 0)
        {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++)
            {
                jsonObject.put(metaData.getColumnName(i), rs.getObject(i));
            }
        } else
        {
            for (String string : keys)
            {
                jsonObject.put(string, rs.getObject(string));
            }
        }

        return jsonObject;
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException
    {
        List<JSONObject> list = getJSONs(query, SqlQuerySettings.FIND_ONE, keys);
        return list.size() > 0 ? list.get(0) : null;
    }

    //
//    @Override
//    public <T extends CloneableObj> T getOne(Class<? extends T> c, Condition query, String... keys) throws DBException
//    {
//        String sql = SqlUtil.toSelect(tableName, checkCondition(query), SqlQuerySettings.FIND_ONE, true, keys);
//
//        PreparedStatement ps = null;
//        T t = null;
//        try
//        {
//
//            ps = conn.prepareStatement(sql);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next())
//            {
//                t = getMyObject(rs, c, keys);
//            }
//        } catch (Exception e)
//        {
//            throw new DBException(e);
//        } finally
//        {
//            WPTool.close(ps);
//        }
//        return t;
//
//    }

//    @Override
//    public <T extends CloneableObj> List<T> get(T temp, Condition query, QuerySettings querySettings,
//            String... keys) throws DBException
//    {
//        String sql = SqlUtil.toSelect(tableName, checkCondition(query), checkQuerySettings(querySettings), true,
// keys);
//        ArrayList<T> list = new ArrayList<>();
//        PreparedStatement ps = null;
//
//        try
//        {
//
//            ps = conn.prepareStatement(sql);
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next())
//            {
//                list.add(getMyObject(rs, temp, keys));
//            }
//        } catch (Exception e)
//        {
//            throw new DBException(e);
//        } finally
//        {
//            WPTool.close(ps);
//        }
//        return list;
//
//    }
//
//    public static void setCloneableObj(CloneableObj cloneableObj, String str,
//            Field field) throws IllegalArgumentException, IllegalAccessException, JSONException,
//            InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException, CloneableObj
//                    .ValueSetException
//
//    {
//        // c = field.getType();
//        String fName = field.getType().getName();
//        field.setAccessible(true);
//
//        if (fName.equals(Character.class.getName()) || fName.equals("char")
//                && str != null)
//        {
//            field.set(cloneableObj, str.charAt(0));
//        } else
//        {
//            Object obj = null;
//
//            if (fName.equals("int"))
//            {
//                obj = Integer.parseInt(str == null ? "0" : str);
//            } else if (fName.equals("byte"))
//            {
//                obj = Byte.parseByte(str == null ? "0" : str);
//            } else if (fName.equals("short"))
//            {
//                obj = Short.parseShort(str == null ? "0" : str);
//            } else if (fName.equals("long"))
//            {
//                obj = Long.parseLong(str == null ? "0" : str);
//            } else if (fName.equals("float"))
//            {
//                obj = Float.parseFloat(str == null ? "0.0" : str);
//            } else if (fName.equals("double"))
//            {
//                obj = Double.parseDouble(str == null ? "0.0" : str);
//            } else if (fName.equals(Boolean.class.getName()) || fName.equals("boolean"))
//            {
//                if (str == null || str.equals("0")
//                        || str.toLowerCase().equals("false"))
//                {
//                    obj = false;
//                } else
//                {
//                    obj = true;
//                }
//            } else if (str != null)
//            {
//                // 具有Constructor(String)构造函数的对象
//                obj = field.getType().getConstructor(String.class).newInstance(str);
//            }
//            cloneableObj.setField(field, obj);
//            //field.set(cloneableObj, obj);
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    private <T extends CloneableObj> T getMyObject(ResultSet rs, T temp, String[] keys) throws Exception
//    {
//        T t = (T) temp.clone();
//        Class<?> c = temp.getClass();
//        if (keys == null || keys.length == 0)
//        {
//            ResultSetMetaData metaData = rs.getMetaData();
//            int columnCount = metaData.getColumnCount();
//            for (int i = 1; i <= columnCount; i++)
//            {
//                String key = metaData.getColumnName(i);
//                String content = rs.getString(i);
//
//                Field field = c.getDeclaredField(key);
//                setCloneableObj(t, content, field);
//            }
//        } else
//        {
//            for (String key : keys)
//            {
//                Field field = c.getDeclaredField(key);
//                String content = rs.getString(key);
//                setCloneableObj(t, content, field);
//            }
//        }
//        return t;
//    }

//    private <T extends CloneableObj> T getMyObject(ResultSet rs, Class<? extends T> c, String[] keys) throws Exception
//    {
//        T t = c.newInstance();
//
//        if (keys == null || keys.length == 0)
//        {
//            ResultSetMetaData metaData = rs.getMetaData();
//            int columnCount = metaData.getColumnCount();
//            for (int i = 1; i <= columnCount; i++)
//            {
//                String key = metaData.getColumnName(i);
//                String content = rs.getString(i);
//
//                Field field = c.getDeclaredField(key);
//                setCloneableObj(t, content, field);
//            }
//        } else
//        {
//            for (String key : keys)
//            {
//                Field field = c.getDeclaredField(key);
//                String content = rs.getString(key);
//                setCloneableObj(t, content, field);
//            }
//        }
//        return t;
//    }

    @Override
    public List<Object> get(Condition query, QuerySettings querySettings, String key) throws DBException
    {

        ArrayList<Object> list = new ArrayList<Object>();

        String sql = SqlUtil.toSelect(tableName, checkCondition(query), checkQuerySettings(querySettings), true, key);

        PreparedStatement ps = null;

        try
        {

            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                list.add(rs.getObject(key));
            }
        } catch (SQLException e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }

        return list;
    }

    @Override
    public int update(Condition query, NameValues updateFields) throws DBException
    {
        if (updateFields == null || updateFields.size() == 0)
        {
            return 0;
        }

        return DataBase.executeSet(conn, tableName, query, updateFields);
    }

    @Override
    public long exists(Condition query) throws DBException
    {
        return DataBase.exists(conn, checkCondition(query), tableName);
    }

    @Override
    public boolean saveBinary(Condition query, String name, byte[] data, int offset, int length) throws DBException
    {

        String sql = "UPDATE `" + tableName
                + "` SET `"
                + name
                + "`=?"
                + (query == null ? ""
                : " WHERE " + checkCondition(query).toFinalObject()) + ";";
        PreparedStatement ps = null;
        try
        {
            ps = conn.prepareStatement(sql);
            ps.setBinaryStream(1, new ByteArrayInputStream(data, offset, length), length);
            ps.executeUpdate();
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }

    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException
    {
        String sql = SqlUtil.toSelect(tableName, checkCondition(query), SqlQuerySettings.FIND_ONE, true, name);
        PreparedStatement ps = null;
        try
        {
            ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            byte[] bs = null;
            if (rs.next())
            {
                bs = FileTool.getData(rs.getBinaryStream(1), 1024);
            }
            return bs;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }
    }

    private static class DataBase
    {

        /**
         * 执行修改，添加，删除的操作，并返回影响的记录数。
         *
         * @param conn
         * @param sql
         * @return 影响的记录数。
         * @throws ClassNotFoundException
         * @throws SQLException
         */
        private static int execute(Connection conn, String sql) throws DBException
        {
            int n = 0;
            PreparedStatement ps = null;
            try
            {
                ps = conn.prepareStatement(sql);
                n = ps.executeUpdate();

            } catch (Exception e)
            {
                throw new DBException(e);
            } finally
            {
                WPTool.close(ps);
            }

            return n;
        }

        private static int executeSet(Connection conn, String tableName, Condition query,
                NameValues updateFields) throws DBException
        {
            int n = 0;
            PreparedStatement ps = null;
            try
            {
                String sql = SqlUtil.toSetValues(tableName, updateFields.names(), checkCondition(query),true);
                ps = conn.prepareStatement(sql);
                for (int i = 0; i < updateFields.size(); i++)
                {
                    setObject(ps, i + 1, updateFields.value(i));
                }

                n = ps.executeUpdate();

            } catch (Exception e)
            {
                throw new DBException(e);
            } finally
            {
                WPTool.close(ps);
            }

            return n;
        }

        private static int executePS(Connection conn, boolean isInsert, String tableName, NameValues addFields)
        {
            int n = 0;
            PreparedStatement ps = null;
            try
            {
                String sql = SqlUtil.toInsertOrReplace(isInsert, tableName, addFields.names(), true);
                ps = conn.prepareStatement(sql);
                for (int i = 0; i < addFields.size(); i++)
                {
                    setObject(ps, i + 1, addFields.value(i));
                }
                n = ps.executeUpdate();

            } catch (Exception e)
            {
                throw new DBException(e);
            } finally
            {
                WPTool.close(ps);
            }

            return n;
        }

        private static void setObject(PreparedStatement ps, int column, Object object) throws SQLException
        {
            ps.setObject(column, object);
        }

        private static int[] addPS(Connection conn, boolean isTransaction, String tableName,
                MultiNameValues multiNameValues)
        {
            String[] names = multiNameValues.getNames();
            PreparedStatement ps = null;
            try
            {

                String sql = SqlUtil.toInsertOrReplace(true, tableName, names, true);
                ps = conn.prepareStatement(sql);
                if (!isTransaction)
                {
                    conn.setAutoCommit(false);
                }

                int n = multiNameValues.count();
                for (int j = 0; j < n; j++)
                {
                    Object[] values = multiNameValues.values(j);
                    for (int k = 0; k < values.length; k++)
                    {
                        setObject(ps, k + 1, values[k]);
                    }
                    ps.addBatch();
                }
                int[] rs = ps.executeBatch();
                if (!isTransaction)
                {
                    conn.commit();
                }

                return rs;
            } catch (BatchUpdateException e)
            {

                try
                {
                    if (!isTransaction)
                    {
                        conn.commit();
                    }
                    int[] rs = e.getUpdateCounts();
                    if (rs.length < multiNameValues.count())
                    {
                        int[] rs2 = new int[multiNameValues.count()];// 初始值为0
                        System.arraycopy(rs, 0, rs2, 0, rs.length);
                        return rs2;
                    } else
                    {
                        return rs;
                    }
                } catch (SQLException e1)
                {
                    // try
                    // {
                    // conn.rollback();
                    // }
                    // catch (SQLException e2)
                    // {
                    //
                    // }
                    throw new DBException(e1);
                }

            } catch (Exception e)
            {
                throw new DBException(e);
            } finally
            {
                WPTool.close(ps);
            }

        }

        /**
         * count某个条件
         *
         * @param conn
         * @param condition
         * @param tableName
         * @return
         * @throws DBException
         */
        public static long exists(Connection conn, Condition condition, String tableName) throws DBException
        {

            long n = 0;

            String sql = "SELECT count(*) rscount FROM `" + tableName
                    + "` WHERE "
                    + condition.toFinalObject() + ";";
            PreparedStatement ps = null;
            try
            {
                ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    n = rs.getLong("rscount");
                }
                ps.close();
            } catch (Exception e)
            {
                throw new DBException(e);
            } finally
            {
                WPTool.close(ps);
            }

            return n;
        }

    }

    @Override
    public void close() throws IOException
    {
        try
        {
            conn.close();
        } catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public List<JSONObject> advancedQuery(AdvancedQuery advancedQuery) throws DBException
    {
        if (!(advancedQuery instanceof SqlAdvancedQuery))
        {
            throw new DBException("the object must be " + SqlAdvancedQuery.class);
        }
        String sql = advancedQuery.toFinalObject().toString();
        return _getJSONS(sql, null);
    }

    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException
    {
        if (!(advancedExecutor instanceof SqlAdvancedExecutor))
        {
            throw new DBException("the object must be " + SqlAdvancedExecutor.class);
        }
        SqlAdvancedExecutor sqlAdvancedNeed = (SqlAdvancedExecutor) advancedExecutor;
        return sqlAdvancedNeed.execute(conn, this);
    }

    @Override
    public boolean supportTransaction() throws DBException
    {
        return true;
    }

    @Override
    public void startTransaction() throws DBException
    {
        try
        {
            conn.setAutoCommit(false);

            isTransaction = true;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public void commitTransaction() throws DBException
    {
        try
        {
            conn.commit();
            isTransaction = false;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public boolean isTransaction()
    {
        return isTransaction;
    }

    @Override
    public void rollback() throws DBException
    {
        try
        {
            conn.rollback();
        } catch (SQLException e)
        {
            throw new DBException(e);
        }
    }

    private Object tempObject;

    @Override
    public Object tempObject(Object tempObject)
    {
        Object obj = this.tempObject;
        this.tempObject = tempObject;
        return obj;
    }

}
