package com.chenyg.oftendb.db.asqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.chenyg.oftendb.db.*;
import com.chenyg.oftendb.db.sql.SqlHandle;
import com.chenyg.oftendb.db.sql.SqlQuerySettings;
import com.chenyg.oftendb.db.sql.SqlUtil;
import com.chenyg.wporter.util.WPTool;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class SqliteHandle implements DBHandle
{

    private String tableName;
    private SQLiteDatabase db;
    private static final TypeUtil.Type<ContentValues>[] TYPES_ADD;
    private static final TypeUtil.Type<TypeUtil.StatementObj>[] TYPES_MULTI_ADD;
    private boolean isTransaction = false;

    static
    {
        TYPES_ADD = TypeUtil.getTypesForAdd();
        TYPES_MULTI_ADD = TypeUtil.getTypesForMultiAdd();
    }

    public SqliteHandle(SQLiteDatabase db, String tableName)
    {
        this.db = db;
        this.tableName = tableName;
    }

    void close(SQLiteStatement sqLiteStatement)
    {
        if (sqLiteStatement != null)
        {
            try
            {
                sqLiteStatement.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private Condition checkCondition(Condition condition)
    {
        return SqlHandle.checkCondition(condition);
    }

    void close(Cursor cursor)
    {
        if (cursor != null)
        {
            try
            {
                cursor.close();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    private void bind(int index, Object value, SQLiteStatement sQLiteStatement)
    {
        if (value == null)
        {
            sQLiteStatement.bindNull(index);
        } else
        {
            int i = Arrays.binarySearch(TYPES_ADD, TypeUtil.Type.forSearch(value.getClass()));
            if (i >= 0)
            {
                TypeUtil.Type<TypeUtil.StatementObj> type = TYPES_MULTI_ADD[i];
                type.put(null, value, new TypeUtil.StatementObj(index, sQLiteStatement));
            } else
            {
                throw new DBException("unknown type of " + value.getClass() + " for sqlite");
            }

        }
    }

    private void put(String name, Object value, ContentValues contentValues)
    {
        if (value == null)
        {
            contentValues.putNull(name);
        } else
        {
            int index = Arrays.binarySearch(TYPES_ADD, TypeUtil.Type.forSearch(value.getClass()));
            if (index >= 0)
            {
                TypeUtil.Type<ContentValues> type = TYPES_ADD[index];
                type.put(name, value, contentValues);
            } else
            {
                throw new DBException("unknown type of " + value.getClass() + " for sqlite");
            }

        }
    }

    private ContentValues parse(NameValues nameValues)
    {
        ContentValues contentValues = new ContentValues(nameValues.size());
        for (int i = 0; i < nameValues.size(); i++)
        {
            put("`"+nameValues.name(i)+"`", nameValues.value(i), contentValues);
        }
        return contentValues;
    }

    @Override
    public boolean add(NameValues nameValues) throws DBException
    {
        try
        {
            long id = db.insertOrThrow(tableName, null, parse(nameValues));
            return id != -1;
        } catch (SQLException e)
        {
            throw new DBException(e);
        }

    }

    @Override
    public int[] add(MultiNameValues multiNameValues) throws DBException
    {
        String sql = SqlUtil.toInsertOrReplace(true, tableName, multiNameValues.getNames(), false);
        SQLiteStatement ss = null;
        try
        {
            ss = db.compileStatement(sql);

            int[] is = null;
            if (!isTransaction())
            {
                db.beginTransaction();
            } else
            {
                is = new int[multiNameValues.count()];
            }


            for (int i = 0; i < multiNameValues.count(); i++)
            {
                Object[] values = multiNameValues.values(i);
                for (int j = 0; j < values.length; j++)
                {
                    bind(j + 1, values[j], ss);
                }
                is[i] = ss.executeInsert() == -1 ? 0 : 1;
            }

            if (!isTransaction())
            {
                db.setTransactionSuccessful();
                db.endTransaction();
                return new int[0];
            } else
            {
                return is;
            }

        } catch (DBException e)
        {
            throw e;
        } catch (SQLException e)
        {
            throw new DBException(e);
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(ss);
        }
    }

    @Override
    public boolean replace(Condition query, NameValues nameValues) throws DBException
    {
        try
        {
            long id = db.replaceOrThrow(tableName, null, parse(nameValues));
            return id != -1;
        } catch (Exception e)
        {
            throw new DBException(e);
        }
    }

    @Override
    public int del(Condition query) throws DBException
    {
        String sql = "DELETE FROM `" + tableName
                + "` WHERE "
                + checkCondition(query).toFinalObject();
        SQLiteStatement statement = null;
        try
        {
            statement = db.compileStatement(sql);
            return statement.executeUpdateDelete();
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }

    }

    @Override
    public List<JSONObject> advancedQuery(AdvancedQuery advancedQuery) throws DBException
    {
        if (!(advancedQuery instanceof SqliteAdvancedQuery))
        {
            throw new DBException("the object must be " + SqliteAdvancedQuery.class);
        }
        String sql = advancedQuery.toFinalObject().toString();
        return _getJSONS(sql, null);
    }

    private List<JSONObject> _getJSONS(String sql, String[] keys)
    {
        ArrayList<JSONObject> list = new ArrayList<JSONObject>();
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);

            while (cursor.moveToNext())
            {
                list.add(getJSONObject(cursor, keys));
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
        return list;
    }

    private JSONObject getJSONObject(Cursor cursor, String[] keys) throws Exception
    {
        JSONObject jsonObject = new JSONObject();
        if (keys == null || keys.length == 0)
        {
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++)
            {
                jsonObject.put(cursor.getColumnName(i), TypeUtil.getObject(cursor, i));
            }
        } else
        {
            for (String string : keys)
            {
                int index = cursor.getColumnIndexOrThrow(string);
                jsonObject.put(string, TypeUtil.getObject(cursor, index));
            }
        }

        return jsonObject;
    }

    @Override
    public Object advancedExecute(AdvancedExecutor advancedExecutor) throws DBException
    {
        if (!(advancedExecutor instanceof SqliteAdvancedExecutor))
        {
            throw new DBException("the object must be " + SqliteAdvancedExecutor.class);
        }
        SqliteAdvancedExecutor sqliteAdvancedExecutor = (SqliteAdvancedExecutor) advancedExecutor;
        return sqliteAdvancedExecutor.execute(db, this);
    }

    @Override
    public JSONObject getOne(Condition query, String... keys) throws DBException
    {
        List<JSONObject> list = getJSONs(query, SqlQuerySettings.FIND_ONE, keys);
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public List<JSONObject> getJSONs(Condition query, QuerySettings querySettings, String... keys) throws DBException
    {
        String sql = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlHandle.checkQuerySettings(querySettings),
                        false, keys);
        return _getJSONS(sql, keys);
    }


    @Override
    public List<Object> get(Condition query, QuerySettings querySettings, String key) throws DBException
    {

        ArrayList<Object> list = new ArrayList<Object>();

        String sql = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlHandle.checkQuerySettings(querySettings),
                        false, key);

        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext())
            {
                list.add(TypeUtil.getObject(cursor, 0));
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }

        return list;
    }

    @Override
    public int update(Condition query, NameValues nameValues) throws DBException
    {
        if (nameValues == null || nameValues.size() == 0)
        {
            return 0;
        }
        int n = 0;
        SQLiteStatement statement = null;
        try
        {
            String sql = SqlUtil.toSetValues(tableName, nameValues.names(), checkCondition(query), false);
            statement = db.compileStatement(sql);
            for (int i = 0; i < nameValues.size(); i++)
            {
                nameValues.value(i);
                bind(i + 1, nameValues.value(i), statement);
            }

            n = statement.executeUpdateDelete();

        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }

        return n;
    }

    @Override
    public long exists(Condition query) throws DBException
    {
        String sql = "SELECT count(*) FROM `" + tableName
                + "` WHERE "
                + checkCondition(query).toFinalObject() + ";";
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            long n = 0;
            if (cursor.moveToNext())
            {
                n = cursor.getLong(0);
            }
            return n;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
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
        SQLiteStatement statement = null;
        try
        {
            statement = db.compileStatement(sql);
            byte[] bs = new byte[length];
            System.arraycopy(data, offset, bs, 0, length);
            statement.bindBlob(1, bs);
            return true;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(statement);
        }
    }

    @Override
    public byte[] getBinary(Condition query, String name) throws DBException
    {
        String sql = SqlUtil
                .toSelect(tableName, checkCondition(query), SqlQuerySettings.FIND_ONE, true, name);
        Cursor cursor = null;
        try
        {
            cursor = db.rawQuery(sql, null);
            byte[] bs = null;
            if (cursor.moveToNext())
            {
                bs = cursor.getBlob(0);
            }
            return bs;
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            close(cursor);
        }
    }

    @Override
    public void close() throws IOException
    {
        try
        {
            db.close();
        } catch (SQLException e)
        {
            throw new IOException(e);
        }
    }

    @Override
    public boolean supportTransaction() throws DBException
    {
        return true;
    }

    @Override
    public boolean isTransaction()
    {
        return isTransaction;
    }

    @Override
    public void startTransaction() throws DBException
    {
        db.beginTransaction();
        isTransaction = true;
    }

    @Override
    public void commitTransaction() throws DBException
    {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void rollback() throws DBException
    {
        db.endTransaction();
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
