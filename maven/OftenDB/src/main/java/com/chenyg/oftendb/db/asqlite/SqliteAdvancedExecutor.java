package com.chenyg.oftendb.db.asqlite;

import android.database.sqlite.SQLiteDatabase;
import com.chenyg.oftendb.db.AdvancedExecutor;
import com.chenyg.oftendb.db.DBException;

/**
 * Created by 宇宙之灵 on 2016/5/3.
 */
public abstract class SqliteAdvancedExecutor extends AdvancedExecutor
{
    protected abstract Object execute(SQLiteDatabase database, SqliteHandle sqliteHandle) throws DBException;

    @Override
    public Object toFinalObject()
    {
        return null;
    }
}
