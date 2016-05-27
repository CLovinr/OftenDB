package com.chenyg.oftendb.data.vcode;

import com.chenyg.oftendb.data.DBHandleSource;
import com.chenyg.oftendb.data.Data;
import com.chenyg.oftendb.data.DataPorter;
import com.chenyg.wporter.InCheckParser;
import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.annotation.ChildIn;
import com.chenyg.wporter.annotation.ChildOut;
import com.chenyg.wporter.annotation.ParamExtra;
import com.chenyg.wporter.base.CheckType;
import com.chenyg.wporter.base.JResponse;
import com.chenyg.wporter.base.RequestMethod;
import com.chenyg.wporter.base.ResponseType;

import java.lang.annotation.Annotation;

/**
 * 含有验证码功能的接口。
 * Created by 刚帅 on 2016/1/11.
 */
public class VcodeDataPorter extends DataPorter
{

    private CommonMethodIns commonMethodIns;

    public VcodeDataPorter(InCheckParser inCheckParser,
            Class<? extends Data> dataClass, DBHandleSource dbHandleSource, VcodeParams vcodeParams)
    {
        super(inCheckParser, dataClass, dbHandleSource);
        init(vcodeParams);
    }

    public VcodeDataPorter(InCheckParser inCheckParser, Class<? extends Annotation> key,
            Class<? extends Data> dataClass, DBHandleSource dbHandleSource, VcodeParams vcodeParams)
    {
        super(inCheckParser, key, dataClass, dbHandleSource);
        init(vcodeParams);
    }


    private void init(VcodeParams vcodeParams)
    {
        commonMethodIns = new CommonMethodIns(vcodeParams);
    }

    /**
     * 得到验证码,GET,tag
     *
     * @param wpObject
     */
    @ChildIn(tiedName = "", neceParams = {"tag"}, method = RequestMethod.GET,
            checkType = CheckType.NONE)
    @ChildOut(ResponseType.SelfResponse)
    public void vcode(WPObject wpObject) throws VcodeHandleException
    {
        commonMethodIns.vcode(wpObject, (String) wpObject.cns[0]);
    }

    /**
     * 检测验证码
     *
     * @param wpObject
     * @param tag
     * @param vcodeStr
     */
    protected JResponse checkVcode(WPObject wpObject, String tag, String vcodeStr)
    {
        return commonMethodIns.checkVcode(wpObject, tag, vcodeStr);
    }


}
