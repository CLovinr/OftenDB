package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.base.JResponse;
import com.chenyg.wporter.base.ResultCode;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by 刚帅 on 2016/1/11.
 */
public class VcodeUser implements Serializable
{

    private static final long serialVersionUID = 1L;

    private static VcodeSaver vcodeSaver;


    static void init(VcodeSaver vcodeSaver)
    {
        VcodeUser.vcodeSaver = vcodeSaver;
    }

    public static class VcodeResult
    {
        /**
         * 0验证码有效，1验证码不存在,2验证码超时
         */
        public int result;
        public Vcode vcode;

        VcodeResult(int result)
        {

        }
    }

    /**
     * 用于存放验证码
     */
    private HashMap<String, Vcode> vcodeMap;
    private int maxSize;

    public VcodeUser(int initCapacity, int maxSize)
    {
        vcodeMap = new HashMap<String, Vcode>(initCapacity);
        this.maxSize = maxSize;
    }

    /**
     * 存放验证码
     *
     * @param tag   标记
     * @param vcode 验证码
     */
    public synchronized void putVCode(String tag, Vcode vcode)
    {
        vcodeMap.put(tag, vcode);

        if (vcodeMap.size() > maxSize)
        {
            String removeTag = tag;
            long time = vcode.time;
            Iterator<String> tags = vcodeMap.keySet().iterator();
            while (tags.hasNext())
            {
                String key = tags.next();
                Vcode code = vcodeMap.get(key);
                if (code.time < time)
                {
                    time = code.time;
                    removeTag = key;
                }
            }
            if (!tag.equals(removeTag))
            {
                vcodeMap.remove(removeTag);
            }
        }
    }

    /**
     * 得到验证码用途,超时才会移除验证码
     *
     * @param tag     标记
     * @param timeout 验证码超时时间
     * @return 验证码用途
     */
    public synchronized VcodeResult popVcode(String tag, int timeout)
    {
        VcodeResult vcodeResult = new VcodeResult(1);

        Vcode vcode = vcodeMap.get(tag);

        if (vcode != null)
        {
            if ((System.currentTimeMillis() - vcode.time) > timeout)
            {
                vcodeMap.remove(tag);
                vcodeResult.result = 2;
            } else
            {
                vcodeResult.result = 0;
                vcodeResult.vcode = vcode;
            }

        }

        return vcodeResult;
    }


    /**
     * 验证验证码
     */
    static JResponse checkVcode(WPObject wpObject, String tag,
            int timeout, String vcodeStr)
    {
        JResponse jResponse = new JResponse(ResultCode.OK_BUT_FAILED);

        VcodeUser vcodeUser = getVcodeUser(wpObject, false, 0);
        if (vcodeUser != null)
        {
            VcodeResult vcodeResult = vcodeUser.popVcode(tag, timeout);
            switch (vcodeResult.result)
            {
                case 0:
                {
                    Vcode vcode = vcodeResult.vcode;
                    if (vcode.vcode.equalsIgnoreCase(vcodeStr))
                    {
                        vcodeSaver.remove(wpObject);
                        jResponse.setCode(ResultCode.SUCCESS);
                    } else
                    {
                        jResponse.setDescription("验证码错误！");
                    }
                }
                break;
                case 1:
                {
                    jResponse.setDescription("验证码无效,请更换验证码(1)");
                }
                break;
                default:
                {
                    jResponse.setDescription("验证码超时,请更换验证码");
                }
            }

            if (vcodeUser.vcodeMap.size() == 0)
            {
                vcodeSaver.remove(wpObject);
            }
        } else
        {
            jResponse.setDescription("验证码无效,请更换验证码(else)");
        }

        return jResponse;
    }

    public static void putVcodeUser(VcodeUser vcodeUser, WPObject wpObject)
    {
        vcodeSaver.put(vcodeUser, wpObject);
    }

    public static VcodeUser
    getVcodeUser(WPObject wpObject, boolean createIfNull, int maxSize)
    {
        VcodeUser vcodeUser = vcodeSaver.get(wpObject);
        if (vcodeUser == null && createIfNull)
        {
            vcodeUser = new VcodeUser(maxSize >= 2 ? 2 : 1, maxSize);
        }
        return vcodeUser;
    }
}

