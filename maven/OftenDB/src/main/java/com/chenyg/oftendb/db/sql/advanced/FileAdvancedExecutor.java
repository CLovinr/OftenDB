package com.chenyg.oftendb.db.sql.advanced;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.chenyg.oftendb.db.Condition;
import com.chenyg.oftendb.db.DBException;

import com.chenyg.oftendb.db.sql.SqlAdvancedExecutor;
import com.chenyg.oftendb.db.sql.SqlCondition;
import com.chenyg.oftendb.db.sql.SqlHandle;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.WPResponse;
import com.chenyg.wporter.security.Base64;
import com.chenyg.wporter.util.FileTool;
import com.chenyg.wporter.util.WPTool;

/**
 * 与文件有关.
 * <p/>
 * <pre>
 * 1.写文件返回的是int，表示影响的条数！
 * 2.读文件：找到并成功输出时返回0，输出错误时返回-1；未找到返回-2
 * 3.删除文件返回的int，表示影响的条数！
 * </pre>
 *
 * @author ZhuiFeng
 */
public class FileAdvancedExecutor extends SqlAdvancedExecutor
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
         * @param bufSize
         * @throws IOException
         */
        public void write(WPObject wpObject, int bufSize) throws IOException
        {
            if (isFound())
            {
                try
                {
                    InputStream in = getInputStream();
                    WPResponse response = wpObject.getResponse();
                    OutputStream os = response.getOutputStream();
                    FileTool.in2out(in, os, bufSize);
                } catch (IOException e)
                {
                    throw e;
                }

            }
        }

        public abstract InputStream getInputStream() throws IOException;

    }

    private abstract static class InGeter
    {
        public abstract InputStream getInputStream() throws IOException;
    }

    private abstract static class OutGeter
    {
        public abstract OutputStream getOutputStream() throws IOException;
    }

    private InGeter inGeter;
    private OutGeter outGeter;

    /**
     * -1:写文件,base64,0:写文件，1：读文件，2：删除文件
     */
    private int type = 9999;
    private SqlCondition query;
    private int bufSize = 2048;
    private String tablename, fieldname;

    public FileAdvancedExecutor(String tablename, String fieldname,
            Condition query)
    {
        this.fieldname = fieldname;
        this.tablename = tablename;
        setQuery(query);
    }

    public void setQuery(Condition query)
    {
        this.query = SqlHandle.checkCondition(query);
    }

    /**
     * 设置写文件对应的缓冲数组大小。
     *
     * @param bufSize
     */
    public void setBufSize(int bufSize)
    {
        this.bufSize = bufSize;
    }

    /**
     * 删除文件,将字段值置为null
     */
    public void del()
    {
        type = 2;
    }

    /**
     * 读取文件
     *
     * @param bufSize
     * @param os
     */
    public void read(int bufSize, final OutputStream os)
    {
        type = 1;
        outGeter = new OutGeter()
        {

            @Override
            public OutputStream getOutputStream() throws IOException
            {
                return os;
            }
        };
    }

    /**
     * 读取文件
     *
     * @param bufSize
     * @param wpObject
     */
    public void read(int bufSize, final WPObject wpObject)
    {
        type = 1;
        outGeter = new OutGeter()
        {

            @Override
            public OutputStream getOutputStream() throws IOException
            {
                return wpObject.getResponse().getOutputStream();
            }
        };
    }

    /**
     * 写文件(二进制)
     *
     * @param in
     */
    public void write(InputStream in)
    {
        this.write(in, false);
    }

    /**
     * 写文件
     *
     * @param in
     * @param isBase64
     */
    public void write(final InputStream in, boolean isBase64)
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
    }

    /**
     * 写文件(二进制)
     *
     * @param wpObject
     */
    public void write(WPObject wpObject)
    {
        this.write(wpObject, false);
    }

    /**
     * 写文件
     *
     * @param wpObject
     * @param isBase64
     */
    public void write(final WPObject wpObject, boolean isBase64)
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
    }

    @Override
    protected Object execute(Connection conn, SqlHandle sqlHandle) throws DBException
    {
        PreparedStatement ps = null;
        Object resutlt = null;
        try
        {

            if (type == -1 || type == 0)
            {
                ps = conn.prepareStatement("UPDATE " + tablename
                        + " SET "
                        + fieldname
                        + "=? "
                        + (query == null ? ""
                        : "WHERE " + query.toFinalObject()));
                InputStream in = type == -1 ? base64ToImage(inGeter.getInputStream())
                        : inGeter.getInputStream();
                ps.setBlob(1, in);

                resutlt = ps.executeUpdate();
            } else if (type == 1)
            {
                ps = conn.prepareStatement("SELECT " + fieldname
                        + " FROM "
                        + tablename
                        + " "
                        + (query == null ? ""
                        : "WHERE " + query.toFinalObject()));
                ResultSet rs = ps.executeQuery();
                if (rs.next())
                {
                    try
                    {
                        InputStream in = rs.getBinaryStream(1);
                        FileTool.in2out(in, outGeter.getOutputStream(), bufSize);
                        resutlt = 0;
                    } catch (SQLException e)
                    {
                        resutlt = -1;
                    }
                } else
                {
                    resutlt = -2;
                }

            } else if (type == 2)
            {
                ps = conn.prepareStatement("DELETE FROM " + tablename
                        + " WHERE "
                        + query.toFinalObject());
                resutlt = ps.executeUpdate();
            }

        } catch (Exception e)
        {
            throw new DBException(e);
        } finally
        {
            WPTool.close(ps);
        }

        return resutlt;
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
