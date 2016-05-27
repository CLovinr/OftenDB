package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;

/**
 * Created by 刚帅 on 2016/1/11.
 */
public interface VcodeHandle
{
    /**
     * 输出验证码。
     *
     * @param wpObject
     * @return 验证码内容
     * @throws VcodeHandleException
     */
    String writeImage(WPObject wpObject) throws VcodeHandleException;
}
