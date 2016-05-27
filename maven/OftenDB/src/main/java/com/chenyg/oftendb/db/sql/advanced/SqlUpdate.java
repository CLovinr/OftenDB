package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.sql.SqlAdvancedExecutor;
import com.chenyg.oftendb.db.sql.SqlHandle;
import com.chenyg.wporter.annotation.InjectionWarn;
import com.chenyg.wporter.util.WPTool;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Created by 刚帅 on 2016/1/22.
 */
public class SqlUpdate extends SqlAdvancedExecutor
{
    private String sql;

    public SqlUpdate(@InjectionWarn String sql)
    {
        this.sql = sql;
    }

    @Override
    protected Object execute(Connection connection, SqlHandle sqlHandle) throws DBException
    {
        PreparedStatement ps = null;
        try
        {
            ps = connection.prepareStatement(sql);
            return ps.executeUpdate();
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }
    }


}
