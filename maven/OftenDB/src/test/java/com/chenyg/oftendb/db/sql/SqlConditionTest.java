package com.chenyg.oftendb.db.sql;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.Unit;
import com.chenyg.wporter.log.LogUtil;

import java.util.Date;

/**
 * SqlCondition Tester.
 *
 * @author <Authors name>
 * @version 1.0
 */
public class SqlConditionTest
{
    public static void main(String[] args)
    {
        SqlCondition condition = new SqlCondition();
        condition.put(Condition.EQ, new Unit("name", "tome")).put(Condition.GTE, new Unit("time", new Date()));

        condition.put(Condition.OR,new SqlCondition().put(Condition.EQ,new Unit("age",18)).put(Condition.EQ,new Unit("age",28)).put(Condition.SUBSTR,new Unit("desc","hi")));



        Object[] objs = (Object[]) condition.toFinalObject();
        LogUtil.printErrPosLn(objs);
    }
} 
