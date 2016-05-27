package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.ToFinal;

public class Selection implements ToFinal
{

    String keyName;

    /**
     * @param tableIndex 表名对应的索引(0开始)
     * @param keyName    列名,可以为*
     */
    Selection(JoinQuery joinCondition, int tableIndex, String keyName)
    {
        this.keyName = joinCondition.tables[tableIndex] + "." + keyName;
    }

    @Override
    public Object toFinalObject()
    {
        return keyName;
    }
}
