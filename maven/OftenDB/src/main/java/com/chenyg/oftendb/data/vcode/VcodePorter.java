package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.WebPorter;
import com.chenyg.wporter.annotation.ChildIn;
import com.chenyg.wporter.annotation.ChildOut;
import com.chenyg.wporter.base.CheckType;
import com.chenyg.wporter.base.JResponse;
import com.chenyg.wporter.base.RequestMethod;
import com.chenyg.wporter.base.ResponseType;

/**
 * 含有验证码功能的接口。
 * Created by 刚帅 on 2016/1/11.
 */
public class VcodePorter extends WebPorter
{

    private CommonMethodIns commonMethodIns;

    public VcodePorter(VcodeParams vcodeParams)
    {
        init(vcodeParams);
    }

    private void init(VcodeParams vcodeParams)
    {
        commonMethodIns = new CommonMethodIns(vcodeParams);
    }

    /**
     * 得到验证码的接口,GET请求,需要tag参数，如：http://localhost/WPort/Vcode/vcode?tag=REG
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
     * 检测验证码,结果码为成功表示验证通过。
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
