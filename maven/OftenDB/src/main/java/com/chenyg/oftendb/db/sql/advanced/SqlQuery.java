package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.sql.SqlAdvancedQuery;

/**
 * Created by 宇宙之灵 on 2015/9/23.
 */
public class SqlQuery extends SqlAdvancedQuery
{
    private String sql;

    /**
     *
     * @param sql 注意sql防注入处理
     */
    public SqlQuery(String sql){
        this.sql=sql;
    }
    @Override
    public Object toFinalObject()
    {
        return sql;
    }
}
