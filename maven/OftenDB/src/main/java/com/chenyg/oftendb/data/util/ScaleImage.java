package com.chenyg.oftendb.data.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;


/**
 * 生成缩略图的，网上找的,依赖于thumbnailator
 *
 * @author Administrator
 */
public class ScaleImage
{


    /**
     * 等比例生成缩略图，高度缩小道到指定值
     *
     * @param fromImageFile 原始图片文件
     * @param saveToFile    保存的图片
     * @param formatHeight  指定的高度
     * @throws IOException
     */
    public static void saveImageAsJpgH(File fromImageFile, File saveToFile, int formatHeight) throws IOException
    {
        BufferedImage bufferedImage = ImageIO.read(fromImageFile);
        int srcW = bufferedImage.getWidth();
        int srcH = bufferedImage.getHeight();

        int changeToWidth = srcW;
        int changeToHeight = srcH;

        if (srcW > 0 && srcH > 0 && formatHeight < srcH)
        {
            changeToHeight = formatHeight;
            changeToWidth = srcW * formatHeight / srcH;
        }

        saveImageAsJpg(fromImageFile, saveToFile, changeToWidth, changeToHeight);

    }


    /**
     * 等比例生成缩略图，宽度度缩小道到指定值
     *
     * @param fromImageFile 原始图片文件
     * @param saveToFile    保存的图片文件
     * @param formatWideth  指定的宽度
     * @throws Exception
     */
    public static void saveImageAsJpgW(File fromImageFile, File saveToFile, int formatWideth) throws IOException
    {
        BufferedImage bufferedImage = ImageIO.read(fromImageFile);
        int srcW = bufferedImage.getWidth();
        int srcH = bufferedImage.getHeight();

        int changeToWidth = srcW;
        int changeToHeight = srcH;

        if (srcW > 0 && srcH > 0 && formatWideth < srcW)
        {
            changeToWidth = formatWideth;
            changeToHeight = srcH * formatWideth / srcW;
        }

        saveImageAsJpg(fromImageFile, saveToFile, changeToWidth, changeToHeight);

    }


    /**
     * @param fromImageFile 原图片
     * @param saveToFile    生成缩略图的原图文件
     * @param dstWidth      生成图片宽度
     * @param dstHeight     生成图片高度
     * @throws IOException
     */
    public static void saveImageAsJpg(File fromImageFile, File saveToFile, int dstWidth,
            int dstHeight) throws IOException
    {
        Thumbnails.of(fromImageFile).size(dstWidth, dstHeight).outputFormat("jpg")
                .toOutputStream(new FileOutputStream(saveToFile));
    }


    /**
     * @param datas      原图片数据
     * @param saveToFile 生成缩略图的原图文件
     * @param dstWidth   生成图片宽度
     * @param dstHeight  生成图片高度
     * @throws IOException
     */
    public static void saveImageAsJpg(byte[] datas, File saveToFile, int dstWidth, int dstHeight) throws IOException
    {
        Thumbnails.of(new ByteArrayInputStream(datas)).size(dstWidth, dstHeight).outputFormat("jpg")
                .toOutputStream(new FileOutputStream(saveToFile));
    }

}
