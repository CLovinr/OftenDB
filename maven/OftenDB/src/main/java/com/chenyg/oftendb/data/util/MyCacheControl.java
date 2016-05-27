package com.chenyg.oftendb.data.util;

import com.chenyg.wporter.WPRequest;
import com.chenyg.wporter.WPResponse;

/**
 * 控制缓存
 *
 * @author ZhuiFeng
 */
public class MyCacheControl
{

    /**
     * 检测资源是否过期，若过期了则重新设置缓存时间.
     *
     * @param sec                   秒
     * @param modelLastModifiedDate 上次修改日期
     * @param request
     * @param response
     * @return true：发送数据；false：不发送.
     */
    public static boolean checkHeaderCache(long sec, long modelLastModifiedDate, WPRequest request, WPResponse response)
    {

        long adddaysM = sec * 1000;
        String dateString = request.getHeader("If-Modified-Since");
        long header = dateString == null ? -1 : Long.parseLong(dateString);
        long now = System.currentTimeMillis();
        if (header > 0 && adddaysM > 0)
        {
            if (modelLastModifiedDate > header)
            {
                response.setStatus(200);
                return true;
            }
            if (header + adddaysM > now)
            {
                response.setStatus(304);
                return false;
            }
        }

        String previousToken = request.getHeader("If-None-Match");
        if (previousToken != null && previousToken.equals(Long.toString(modelLastModifiedDate)))
        {
            response.setStatus(304);
            return false;
        }
        response.setHeader("ETag", Long.toString(modelLastModifiedDate));
        setRespHeaderCache(sec, request, response);
        return true;

    }

    /**
     * 设置资源缓存时间
     *
     * @param sec      秒
     * @param request
     * @param response
     * @return
     */
    public static boolean setRespHeaderCache(long sec, WPRequest request, WPResponse response)
    {

        long adddaysM = sec * 1000;
        String maxAgeDirective = "max-age=" + sec;
        response.setHeader("Cache-Control", maxAgeDirective);
        response.setStatus(200);
        long time = System.currentTimeMillis();
        response.addHeader("Last-Modified", time + "");
        response.addHeader("Expires", (time + adddaysM) + "");
        return true;
    }
}
