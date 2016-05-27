package com.chenyg.oftendb.db.mongodb;

import com.chenyg.oftendb.db.AdvancedQuery;
import com.chenyg.oftendb.db.DBException;
import com.mongodb.DBCollection;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by 宇宙之灵 on 2015/9/23.
 */
public abstract class MongoAdvancedQuery extends AdvancedQuery
{
    protected abstract List<JSONObject> execute(DBCollection collection,MongoHandle mongoHandle) throws DBException;

}
