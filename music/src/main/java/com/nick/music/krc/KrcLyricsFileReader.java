package com.nick.music.krc;

import android.graphics.Color;
import android.util.Base64;


import androidx.core.util.Pair;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.nick.music.R;
import com.nick.music.model.LyricsInfo;
import com.nick.music.model.LyricsLineInfo;
import com.nick.music.model.LyricsTag;
import com.nick.music.model.TranslateLrcLineInfo;
import com.nick.music.util.RandomIndexUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: krcs歌词读取器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/12/25 16:29
 * @Throws:
 */

public class KrcLyricsFileReader extends LyricsFileReader {

    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌词上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";
    private final static String LEGAL_HASH_PREFIX = "[hash:";
    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";
    private final static String LEGAL_SIGN_PREFIX = "[sign:";
    private final static String LEGAL_QQ_PREFIX = "[qq:";
    private final static String LEGAL_TOTAL_PREFIX = "[total:";
    private final static String LEGAL_LANGUAGE_PREFIX = "[language:";
    /**
     * 解码参数
     */
    private static final char[] key = {'@', 'G', 'a', 'w', '^', '2', 't', 'G',
            'Q', '6', '1', '-', 'Î', 'Ò', 'n', 'i'};

    public KrcLyricsFileReader() {
    }

    @Override
    public LyricsInfo readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        return null;
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        return new LyricsInfo();
    }

    public LyricsInfo readFile(File file) throws Exception {
        LyricsInfo lyricsInfo = new LyricsInfo();
        String lyricsTextStr = FileIOUtils.readFile2String(file);
        String[] lyricsTexts = lyricsTextStr.split("\n");
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
        Map<String, Object> lyricsTags = new HashMap<String, Object>();
        int index = 0;
        List<LyricsLineInfo> tempList = new ArrayList<>();
        for (String lineInfo : lyricsTexts) {
            // 行读取，并解析每行歌词的内容
            LyricsLineInfo lyricsLineInfo = parserLineInfos(lyricsTags,
                    lineInfo, lyricsInfo);
            if (lyricsLineInfo == null) {
                continue;
            }
            checkLineIsStartSex(tempList, lyricsLineInfo);
            if (tempList.size() == 1) {
                continue;
            }
            LyricsLineInfo appendLyricsLineInfo = mergeLyricsLine(tempList);
            if (appendLyricsLineInfo != null) {
                lyricsLineInfos.put(index, appendLyricsLineInfo);
                index++;
                tempList.clear();
                continue;
            }

            lyricsLineInfos.put(index, lyricsLineInfo);
            index++;
        }
        // 设置歌词的标签类
        lyricsInfo.setLyricsTags(lyricsTags);
        //
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        return lyricsInfo;
    }

    //检测是否是对唱类型开头
    private void checkLineIsStartSex(List<LyricsLineInfo> tempList,LyricsLineInfo lyricsLineInfo){
        if (tempList.size()==1){
            tempList.add(lyricsLineInfo);
        }
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        //如果是 男: 女: 这种类型开头
        if (lineLyrics.length()==2&&(lineLyrics.contains("：")||lineLyrics.contains(":"))){
            tempList.add(lyricsLineInfo);
        }
    }

    //开始拼接歌词
    private LyricsLineInfo mergeLyricsLine(List<LyricsLineInfo> tempList){
        if (tempList.size()==2){
            LogUtils.i("开始拼接数据:"+tempList);
            LyricsLineInfo sexSingInfo = tempList.get(0);
            LyricsLineInfo lyricsLineInfo = tempList.get(1);
            //拼接行歌词
            String sexLineLyrics = sexSingInfo.getLineLyrics();
            String lineLyrics = lyricsLineInfo.getLineLyrics();
            String appendLineLyrics = sexLineLyrics.concat(lineLyrics);
            //拼接歌词字
            String[] sexLyricsWords = sexSingInfo.getLyricsWords();
            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            String[] appendLyricsWord = new String[sexLyricsWords.length+lyricsWords.length];
            //拼接歌词字时长
            long[] sexSingInfoWordsDisInterval = sexSingInfo.getWordsDisInterval();
            long[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
            long[] appendWordsDisInterval = new long[sexSingInfoWordsDisInterval.length+wordsDisInterval.length];
            //拼接歌词在RhythmView显示位置下标
            int[] sexWordsIndex = sexSingInfo.getWordsIndex();
            int[] wordsIndex = lyricsLineInfo.getWordsIndex();
            int[] appendWordsIndex = new int[sexWordsIndex.length+wordsIndex.length];
            //歌词开始时间和结束时间
            long startTime = sexSingInfo.getStartTime();
            long endTime = lyricsLineInfo.getEndTime();
            //拼接歌词字的绝对开始时间
            long[] sexWordsStartTime = sexSingInfo.getWordsStartTime();
            long[] wordsStartTime = lyricsLineInfo.getWordsStartTime();
            long[] appendWordsStartTime = new long[sexWordsStartTime.length+ wordsStartTime.length];

            for (int i = 0; i < sexLyricsWords.length; i++) {
                appendLyricsWord[i] = sexLyricsWords[i];
                appendWordsDisInterval[i] = sexSingInfoWordsDisInterval[i];
                appendWordsIndex[i] = sexWordsIndex[i];
                appendWordsStartTime[i] = sexWordsStartTime[i];
            }
            for (int i = 0; i < lyricsWords.length; i++) {
                appendLyricsWord[sexLyricsWords.length+i] = lyricsWords[i];
                appendWordsDisInterval[sexSingInfoWordsDisInterval.length+i] = wordsDisInterval[i];
                appendWordsIndex[sexWordsIndex.length+i] = wordsIndex[i];
                appendWordsStartTime[sexWordsStartTime.length+i] = wordsStartTime[i];
            }
            sexSingInfo.setLineLyrics(appendLineLyrics);
            sexSingInfo.setLyricsWords(appendLyricsWord);
            sexSingInfo.setWordsDisInterval(appendWordsDisInterval);
            sexSingInfo.setWordsIndex(appendWordsIndex);
            sexSingInfo.setStartTime(startTime);
            sexSingInfo.setEndTime(endTime);
            sexSingInfo.setWordsStartTime(appendWordsStartTime);
            return sexSingInfo;
        }
        return null;
    }


    /**
     * 解析歌词
     *
     * @param lyricsTags
     * @param lineInfo
     * @param lyricsIfno
     * @return
     */
    private LyricsLineInfo parserLineInfos(Map<String, Object> lyricsTags,
                                           String lineInfo, LyricsInfo lyricsIfno) throws Exception {
        LyricsLineInfo lyricsLineInfo = null;
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_HASH_PREFIX)
                || lineInfo.startsWith(LEGAL_SIGN_PREFIX)
                || lineInfo.startsWith(LEGAL_QQ_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else if (lineInfo.startsWith(LEGAL_LANGUAGE_PREFIX)) {
            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            // 解析翻译歌词
            // 获取json base64字符串
            String translateJsonBase64String = temp.length == 1 ? "" : temp[1];
            if (!translateJsonBase64String.equals("")) {

                String translateJsonString = new String(
                        Base64.decode(translateJsonBase64String, Base64.NO_WRAP));
                parserOtherLrc(lyricsIfno, translateJsonString);
            }
        } else {
            // 匹配歌词行
            Pattern pattern = Pattern.compile("\\[\\d+,\\d+\\]");
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                lyricsLineInfo = new LyricsLineInfo();
                // [此行开始时刻距0时刻的毫秒数,此行持续的毫秒数]<0,此字持续的毫秒数,0>歌<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>词<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>正<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>文
                // 获取行的出现时间和结束时间
                int mStartIndex = matcher.start();
                int mEndIndex = matcher.end();
                String[] lineTime = lineInfo.substring(mStartIndex + 1,
                        mEndIndex - 1).split(",");
                //

                long startTime = modifyNumber(Long.parseLong(lineTime[0]));
                long endTime = modifyNumber(startTime + Long.parseLong(lineTime[1]));
                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setStartTime(startTime);
                // 获取歌词信息，将开始的行时间[xxx,xxx]去掉
                String lineContent = lineInfo.substring(mEndIndex,
                        lineInfo.length());

                // 歌词匹配的正则表达式
                String regex = "\\<\\d+,\\d+,\\d+\\>";
                Pattern lyricsWordsPattern = Pattern.compile(regex);
                Matcher lyricsWordsMatcher = lyricsWordsPattern
                        .matcher(lineContent);

                if (lyricsWordsMatcher == null) {
                    return null;
                }

                // 歌词分隔
                String[] lineLyricsTemp = lineContent.split(regex);
                String[] lyricsWords = getLyricsWords(lineLyricsTemp);
                lyricsLineInfo.setLyricsWords(lyricsWords);

                // 获取每个歌词的时间
                long[] wordsDisInterval = new long[lyricsWords.length];
                //每个歌词开始时间
                long[] wordsStartTime = new long[lyricsWords.length];
                //每个歌词在view的index
                int[] wordsIndex = new int[lyricsWords.length];
                //时间偏移量
//                long offset = Long.parseLong((String) lyricsTags.get(LyricsTag.TAG_OFFSET));
                int index = 0;
                while (lyricsWordsMatcher.find()) {

                    //验证
                    if (index >= wordsDisInterval.length) {
                        throw new Exception("字标签个数与字时间标签个数不相符");
                    }

                    //取出匹配字对应的<>数据
                    String wordsDisIntervalStr = lyricsWordsMatcher.group();
                    //去掉括号<>
                    String wordsDisIntervalStrTemp = wordsDisIntervalStr
                            .substring(wordsDisIntervalStr.indexOf('<') + 1, wordsDisIntervalStr.lastIndexOf('>'));
                    // 得到 xx,xxx,0的数据再以,拆分
                    String[] wordsDisIntervalTemp = wordsDisIntervalStrTemp
                            .split(",");

                    //
                    //获取字的绝对开始时间
                    wordsStartTime[index] = modifyNumber(Long.parseLong(wordsDisIntervalTemp[0])+startTime);
                    //获取每个字的持续时间
                    wordsDisInterval[index] = modifyNumber(Long.parseLong(wordsDisIntervalTemp[1]));
                    //设置每个歌词在view中的显示下标
                    wordsIndex[index] = RandomIndexUtil.getInstance().getRandom();
                    index++;

                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);
                lyricsLineInfo.setWordsStartTime(wordsStartTime);
                lyricsLineInfo.setWordsIndex(wordsIndex);

                // 获取行歌词
                String lineLyrics = lyricsWordsMatcher.replaceAll("");
                lyricsLineInfo.setLineLyrics(lineLyrics);
            }

        }
        return lyricsLineInfo;
    }

    private long modifyNumber(long x){
        long digit = x % 10;
        if (digit == 0) {
            return x;
        }else {
            return x + 10 - digit;
        }
    }

    /**
     * 解析翻译和音译歌词
     *
     * @param lyricsIfno
     * @param translateJsonString
     */
    private void parserOtherLrc(LyricsInfo lyricsIfno,
                                String translateJsonString) throws Exception {

        JSONObject resultObj = new JSONObject(translateJsonString);
        JSONArray contentArrayObj = resultObj.getJSONArray("content");
        for (int i = 0; i < contentArrayObj.length(); i++) {
            JSONObject dataObj = contentArrayObj.getJSONObject(i);
            JSONArray lyricContentArrayObj = dataObj
                    .getJSONArray("lyricContent");
            int type = dataObj.getInt("type");
            if (type == 1) {
                // 解析翻译歌词
                if (lyricsIfno.getTranslateLrcLineInfos() == null || lyricsIfno.getTranslateLrcLineInfos().size() == 0)
                    parserTranslateLrc(lyricsIfno, lyricContentArrayObj);

            } else if (type == 0) {
                // 解析音译歌词
                if (lyricsIfno.getTransliterationLrcLineInfos() == null || lyricsIfno.getTransliterationLrcLineInfos().size() == 0)
                    parserTransliterationLrc(lyricsIfno,
                            lyricContentArrayObj);
            }
        }
    }

    /**
     * 解析音译歌词
     *
     * @param lyricsIfno
     * @param lyricContentArrayObj
     */
    private void parserTransliterationLrc(LyricsInfo lyricsIfno,
                                          JSONArray lyricContentArrayObj) throws Exception {

        // 音译歌词集合
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<LyricsLineInfo>();
        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            // 音译行歌词
            LyricsLineInfo transliterationLrcLineInfo = new LyricsLineInfo();
            String[] lyricsWords = new String[lrcDataArrayObj.length()];
            StringBuilder lineLyrics = new StringBuilder();
            for (int k = 0; k < lrcDataArrayObj.length(); k++) {
                if (k == lrcDataArrayObj.length() - 1) {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim();
                } else {
                    lyricsWords[k] = lrcDataArrayObj.getString(k).trim() + " ";
                }
                lineLyrics.append(lyricsWords[k]);
            }
            transliterationLrcLineInfo.setLineLyrics(lineLyrics.toString());
            transliterationLrcLineInfo.setLyricsWords(lyricsWords);

            transliterationLrcLineInfos.add(transliterationLrcLineInfo);
        }
        // 添加音译歌词
        if (transliterationLrcLineInfos.size() > 0) {
            lyricsIfno
                    .setTransliterationLrcLineInfos(transliterationLrcLineInfos);
        }
    }

    /**
     * 解析翻译歌词
     *
     * @param lyricsIfno
     * @param lyricContentArrayObj
     */
    private void parserTranslateLrc(LyricsInfo lyricsIfno,
                                    JSONArray lyricContentArrayObj) throws Exception {

        // 翻译歌词集合
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<TranslateLrcLineInfo>();

        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            String lrcComtext = lrcDataArrayObj.getString(0);

            // 翻译行歌词
            TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
            translateLrcLineInfo.setLineLyrics(lrcComtext);

            translateLrcLineInfos.add(translateLrcLineInfo);
        }
        // 添加翻译歌词
        if (translateLrcLineInfos.size() > 0) {
            lyricsIfno.setTranslateLrcLineInfos(translateLrcLineInfos);
        }
    }

    /**
     * 分隔每个歌词
     *
     * @param lineLyricsTemp
     * @return
     */
    private String[] getLyricsWords(String[] lineLyricsTemp) throws Exception {
        String[] temp = null;
        if (lineLyricsTemp.length < 2) {
            return new String[lineLyricsTemp.length];
        }
        //
        temp = new String[lineLyricsTemp.length - 1];
        for (int i = 1; i < lineLyricsTemp.length; i++) {
            temp[i - 1] = lineLyricsTemp[i];
        }
        return temp;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("krc");
    }

    @Override
    public String getSupportFileExt() {
        return "krc";
    }

}
