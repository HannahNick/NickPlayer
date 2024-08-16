package com.nick.music.krc;

import android.graphics.Paint;
import android.text.TextUtils;


import com.blankj.utilcode.util.FileIOUtils;
import com.nick.music.model.LyricsInfo;
import com.nick.music.model.LyricsLineInfo;
import com.nick.music.model.LyricsTag;
import com.nick.music.util.LyricsUtils;
import com.nick.music.util.TimeUtils;
import com.xyz.base.utils.L;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * lrc歌词解析器
 * Created by zhangliangming on 2018-02-24.
 */

public class LrcLyricsFileReader extends LyricsFileReader {

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

    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";

    private final static String LEGAL_TOTAL_PREFIX = "[total:";

    private final Paint mPaint = new Paint();
    //屏幕是1920的宽度，但是歌词view有间距，所以就设置1820为最大显示宽度
    private float MAX_Width = 750f;

    private float mTextSize = 0f;

    private float mRollTextSize = 30f;
    private final Paint mRollPaint = new Paint();

    public LrcLyricsFileReader() {
        mRollPaint.setTextSize(mRollTextSize);
        mTextSize = 50f;
        mPaint.setTextSize(mTextSize);
        MAX_Width = 750f;
    }

    /**
     * 读取歌词文件
     *
     * @param file
     * @return
     */
    @Override
    public LyricsInfo readFile(File file) throws Exception {
        String lyricsTextStr = FileIOUtils.readFile2String(file);
        List<String> lyricsTexts = Arrays.asList(lyricsTextStr.split("\n"));
        L.i(lyricsTexts);
        return parseLyricsTextStr(lyricsTexts);
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        lyricsIfno.setLyricsType(LyricsInfo.LRC);

        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    getDefaultCharset()));

            // 这里面key为该行歌词的开始时间，方便后面排序
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();
            String lineInfo = "";
            while ((lineInfo = br.readLine()) != null) {
                // 解析歌词
                parserLineInfos(lyricsLineInfosTemp, lyricsTags, lineInfo);
            }
            in.close();
            in = null;
            // 重新封装
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            int index = 0;
            Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
            while (it.hasNext()) {
                lyricsLineInfos
                        .put(index++, lyricsLineInfosTemp.get(it.next()));
            }
            it = null;
            // 设置歌词的标签类
            lyricsIfno.setLyricsTags(lyricsTags);
            //
            lyricsIfno.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsIfno;
    }



    public LyricsInfo parseLyricsTextStr(List<String> lyricsList){
        LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsFileExt(getSupportFileExt());
        lyricsInfo.setLyricsType(LyricsInfo.LRC);

        SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<>();
        Map<String, Object> lyricsTags = new HashMap<>();
        lyricsList.forEach(lineString->{
            // 解析歌词
            parserLineInfos(lyricsLineInfosTemp, lyricsTags, lineString);
        });
        // 重新封装
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<>();
        int index = 0;
        for (Integer integer : lyricsLineInfosTemp.keySet()) {
            LyricsLineInfo lyricsLineInfo = lyricsLineInfosTemp.get(integer);
            splitLyricsLine(lyricsLineInfo);
            lyricsLineInfos.put(index++, lyricsLineInfo);
        }
        // 设置歌词的标签类
        lyricsInfo.setLyricsTags(lyricsTags);
        //
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        return lyricsInfo;
    }

    /**
     * 过长的歌词进行分割
     * @param lyricsLineInfo 文件解析出来的行歌词数据
     */
    private void splitLyricsLine(LyricsLineInfo lyricsLineInfo){
        if (lyricsLineInfo==null){
            return;
        }

        List<LyricsLineInfo> tempLyricsLine = new ArrayList<>();
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        if (isTextFitInOneLine(lineLyrics)){
            LyricsLineInfo tempLineInfo = new LyricsLineInfo();
            tempLineInfo.setLineLyrics(lineLyrics);
            tempLyricsLine.add(tempLineInfo);
        }else {
            float accumulatedWidth = 0f;
            StringBuilder currentLine = new StringBuilder();
            String [] lineWordArray = lineLyrics.split("");
            for (int i = 0; i < lineWordArray.length; i++) {
                String word = lineWordArray[i];
                float wordWidth = mPaint.measureText(word);
                if (accumulatedWidth + wordWidth <= MAX_Width) {
                    currentLine.append(word);
                    accumulatedWidth += wordWidth;
                } else {
                    if (!TextUtils.isEmpty(currentLine.toString())){
                        LyricsLineInfo tempLineInfo = new LyricsLineInfo();
                        tempLineInfo.setLineLyrics(currentLine.toString());
                        tempLyricsLine.add(tempLineInfo);
                    }
                    currentLine = new StringBuilder(word);
                    accumulatedWidth = wordWidth;
                }
            }
            if (currentLine.length() > 0) {
                LyricsLineInfo lastLineInfo = new LyricsLineInfo();
                lastLineInfo.setLineLyrics(currentLine.toString());
                tempLyricsLine.add(lastLineInfo);
            }
        }
        lyricsLineInfo.setSplitLyricsLineInfos(tempLyricsLine);
    }

    private boolean isTextFitInOneLine(String lineText) {
        mPaint.setTextSize(mTextSize);
        mPaint.setLetterSpacing(0.1f);
        float textWidth = mPaint.measureText(lineText);
        return textWidth <= MAX_Width;
    }

    @Override
    public LyricsInfo readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        lyricsIfno.setLyricsType(LyricsInfo.LRC);

        if (!TextUtils.isEmpty(lrcContent)) {

            // 这里面key为该行歌词的开始时间，方便后面排序
            SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp = new TreeMap<Integer, LyricsLineInfo>();
            Map<String, Object> lyricsTags = new HashMap<String, Object>();

            // 获取歌词内容
            String lrcContents[] = lrcContent.split("\n");
            for (int i = 0; i < lrcContents.length; i++) {
                String lineInfo = lrcContents[i];

                // 解析歌词
                parserLineInfos(lyricsLineInfosTemp,
                        lyricsTags, lineInfo);
            }

            // 重新封装
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
            int index = 0;
            Iterator<Integer> it = lyricsLineInfosTemp.keySet().iterator();
            while (it.hasNext()) {
                LyricsLineInfo lyricsLineInfo = lyricsLineInfosTemp.get(it.next());
                LyricsUtils.splitLrcLyrics(lyricsLineInfo,mPaint,mTextSize);
                lyricsLineInfos.put(index++, lyricsLineInfo);
            }
            it = null;
            // 设置歌词的标签类
            lyricsIfno.setLyricsTags(lyricsTags);
            //
            lyricsIfno.setLyricsLineInfoTreeMap(lyricsLineInfos);
        }
        return lyricsIfno;

    }

    /**
     * 解析行歌词
     *
     * @param lyricsLineInfosTemp 排序集合
     * @param lyricsTags          歌曲标签
     * @param lineInfo            行歌词内容
     * @throws Exception
     */
    private void parserLineInfos(SortedMap<Integer, LyricsLineInfo> lyricsLineInfosTemp, Map<String, Object> lyricsTags, String lineInfo){
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
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else {
            //时间标签
            String timeRegex = "\\[\\d+:\\d+.\\d+\\]";
            String timeRegexs = "(" + timeRegex + ")+";
            // 如果含有时间标签，则是歌词行
            Pattern pattern = Pattern.compile(timeRegexs);
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                Pattern timePattern = Pattern.compile(timeRegex);
                Matcher timeMatcher = timePattern
                        .matcher(matcher.group());
                //遍历时间标签
                while (timeMatcher.find()) {
                    lyricsLineInfo = new LyricsLineInfo();
                    //获取开始时间
                    String startTimeString = timeMatcher.group().trim();
                    int startTime = TimeUtils.parseInteger(startTimeString.substring(startTimeString.indexOf('[') + 1, startTimeString.lastIndexOf(']')));
                    lyricsLineInfo.setStartTime(startTime);
                    //获取歌词内容
                    int timeEndIndex = matcher.end();
                    String lineLyrics = lineInfo.substring(timeEndIndex, lineInfo.length()).trim();
                    lyricsLineInfo.setLineLyrics(lineLyrics);
                    lyricsLineInfosTemp.put(startTime, lyricsLineInfo);
                }
            }
        }
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("lrc");
    }

    @Override
    public String getSupportFileExt() {
        return "lrc";
    }
}
