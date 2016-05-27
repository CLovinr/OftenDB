package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.Unit;

public class LinkUnit extends Unit
{

    private Integer tableIndex1, tableIndex2;
    private boolean isAdded=false;

    /**
     * 
     * @param name
     * @param value 可以是列名
     * @param tableIndex1 name对应的table的索引，或者为null
     * @param tableIndex2 value对应的table的索引，或者为null
     */
    public LinkUnit(String name, Object value, Integer tableIndex1, Integer tableIndex2)
    {
	super(name, value);
	this.tableIndex1 = tableIndex1;
	this.tableIndex2 = tableIndex2;
    }

    void visit(JoinQuery joinCondition)
    {
	if (isAdded)
	{
	    throw new RuntimeException("it is already added!");
	}
	if (tableIndex1 != null)
	{
	    setParam1Value(false);
	    param1 = joinCondition.tables[tableIndex1] + "." + param1;
	}
	if (tableIndex2 != null)
	{
	    setParam2Value(false);
	    param2 = joinCondition.tables[tableIndex2] + "." + param2;
	}
	isAdded=true;
    }

}