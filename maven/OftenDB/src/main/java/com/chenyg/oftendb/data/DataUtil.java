package com.chenyg.oftendb.data;


import com.chenyg.oftendb.data.ParamsGetter.Params;
import com.chenyg.oftendb.db.NameValues;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.JResponse;
import com.chenyg.wporter.base.ResultCode;
import org.json.JSONException;
import org.json.JSONObject;

public class DataUtil
{

    /**
     * 构造一个ParamsGeter.
     *
     * @param params
     * @return
     */
    public static ParamsGetter newParamsGetter(final Params params)
    {
        ParamsGetter paramsGetter = new ParamsGetter()
        {

            @Override
            public Params getParams()
            {
                return params;
            }
        };

        return paramsGetter;
    }


    public static ParamsGetter newParamsGetter(DataAble dataAble)
    {
        Params params = new Params(dataAble);
        return newParamsGetter(params);
    }

    public static ParamsGetter newParamsGetter(Class<? extends DataAble> c)
    {
        Params params = new Params(c);
        return newParamsGetter(params);
    }


    /**
     * 从WPObject参数中构造一个Data（必须有无参构造函数）
     *
     * @param dataClass
     * @param wpObject
     * @return
     * @throws NewDataException
     */
    public <T extends DataAble> T createData(Class<T> dataClass, WPObject wpObject) throws NewDataException
    {
        try
        {
            T t = dataClass.newInstance();
            t.setParams(wpObject.inNames.cnNames, wpObject.cns, wpObject.inNames.cuNames, wpObject.cus,wpObject.inNames.innerNames,wpObject.inners);
            t.whenSetDataFinished(SetType.CREATE, Data.OPTION_CODE_DEFAULT, wpObject, null);
            return t;
        } catch (Exception e)
        {
            throw new NewDataException(e.toString());
        }

    }

    /**
     * @param dataAble 默认情况下，null值的类变量不会被添加,除非Key.nullSetOrAdd==true.
     * @return
     * @throws Exception
     */
    public static NameValues toNameValues(Params params,
            DataAble dataAble) throws Exception
    {
        return dataAble.toNameValues(params);
    }

    /**
     * 若结果码为成功，且结果为JSONObject(不为null)时返回true.
     */
    public static boolean isResultJSON(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            Object object = jResponse.getResult();
            if (object != null && (object instanceof JSONObject))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 返回-1:结果码不为成功，0：结果码为成功且结果为null，1：结果码为成功且结果不为null。
     *
     * @param jResponse
     * @return
     */
    public static int checkResult(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            if (jResponse.getResult() == null)
            {
                return 0;
            } else
            {
                return 1;
            }
        } else
        {
            return -1;
        }
    }

    /**
     * 当且仅当结果码为成功，且结果为true时返回真；若结果码为成功而结果不为Boolean型，会出现异常。
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean resultTrue(JResponse jResponse)
    {
        Object rs = jResponse.getResult();
        if (jResponse.getCode() == ResultCode.SUCCESS && (Boolean) rs)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码为成功且结果不为null时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean notNull(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS && jResponse.getResult() != null)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码不为成功时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean notSuccess(JResponse jResponse)
    {
        if (jResponse.getCode() != ResultCode.SUCCESS)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 当且仅当结果码为成功时返回true
     *
     * @param jResponse JSONResponse
     * @return 判断结果
     */
    public static boolean success(JResponse jResponse)
    {
        if (jResponse.getCode() == ResultCode.SUCCESS)
        {
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * 通过扫描@key注解，结合KeysSelection，得到选择的字段。
     */
    public static String[] getKeys(KeysSelection keysSelection, ParamsGetter paramsGetter)
    {
        Params params = paramsGetter.getParams();
        DataAble dataAble = null;
        try
        {
            dataAble = params.getDataAble();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return dataAble.getFinalKeys(keysSelection, params);
    }

    /**
     * 把cns和cus转换成json对象
     *
     * @param wpObject
     * @param wpObject
     * @param containsNull 是否包含null值键值对
     * @return
     */
    public static JSONObject toJsonObject(WPObject wpObject, boolean containsNull)
    {
        JSONObject jsonObject = new JSONObject();

        try
        {
            String[] names = wpObject.inNames.cnNames;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wpObject.cns[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i], wpObject.cns[i]);
            }
            names = wpObject.inNames.cuNames;
            for (int i = 0; i < names.length; i++)
            {
                if (!containsNull && wpObject.cus[i] == null)
                {
                    continue;
                }
                jsonObject.put(names[i], wpObject.cus[i]);
            }
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        return jsonObject;
    }


    public JResponse simpleDeal(SimpleDealt simpleDealt, Object... objects)
    {
        JResponse jResponse = new JResponse();

        try
        {
            simpleDealt.deal(jResponse, objects);
            jResponse.setCode(ResultCode.SUCCESS);
        } catch (Exception e)
        {
            jResponse.setCode(ResultCode.SERVER_EXCEPTION);
            jResponse.setDescription(e.toString());
            simpleDealt.onException(e, jResponse, objects);
        }

        return jResponse;
    }


}
