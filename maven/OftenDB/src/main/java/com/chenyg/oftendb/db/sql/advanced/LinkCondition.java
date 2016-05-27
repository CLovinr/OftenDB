package com.chenyg.oftendb.db.sql.advanced;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.Operator;
import com.chenyg.oftendb.db.Unit;
import com.chenyg.oftendb.db.sql.SqlCondition;

public class LinkCondition extends SqlCondition
{


    private JoinQuery joinCondition;

    LinkCondition(JoinQuery joinCondition)
    {
        this.joinCondition = joinCondition;
    }

    @Override
    public Condition put(Operator operator, Condition condition)
    {
        if (!(condition instanceof LinkCondition))
        {
            throw new RuntimeException(LinkCondition.class + " is acceptted!");
        }
        return super.put(operator, condition);
    }

    @Override
    public LinkCondition put(Operator operator, Unit unit)
    {
        if (unit instanceof LinkUnit)
        {
            LinkUnit linkUnit = (LinkUnit) unit;
            linkUnit.visit(joinCondition);
        }

        super.put(operator, unit);
        return this;
    }
}
