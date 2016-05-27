package com.chenyg.oftendb.data;

import com.chenyg.wporter.base.SimpleAppValues;

public class DataKeyValues extends SimpleAppValues
{
    private String contentType;

    public static final String CONTENT_TYPE_KEY = "Content-Type";

    public DataKeyValues(String... names)
    {
	super(names);
    }

    @Override
    public DataKeyValues names(String... names)
    {
	super.names(names);
	return this;
    }

    @Override
    public DataKeyValues values(Object... values)
    {
	super.values(values);
	return this;

    }

    public DataKeyValues setContentType(String contentType)
    {
	this.contentType = contentType;
	return this;
    }

    /**
     * 得到类型
     * 
     * @return
     */
    public String getContentType()
    {
	return contentType;
    }
}
