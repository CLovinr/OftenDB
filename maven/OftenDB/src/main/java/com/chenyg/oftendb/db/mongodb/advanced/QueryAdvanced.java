package com.chenyg.oftendb.db.mongodb.advanced;

import java.util.List;

import com.chenyg.oftendb.db.mongodb.MongoAdvancedQuery;
import org.json.JSONObject;

import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.QuerySettings;
import com.chenyg.oftendb.db.mongodb.MongoCondition;
import com.chenyg.oftendb.db.mongodb.MongoHandle;
import com.chenyg.wporter.base.SimpleAppValues;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * 返回类型List&#60;JSONObject&#62;
 *
 * @author ZhuiFeng
 */
public class QueryAdvanced extends MongoAdvancedQuery
{

    public static class RegexNameValues extends SimpleAppValues
    {

    }

    private RegexNameValues regexNameValues;
    private QuerySettings querySettings;
    private String[] keys;
    private DBObject query;

    /**
     * 0:正则，1:DBObject
     */
    private int type;

    public void query(DBObject query, QuerySettings querySettings, String... keys)
    {
        this.query = query;
        this.querySettings = querySettings;
        this.keys = keys;
        type = 1;
    }

    /**
     * 正则表达式查询。
     *
     * @param regexNameValues
     * @param querySettings
     * @param keys            为空则表示取得所有键值
     */
    public void regExpQuery(RegexNameValues regexNameValues, QuerySettings querySettings, String... keys)
    {
        this.regexNameValues = regexNameValues;
        this.querySettings = querySettings;
        this.keys = keys;
        type = 0;
    }

    @Override
    public Object toFinalObject()
    {
        return null;
    }

    @Override
    protected List<JSONObject> execute(DBCollection collection, MongoHandle mongoHandle) throws DBException
    {
        final DBObject query = type == 0 ? new BasicDBObject() : this.query;

        if (type == 0 && regexNameValues != null)
        {
            String[] names = regexNameValues.getNames();
            Object[] values = regexNameValues.getValues();
            for (int i = 0; i < names.length; i++)
            {
                query.put(names[i], new BasicDBObject("$regex", values[i]));
            }
        }

        MongoCondition mongoCondition = new MongoCondition()
        {
            public Object toFinalObject() throws com.chenyg.oftendb.db.Condition.ConditionException
            {
                return query;
            }
        };

        return mongoHandle.getJSONs(mongoCondition, querySettings, keys);
    }

}
