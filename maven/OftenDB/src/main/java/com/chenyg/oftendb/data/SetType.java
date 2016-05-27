package com.chenyg.oftendb.data;

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
     * 用于{@linkplain Common#createData(Class, com.chenyg.wporter.WPObject)}
     */
    CREATE
}
