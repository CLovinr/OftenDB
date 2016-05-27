package com.chenyg.oftendb.data;

import java.io.IOException;
import java.util.List;

import com.chenyg.oftendb.db.*;
import com.chenyg.wporter.WPCallException;
import com.chenyg.wporter.base.JResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import com.chenyg.oftendb.data.ParamsGetter.Params;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.ResultCode;
import com.chenyg.wporter.util.WPTool;


/**
 * 简化一些数据库常用的操作（并不是全部）
 */
public class Common
{

    private interface Dealt
    {
        /**
         * 如果{@linkplain DataAble#forQuery()}为null，才会调用此函数。
         *
         * @return 条件
         */
        Condition getCondition();

        void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                Condition _condition, Object[] otherParams) throws Exception;
    }

    /**
     * 默认的
     */
    public static final Common C = new Common(null);
    private DBHandle _dbHandle;

    private Common(DBHandle dbHandle)
    {
        this._dbHandle = dbHandle;
    }


    /**
     * @param willSetParams  是否会调用{@linkplain DataAble#setParams(String[], Object[], String[], Object[], String[], Object[])}
     * @param dbHandleSource
     * @param setType        不为空，则会进行
     *                       {@linkplain #setDataFields(DataAble, boolean, WPObject, SetType, int, DBHandleAccess)}
     */
    private JResponse commonDealt(Dealt dealt, boolean willSetParams, DBHandleSource dbHandleSource,
            ParamsGetter paramsGetter,
            WPObject wpObject, SetType setType, int optionCode, Object... otherParams)
    {
        JResponse jResponse = new JResponse();
        DBHandle dbHandle = null;
        try
        {
            DataAble data;
            Condition condition;
            String rs = null;
            Params params = paramsGetter.getParams();

            {
                data = params.newData();
                if (setType != null)
                {

                    if (dbHandle == null)
                    {
                        dbHandle = dbHandleSource.getDbHandle(paramsGetter, this._dbHandle);
                    }
                    rs = setDataFields(data, willSetParams, wpObject, setType, optionCode,
                            new DBHandleAccess(dbHandleSource, dbHandle));
                }
                condition = data.forQuery();
                if (condition == null)
                {
                    condition = dealt.getCondition();
                }
            }

            if (rs == null)
            {
                if (dbHandle == null)
                {
                    dbHandle = dbHandleSource.getDbHandle(paramsGetter, this._dbHandle);
                }

                if (condition != null)
                {
                    data.dealNames(condition);
                }
                dealt.deal(jResponse, dbHandle, data, condition, otherParams);

            } else
            {
                jResponse.setCode(ResultCode.OK_BUT_FAILED);
                jResponse.setDescription(rs);
            }
        } catch (DBException e)
        {
            jResponse.setCode(ResultCode.DB_EXCEPTION);
            jResponse.setDescription(e.toString());
            jResponse.setExCause(e);
            if (wpObject != null && wpObject.getWPLog() != null)
            {
                wpObject.getWPLog().error(e.getMessage(), e);
            }

        } catch (Exception e)
        {

            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription("On OftenDB:" + e.toString());
            jResponse.setExCause(e);
            if (wpObject != null && wpObject.getWPLog() != null)
            {
                wpObject.getWPLog().error(e.getMessage(), e);
            }

        } finally
        {
            if (dbHandle != null && !dbHandle.isTransaction())
            {
                WPTool.close(dbHandle);
                dbHandleSource.afterClose(dbHandle);
            }

        }

        return jResponse;
    }


    private Condition getQuery(DBHandleSource dbHandleSource, ParamsSelection paramsSelection, WPObject wpObject,
            Params params) throws WPCallException
    {
        try
        {

            DataAble dataAble = params.getDataAble();

            return dataAble.getQuery(dbHandleSource, paramsSelection, wpObject, params);
        } catch (Exception e)
        {
            JResponse jResponse = new JResponse();
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription("On OftenDB:" + e.toString());
            jResponse.setExCause(e);
            if (wpObject != null && wpObject.getWPLog() != null)
            {
                wpObject.getWPLog().error(e.getMessage(), e);
            }
            WPCallException callException = new WPCallException(jResponse);

            throw callException;
        }
    }


    /**
     * 得到事务操作
     */
    public static TransactionHandle<Common> getTransactionHandle(final DBHandleSource dbHandleSource,
            final ParamsGetter paramsGetter)
    {
        TransactionHandle<Common> transactionHandle = new TransactionHandle<Common>()
        {
            Common common = initCommon();

            @Override
            public void startTransaction() throws DBException
            {
                common._dbHandle.startTransaction();
            }

            private Common initCommon()
            {
                DBHandle _dDbHandle_ = dbHandleSource.getDbHandle(paramsGetter, null);
                Common common = new Common(_dDbHandle_);

                if (!common._dbHandle.supportTransaction())
                {
                    throw new DBException("the dbhandle '" + common._dbHandle.getClass()
                            + "' not support transaction");
                }

                return common;
            }

            @Override
            public void commitTransaction() throws DBException
            {
                common._dbHandle.commitTransaction();
            }

            @Override
            public Common common()
            {
                return common;
            }

            @Override
            public void close() throws IOException
            {
                common._dbHandle.close();
                dbHandleSource.afterClose(common._dbHandle);
            }

            @Override
            public void rollback() throws DBException
            {
                common._dbHandle.rollback();
            }
        };

        return transactionHandle;
    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, boolean, WPObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            WPObject wpObject)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, boolean, NameValues, WPObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            NameValues nameValues, WPObject wpObject)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, nameValues, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 添加单条数据.若成功，返回结果码为ResultCode.SUCCESS，若此时响应数据，则结果为JSONObject.
     *
     * @param responseData 是否在添加成功时，返回添加的对象。
     * @param nameValues
     * @param wpObject
     * @param optionCode
     * @return 操作结果
     */

    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            final NameValues nameValues, WPObject wpObject, int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, nameValues, wpObject, optionCode);
    }


    private JResponse _addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, boolean responseData,
            final NameValues nameValues, WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                boolean success = dbHandle.add(nameValues);
                if (success)
                {
                    boolean responseData = (Boolean) otherParams[0];
                    jResponse.setCode(ResultCode.SUCCESS);
                    jResponse.setResult(responseData ? data.toJsonObject()
                            : null);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                    jResponse.setDescription("add to db failed!");
                }

            }

            @Override
            public Condition getCondition()
            {
                return null;
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.ADD, optionCode,
                responseData);

    }


    /**
     * 添加单条数据.若成功，返回结果码为ResultCode.SUCCESS，若此时响应数据，则结果为JSONObject.
     *
     * @param responseData 是否在添加成功时，返回添加的对象。
     * @param wpObject
     * @param optionCode
     * @return 操作结果
     */

    public JResponse addData(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter, boolean responseData,
            WPObject wpObject,
            int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, responseData, wpObject, optionCode);
    }

    private JResponse _addData(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter, boolean responseData,
            WPObject wpObject,
            int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition condition, Object[] otherParams) throws Exception
            {

                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);
                boolean success = dbHandle.add(nameValues);
                if (success)
                {
                    boolean responseData = (Boolean) otherParams[0];
                    jResponse.setCode(ResultCode.SUCCESS);
                    jResponse.setResult(responseData ? data.toJsonObject()
                            : null);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                    jResponse.setDescription("add to db failed!");
                }

            }

            @Override
            public Condition getCondition()
            {
                return null;
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wpObject, SetType.ADD, optionCode,
                responseData);

    }


    /**
     * @see #addData(DBHandleSource, ParamsGetter, MultiNameValues, WPObject, int)
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WPObject wpObject)
    {
        return _addData(dbHandleSource, paramsGetter, multiNameValues, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 批量添加.返回结果码为ResultCode.SUCCESS时，若结果为null不明确;为json数组，里面放的是整型数组.
     *
     * @param multiNameValues
     * @param wpObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WPObject wpObject, int optionCode)
    {
        return _addData(dbHandleSource, paramsGetter, multiNameValues, wpObject, optionCode);
    }

    private JResponse _addData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final MultiNameValues multiNameValues,
            WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                int[] rs = dbHandle.add(multiNameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(toJArray(rs));
            }

            private Object toJArray(int[] rs)
            {
                JSONArray array = null;
                if (rs != null)
                {
                    array = new JSONArray();
                    for (int i : rs)
                    {
                        array.put(i);
                    }
                }
                return array;
            }

            @Override
            public Condition getCondition()
            {
                return null;
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.ADD, optionCode);

    }


    /**
     * @see #replaceData(DBHandleSource, ParamsGetter, Condition, WPObject, int)
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            Condition condition,
            WPObject wpObject)
    {
        return replaceData(dbHandleSource, paramsGetter, condition, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     *
     * @param condition
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter,
            final Condition condition,
            WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);

                boolean success = dbHandle.replace(_condition, nameValues);
                if (success)
                {
                    jResponse.setCode(ResultCode.SUCCESS);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                }

            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wpObject, SetType.REPLACE, optionCode);

    }


    /**
     * @see #replaceData(DBHandleSource, ParamsGetter, Condition, WPObject, NameValues, int)
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            WPObject wpObject, NameValues nameValues)
    {
        return replaceData(dbHandleSource, paramsGetter, condition, wpObject, nameValues, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * replace数据.若成功，返回结果码为ResultCode.SUCCESS.
     *
     * @param condition
     * @param wpObject
     * @param nameValues
     * @return
     */
    public JResponse replaceData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WPObject wpObject, final NameValues nameValues, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws IllegalArgumentException, IllegalAccessException
            {

                boolean success = dbHandle.replace(_condition, nameValues);
                if (success)
                {
                    jResponse.setCode(ResultCode.SUCCESS);
                } else
                {
                    jResponse.setCode(ResultCode.OK_BUT_FAILED);
                }

            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.REPLACE, optionCode);

    }


    /**
     * 删除数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为删除的记录个数（可能为0）.
     *
     * @param paramsSelection 用于生成查询条件,不为null才会设置查询条件.
     * @param wpObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse deleteData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WPObject wpObject, int optionCode)
    {
        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wpObject,
                    paramsGetter.getParams());
        } catch (WPCallException e)
        {
            return e.getJResponse();
        }
        return _deleteData(dbHandleSource, paramsGetter, condition, wpObject, optionCode);
    }


    ///////////////////////

    /**
     * @see #deleteData(DBHandleSource, ParamsGetter, Condition, WPObject, int)
     */
    public JResponse deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            WPObject wpObject)
    {
        return _deleteData(dbHandleSource, paramsGetter, condition, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * @see #deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WPObject,
     * int)
     */
    public JResponse deleteData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WPObject wpObject)
    {
        return deleteData2(dbHandleSource, paramsGetter, paramsSelection, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 删除数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为删除的记录个数（可能为0）.
     *
     * @param condition
     * @param wpObject
     * @param optionCode
     * @return 操作结果
     */
    public JResponse deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            WPObject wpObject, int optionCode)
    {
        return _deleteData(dbHandleSource, paramsGetter, condition, wpObject, optionCode);
    }


    //////////////////////

    private JResponse _deleteData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                int n = dbHandle.del(_condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }

            @Override
            public Condition getCondition()
            {
                return condition;
            }
        };
        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wpObject, SetType.DELETE,
                optionCode);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONObject或null.
     *
     * @param condition
     * @param keysSelection
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse queryOne(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            KeysSelection keysSelection, WPObject wpObject, int optionCode)
    {
        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wpObject, optionCode);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONObject或null.
     *
     * @param paramsSelection
     * @param keysSelection
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse queryOne2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WPObject wpObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wpObject,
                    paramsGetter.getParams());
        } catch (WPCallException e)
        {
            return e.getJResponse();
        }

        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wpObject, optionCode);
    }


    private JResponse _queryOne(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter,
            final Condition condition,
            final KeysSelection _keysSelection, WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Params params = paramsGetter.getParams();
                KeysSelection keysSelection = data.keys();
                if (keysSelection == null)
                {
                    keysSelection = _keysSelection;
                }
                String[] keys = data.getFinalKeys(keysSelection,
                        params);// getKeys(data, params.getDataClass(), params.getKeyClass(), _keysSelection,
                // paramsGetter);

                JSONObject jsonObject = dbHandle.getOne(_condition, keys);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(jsonObject);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.QUERY,
                optionCode);

    }

    // /////////////////


    /**
     * @see #queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings,
     * KeysSelection, WPObject, int)
     */
    public JResponse queryData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject)
    {
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wpObject,
                DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * @see #queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings,
     * KeysSelection, WPObject, int)
     */
    public JResponse queryData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject)
    {
        return queryData2(dbHandleSource, paramsGetter, paramsSelection, querySettings, keysSelection, wpObject,
                DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection,
     * WPObject, int)
     */
    public JResponse queryOne(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            KeysSelection keysSelection, WPObject wpObject)
    {
        return _queryOne(dbHandleSource, paramsGetter, condition, keysSelection, wpObject,
                DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection,
     * WPObject, int)
     */
    public JResponse queryOne2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WPObject wpObject)
    {
        return queryOne2(dbHandleSource, paramsGetter, paramsSelection, keysSelection, wpObject,
                DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONArray,array里的元素是JSONObject.
     *
     * @param condition
     * @param querySettings
     * @param keysSelection
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse queryData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject, int optionCode)
    {
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wpObject, optionCode);
    }


    /**
     * 查询数据。若成功，返回结果码为ResultCode.SUCCESS,结果为JSONArray,array里的元素是JSONObject.
     *
     * @param paramsSelection
     * @param querySettings
     * @param keysSelection
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse queryData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wpObject,
                    paramsGetter.getParams());
        } catch (WPCallException e)
        {
            return e.getJResponse();
        }
        return _queryData(dbHandleSource, paramsGetter, condition, querySettings, keysSelection, wpObject, optionCode);
    }

    private JResponse _queryData(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter,
            final Condition condition,
            final QuerySettings querySettings, final KeysSelection _keysSelection, WPObject wpObject, int optionCode)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Params params = paramsGetter.getParams();
                KeysSelection keysSelection = data.keys();
                if (keysSelection == null)
                {
                    keysSelection = _keysSelection;
                }
                String[] keys = data.getFinalKeys(keysSelection,
                        params);//getKeys(data, params.getDataClass(), params.getKeyClass(), _keysSelection,
                // paramsGetter);
                if (querySettings != null)
                {
                    data.dealNames(querySettings);
                }

                List<JSONObject> list = dbHandle.getJSONs(_condition, querySettings, keys);
                JSONArray array = new JSONArray(list);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(array);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.QUERY,
                optionCode);

    }


    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为json数组。
     *
     * @param advancedQuery
     * @param wpObject
     * @return 操作结果
     */
    public JResponse queryAdvanced(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final AdvancedQuery advancedQuery, WPObject wpObject)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                List<JSONObject> list = dbHandle.advancedQuery(advancedQuery);
                JSONArray array = new JSONArray(list);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(array);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, null, 0);

    }


    /**
     * 高级查询，若成功，则结果码为SUCCESS,结果为
     * {@linkplain DBHandle#advancedExecute(AdvancedExecutor)}的结果
     *
     * @param advancedExecutor
     * @param wpObject
     * @return 操作结果
     */

    public JResponse advancedExecute(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            final AdvancedExecutor advancedExecutor,
            WPObject wpObject)
    {

        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Object object = dbHandle.advancedExecute(advancedExecutor);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(object);
            }

        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, null, 0);

    }


    /**
     * 统计数据.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @param condition
     * @param wpObject
     * @return 操作结果
     */
    public JResponse count(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter, final Condition condition,
            WPObject wpObject)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {

                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                long n = dbHandle.exists(_condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.QUERY,
                DataAble.OPTION_CODE_EXISTS);

    }


    /**
     * 查询数据是否存在.若成功，返回结果码为ResultCode.SUCCESS,并且结果为一个long值，表示存在的数目.
     *
     * @param key      键名 会进行@Key处理，以替换成数据库对应的名称。
     * @param value    键值
     * @param wpObject
     * @return 操作结果
     */
    public JResponse exists(final DBHandleSource dbHandleSource, final ParamsGetter paramsGetter, final String key,
            final Object value, WPObject wpObject)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {

                return null;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                Condition condition = dbHandleSource.newCondition();
                Params params = paramsGetter.getParams();
                condition.put(Condition.EQ, new Unit(key, value));
                long n = dbHandle.exists(condition);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.QUERY,
                DataAble.OPTION_CODE_EXISTS);

    }


    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param paramsSelection
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse updateData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WPObject wpObject, int optionCode)
    {

        Condition condition = null;
        try
        {
            condition = getQuery(dbHandleSource, paramsSelection, wpObject,
                    paramsGetter.getParams());
        } catch (WPCallException e)
        {
            return e.getJResponse();
        }
        return updateData(dbHandleSource, paramsGetter, condition, wpObject, optionCode);
    }


    /**
     * @see #updateData(DBHandleSource, ParamsGetter, Condition, WPObject, int)
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            WPObject wpObject)
    {
        return updateData(dbHandleSource, paramsGetter, condition, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * @see #updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WPObject, int)
     */
    public JResponse updateData2(DBHandleSource dbHandleSource, ParamsGetter paramsGetter,
            ParamsSelection paramsSelection,
            WPObject wpObject)
    {
        return updateData2(dbHandleSource, paramsGetter, paramsSelection, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }


    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param condition
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse updateData(DBHandleSource dbHandleSource, final ParamsGetter paramsGetter,
            final Condition condition,
            WPObject wpObject, int optionCode)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                NameValues nameValues = DataUtil.toNameValues(paramsGetter.getParams(), data);
                int n = dbHandle.update(_condition, nameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, true, dbHandleSource, paramsGetter, wpObject, SetType.UPDATE,
                optionCode);

    }

    /**
     * @see #updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WPObject, int)
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, Condition condition,
            NameValues nameValues, WPObject wpObject)
    {
        return updateData(dbHandleSource, paramsGetter, condition, nameValues, wpObject, DataAble.OPTION_CODE_DEFAULT);
    }

    /**
     * 保存数据.若成功，返回结果码为ResultCode.SUCCESS,且结果为影响的记录条数。
     *
     * @param condition
     * @param nameValues
     * @param wpObject
     * @param optionCode
     * @return
     */
    public JResponse updateData(DBHandleSource dbHandleSource, ParamsGetter paramsGetter, final Condition condition,
            final NameValues nameValues, WPObject wpObject, int optionCode)
    {
        Dealt dealt = new Dealt()
        {

            @Override
            public Condition getCondition()
            {
                return condition;
            }

            @Override
            public void deal(JResponse jResponse, DBHandle dbHandle, DataAble data,
                    Condition _condition, Object[] otherParams) throws Exception
            {
                int n = dbHandle.update(_condition, nameValues);
                jResponse.setCode(ResultCode.SUCCESS);
                jResponse.setResult(n);
            }
        };

        return commonDealt(dealt, false, dbHandleSource, paramsGetter, wpObject, SetType.UPDATE, optionCode);

    }


    /**
     * 设置Data对象的类变量值
     */
    private String setDataFields(DataAble data, boolean willSetParams, WPObject wpObject, SetType setType,
            int optionCode,
            DBHandleAccess dbHandleAccess)
    {
        String rs = null;
        try
        {
            switch (setType)
            {
                case ADD:
                case REPLACE:
                case UPDATE:
                    if (wpObject != null && willSetParams)
                    {
                        data.setParams(wpObject.inNames.fnNames, wpObject.fns, wpObject.inNames.fuNames, wpObject.fus,null,null);
                        data.setParams(wpObject.inNames.cnNames, wpObject.cns, wpObject.inNames.cuNames, wpObject.cus,wpObject.inNames.innerNames,wpObject.inners);
                    }
                    break;
                default:
                    break;

            }
            data.whenSetDataFinished(setType, optionCode, wpObject, dbHandleAccess);
        } catch (Exception e)
        {
            e.printStackTrace();
            rs = e.toString();
        }
        return rs;
    }


}
