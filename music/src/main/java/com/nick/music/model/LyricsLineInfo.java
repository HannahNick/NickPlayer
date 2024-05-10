package com.nick.music.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.nick.music.R;

import java.util.List;

/**
 * 歌词实体类
 *
 * @author zhangliangming
 */
public class LyricsLineInfo {

    /**
     * 歌词开始时间
     */
    private long mStartTime;
    /**
     * 歌词结束时间
     */
    private long mEndTime;
    /**
     * 该行歌词
     */
    private String mLineLyrics = "";

    /**
     * 歌词数组，用来分隔每个歌词
     */
    private String[] mLyricsWords;

    /**
     * 颜色数组，用来表示每个歌词描的颜色
     */
    private int[] mWordsColors;
    /**
     * 数组，用来存放每个歌词的持续时间
     */
    private long[] mWordsDisInterval;

    /**
     * 数组，用来存放每个歌词的相对开始时间
     */
    private long[] mWordsStartTime;

    /**
     * 数组，用来存放歌词在节奏view上的下标
     */
    private int[] mWordsIndex;

    public String[] getLyricsWords() {
        return mLyricsWords;
    }

    /**
     * 分割歌词行歌词
     */
    private List<LyricsLineInfo> mSplitDynamicLrcLineInfos;

    public List<LyricsLineInfo> getSplitLyricsLineInfos() {
        return mSplitDynamicLrcLineInfos;
    }

    public void setSplitLyricsLineInfos(
            List<LyricsLineInfo> splitDynamicLrcLineInfos) {
        this.mSplitDynamicLrcLineInfos = splitDynamicLrcLineInfos;
    }

    public void setLyricsWords(String[] lyricsWords) {
        if (lyricsWords == null) return;
        String[] tempArray = new String[lyricsWords.length];
        for (int i = 0; i < lyricsWords.length; i++) {
            String temp = lyricsWords[i];
            if (TextUtils.isEmpty(temp)) {
                tempArray[i] = "";
            } else {
                tempArray[i] = temp.replaceAll("\r|\n", "");
            }
        }
        this.mLyricsWords = tempArray;
    }

    /**
     * 使用该方法一定要先setLineLyrics
     */
    public void setLyricsWords2(String[] lyricsWords){
        if (lyricsWords == null) return;
        StringBuilder sb = new StringBuilder();
        for (String lyricsWord : lyricsWords) {
            sb.append(lyricsWord);
        }
        if (!mLineLyrics.equals(sb.toString())){
            String[] array = new String[1];
            array[0] = mLineLyrics;
            this.mLyricsWords = array;
            return;
        }

        String[] tempArray = new String[lyricsWords.length];
        for (int i = 0; i < lyricsWords.length; i++) {
            String temp = lyricsWords[i];
            if (TextUtils.isEmpty(temp)) {
                tempArray[i] = "";
            } else {
                tempArray[i] = temp.replaceAll("\r|\n", "");
            }
        }
        this.mLyricsWords = tempArray;
    }

    public int[] getWordsIndex() {
        return mWordsIndex;
    }

    public void setWordsIndex(int[] mWordsIndex) {
        this.mWordsIndex = mWordsIndex;
    }

    public long[] getWordsDisInterval() {
        return mWordsDisInterval;
    }

    public void setWordsDisInterval(long[] wordsDisInterval) {
        this.mWordsDisInterval = wordsDisInterval;
    }

    public long[] getWordsStartTime() {
        return mWordsStartTime;
    }

    public void setWordsStartTime(long[] mWordsStartTime) {
        this.mWordsStartTime = mWordsStartTime;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public void setStartTime(long mStartTime) {
        this.mStartTime = mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public void setEndTime(long mEndTime) {
        this.mEndTime = mEndTime;
    }

    public String getLineLyrics() {
        return mLineLyrics;
    }

    public void setLineLyrics(String mLineLyrics) {
        if (!TextUtils.isEmpty(mLineLyrics)) {
            this.mLineLyrics = mLineLyrics.replaceAll("\r|\n", "");
        }
    }

    public void setWordColors(int[] wordsColors){
        this.mWordsColors = wordsColors;
    }

    public int[] getWordColors(){
        return mWordsColors;
    }

//    /**
//     * 复制
//     *
//     * @param dist 要复制的实体类
//     * @param orig 原始实体类
//     */
//    public void copy(LyricsLineInfo dist, LyricsLineInfo orig) {
//        if (orig.getWordsDisInterval() != null) {
//            dist.setWordsDisInterval(orig.getWordsDisInterval());
//        }
//        dist.setStartTime(orig.getStartTime());
//        dist.setEndTime(orig.getEndTime());
//
//        if (orig.getLyricsWords() != null) {
//            dist.setLyricsWords(orig.getLyricsWords());
//        }
//
//        dist.setLineLyrics(orig.getLineLyrics());
//
//    }


    @Override
    public String toString() {
        return "LyricsLineInfo{" +
                "mStartTime=" + mStartTime +
                ", mEndTime=" + mEndTime +
                ", mLineLyrics='" + mLineLyrics + '\'' +
                '}';
    }
}
