package com.chenyg.oftendb.db.sql.advanced;

import java.util.ArrayList;
import java.util.List;

import com.chenyg.oftendb.db.BaseEasier;
import com.chenyg.oftendb.db.ToFinal;

public class Join implements ToFinal
{

    private int tableIndex1;
    private int tableIndex2;
    private JoinType joinType;
    private LinkCondition[] linkConditions;
    private JoinQuery joinCondition;
    private List<Temp> list;
    private int[] tableIndexes;

    private class Temp
    {
        JoinType joinType;
        LinkCondition[] linkConditions;
        int tableIndex;

        public Temp(JoinType joinType, int tableIndex, LinkCondition[] linkConditions)
        {
            this.joinType = joinType;
            this.tableIndex = tableIndex;
            this.linkConditions = linkConditions;
        }
    }

    Join(JoinQuery joinCondition, int tableIndex1, int tableIndex2, JoinType joinType)
    {
        this.joinCondition = joinCondition;
        this.tableIndex1 = tableIndex1;
        this.tableIndex2 = tableIndex2;
        this.joinType = joinType;
    }

    /**
     * 设置该值会使得{@linkplain #setTableIndexes(int...)}失效.
     *
     * @param joinType
     * @param tableIndex
     * @param linkConditions
     * @return
     */
    public Join join(JoinType joinType, int tableIndex, LinkCondition... linkConditions)
    {
        if (list == null)
        {
            list = new ArrayList<Temp>();
        }
        list.add(new Temp(joinType, tableIndex, linkConditions));
        this.tableIndexes = null;
        return this;
    }

    /**
     * 设置该值会使得{@linkplain #join(JoinType, int, LinkCondition...)}失效.
     *
     * @param tableIndexes
     */
    public void setTableIndexes(int... tableIndexes)
    {
        this.tableIndexes = tableIndexes;
        this.list = null;
    }

    /**
     * 设置Join的条件
     *
     * @param linkConditions
     */
    public void setLinkConditions(LinkCondition... linkConditions)
    {
        this.linkConditions = linkConditions;
    }

    private void addCondition(StringBuilder stringBuilder, LinkCondition[] linkConditions)
    {
        if (linkConditions != null)
        {

            for (int i = 0; i < linkConditions.length - 1; i++)
            {
                stringBuilder.append(linkConditions[i].toFinalObject()).append(" AND ");
            }
            if (linkConditions.length > 0)
            {
                stringBuilder.append(linkConditions[linkConditions.length - 1].toFinalObject()).append(" ");
            }
        }
    }

    @Override
    public Object toFinalObject()
    {

        if (linkConditions == null && tableIndexes == null)
        {
            throw new RuntimeException("the join is not completed!");
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (tableIndexes == null)
        {
            stringBuilder.append(joinCondition.tables[tableIndex1]).append(" ").append(joinType.getSqlStr())
                    .append(" ");
            stringBuilder.append(joinCondition.tables[tableIndex2]).append(" ON ");
            addCondition(stringBuilder, linkConditions);
            if (list != null)
            {
                for (int i = 0; i < list.size(); i++)
                {
                    Temp temp = list.get(i);
                    stringBuilder.append(temp.joinType.getSqlStr()).append(" ");
                    stringBuilder.append(joinCondition.tables[temp.tableIndex]).append(" ON ");
                    addCondition(stringBuilder, temp.linkConditions);
                }
            }
        } else
        {
            for (int index : tableIndexes)
            {
                stringBuilder.append(joinCondition.tables[index]).append(",");
            }
            BaseEasier.removeEndChar(stringBuilder, ',');
        }

        return stringBuilder;
    }

}
