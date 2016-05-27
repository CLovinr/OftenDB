package com.chenyg.oftendb.db.mongodb;

import com.chenyg.oftendb.db.AdvancedExecutor;
import com.chenyg.oftendb.db.DBException;
import com.mongodb.DBCollection;

public abstract class MongoAdvancedExecutor extends AdvancedExecutor
{

    protected abstract Object execute(DBCollection collection,MongoHandle mongoHandle) throws DBException;

}
