package com.chenyg.oftendb.db;

import java.util.ArrayList;
import java.util.List;

public class MultiNameValues
{
    private String[] names;
    private List<Object[]> valueList;

   
    public MultiNameValues()
    {
	this.valueList = new ArrayList<Object[]>();
    }

    public MultiNameValues names(String... names)
    {
	this.names = names;
	return this;
    }

    public String[] getNames()
    {
	return names;
    }

    public int count()
    {
	return valueList.size();
    }

    public Object[] values(int index)
    {
	return valueList.get(index);
    }

    public MultiNameValues addValues(Object... values)
    {
	valueList.add(values);
	return this;
    }

}
