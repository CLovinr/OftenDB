package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;
import com.chenyg.wporter.util.WPTool;


/**
 * 基于会话的存储。
 * Created by 刚帅 on 2016/1/11.
 */
public class SessionVcodeSaver implements VcodeSaver
{
    public static final String SESSION_KEY = "s-vcode-saver";

    private String keyName;

    public SessionVcodeSaver()
    {
        this.keyName = SESSION_KEY;
    }

    /**
     * @param attributeKey 若为null，则使用默认的{@linkplain #SESSION_KEY}。
     */
    public SessionVcodeSaver(String attributeKey)
    {
        this.keyName = WPTool.isEmpty(attributeKey) ? SESSION_KEY : attributeKey;
    }


    @Override
    public void put(VcodeUser vcodeUser, WPObject wpObject)
    {
        wpObject.getSession().setAttribute(keyName, vcodeUser);
    }

    @Override
    public VcodeUser get(WPObject wpObject)
    {
        return (VcodeUser) wpObject.getSession().getAttribute(keyName);
    }

    @Override
    public void remove(WPObject wpObject)
    {
        wpObject.getSession().removeAttribute(keyName);
    }
}
