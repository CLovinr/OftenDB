package com.chenyg.oftendb.db.sql.advanced;


import com.chenyg.oftendb.db.BaseEasier;

import com.chenyg.oftendb.db.sql.SqlAdvancedQuery;


public class JoinQuery extends SqlAdvancedQuery
{

    String[] tables;
    private Selection[] selections;
    private Join join;
    private LinkCondition where;
    private LinkQuerySettings linkQuerySettings;
    private Integer countIndex;
    private String countKey;

    public JoinQuery(String... tables)
    {
        this.tables = tables;
    }

    /**
     * 用于设置统计。设置了之后，{@linkplain #setSelections(Selection...)}将无效.[{rscount:统计结果}]
     *
     * @param countIndex 表索引
     * @param field      字段名
     */
    public void setCount(Integer countIndex, String field)
    {
        this.countIndex = countIndex;
        this.countKey = field;
    }

    @Override
    public Object toFinalObject()
    {
        if (countIndex == null)
        {
            checkArray(selections, Selection.class.getName() + " array");
        }

        if (join == null)
        {
            throw new RuntimeException("you should set " + Join.class);
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        if (countIndex != null)
        {
            stringBuilder.append("count(").append(tables[countIndex]).append(countKey).append(") rscount ");
        } else
        {
            for (Selection selection : selections)
            {
                stringBuilder.append(selection.toFinalObject()).append(",");
            }
        }

        BaseEasier.removeEndChar(stringBuilder, ',');

        stringBuilder.append(" FROM ");

        stringBuilder.append(join.toFinalObject()).append(" ");

        if (where != null)
        {
            stringBuilder.append("WHERE ").append(where.toFinalObject()).append(" ");
        }

        if (linkQuerySettings != null)
        {
            Object object = linkQuerySettings.toFinalObject();
            if (object != null)
            {
                stringBuilder.append(" ORDER BY ").append(object);
            }
            if (linkQuerySettings.getLimit() != null)
            {
                int offset = linkQuerySettings.getSkip() == null ? 0 : linkQuerySettings.getSkip();
                int count = linkQuerySettings.getLimit();
                stringBuilder.append(" LIMIT ").append(offset).append(",").append(count);
            }
        }

        return stringBuilder;
    }

    /**
     * 用于排序、分页等
     *
     * @param linkQuerySettings
     */

    public void setLinkQuerySettings(LinkQuerySettings linkQuerySettings)
    {
        this.linkQuerySettings = linkQuerySettings;
    }

    /**
     * where条件
     *
     * @param where
     */
    public void setWhere(LinkCondition where)
    {
        this.where = where;
    }

    public void setJoin(Join join)
    {
        this.join = join;
    }

    private <T> void checkArray(T[] ts, String name)
    {
        if (ts == null || ts.length == 0)
        {
            throw new RuntimeException("the " + name + " should not be empty or null!");
        }
    }

    /**
     * 设置列的选择
     *
     * @param selections
     */
    public void setSelections(Selection... selections)
    {
        checkArray(selections, Selection.class.getName() + " array");
        this.selections = selections;
    }

    /**
     * 构造一个LinkCondition
     *
     * @return
     */
    public LinkCondition newLinkCondition()
    {
        return new LinkCondition(this);
    }

    public Join newJoin(int tableIndex1, int tableIndex2, JoinType joinType)
    {
        return new Join(this, tableIndex1, tableIndex2, joinType);
    }

    public Selection newSelection(int tableIndex, String keyName)
    {
        return new Selection(this, tableIndex, keyName);
    }

    public LinkQuerySettings newLinkQuerySettings()
    {
        return new LinkQuerySettings(this);
    }


}
