package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.JResponse;

/**
 * Created by 刚帅 on 2016/1/11.
 */
class CommonMethodIns implements CommonMethod
{
    private VcodeParams vcodeParams;

    public CommonMethodIns(VcodeParams vcodeParams)
    {
        this.vcodeParams = vcodeParams;
    }

    @Override
    public void vcode(WPObject wpObject, String tag) throws VcodeHandleException
    {
        try
        {
            VcodeUser vcodeUser = VcodeUser.getVcodeUser(wpObject, true, vcodeParams.maxSize);
            String vcode = vcodeParams.vcodeHandle.writeImage(wpObject);
            if (vcode != null)
            {
                vcodeUser.putVCode(tag, new Vcode(vcode));
                VcodeUser.putVcodeUser(vcodeUser, wpObject);
            }

        } catch (VcodeHandleException e)
        {
            throw e;
        }
    }

    @Override
    public JResponse checkVcode(WPObject wpObject, String tag, String vcodeStr)
    {
        return VcodeUser.checkVcode(wpObject, tag, vcodeParams.vcodeTimeout, vcodeStr);
    }
}
