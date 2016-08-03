package com.chenyg.oftendb.data;

import com.chenyg.wporter.WPObject;

/**
 * 设置类型
 * 
 * @author ZhuiFeng
 *
 */
public enum SetType
{
    
    ADD,REPLACE, UPDATE, DELETE, QUERY,
    /**
     * 用于{@linkplain Common#createData(Class, WPObject)}
     */
    CREATE
}
