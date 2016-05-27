package com.chenyg.oftendb.db.sql;

import com.chenyg.oftendb.db.BaseEasier;
import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.Operator;
import com.chenyg.oftendb.db.Unit;

public class SqlCondition extends Condition
{

    private boolean isAnd = true;
    private Class<?> dealNamesClass;

    /**
     * <pre>
     * 通配符 说明
     * _    与任意单字符匹配
     * %    与包含一个或多个字符的字符串匹配
     * [ ]  与特定范围（例如，[a-f]）或特定集（例如，[abcdef]）中的任意单字符匹配。
     * [^]  与特定范围（例如，[^a-f]）或特定集（例如，[^abcdef]）之外的任意单字符匹配。
     * </pre>
     */
    public static final Operator LIKE = new MyOperator("LIKE");

    public static final Operator IS_NULL = new MyOperator("is NULL");

    public static final Operator IS_NOT_NULL = new MyOperator("is not NULL");

    /**
     * 会进行sql注入处理.
     */
    @Override
    public Object toFinalObject() throws ConditionException
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < size(); i++)
        {
            Operator operator = getOperator(i);
            if (operator == NOT || operator == OR)
            {
                deal2(operator, get(i), stringBuilder);
            } else
            {
                dealNormal(operator, get(i), stringBuilder);
            }
        }

        if (stringBuilder.length() == 0)
        {
            stringBuilder.append("TRUE");
        }
        return stringBuilder.toString();
    }


    @Override
    public String toString()
    {
        Object obj = toFinalObject();

        return obj != null ? obj.toString() : super.toString();
    }

    private void deal2(Operator operator, Object object, StringBuilder stringBuilder)
    {
        if (!(object instanceof SqlCondition))
        {
            throw new ConditionException("value should be type of " + getClass()
                    + ".current type is "
                    + object.getClass());
        }
        SqlCondition condition = (SqlCondition) object;

        link(stringBuilder);

        if (operator == NOT)
        {
            stringBuilder.append("NOT");

        } else if (operator == OR)
        {
            condition.isAnd = false;
        } else
        {
            throw new ConditionException("the operator should be " + NOT
                    + " or "
                    + OR
                    + " for value of BasicCondition type");
        }
        stringBuilder.append("(").append(condition.toFinalObject()).append(")");
    }

    private void link(StringBuilder stringBuilder)
    {
        if (stringBuilder.length() > 0)
        {
            stringBuilder.append(isAnd ? " AND " : " OR ");
        }
    }

    /**
     * @param unit
     * @Key注解的处理
     */
    private void dealNames(Unit unit)
    {
        if (dealNamesClass == null)
        {
            return;
        }
        if (!unit.isParam1Value())
        {
            unit.setParam1(BaseEasier.dealWith_Key(dealNamesClass, unit.getParam1()));
        }
        if (!unit.isParam2Value())
        {
            unit.setParam2(BaseEasier.dealWith_Key(dealNamesClass, (String) unit.getParam2()));
        }
    }

    /**
     * 从字符串转换出Operator
     *
     * @param op
     * @return
     */
    public static Operator fromStr(String op)
    {
        op = op.toLowerCase();
        Operator operator = Condition.fromStr(op);
        if (operator != null)
        {
            return operator;
        }
        if (op.equals("like"))
        {
            operator = LIKE;
        } else if (op.equals("is null"))
        {
            operator = IS_NULL;
        } else if (op.equals("is not null"))
        {
            operator = IS_NOT_NULL;
        } else
        {
            throw new ConditionException("unknown operator " + op);
        }
        return operator;
    }

    private void dealNormal(Operator operator, Object object, StringBuilder stringBuilder)
    {
        if (!(object instanceof Unit))
        {
            throw new ConditionException(operator + "-Unit is accept!");
        }
        Unit unit = (Unit) object;

        dealNames(unit);// @Key注解的处理

        link(stringBuilder);

        stringBuilder.append(unit.isParam1Value() ? SqlUtil.checkStr(unit.getParam1())
                : "`" + unit.getParam1() + "`");
        stringBuilder.append(" ");

        if (operator == IS_NOT_NULL)
        {
            stringBuilder.append("is not NULL ");
            return;
        } else if (operator == IS_NULL)
        {
            stringBuilder.append("is NULL ");
            return;
        }

        if (operator == IN || operator == NIN)
        {
            StringBuilder sBuilder = new StringBuilder("(");
            if (unit.getParam2() instanceof Object[])
            {
                Object[] objects = (Object[]) unit.getParam2();
                if (objects != null)
                {
                    for (int i = 0; i < objects.length - 1; i++)
                    {
                        sBuilder.append(SqlUtil.checkStr(objects[i])).append(",");
                    }
                    if (objects.length > 0)
                    {
                        sBuilder.append(SqlUtil.checkStr(objects[objects.length - 1]));
                    }
                }
            } else
            {
                sBuilder.append(SqlUtil.checkStr(unit.getParam2()));
            }

            sBuilder.append(") ");
            stringBuilder.append(operator == IN ? "in " : "not in ").append(sBuilder);
            return;
        }

        if (operator == GT)
        {
            stringBuilder.append(">");
        } else if (operator == GTE)
        {
            stringBuilder.append(">=");
        } else if (operator == LT)
        {
            stringBuilder.append("<");
        } else if (operator == LTE)
        {
            stringBuilder.append("<=");
        } else if (operator == NE)
        {
            stringBuilder.append("!=");
        } else if (operator == EQ)
        {
            stringBuilder.append("=");
        } else if (operator == LIKE)
        {
            stringBuilder.append("LIKE");
        } else if (operator == SUBSTR)
        {
            stringBuilder.append("LIKE ");
            stringBuilder.append(SqlUtil.checkStr("%" + unit.getParam2() + "%"));
            return;
        } else
        {
            throw new ConditionException("unknown operator " + operator);
        }
        stringBuilder.append(" ");
        stringBuilder.append(unit.isParam2Value() ? SqlUtil.checkStr(unit.getParam2())
                : "`" + unit.getParam2() + "`");
    }

    @Override
    public void dealNames(Class<?> c)
    {
        this.dealNamesClass = c;
    }
}
