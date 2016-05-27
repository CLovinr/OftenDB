package com.chenyg.oftendb.data.vcode;

/**
 * Created by 刚帅 on 2016/1/11.
 */
public class VcodeParams
{
    VcodeHandle vcodeHandle;
    int vcodeTimeout;
    int maxSize;

    /**
     *
     * @param vcodeSaver
     * @param vcodeHandle
     * @param vcodeTimeout 验证码超时时间（毫秒）
     * @param maxSize 一个用户最多的验证码数量
     */
    public VcodeParams(VcodeSaver vcodeSaver, VcodeHandle vcodeHandle, int vcodeTimeout,int maxSize)
    {
        VcodeUser.init(vcodeSaver);
        this.vcodeHandle = vcodeHandle;
        this.vcodeTimeout = vcodeTimeout;
        this.maxSize=maxSize;
    }
}

