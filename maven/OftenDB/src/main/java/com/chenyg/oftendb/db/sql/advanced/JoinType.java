package com.chenyg.oftendb.db.sql.advanced;

/**
 * 连接类型
 * 
 * @author ZhuiFeng
 *
 */
public enum JoinType
{
    Left("LEFT JOIN"), Right("RIGHT JOIN"), Inner("INNER JOIN");
    
    private String sqlStr;
    
    private JoinType(String sqlStr)
    {
	this.sqlStr=sqlStr;
    }
    
    public String getSqlStr()
    {
	return sqlStr;
    }
}