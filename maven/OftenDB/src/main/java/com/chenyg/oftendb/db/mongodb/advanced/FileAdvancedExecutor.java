package com.chenyg.oftendb.db.mongodb.advanced;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.chenyg.wporter.security.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chenyg.oftendb.data.DataKeyValues;
import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;
import com.chenyg.oftendb.db.mongodb.MongoAdvancedExecutor;
import com.chenyg.oftendb.db.mongodb.MongoHandle;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.WPResponse;
import com.chenyg.wporter.util.FileTool;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * 与文件有关,使用的是GridFS.
 * <p/>
 * <pre>
 * 1.写文件返回的是Boolean，表示是否成功！
 * 2.读文件返回的是{@linkplain FileResult}
 * 3.删除文件返回的Boolean，表示是否成功！
 * 4.列出文件返回的是json数组
 * 5.修改文件信息返回的是boolean
 * </pre>
 *
 * @author ZhuiFeng
 */
public class FileAdvancedExecutor extends MongoAdvancedExecutor
{

    /**
     * 读数据库文件的结果
     *
     * @author ZhuiFeng
     */
    public abstract class FileResult
    {
        private boolean isFound;

        public FileResult(boolean isFound)
        {
            this.isFound = isFound;
        }

        /**
         * 是否找到了
         *
         * @return
         */
        public boolean isFound()
        {
            return isFound;
        }

        /**
         * 把文件流进行输出。
         *
         * @param wpObject
         * @param writeKeyValues 是否写文件的一些属性。
         * @param bufSize
         * @throws IOException
         */
        public void write(WPObject wpObject, boolean writeKeyValues, int bufSize) throws IOException
        {
            if (isFound())
            {
                try
                {
                    if (writeKeyValues)
                    {
                        writeAppValues(wpObject);
                    }
                    InputStream in = getInputStream();
                    WPResponse response = wpObject.getResponse();
                    response.setContentType(getContentType());
                    OutputStream os = response.getOutputStream();
                    FileTool.in2out(in, os, bufSize);
                } catch (IOException e)
                {
                    throw e;
                }

            }
        }

        private void writeAppValues(WPObject wpObject)
        {
            DataKeyValues keyValues = getDataKeyValues();

            if (keyValues != null)
            {
                WPResponse response = wpObject.getResponse();
                String[] names = keyValues.getNames();
                Object[] keys = keyValues.getValues();
                response.addHeader(DataKeyValues.CONTENT_TYPE_KEY, keyValues.getContentType());
                for (int i = 0; i < names.length; i++)
                {
                    response.addHeader(names[i], keys[i] == null ? null : keys[i].toString());
                }
            }
        }

        public abstract InputStream getInputStream() throws IOException;

        public abstract DataKeyValues getDataKeyValues();

        /**
         * 得到上传日期
         *
         * @return
         */
        public abstract long getUploadDate();

        public abstract long length();

        public abstract String md5();

        public abstract String getContentType();

    }

    private abstract static class InGeter
    {
        public abstract InputStream getInputStream() throws IOException;
    }

    private InGeter inGeter;
    private DataKeyValues dataKeyValues;

    /**
     * -1:写文件,base64,0:写文件，1：读文件，2：删除文件,3：列出文件，4：修改文件信息
     */
    private int type = 9999;
    private DBObject dbObject;
    private int bufSize = 2048;
    private Object[] objects;

    /**
     * 设置写文件对应的缓冲数组大小。
     *
     * @param bufSize
     */
    public void setBufSize(int bufSize)
    {
        this.bufSize = bufSize;
    }

    public void update(Condition query, DataKeyValues keyValues)
    {
        dbObject = MongoHandle.checkToFinal(query);
        this.dataKeyValues = keyValues;
        type = 4;
    }

    /**
     * 列出文件
     *
     * @param query
     * @param sort
     */
    public void list(Condition query, Condition sort)
    {
        DBObject dbObject = MongoHandle.checkToFinal(query);
        DBObject sortObject = MongoHandle.checkToFinal(sort);
        objects = new Object[]{dbObject, sortObject};
        type = 3;
    }

    /**
     * 删除文件
     *
     * @param query
     */
    public void del(Condition query)
    {
        dbObject = MongoHandle.checkToFinal(query);
        type = 2;

    }

    /**
     * 读取文件
     *
     * @param query
     */
    public void read(Condition query)
    {
        dbObject = MongoHandle.checkToFinal(query);
        type = 1;
    }

    /**
     * 写文件(二进制)
     *
     * @param in
     * @param keyValues
     */
    public void write(InputStream in, DataKeyValues keyValues)
    {
        this.write(in, keyValues, false);
    }

    /**
     * 写文件
     *
     * @param in
     * @param keyValues
     * @param isBase64
     */
    public void write(final InputStream in, DataKeyValues keyValues, boolean isBase64)
    {
        type = isBase64 ? -1 : 0;
        this.inGeter = new InGeter()
        {

            @Override
            public InputStream getInputStream()
            {
                return in;
            }
        };
        this.dataKeyValues = keyValues;
    }

    /**
     * 写文件(二进制)
     *
     * @param wpObject
     * @param keyValues
     */
    public void write(WPObject wpObject, DataKeyValues keyValues)
    {
        this.write(wpObject, keyValues, false);
    }

    /**
     * 写文件
     *
     * @param wpObject
     * @param keyValues
     * @param isBase64
     */
    public void write(final WPObject wpObject, DataKeyValues keyValues, boolean isBase64)
    {
        type = isBase64 ? -1 : 0;
        this.inGeter = new InGeter()
        {

            @Override
            public InputStream getInputStream() throws IOException
            {
                return wpObject.getRequest().getInputStream();
            }
        };
        this.dataKeyValues = keyValues;
    }

    @Override
    public Object toFinalObject()
    {
        return null;
    }

    @Override
    protected Object execute(DBCollection collection, MongoHandle mongoHandle) throws DBException
    {
        DB db = collection.getDB();
        Object resutlt = null;
        try
        {
            GridFS gridFS = new GridFS(db);
            if (type == -1 || type == 0)
            {

                GridFSInputFile gridFSInputFile = gridFS.createFile();
                if (dataKeyValues != null)
                {
                    String[] keys = dataKeyValues.getNames();
                    Object[] values = dataKeyValues.getValues();
                    if (dataKeyValues.getContentType() != null)
                    {
                        gridFSInputFile.setContentType(dataKeyValues.getContentType());
                    }

                    for (int i = 0; i < values.length; i++)
                    {
                        gridFSInputFile.put(keys[i], values[i]);
                    }
                }

                InputStream in = type == -1 ? base64ToImage(inGeter.getInputStream()) : inGeter.getInputStream();
                OutputStream os = gridFSInputFile.getOutputStream();
                FileTool.in2out(in, os, bufSize);
                resutlt = true;
            } else if (type == 1)
            {
                final GridFSDBFile gridFSDBFile = gridFS.findOne(dbObject);

                resutlt = new FileResult(gridFSDBFile != null)
                {
                    private DataKeyValues keyValues = null;

                    @Override
                    public DataKeyValues getDataKeyValues()
                    {
                        if (keyValues == null && gridFSDBFile != null)
                        {
                            Set<String> keySet = gridFSDBFile.keySet();
                            Iterator<String> keys = gridFSDBFile.keySet().iterator();
                            List<String> listKeys = new ArrayList<String>(keySet.size());
                            List<Object> listValues = new ArrayList<Object>(keySet.size());
                            while (keys.hasNext())
                            {
                                String key = keys.next();
                                listKeys.add(key);
                                listValues.add(gridFSDBFile.get(key));
                            }

                            keyValues = new DataKeyValues();
                            keyValues.setContentType(gridFSDBFile.getContentType());
                            keyValues.names(listKeys.toArray(new String[0]));
                            keyValues.values(listValues.toArray(new Object[0]));
                        }
                        return keyValues;
                    }

                    @Override
                    public InputStream getInputStream()
                    {
                        return gridFSDBFile.getInputStream();
                    }

                    @Override
                    public long getUploadDate()
                    {
                        return gridFSDBFile.getUploadDate().getTime();
                    }

                    @Override
                    public long length()
                    {
                        return gridFSDBFile.getLength();
                    }

                    @Override
                    public String md5()
                    {
                        return gridFSDBFile.getMD5();
                    }

                    @Override
                    public String getContentType()
                    {
                        return gridFSDBFile.getContentType();
                    }
                };

            } else if (type == 2)
            {
                gridFS.remove(dbObject);
                resutlt = true;
            } else if (type == 3)
            {

                List<GridFSDBFile> list = gridFS.find((DBObject) objects[0], (DBObject) objects[1]);

                JSONArray array = new JSONArray();

                for (int i = 0; i < list.size(); i++)
                {
                    array.put(toJsonObject(list.get(i)));
                }

                resutlt = array;
            } else
            {

                if (dataKeyValues != null)
                {
                    GridFSDBFile gridFSDBFile = gridFS.findOne(dbObject);
                    String[] names = dataKeyValues.getNames();
                    Object[] values = dataKeyValues.getValues();
                    if (dataKeyValues.getContentType() != null)
                    {
                        gridFSDBFile.put(DataKeyValues.CONTENT_TYPE_KEY, dataKeyValues.getContentType());
                    }
                    for (int i = 0; i < names.length; i++)
                    {
                        gridFSDBFile.put(names[i], values[i]);
                    }
                    gridFSDBFile.save();

                }

                resutlt = true;
            }
        } catch (Exception e)
        {
            throw new DBException(e);
        }

        return resutlt;
    }

    private JSONObject toJsonObject(GridFSDBFile gridFSDBFile) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        Iterator<String> keys = gridFSDBFile.keySet().iterator();

        while (keys.hasNext())
        {
            String key = keys.next();
            jsonObject.put(key, gridFSDBFile.get(key));
        }

        return jsonObject;
    }


    private static InputStream base64ToImage(final InputStream in) throws IOException
    {
        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                try
                {
                    BufferedOutputStream bos = new BufferedOutputStream(pipedOutputStream);
                    Base64.decode(in, bos);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
        return pipedInputStream;
    }

}
