package com.chenyg.oftendb.db.asqlite;

import com.chenyg.oftendb.db.AdvancedQuery;
import com.chenyg.oftendb.db.sql.SqlAdvancedQuery;
import com.chenyg.oftendb.db.sql.SqlUtil;

/**
 * Created by 宇宙之灵 on 2016/5/3.
 */
public class SqliteAdvancedQuery extends AdvancedQuery
{

    SqlUtil.WhereSQL whereSQL;
    String[] keys;

    /**
     * @param whereSQL
     * @param keys     大小为0表示选择全部。
     */
    public SqliteAdvancedQuery(SqlUtil.WhereSQL whereSQL, String... keys)
    {
        this.whereSQL = whereSQL;
        this.keys = keys;
    }

    @Override
    public Object toFinalObject()
    {
        throw new RuntimeException("can not be invoked!");
    }
}
