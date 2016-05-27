package com.chenyg.oftendb.data;


import com.chenyg.wporter.base.JResponse;

public interface SimpleDealt
{
   void deal(JResponse jResponse, Object... objects)throws Exception;
   void onException(Exception e, JResponse jResponse, Object... objects);
}
