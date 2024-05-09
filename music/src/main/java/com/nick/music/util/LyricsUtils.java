package com.nick.music.util;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;


import com.nick.music.model.LyricsInfo;
import com.nick.music.model.LyricsLineInfo;
import com.nick.music.model.TranslateLrcLineInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;

/**
 * 歌词处理类
 * Created by zhangliangming on 2018-02-23.
 */

public class LyricsUtils {

    /**
     * 绘画单行动感歌词行
     *
     * @param canvas
     * @param lyricsType         歌词类型
     * @param paint              默认画笔
     * @param paintHL            高亮画笔
     * @param paintOutline       轮廓画笔
     * @param lyricsLineInfo     行歌词数据
     * @param lyricsLineHLWidth  歌词行高亮宽度
     * @param viewWidth          视频宽度
     * @param lyricsWordIndex    行歌词字索引
     * @param lyricsWordHLTime   行歌词高亮时间
     * @param textY              y轴位置
     * @param paddingLeftOrRight
     * @param paintColor
     * @param paintHLColor
     */
    public static void drawDynamiLyrics(Canvas canvas, int lyricsType, Paint paint, Paint paintHL, Paint paintOutline, LyricsLineInfo lyricsLineInfo, float lyricsLineHLWidth, int viewWidth, int lyricsWordIndex, float lyricsWordHLTime, float textY, float paddingLeftOrRight, int[] paintColor, int[] paintHLColor) {
        //获取行歌词高亮宽度
        lyricsLineHLWidth = getLineLyricsHLWidth(lyricsType, paint, lyricsLineInfo, lyricsWordIndex, lyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(paint, curLyrics);
        // 当前歌词行的x坐标
        float textX = getHLMoveTextX(curLrcTextWidth, lyricsLineHLWidth, viewWidth, paddingLeftOrRight);
        drawOutline(canvas, paintOutline, curLyrics, textX, textY);
        drawDynamicText(canvas, paint, paintHL, paintColor, paintHLColor, curLyrics, lyricsLineHLWidth, textX, textY);
    }

    /**
     * 获取高亮移动的x位置（注：该方法在歌词不换行时使用）
     *
     * @param curLrcTextWidth    当前行宽度
     * @param lineLyricsHLWidth  当前行高亮歌词宽度
     * @param viewWidth          视图宽度
     * @param paddingLeftOrRight 左右间隔距离
     * @return
     */
    public static float getHLMoveTextX(float curLrcTextWidth, float lineLyricsHLWidth, int viewWidth, float paddingLeftOrRight) {
        float textX = 0;
        if (curLrcTextWidth > viewWidth) {
            if (lineLyricsHLWidth >= viewWidth / 2) {
                if ((curLrcTextWidth - lineLyricsHLWidth) >= viewWidth / 2) {
                    textX = (viewWidth / 2 - lineLyricsHLWidth);
                } else {
                    textX = viewWidth - curLrcTextWidth
                            - paddingLeftOrRight;
                }
            } else {
                textX = paddingLeftOrRight;
            }
        } else {
            // 如果歌词宽度小于view的宽
            textX = (viewWidth - curLrcTextWidth) / 2;
        }
        return textX;
    }

    /**
     * 获取高亮移动的x位置（注：该方法在歌词不换行时使用）
     *
     * @param curLrcTextWidth    当前行宽度
     * @param lineLyricsHLWidth  当前行高亮歌词宽度
     * @param viewWidth          视图宽度
     * @param paddingLeftOrRight 左右间隔距离
     * @return
     */
    public static float getFristLrcMoveTextX(float curLrcTextWidth, float lineLyricsHLWidth, int viewWidth, float paddingLeftOrRight) {
        float textX = 0;
        if (curLrcTextWidth > viewWidth) {
            if (lineLyricsHLWidth >= viewWidth / 2) {
                if ((curLrcTextWidth - lineLyricsHLWidth) >= viewWidth / 2) {
                    textX = (viewWidth / 2 - lineLyricsHLWidth);
                } else {
                    textX = viewWidth - curLrcTextWidth
                            - paddingLeftOrRight;
                }
            } else {
                textX = paddingLeftOrRight;
            }
        } else {
            // 居左
            textX = paddingLeftOrRight;
        }
        return textX;
    }


    /**
     * 获取高亮移动的x位置（注：该方法在歌词不换行时使用）
     *
     * @param curLrcTextWidth    当前行宽度
     * @param lineLyricsHLWidth  当前行高亮歌词宽度
     * @param viewWidth          视图宽度
     * @param paddingLeftOrRight 左右间隔距离
     * @return
     */
    public static float getSecondLrcMoveTextX(float curLrcTextWidth, float lineLyricsHLWidth, int viewWidth, float paddingLeftOrRight) {
        float textX = 0;
        if (curLrcTextWidth > viewWidth) {
            if (lineLyricsHLWidth >= viewWidth / 2) {
                if ((curLrcTextWidth - lineLyricsHLWidth) >= viewWidth / 2) {
                    textX = (viewWidth / 2 - lineLyricsHLWidth);
                } else {
                    textX = viewWidth - curLrcTextWidth
                            - paddingLeftOrRight;
                }
            } else {
                textX = paddingLeftOrRight;
            }
        } else {
            textX = viewWidth - curLrcTextWidth
                    - paddingLeftOrRight;
        }
        return textX;
    }


    /**
     * 获取行歌词高亮的宽度
     *
     * @param paint
     * @param lyricsLineInfo
     * @param lyricsWordIndex
     * @param lyricsWordHLTime
     * @return
     */
    public static float getLineLyricsHLWidth(int lyricsType, Paint paint, LyricsLineInfo lyricsLineInfo, int lyricsWordIndex, float lyricsWordHLTime) {
        float lineLyricsHLWidth = 0;

        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = LyricsUtils.getTextWidth(paint, curLyrics);
        if (lyricsType == LyricsInfo.LRC || lyricsWordIndex == -2) {
            // 整行歌词
            lineLyricsHLWidth = curLrcTextWidth;
        } else {
            if (lyricsWordIndex != -1) {
                String lyricsWords[] = lyricsLineInfo.getLyricsWords();
                long wordsDisInterval[] = lyricsLineInfo
                        .getWordsDisInterval();
                // 当前歌词之前的歌词
                StringBuilder lyricsBeforeWord = new StringBuilder();
                for (int i = 0; i < lyricsWordIndex; i++) {
                    lyricsBeforeWord.append(lyricsWords[i]);
                }
                // 当前歌词字
                String lrcNowWord = lyricsWords[lyricsWordIndex].trim();// 去掉空格
                // 当前歌词之前的歌词长度
                float lyricsBeforeWordWidth = paint
                        .measureText(lyricsBeforeWord.toString());

                // 当前歌词长度
                float lyricsNowWordWidth = paint.measureText(lrcNowWord);

                float len = lyricsNowWordWidth
                        / wordsDisInterval[lyricsWordIndex]
                        * lyricsWordHLTime;
                lineLyricsHLWidth = lyricsBeforeWordWidth + len;
            }
        }

        return lineLyricsHLWidth;
    }

    /**
     * 获取lrc行歌词高亮的宽度
     *
     * @param paint
     * @param lrcLineInfos
     * @param lyricsLineInfo
     * @param lyricsLineNum
     * @param lyricsWordHLTime
     * @return
     */
    public static float getLrcHLWidth(Paint paint, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, LyricsLineInfo lyricsLineInfo, int lyricsLineNum, float lyricsWordHLTime) {
        float lineLyricsHLWidth = 0;
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = LyricsUtils.getTextWidth(paint, curLyrics);
        long startTime = lyricsLineInfo.getStartTime();
        long endTime = 0;
        if (lyricsLineNum + 1 < lrcLineInfos.size()) {
            endTime = lrcLineInfos.get(lyricsLineNum + 1).getStartTime();
        } else {
            //最后一行歌词，没办法计算时间长度
            endTime = startTime;
        }
        long sumTime = endTime - startTime;
        if (sumTime == 0) return 0;
        lineLyricsHLWidth = lyricsWordHLTime / sumTime * curLrcTextWidth;
        return lineLyricsHLWidth;
    }


    /**
     * 绘画动感文本
     *
     * @param canvas
     * @param paint   默认画笔
     * @param paintHL 高亮画笔
     * @param text    文本
     * @param hlWidth 高亮宽度
     * @param x
     * @param y
     */
    public static void drawDynamicText(Canvas canvas, Paint paint, Paint paintHL, int[] paintColor, int[] paintHLColor, String text, float hlWidth, float x, float y) {
        canvas.save();

        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, paintColor, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
        //设置动感歌词过渡效果
        canvas.clipRect(x, y - getRealTextHeight(paint), x + hlWidth,
                y + getRealTextHeight(paint));

        //设置为上下渐变
        LinearGradient linearGradientHL = new LinearGradient(x, y - getTextHeight(paint), x, y, paintHLColor, null, Shader.TileMode.CLAMP);
        paintHL.setShader(linearGradientHL);
        canvas.drawText(text, x, y, paintHL);
        canvas.restore();
    }

    /**
     * 绘画动感文本
     *
     * @param canvas
     * @param paint   默认画笔
     * @param paintHL 高亮画笔
     * @param text    文本
     * @param hlWidth 高亮宽度
     * @param x
     * @param y
     */
    public static void drawDynamicText(Canvas canvas, Paint paint, Paint paintHL, String text, float hlWidth, float x, float y) {
        canvas.save();
        canvas.drawText(text, x, y, paint);
        //设置动感歌词过渡效果
        canvas.clipRect(x, y - getRealTextHeight(paint), x + hlWidth,
                y + getRealTextHeight(paint));
        canvas.drawText(text, x, y, paintHL);
        canvas.restore();
    }

    /**
     * 描绘轮廓
     *
     * @param canvas
     * @param text
     * @param x
     * @param y
     */
    public static void drawOutline(Canvas canvas, Paint paint, String text, float x, float y) {
        canvas.drawText(text, x - 1, y, paint);
        canvas.drawText(text, x + 1, y, paint);
        canvas.drawText(text, x, y + 1, paint);
        canvas.drawText(text, x, y - 1, paint);
    }


    /**
     * 绘画文本
     *
     * @param canvas
     * @param paint
     * @param paintColor
     * @param text
     * @param x
     * @param y
     */
    public static void drawText(Canvas canvas, Paint paint, int[] paintColor, String text, float x, float y) {
        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, paintColor, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    public static int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public static int getTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) -(fm.ascent + fm.descent);
    }

    /**
     * 获取文本宽度
     *
     * @param paint
     * @param text
     * @return
     */
    public static float getTextWidth(Paint paint, String text) {
        return paint
                .measureText(text);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 获取每个字的时间，翻译歌词的字时间为平均时间
     *
     * @param defLyricsLineInfo
     * @param newLyricsWords
     * @return
     */
    private static int[] getWordsDisInterval(LyricsLineInfo defLyricsLineInfo, String[] newLyricsWords) {
        if (newLyricsWords.length == 0) return null;
        int[] wordsDisInterval = new int[newLyricsWords.length];
        int sumTime = 0;
        for (int i = 0; i < defLyricsLineInfo.getWordsDisInterval().length; i++) {
            sumTime += defLyricsLineInfo.getWordsDisInterval()[i];
        }
        int avgTime = sumTime / wordsDisInterval.length;
        for (int i = 0; i < wordsDisInterval.length; i++) {
            wordsDisInterval[i] = avgTime;
        }
        return wordsDisInterval;
    }


    /**
     * 通过播放的进度，获取所唱歌词行数
     *
     * @param lyricsType        歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lyricsLineTreeMap 歌词集合
     * @param curPlayingTime    当前播放进度
     * @param playOffset        时间补偿值
     * @return
     */
    public static int getLineNumber(int lyricsType, TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, long curPlayingTime, long playOffset) {

        //添加歌词增量
        long newPlayingTime = curPlayingTime + playOffset;
        if (lyricsType == LyricsInfo.LRC) {
            //lrc歌词
            for (int i = 0; i < lyricsLineTreeMap.size(); i++) {

                if (newPlayingTime < lyricsLineTreeMap.get(i).getStartTime()) return 0;

                if (newPlayingTime >= lyricsLineTreeMap.get(i).getStartTime()
                        && i + 1 < lyricsLineTreeMap.size()
                        && newPlayingTime <= lyricsLineTreeMap.get(i + 1).getStartTime()) {
                    return i;
                }
            }
            if (lyricsLineTreeMap.size() > 0) {
                return lyricsLineTreeMap.size() - 1;
            }
        } else if (lyricsType == LyricsInfo.DYNAMIC) {
            //动感歌词
            for (int i = 0; i < lyricsLineTreeMap.size(); i++) {
                if (newPlayingTime >= lyricsLineTreeMap.get(i).getStartTime()
                        && newPlayingTime <= lyricsLineTreeMap.get(i).getEndTime()) {
                    return i;
                }
                if (newPlayingTime > lyricsLineTreeMap.get(i).getEndTime()
                        && i + 1 < lyricsLineTreeMap.size()
                        && newPlayingTime <= lyricsLineTreeMap.get(i + 1).getStartTime()) {
                    return i;
                }
            }
            if (newPlayingTime >= lyricsLineTreeMap.get(lyricsLineTreeMap.size() - 1)
                    .getEndTime()) {
                return lyricsLineTreeMap.size() - 1;
            }
        }
        return 0;
    }


    /**
     * 获取当前时间对应的行歌词文本
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static String getLineLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return null;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return null;

        LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(lyricsLineNum);

        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }

    /**
     * 获取分割后歌词的当前时间对应的行歌词文本
     *
     * @param lyricsType     歌词类型 LyricsInfo.LRC OR LyricsInfo.DYNAMIC
     * @param lrcLineInfos   歌词集合
     * @param curPlayingTime 当前播放进度
     * @param playOffset     时间补偿值
     * @return
     */
    public static String getSplitLineLrc(int lyricsType, TreeMap<Integer, LyricsLineInfo> lrcLineInfos, long curPlayingTime, long playOffset) {
        if (lrcLineInfos == null) return null;
        int lyricsLineNum = getLineNumber(lyricsType, lrcLineInfos, curPlayingTime, playOffset);
        if (lyricsLineNum >= lrcLineInfos.size())
            return null;
        int splitLyricsLineNum = -1;
        if (lyricsType == LyricsInfo.DYNAMIC) {
            splitLyricsLineNum = getSplitDynamicLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        } else {
            splitLyricsLineNum = getSplitLrcLyricsLineNum(lrcLineInfos, lyricsLineNum, curPlayingTime, playOffset);
        }

        List<LyricsLineInfo> splitLyricsLineInfos = lrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
        if (splitLyricsLineNum == -1 || splitLyricsLineNum >= splitLyricsLineInfos.size()) {
            return null;
        }

        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(splitLyricsLineNum);

        if (lyricsLineInfo == null)
            return null;
        return lyricsLineInfo.getLineLyrics();
    }


    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     */
    private static void splitLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth) {

        final List<LyricsLineInfo> lyricsLineInfos = new ArrayList<LyricsLineInfo>();
        splitLineLyrics(lyricsLineInfo, paint, textMaxWidth, new ForeachListener() {
            @Override
            public void foreach(LyricsLineInfo mLyricsLineInfo) {
                lyricsLineInfos.add(mLyricsLineInfo);
            }
        });

        lyricsLineInfo.setSplitLyricsLineInfos(lyricsLineInfos);

    }

    /**
     * 分割歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     * @param foreachListener
     */
    private static void splitLineLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth, ForeachListener foreachListener) {
        String lineLyrics = lyricsLineInfo.getLineLyrics().trim();
        // 行歌词数组
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        float maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lyricsWords.length; i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lyricsWords[i]);
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lyricsWords.length) {
                    nextLyricsWordWidth = (int) paint.measureText(lyricsWords[(i + 1)]);
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, i);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lyricsWords.length) {
                        startIndex = lyricsWords.length - 1;
                    }
                } else if (i == lyricsWords.length - 1) {
                    LyricsLineInfo newLyricsLineInfo = getNewLyricsLineInfo(
                            lyricsLineInfo, startIndex, lyricsWords.length - 1);

                    if (newLyricsLineInfo != null && foreachListener != null) {
                        foreachListener.foreach(newLyricsLineInfo);
                    }
                }
            }

        } else {
            if (foreachListener != null) {
                foreachListener.foreach(lyricsLineInfo);
            }
        }
    }

    /**
     * 分割lrc歌词
     *
     * @param lyricsLineInfo
     * @param paint
     * @param textMaxWidth
     */
    public static void splitLrcLyrics(LyricsLineInfo lyricsLineInfo, Paint paint, float textMaxWidth) {
        List<LyricsLineInfo> lyricsLineInfos = new ArrayList<>();
        String lineLyrics = lyricsLineInfo.getLineLyrics().trim();
        // 每行的歌词长度
        int lineWidth = (int) paint.measureText(lineLyrics);
        float maxLineWidth = textMaxWidth;
        if (lineWidth > maxLineWidth) {

            int lyricsWordsWidth = 0;
            //开始索引和结束索引
            int startIndex = 0;
            for (int i = 0; i < lineLyrics.length(); i++) {
                // 当前的歌词宽度
                lyricsWordsWidth += (int) paint.measureText(lineLyrics.charAt(i) + "");
                //下一个字的宽度
                int nextLyricsWordWidth = 0;
                if ((i + 1) < lineLyrics.length()) {
                    nextLyricsWordWidth = (int) paint.measureText(lineLyrics.charAt(i + 1) + "");
                }
                if (lyricsWordsWidth + nextLyricsWordWidth > maxLineWidth) {

                    LyricsLineInfo newLyricsLineInfo = getNewLrcLyricsLineInfo(
                            lyricsLineInfo, startIndex, i);

                    if (newLyricsLineInfo != null) {
                        lyricsLineInfos.add(newLyricsLineInfo);
                    }

                    //
                    lyricsWordsWidth = 0;
                    startIndex = i + 1;
                    if (startIndex == lineLyrics.length()) {
                        startIndex = lineLyrics.length() - 1;
                    }
                } else if (i == lineLyrics.length() - 1) {
                    LyricsLineInfo newLyricsLineInfo = getNewLrcLyricsLineInfo(
                            lyricsLineInfo, startIndex, lineLyrics.length() - 1);

                    if (newLyricsLineInfo != null) {
                        lyricsLineInfos.add(newLyricsLineInfo);
                    }
                }
            }

        } else {
            lyricsLineInfos.add(lyricsLineInfo);
        }
        lyricsLineInfo.setSplitLyricsLineInfos(lyricsLineInfos);
    }

    /**
     * 根据新歌词的索引和旧歌词数据，构造新的歌词数据
     *
     * @param lyricsLineInfo 旧的行歌词数据
     * @param startIndex     开始歌词索引
     * @param lastIndex      结束歌词索引
     * @return
     */
    private static LyricsLineInfo getNewLrcLyricsLineInfo(
            LyricsLineInfo lyricsLineInfo, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;
        LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
        // 行开始时间
        long lineStartTime = lyricsLineInfo.getStartTime();
        StringBuilder lineLyrics = new StringBuilder();
        for (int i = startIndex; i <= lastIndex; i++) {
            lineLyrics.append(lyricsLineInfo.getLineLyrics().charAt(i));
        }
        newLyricsLineInfo.setStartTime(lineStartTime);
        newLyricsLineInfo.setLineLyrics(lineLyrics.toString());

        return newLyricsLineInfo;
    }

    /**
     * 根据新歌词的索引和旧歌词数据，构造新的歌词数据
     *
     * @param lyricsLineInfo 旧的行歌词数据
     * @param startIndex     开始歌词索引
     * @param lastIndex      结束歌词索引
     * @return
     */
    private static LyricsLineInfo getNewLyricsLineInfo(
            LyricsLineInfo lyricsLineInfo, int startIndex, int lastIndex) {

        if (lastIndex < 0)
            return null;
        LyricsLineInfo newLyricsLineInfo = new LyricsLineInfo();
        // 行开始时间
        long lineStartTime = lyricsLineInfo.getStartTime();
        long startTime = lineStartTime;
        int endTime = 0;
        StringBuilder lineLyrics = new StringBuilder();
        List<String> lyricsWordsList = new ArrayList<>();
        List<Long> wordsDisIntervalList = new ArrayList<>();
        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        long[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
        for (int i = 0; i <= lastIndex; i++) {
            if (i < startIndex) {
                startTime += wordsDisInterval[i];
            } else {
                lineLyrics.append(lyricsWords[i]);
                wordsDisIntervalList.add(wordsDisInterval[i]);
                lyricsWordsList.add(lyricsWords[i]);
                endTime += wordsDisInterval[i];
            }
        }
        endTime += startTime;
        //
        String[] newLyricsWords = lyricsWordsList
                .toArray(new String[lyricsWordsList.size()]);
        long newWordsDisInterval[] = getWordsDisIntervalList(wordsDisIntervalList);
        newLyricsLineInfo.setEndTime(endTime);
        newLyricsLineInfo.setStartTime(startTime);
        newLyricsLineInfo.setLineLyrics(lineLyrics.toString());
        newLyricsLineInfo.setLyricsWords(newLyricsWords);
        newLyricsLineInfo.setWordsDisInterval(newWordsDisInterval);

        return newLyricsLineInfo;
    }

    /**
     * 获取每个歌词的时间
     *
     * @param wordsDisIntervalList
     * @return
     */
    private static long[] getWordsDisIntervalList(
            List<Long> wordsDisIntervalList) {
        long wordsDisInterval[] = new long[wordsDisIntervalList.size()];
        for (int i = 0; i < wordsDisIntervalList.size(); i++) {
            wordsDisInterval[i] = wordsDisIntervalList.get(i);
        }
        return wordsDisInterval;
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * 获取分割后的动感歌词行索引
     *
     * @param lyricsLineTreeMap
     * @param origLineNumber    原行号
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitDynamicLyricsLineNum(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int origLineNumber, long oldPlayingTime, long playOffset) {
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(origLineNumber);
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(lyricsLineInfos, oldPlayingTime, playOffset);
    }

    /**
     * 获取分割后的lrc歌词行索引
     *
     * @param mLrcLineInfos
     * @param mLyricsLineNum
     * @param playProgress
     * @param playOffset
     * @return
     */
    public static int getSplitLrcLyricsLineNum(TreeMap<Integer, LyricsLineInfo> mLrcLineInfos, int mLyricsLineNum, long playProgress, long playOffset) {
        LyricsLineInfo lyrLine = mLrcLineInfos.get(mLyricsLineNum);
        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLrcLyricsLineNum(lyricsLineInfos, playProgress, playOffset);
    }

    /**
     * 获取分割后的行索引
     *
     * @param lyricsLineInfos
     * @param playProgress
     * @param playOffset
     * @return
     */
    private static int getSplitLrcLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, long playProgress, long playOffset) {
        //添加歌词增量
        long curPlayingTime = playProgress + playOffset;
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            if (curPlayingTime < lyricsLineInfos.get(i).getStartTime()) return 0;

            if (curPlayingTime >= lyricsLineInfos.get(i).getStartTime()
                    && i + 1 < lyricsLineInfos.size()
                    && curPlayingTime <= lyricsLineInfos.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (lyricsLineInfos.size() > 0) {
            return lyricsLineInfos.size() - 1;
        }
        return 0;
    }


    /**
     * 获取额外分割歌词索引
     *
     * @param lyricsLineInfos
     * @param origLineNumber
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitExtraLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, int origLineNumber, long oldPlayingTime, long playOffset) {
        LyricsLineInfo lyrLine = lyricsLineInfos.get(origLineNumber);
        List<LyricsLineInfo> newLineInfos = lyrLine.getSplitLyricsLineInfos();
        return getSplitLyricsLineNum(newLineInfos, oldPlayingTime, playOffset);
    }

    /**
     * 获取分割后的行索引
     *
     * @param lyricsLineInfos
     * @param oldPlayingTime
     * @return
     */
    private static int getSplitLyricsLineNum(List<LyricsLineInfo> lyricsLineInfos, long oldPlayingTime, long playOffset) {
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        for (int i = 0; i < lyricsLineInfos.size(); i++) {

            if (curPlayingTime < lyricsLineInfos.get(i).getStartTime()) return 0;

            if (curPlayingTime >= lyricsLineInfos.get(i).getStartTime()
                    && curPlayingTime <= lyricsLineInfos.get(i).getEndTime()) {
                return i;
            }
            if (curPlayingTime > lyricsLineInfos.get(i).getEndTime()
                    && i + 1 < lyricsLineInfos.size()
                    && curPlayingTime <= lyricsLineInfos.get(i + 1).getStartTime()) {
                return i;
            }
        }
        if (curPlayingTime >= lyricsLineInfos.get(lyricsLineInfos.size() - 1)
                .getEndTime()) {
            return lyricsLineInfos.size() - 1;
        }
        return 0;
    }

    /**
     * 获取分割歌词后的歌词字索引
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitLyricsWordIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);

        List<LyricsLineInfo> lyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = lyricsLineInfos.get(i);
            long elapseTime = temp.getStartTime();
            if (curPlayingTime < elapseTime) return -1;
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime <= elapseTime) {
                    return j;
                }
            }
            long endTime = temp.getEndTime();
            if (elapseTime < curPlayingTime && curPlayingTime <= endTime) {
                break;
            }

        }
        //整句已经播放完成
        return -2;
    }

    /**
     * 获取分割歌词后的歌词字索引
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getLyricsWordIndex(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        long elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return -1;

        for (int j = 0; j < lyrLine.getLyricsWords().length; j++) {
            elapseTime += lyrLine.getWordsDisInterval()[j];
            if (curPlayingTime <= elapseTime) {
                return j;
            }
        }

        //整句已经播放完成
        return -2;
    }

    /**
     * 获取分割额外歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getSplitExtraLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        List<LyricsLineInfo> newLyricsLineInfos = lyrLine.getSplitLyricsLineInfos();
        for (int i = 0; i < newLyricsLineInfos.size(); i++) {
            LyricsLineInfo temp = newLyricsLineInfos.get(i);
            long elapseTime = temp.getStartTime();
            if (curPlayingTime < elapseTime) return -1;
            for (int j = 0; j < temp.getLyricsWords().length; j++) {
                elapseTime += temp.getWordsDisInterval()[j];
                if (curPlayingTime <= elapseTime) {
                    return j;
                }
            }
            long endTime = temp.getEndTime();
            if (elapseTime < curPlayingTime && curPlayingTime <= endTime) {
                break;
            }

        }
        //整句已经播放完成
        return -2;
    }

    /**
     * 获取额外歌词字索引
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @return
     */
    public static int getExtraLyricsWordIndex(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return -1;

        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        long elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return -1;
        for (int j = 0; j < lyrLine.getLyricsWords().length; j++) {
            elapseTime += lyrLine.getWordsDisInterval()[j];
            if (curPlayingTime <= elapseTime) {
                return j;
            }
        }

        //整句已经播放完成
        return -2;
    }

    /**
     * 获取当前歌词的第几个歌词的播放时间
     *
     * @param lyricsLineNum  行数
     * @param oldPlayingTime
     * @return
     */
    public static long getDisWordsIndexLenTime(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return 0;
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        long elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return 0;
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime <= elapseTime) {
                return lyrLine.getWordsDisInterval()[i] - (elapseTime - curPlayingTime);
            }
        }

        return 0;
    }

    /**
     * 获取lrc歌词的时间长度
     *
     * @param lyricsLineTreeMap
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @param playOffset
     * @return
     */
    public static long getLrcLenTime(TreeMap<Integer, LyricsLineInfo> lyricsLineTreeMap, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return 0;
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineTreeMap.get(lyricsLineNum);
        if (curPlayingTime < lyrLine.getStartTime()) return 0;
        long elapseTime = curPlayingTime - lyrLine.getStartTime();
        return elapseTime;
    }

    /**
     * 获取翻译歌词行的第几个歌词的播放时间
     *
     * @param lyricsLineInfos
     * @param lyricsLineNum
     * @param oldPlayingTime
     * @param playOffset
     * @return
     */
    public static long getTranslateLrcDisWordsIndexLenTime(List<LyricsLineInfo> lyricsLineInfos, int lyricsLineNum, long oldPlayingTime, long playOffset) {
        if (lyricsLineNum < 0)
            return 0;
        //添加歌词增量
        long curPlayingTime = oldPlayingTime + playOffset;
        LyricsLineInfo lyrLine = lyricsLineInfos.get(lyricsLineNum);
        long elapseTime = lyrLine.getStartTime();
        if (curPlayingTime < elapseTime) return 0;
        for (int i = 0; i < lyrLine.getLyricsWords().length; i++) {
            elapseTime += lyrLine.getWordsDisInterval()[i];
            if (curPlayingTime <= elapseTime) {
                return lyrLine.getWordsDisInterval()[i] - (elapseTime - curPlayingTime);
            }
        }
        return 0;
    }

    /**
     * 默认歌词遍历
     */
    private interface ForeachListener {
        /**
         * 遍历
         *
         * @param lyricsLineInfo
         */
        public void foreach(LyricsLineInfo lyricsLineInfo);

    }

}
