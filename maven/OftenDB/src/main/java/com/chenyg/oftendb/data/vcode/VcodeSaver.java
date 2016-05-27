package com.chenyg.oftendb.data.vcode;

import com.chenyg.wporter.WPObject;

/**
 * Created by 刚帅 on 2016/1/11.
 */
public interface VcodeSaver
{
    void put(VcodeUser vcodeUser, WPObject wpObject);
    VcodeUser get(WPObject wpObject);
    void remove(WPObject wpObject);
}
