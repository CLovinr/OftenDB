package com.chenyg.oftendb.db.mongodb;


import com.chenyg.oftendb.db.MultiNameValues;
import com.chenyg.oftendb.db.NameValues;
import com.chenyg.oftendb.db.NameValues.Foreach;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.BasicBSONObject;

class Util
{

    public static DBObject toDbObject(NameValues nameValues)
    {
        final BasicDBObject basicDBObject = new BasicDBObject();
        nameValues.forEach(new Foreach()
        {

            @Override
            public boolean forEach(String name, Object value)
            {
                basicDBObject.append(name, value);
                return true;
            }
        });

        return basicDBObject;
    }


    public static BasicDBList toDBList(MultiNameValues multiNameValues)
    {
        String[] names = multiNameValues.getNames();
        BasicDBList basicDBList = new BasicDBList();
        for (int i = 0; i < multiNameValues.count(); i++)
        {
            Object[] values = multiNameValues.values(i);
            BasicBSONObject bsonObject = new BasicBSONObject(values.length);
            for (int j = 0; j < names.length; j++)
            {
                bsonObject.append(names[j], values[j]);
            }
            basicDBList.add(bsonObject);
        }

        return basicDBList;
    }

}
