package com.chenyg.oftendb.db.mongodb.advanced;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import com.chenyg.wporter.WPObject;
import org.json.JSONObject;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.NameValues;
import com.chenyg.oftendb.db.Unit;
import com.chenyg.oftendb.db.NameValues.Foreach;
import com.chenyg.oftendb.db.mongodb.MongoAdvancedExecutor;
import com.chenyg.oftendb.db.mongodb.MongoCondition;
import com.chenyg.oftendb.db.mongodb.MongoHandle;
import com.chenyg.oftendb.db.mongodb.MongodbUtil;
import com.chenyg.wporter.util.BytesTool;
import com.chenyg.wporter.util.WPTool;
import com.mongodb.DBCollection;

/**
 * <pre>
 * 方式一：多文档方式
 * 把输入流存入多个文档中.
 * 1.主文档为{isDirty_multi:true/false,length:总长度,blockCount:大小,_id:${_id},${nameValues}}。
 * 2.数据分别存入{_id:${_id}0,${name}:字节数据,mainId:${_id}},...
 * 方式二：单文档方式
 * 把输入流出入一个文档中。（对于固定文档不应该使用此方式）
 * 1.{isDirty_single:true/false,length:总长度,blockCount:大小,_id:${_id},${nameValues},${name}0:bytes,${name}1:bytes,...,
 * ${name}blockCount-1:bytes}
 * 注意：
 * 1.文档中含有isDirty:true的数据一定是脏数据。
 * 2.不管是哪种方式，都是采用多次的方式进行写入的。
 * 返回值:
 * 1.保存,boolean
 * 2.读取,null
 * 3.删除,int
 * 4.获取信息，JSONObject|null
 * </pre>
 *
 * @author ZhuiFeng
 */
public class DocAdvancedExecutor extends MongoAdvancedExecutor
{
    private boolean isSingleType;
    private Object[] params;

    /**
     * 0保存,1读取到OutputStream,2删除单文档脏数据,3获取信息,4读取到WPObject的Response输出流中，5.删除多文档脏数据
     */
    private int type;

    /**
     * @param isSingleType 是否是单文档方式
     */
    public DocAdvancedExecutor(boolean isSingleType)
    {
        this.isSingleType = isSingleType;
    }

    /**
     * 读取信息
     *
     * @param _id  id
     * @param keys 要读取的键值
     */
    public void readInfo(String _id, String... keys)
    {
        this.params = new Object[]{_id, keys};
        type = 3;
    }


    /**
     * @param in        输入流
     * @param _id       文档id（唯一性）
     * @param name      实际数据的键名
     * @param eachSize  每个块最大字节数
     * @param keyValues 附加信息
     * @throws DBException
     */
    public void save(InputStream in, String _id, String name, int eachSize, NameValues keyValues) throws DBException
    {
        this.params = new Object[]{in, _id, name, eachSize, keyValues};
        type = 0;
    }

    /**
     * 把数据写到输出流中(会关闭输出流)
     *
     * @param _id  id值
     * @param name 实际数据的键名
     * @param os   输出流
     * @throws DBException
     */
    public void readTo(String _id, String name, OutputStream os) throws DBException
    {
        this.params = new Object[]{_id, name, os};
        type = 1;
    }

    /**
     * 把数据写到输出流中(会关闭输出流)
     *
     * @param _id      id
     * @param name     实际数据的键名
     * @param wpObject 用于输出数据的
     * @throws DBException
     */
    public void readTo(String _id, String name, WPObject wpObject) throws DBException
    {
        this.params = new Object[]{_id, name, wpObject};
        type = 4;
    }

    /**
     * 会建立索引:isDirty_multi,mainId,isDirty_single
     *
     * @param collection
     */
    public static void createInIndexes(DBCollection collection)
    {
        MongodbUtil.createIndex(collection, 1, false, "isDirty_multi", "mainId", "isDirty_single");
    }

    /**
     * 删除所有脏数据文档(单文档方式)*
     *
     * @throws DBException
     */
    public void delDirtyDocsOfSingle() throws DBException
    {
        type = 2;
    }

    /**
     * 删除所有脏数据文档(多文档方式)*
     *
     * @throws DBException
     */
    public void delDirtyDocsOfMulti() throws DBException
    {
        type = 5;
    }

    /**
     * 删除所有脏数据文档(多文档方式)
     *
     * @param mongoHandle 操作
     * @return 返回删除的数量
     * @throws DBException
     */
    private static int delDirtyDocsOfMulti(MongoHandle mongoHandle) throws DBException
    {

        Condition condition = new MongoCondition();
        condition.put(Condition.EQ, new Unit("isDirty_multi", true));
        return mongoHandle.del(condition);
    }

    /**
     * 删除所有脏数据文档(单文档方式)
     *
     * @param mongoHandle 操作
     * @return 返回删除的数量
     * @throws DBException
     */
    private static int delDirtyDocsOfSingle(MongoHandle mongoHandle) throws DBException
    {

        Condition condition = new MongoCondition();
        condition.put(Condition.EQ, new Unit("isDirty_single", true));
        return mongoHandle.del(condition);
    }

    private static boolean save2MuiltiDocs(InputStream in, MongoHandle mongoHandle, String _id, String name,
            int eachSize, NameValues keyValues)
    {
        ByteBuffer buffer = ByteBuffer.allocate(eachSize);
        try
        {

            boolean success;

            // 添加主文档
            NameValues nameValues = new NameValues();
            addNameValues(keyValues, nameValues);
            int length = 0;
            nameValues.put("_id", _id).put("isDirty_multi", true).put("blockCount", 0).put("length", length);
            success = mongoHandle.add(nameValues);

            // 添加数据文档
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            NameValues _nameValues = new NameValues(3);
            int i = 0;
            while (success && BytesTool.read(buffer, in).hasRemaining())
            {
                length += buffer.remaining();
                _nameValues.clear();
                bos.reset();
                bos.write(buffer.array(), 0, buffer.remaining());
                _nameValues.put(name, bos.toByteArray());
                _nameValues.put("_id", _id + i++).put("mainId", _id);
                success = mongoHandle.add(_nameValues);
            }

            // 更改标志
            if (success)
            {
                _nameValues.clear();
                _nameValues.put("isDirty_multi", false).put("blockCount", i).put("length", length);
                Condition condition = new MongoCondition();
                condition.put(Condition.EQ, new Unit("_id", _id));
                success = mongoHandle.update(condition, _nameValues) > 0;
            }
            return success;
        } catch (IOException e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(in);
        }
    }

    private static void addNameValues(NameValues from, final NameValues to)
    {
        if (from != null)
        {
            from.forEach(new Foreach()
            {

                @Override
                public boolean forEach(String name, Object value)
                {
                    to.put(name, value);
                    return true;
                }
            });
        }
    }

    private static boolean save2SingleDoc(InputStream in, MongoHandle mongoHandle, String _id, String name,
            int eachSize, NameValues keyValues)
    {
        ByteBuffer buffer = ByteBuffer.allocate(eachSize);
        try
        {

            boolean success;

            // 写初始化信息
            NameValues nameValues = new NameValues();
            addNameValues(keyValues, nameValues);
            nameValues.put("_id", _id).put("isDirty_single", true);
            success = mongoHandle.add(nameValues);

            // 写数据
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            NameValues _nameValues = new NameValues(1);
            Condition condition = new MongoCondition();
            condition.put(Condition.EQ, new Unit("_id", _id));
            int i = 0;
            int length = 0;
            while (success && BytesTool.read(buffer, in).hasRemaining())
            {
                length += buffer.remaining();
                _nameValues.clear();
                bos.reset();
                bos.write(buffer.array(), 0, buffer.remaining());
                _nameValues.put(name + i++, bos.toByteArray());
                success = mongoHandle.update(condition, _nameValues) > 0;
            }

            // 更新标志
            if (success)
            {
                _nameValues.clear();
                _nameValues.put("isDirty_single", false).put("blockCount", i).put("length", length);
                success = mongoHandle.update(condition, _nameValues) > 0;
            }
            return success;
        } catch (IOException e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(in);
        }
    }

    private static void readTo_single(MongoHandle mongoHandle, String _id, String name,
            OutputStream os) throws DBException
    {
        try
        {
            Condition condition = new MongoCondition();
            condition.put(Condition.EQ, new Unit("_id", _id));
            List<JSONObject> list = mongoHandle.getJSONs(condition, null, "isDirty_single", "blockCount");
            if (list.size() == 0)
            {
                throw new DBException("not found(_id=" + _id + ")");
            }
            JSONObject jsonObject = list.get(0);
            if (jsonObject.getBoolean("isDirty_single"))
            {
                throw new DBException("the doc is dirty(_id=" + _id + ")");
            }
            int blockCount = jsonObject.getInt("blockCount");
            for (int i = 0; i < blockCount; i++)
            {
                List<Object> list2 = mongoHandle.get(condition, null, name + i);
                Object object;
                if (list2.size() == 0 || ((object = list2.get(0)) == null)
                        || !(object instanceof byte[]))
                {
                    throw new DBException("the data of block " + i
                            + " is damaged(_id="
                            + _id
                            + ")");
                }
                byte[] bs = (byte[]) object;
                os.write(bs, 0, bs.length);
            }
            os.flush();
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(os);
        }
    }

    private static void readTo_multi(MongoHandle mongoHandle, String _id, String name,
            OutputStream os) throws DBException
    {
        try
        {
            Condition condition = new MongoCondition();
            condition.put(Condition.EQ, new Unit("_id", _id));
            List<JSONObject> list = mongoHandle.getJSONs(condition, null, "isDirty_multi", "blockCount");
            if (list.size() == 0)
            {
                throw new DBException("not found(_id=" + _id + ")");
            }
            JSONObject jsonObject = list.get(0);
            if (jsonObject.getBoolean("isDirty_multi"))
            {
                throw new DBException("the doc is dirty(_id=" + _id + ")");
            }
            int blockCount = jsonObject.getInt("blockCount");
            for (int i = 0; i < blockCount; i++)
            {
                List<Object> list2 = mongoHandle
                        .get(new MongoCondition().put(Condition.EQ, new Unit("_id", _id + i)), null, name);
                Object object;
                if (list2.size() == 0 || ((object = list2.get(0)) == null)
                        || !(object instanceof byte[]))
                {
                    throw new DBException("the data of block " + i
                            + " is damaged(_id="
                            + _id
                            + ")");
                }
                byte[] bs = (byte[]) object;
                os.write(bs, 0, bs.length);
            }
            os.flush();
        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(os);
        }
    }

    @Override
    public Object toFinalObject()
    {
        return null;
    }

    @Override
    protected Object execute(DBCollection collection, MongoHandle mongoHandle) throws DBException
    {
        Object object = null;
        switch (type)
        {
            case 0:
            {
                InputStream in = (InputStream) params[0];
                String _id = (String) params[1];
                String name = (String) params[2];
                int eachSize = (Integer) params[3];
                NameValues keyValues = (NameValues) params[4];
                if (isSingleType)
                {
                    object = save2SingleDoc(in, mongoHandle, _id, name, eachSize, keyValues);
                } else
                {
                    object = save2MuiltiDocs(in, mongoHandle, _id, name, eachSize, keyValues);
                }
            }
            break;
            case 1:
            {
                String _id = (String) params[0];
                String name = (String) params[1];
                OutputStream os = (OutputStream) params[2];
                if (isSingleType)
                {
                    readTo_single(mongoHandle, _id, name, os);
                } else
                {
                    readTo_multi(mongoHandle, _id, name, os);
                }
            }
            break;
            case 2:
            {
                object = delDirtyDocsOfSingle(mongoHandle);
            }
            break;
            case 3:
            {
                String _id = (String) params[0];
                String[] keys = (String[]) params[1];
                Condition condition = new MongoCondition();
                condition.put(Condition.EQ, new Unit("_id", _id));
                List<JSONObject> list = mongoHandle.getJSONs(condition, null, keys);
                object = list.size() > 0 ? list.get(0) : null;
            }
            break;
            case 4:
            {
                String _id = (String) params[0];
                String name = (String) params[1];
                WPObject wpObject = (WPObject) params[2];
                OutputStream os;
                try
                {
                    os = wpObject.getResponse().getOutputStream();
                } catch (IOException e)
                {
                    throw new DBException(e);
                }
                if (isSingleType)
                {
                    readTo_single(mongoHandle, _id, name, os);
                } else
                {
                    readTo_multi(mongoHandle, _id, name, os);
                }
            }
            break;
            case 5:
            {
                object = delDirtyDocsOfMulti(mongoHandle);
            }
            break;
        }

        return object;
    }
}
