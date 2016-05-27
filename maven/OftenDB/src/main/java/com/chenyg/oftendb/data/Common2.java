package com.chenyg.oftendb.data;

import com.chenyg.oftendb.db.*;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.JResponse;

import java.io.IOException;

/**
 * Created by 刚帅 on 2016/1/19.
 */
public class Common2
{

    public static final Common2 C = new Common2(Common.C);

    private Common common;

    private Common2(Common common)
    {
        this.common = common;
    }


    public static TransactionHandle<Common2> getTransactionHandle(final SourceAndPGetter sourceAndPGeter)
    {
        TransactionHandle<Common2> thandle = new TransactionHandle<Common2>()
        {
            TransactionHandle<Common> transactionHandle = Common.getTransactionHandle(sourceAndPGeter, sourceAndPGeter);
            Common2 common2 = new Common2(transactionHandle.common());

            @Override
            public Common2 common()
            {
                return common2;
            }

            @Override
            public void startTransaction() throws DBException
            {
                transactionHandle.startTransaction();
            }

            @Override
            public void commitTransaction() throws DBException
            {
                transactionHandle.commitTransaction();
            }

            @Override
            public void rollback() throws DBException
            {
                transactionHandle.rollback();
            }

            @Override
            public void close() throws IOException
            {
                transactionHandle.close();
            }
        };

        return thandle;
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, NameValues, WPObject)
     */
    public JResponse addData(SourceAndPGetter sourceAndPGeter, boolean responseData,
            NameValues nameValues, WPObject wpObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, responseData, nameValues, wpObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, boolean, WPObject)
     */
    public JResponse addData(SourceAndPGetter sourceAndPGeter, boolean responseData,
            WPObject wpObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, responseData, wpObject);
    }

    /**
     * @see Common#addData(DBHandleSource, ParamsGetter, MultiNameValues, WPObject)
     */
    public JResponse addData(SourceAndPGetter sourceAndPGeter,
            final MultiNameValues multiNameValues,
            WPObject wpObject)
    {
        return common.addData(sourceAndPGeter, sourceAndPGeter, multiNameValues, wpObject);
    }

    /**
     * @see Common#advancedExecute(DBHandleSource, ParamsGetter, AdvancedExecutor, WPObject)
     */
    public JResponse advancedExecute(SourceAndPGetter sourceAndPGeter,
            AdvancedExecutor advancedExecutor,
            WPObject wpObject)
    {
        return common.advancedExecute(sourceAndPGeter, sourceAndPGeter, advancedExecutor, wpObject);
    }

    /**
     * @see Common#count(DBHandleSource, ParamsGetter, Condition, WPObject)
     */
    public JResponse count(SourceAndPGetter sourceAndPGeter, Condition condition,
            WPObject wpObject)
    {
        return common.count(sourceAndPGeter, sourceAndPGeter, condition, wpObject);
    }

    /**
     * @see Common#deleteData(DBHandleSource, ParamsGetter, Condition, WPObject)
     */
    public JResponse deleteData(SourceAndPGetter sourceAndPGeter, Condition condition,
            WPObject wpObject)
    {
        return common.deleteData(sourceAndPGeter, sourceAndPGeter, condition, wpObject);
    }


    /**
     * @see Common#deleteData2(DBHandleSource, ParamsGetter, ParamsSelection, WPObject)
     */
    public JResponse deleteData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            WPObject wpObject)
    {
        return common.deleteData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, wpObject);
    }


    /**
     * @see Common#exists(DBHandleSource, ParamsGetter, String, Object, WPObject)
     */
    public JResponse exists(SourceAndPGetter sourceAndPGeter, String key,
            Object value, WPObject wpObject)
    {
        return common.exists(sourceAndPGeter, sourceAndPGeter, key, value, wpObject);
    }

    /**
     * @see Common#queryAdvanced(DBHandleSource, ParamsGetter, AdvancedQuery, WPObject)
     */
    public JResponse queryAdvanced(SourceAndPGetter sourceAndPGeter,
            AdvancedQuery advancedQuery, WPObject wpObject)
    {
        return common.queryAdvanced(sourceAndPGeter, sourceAndPGeter, advancedQuery, wpObject);
    }

    /**
     * @see Common#queryData(DBHandleSource, ParamsGetter, Condition, QuerySettings, KeysSelection, WPObject)
     */
    public JResponse queryData(SourceAndPGetter sourceAndPGeter, Condition condition,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject)
    {
        return common.queryData(sourceAndPGeter, sourceAndPGeter, condition, querySettings, keysSelection, wpObject);
    }

    /**
     * @see Common#queryData2(DBHandleSource, ParamsGetter, ParamsSelection, QuerySettings, KeysSelection, WPObject)
     */
    public JResponse queryData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            QuerySettings querySettings, KeysSelection keysSelection, WPObject wpObject)
    {
        return common
                .queryData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, querySettings, keysSelection, wpObject);
    }

    /**
     * @see Common#queryOne(DBHandleSource, ParamsGetter, Condition, KeysSelection, WPObject)
     */
    public JResponse queryOne(SourceAndPGetter sourceAndPGeter, Condition condition,
            KeysSelection keysSelection, WPObject wpObject)
    {
        return common.queryOne(sourceAndPGeter, sourceAndPGeter, condition, keysSelection, wpObject);
    }

    /**
     * @see Common#queryOne2(DBHandleSource, ParamsGetter, ParamsSelection, KeysSelection, WPObject)
     */
    public JResponse queryOne2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            KeysSelection keysSelection, WPObject wpObject)
    {
        return common.queryOne2(sourceAndPGeter, sourceAndPGeter, paramsSelection, keysSelection, wpObject);
    }


    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, WPObject)
     */
    public JResponse replaceData(SourceAndPGetter sourceAndPGeter,
            Condition condition,
            WPObject wpObject)
    {
        return common.replaceData(sourceAndPGeter, sourceAndPGeter, condition, wpObject);
    }

    /**
     * @see Common#replaceData(DBHandleSource, ParamsGetter, Condition, WPObject, NameValues)
     */
    public JResponse replaceData(SourceAndPGetter sourceAndPGeter, Condition condition,
            WPObject wpObject, NameValues nameValues)
    {
        return common.replaceData(sourceAndPGeter, sourceAndPGeter, condition, wpObject, nameValues);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, NameValues, WPObject)
     */
    public JResponse updateData(SourceAndPGetter sourceAndPGeter, Condition condition,
            NameValues nameValues, WPObject wpObject)
    {
        return common.updateData(sourceAndPGeter, sourceAndPGeter, condition, nameValues, wpObject);
    }

    /**
     * @see Common#updateData(DBHandleSource, ParamsGetter, Condition, WPObject)
     */
    public JResponse updateData(SourceAndPGetter sourceAndPGeter, Condition condition,
            WPObject wpObject)
    {
        return common.updateData(sourceAndPGeter, sourceAndPGeter, condition, wpObject);
    }


    /**
     * @see Common#updateData2(DBHandleSource, ParamsGetter, ParamsSelection, WPObject)
     */
    public JResponse updateData2(SourceAndPGetter sourceAndPGeter,
            ParamsSelection paramsSelection,
            WPObject wpObject)
    {
        return common.updateData2(sourceAndPGeter, sourceAndPGeter, paramsSelection, wpObject);
    }

}
