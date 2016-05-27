package com.chenyg.oftendb.data.vcode;

import java.io.Serializable;

/**
 * Created by 刚帅 on 2016/1/11.
 */
public class Vcode implements Serializable
{
    private static final long serialVersionUID = 1L;
    public long time;
    public String vcode;

    public Vcode(String vcode)
    {
        this.vcode = vcode;
        this.time = System.currentTimeMillis();
    }
}
