package com.chenyg.oftendb.db.sql;

import java.sql.Connection;

import com.chenyg.oftendb.db.AdvancedExecutor;
import com.chenyg.oftendb.db.DBException;

public abstract class SqlAdvancedExecutor extends AdvancedExecutor
{

    protected abstract Object execute(Connection connection, SqlHandle sqlHandle) throws DBException;

    @Override
    public final Object toFinalObject()
    {
        return null;
    }
}
