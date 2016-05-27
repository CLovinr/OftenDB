package com.chenyg.oftendb.db;

public class Util
{
    /**
     * 转义所有正则表达式的特殊字符
     * 
     * @param keyword
     * @return
     */
    public static String escapeExprSpecialWord(String keyword)
    {
	if (keyword != null && keyword.length() > 0)
	{
	    String[] special = { "\\",
		    "$",
		    "(",
		    ")",
		    "*",
		    "+",
		    ".",
		    "[",
		    "]",
		    "?",
		    "^",
		    "{",
		    "}",
		    "|" };
	    for(String key:special)
	    {
		if (keyword.contains(key))
		{
		    keyword = keyword.replaceAll(key, "\\" + key);
		}
	    }
	}
	return keyword;
    }
}
