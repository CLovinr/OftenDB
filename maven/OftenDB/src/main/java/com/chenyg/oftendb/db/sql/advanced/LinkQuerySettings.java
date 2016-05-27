package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.sql.SqlQuerySettings;

public class LinkQuerySettings extends SqlQuerySettings
{
    private JoinQuery joinCondition;
    private Integer currentTableIndex;

    LinkQuerySettings(JoinQuery joinCondition)
    {
	this.joinCondition = joinCondition;
    }

    /**
     * 设置当前使用的表名对应的索引，设置为null表示不使用
     * @param currentTableIndex
     * @return
     */
    public LinkQuerySettings setCurrentTableIndex(Integer currentTableIndex)
    {
	this.currentTableIndex = currentTableIndex;
	return this;
    }

    @Override
    public LinkQuerySettings putOrder(String name, int n)
    {
	if (currentTableIndex != null)
	{
	    name = joinCondition.tables[currentTableIndex] + "." + name;
	}
	super.putOrder(name, n);
	return this;
    }
}
