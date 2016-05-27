package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.JResponse;

/**
 * Created by 刚帅 on 2016/1/11.
 */
interface CommonMethod
{
    void vcode(WPObject wpObject, String tag) throws VcodeHandleException;

    JResponse checkVcode(WPObject wpObject, String tag, String vcodeStr);
}
