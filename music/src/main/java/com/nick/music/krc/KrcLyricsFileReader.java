package com.nick.music.krc;

import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;


import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.DeviceUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.nick.music.model.LyricsInfo;
import com.nick.music.model.LyricsLineInfo;
import com.nick.music.model.LyricsTag;
import com.nick.music.model.TranslateLrcLineInfo;
import com.nick.music.util.LyricsUtils;
import com.nick.music.util.RandomIndexUtil;
import com.xyz.base.utils.L;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    private final Paint mPaint = new Paint();
    //屏幕是1920的宽度，但是歌词view有间距，所以就设置1820为最大显示宽度
    private float MAX_Width = 1820f;

    private float mTextSize = 0f;

    private float mRollTextSize = 30f;
    private final Paint mRollPaint = new Paint();

    public KrcLyricsFileReader() {
        mRollPaint.setTextSize(mRollTextSize);
        mTextSize = 105f;
        mPaint.setTextSize(mTextSize);
        MAX_Width = 1820f;
    }

    @Override
    public LyricsInfo readLrcText(String dynamicContent, String lrcContent, String extraLrcContent, String lyricsFilePath) throws Exception {
        return null;
    }

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        return new LyricsInfo();
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    /**
     * 原始歌词和注音歌词行数是否一致
     */
    private boolean mOriginLineSizeEqTransliterSize = false;
    /**
     * 原始歌词和翻译歌词行数是否一致
     */
    private boolean mOriginLineSizeEqTranslateSize = false;
    /**
     * 是否检查过原始歌词和注音歌词行数
     */
    private boolean mHasCheckOriginLineSizeEqTransliterSize = false;

    /**
     * 解析歌词，按换行分割
     *
     * @param lyricsList
     * @return
     */
    public LyricsInfo parseLyricsTextStr(List<String> lyricsList) throws Exception {
        mHasCheckOriginLineSizeEqTransliterSize = false;
        mOriginLineSizeEqTransliterSize = false;
        LyricsInfo lyricsInfo = new LyricsInfo();
        lyricsInfo.setLyricsType(LyricsInfo.DYNAMIC);
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<>();
        Map<String, Object> lyricsTags = new HashMap<>();
        int index = 0;
        int transliterationIndex = 0;
        List<LyricsLineInfo> originTempList = new ArrayList<>();
        int lyricsTextLinelength = lyricsList.size();
        for (int i = 0; i < lyricsTextLinelength; i++) {
            String lineInfo = lyricsList.get(i);
            // 行读取，并解析每行歌词的内容
            LyricsLineInfo lyricsLineInfo = parserLineInfos(lyricsTags,
                    lineInfo, lyricsInfo);
            if (lyricsLineInfo == null) {
                continue;
            }
            checkOriginLineSizeEqTransliterSize(lyricsTextLinelength - i,
                    lyricsInfo.getTranslateLrcLineInfos() == null ? 0 : lyricsInfo.getTranslateLrcLineInfos().size(),
                    lyricsInfo.getTransliterationLrcLineInfos() == null ? 0 : lyricsInfo.getTransliterationLrcLineInfos().size());
            checkLineIsStartSex(originTempList, lyricsLineInfo);
            if (originTempList.size() == 1) {//有男女对唱的单独行跳过此轮循环将在下一轮循环中拼接
                continue;
            }
            //拼接歌词
            LyricsLineInfo appendLyricsLineInfo = mergeLyricsLine(originTempList);
            //获取文件翻译数据
            List<TranslateLrcLineInfo> translateLrcLineInfos = null;
            //原始歌词行数和翻译歌词行数对不上就不用处理翻译数据了
            if (mOriginLineSizeEqTranslateSize) {
                translateLrcLineInfos = lyricsInfo.getTranslateLrcLineInfos();
            }
            //获取注音数据
            LyricsLineInfo transliterationLrcLineInfos = null;
            //原始歌词行数和音译歌词行数对不上就不用处理注音数据了
            if (mOriginLineSizeEqTransliterSize) {
                transliterationLrcLineInfos = lyricsInfo.getTransliterationLrcLineInfos() == null ? null : lyricsInfo.getTransliterationLrcLineInfos().get(transliterationIndex);
                transliterationIndex++;
            }
            if (appendLyricsLineInfo != null) {
                LyricsLineInfo afterMergeTransliteration = mergeTransliteration(lyricsInfo.getTransliterationLrcLineInfos(), index + 1);
                transliterationIndex--;
                mergeTranslate(translateLrcLineInfos, index + 1);
                Pair<List<LyricsLineInfo>, List<LyricsLineInfo>> pair = wrapLyricsLineInfos(appendLyricsLineInfo, afterMergeTransliteration);
                for (int j = 0; j < pair.first.size(); j++) {
                    if (j > 0) {
                        if (translateLrcLineInfos != null) {
                            translateLrcLineInfos.add(index, new TranslateLrcLineInfo(""));
                        }
                        if (afterMergeTransliteration != null) {
                            lyricsInfo.getTransliterationLrcLineInfos().add(index, pair.second.get(j));
                        }
                    }
                    lyricsLineInfos.put(index, pair.first.get(j));
                    index++;
                }
                originTempList.clear();
                continue;
            }
            Pair<List<LyricsLineInfo>, List<LyricsLineInfo>> pair = wrapLyricsLineInfos(lyricsLineInfo, transliterationLrcLineInfos);
            for (int k = 0; k < pair.first.size(); k++) {
                if (k > 0) {
                    if (translateLrcLineInfos != null) {
                        translateLrcLineInfos.add(index, new TranslateLrcLineInfo(""));
                    }
                    if (transliterationLrcLineInfos != null) {
                        lyricsInfo.getTransliterationLrcLineInfos().add(index, pair.second.get(k));
                    }
                }
                lyricsLineInfos.put(index, pair.first.get(k));
                index++;
            }
        }
        // 设置歌词的标签类
        lyricsInfo.setLyricsTags(lyricsTags);
        // 设置每行歌词数据
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        return lyricsInfo;
    }

    /**
     * 查看原始歌词行数是否和音译歌词行数一致
     *
     * @param originLineSize 原始歌词行数
     * @param translateSize  翻译歌词行数
     * @param transliterSize 音译歌词行数
     */
    private void checkOriginLineSizeEqTransliterSize(int originLineSize, int translateSize, int transliterSize) {
        if (mHasCheckOriginLineSizeEqTransliterSize) {
            return;
        }
        mHasCheckOriginLineSizeEqTransliterSize = true;
        mOriginLineSizeEqTranslateSize = originLineSize == translateSize;
        mOriginLineSizeEqTransliterSize = originLineSize == transliterSize;
//        L.i("mOriginLineSizeEqTransliterSize:" + mOriginLineSizeEqTransliterSize + ",originLineSize=" + originLineSize + ",translateSize=" + translateSize + ",transliterSize=" + transliterSize);
    }

    /**
     * 拼接翻译数据
     *
     * @param translate 翻译list
     * @param index     需要去除的元素下标
     */
    public void mergeTranslate(List<TranslateLrcLineInfo> translate, int index) {
        if (translate == null || index <= 0 || index >= translate.size()) {
            return;
        }
        //将index下标的元素删除，并把删除的元素值拼接给index-1的元素
        TranslateLrcLineInfo lastTranslateLrcLineInfo = translate.get(index - 1);
        TranslateLrcLineInfo element = translate.remove(index);
        lastTranslateLrcLineInfo.setLineLyrics(lastTranslateLrcLineInfo.getLineLyrics().concat(element.getLineLyrics()));
    }

    /**
     * 拼接音译数据
     * @param transliterList 音译list
     * @param index 需要去除的元素下标
     */
    public LyricsLineInfo mergeTransliteration(List<LyricsLineInfo> transliterList, int index){
        if (transliterList == null || index <= 0 || index >= transliterList.size()) {
            return null;
        }
        LyricsLineInfo lastLyricsLineInfo = transliterList.get(index-1);
        LyricsLineInfo element = transliterList.remove(index);
        String[] lastLyricsWords = lastLyricsLineInfo.getLyricsWords();
        String[] elementLyricsWords = element.getLyricsWords();
        String[] concatResult = Stream.concat(Arrays.stream(lastLyricsWords), Arrays.stream(elementLyricsWords)).toArray(String[]::new);

        lastLyricsLineInfo.setLyricsWords(concatResult);
        lastLyricsLineInfo.setLineLyrics(lastLyricsLineInfo.getLineLyrics().concat(element.getLineLyrics()));
        return lastLyricsLineInfo;
    }

    /**
     * 包装翻译歌词
     *
     * @param lyricsLineInfo                默认标准行歌词数据
     * @param transliterationLyricsLineInfo 音译歌词
     * @return
     */
    private Pair<List<LyricsLineInfo>, List<LyricsLineInfo>> wrapLyricsLineInfos(LyricsLineInfo lyricsLineInfo, LyricsLineInfo transliterationLyricsLineInfo) {
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        List<String> strings = splitLyricsLine(lineLyrics, lyricsLineInfo);
        return mapLyricsLineInfoList(strings, lyricsLineInfo, transliterationLyricsLineInfo);
    }


    public LyricsInfo parseLyricsTextStr(String[] lyricsList) throws Exception {
        LyricsInfo lyricsInfo = new LyricsInfo();
        TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
        Map<String, Object> lyricsTags = new HashMap<String, Object>();
        int index = 0;
        List<LyricsLineInfo> tempList = new ArrayList<>();
        for (String lineInfo : lyricsList) {
            // 行读取，并解析每行歌词的内容
            LyricsLineInfo lyricsLineInfo = parserLineInfos(lyricsTags,
                    lineInfo, lyricsInfo);
            if (lyricsLineInfo == null) {
                continue;
            }
            checkLineIsStartSex(tempList, lyricsLineInfo);
            if (tempList.size() == 1) {//有男女对唱的单独行跳过此轮循环将在下一轮循环中拼接
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
        // 设置每行歌词数据
        lyricsInfo.setLyricsLineInfoTreeMap(lyricsLineInfos);
        return lyricsInfo;
    }

    public LyricsInfo readFile(File file) throws Exception {
        String lyricsTextStr = FileIOUtils.readFile2String(file);
        List<String> lyricsTexts = Arrays.asList(lyricsTextStr.split("\n"));

        return parseLyricsTextStr(lyricsTexts);
    }

    /**
     * 检测是否是对唱类型开头
     *
     * @param originTempList
     * @param lyricsLineInfo 当前行歌词
     */
    private void checkLineIsStartSex(List<LyricsLineInfo> originTempList, LyricsLineInfo lyricsLineInfo) {
        if (originTempList.size() == 1) {
            originTempList.add(lyricsLineInfo);
        }
        String lineLyrics = lyricsLineInfo.getLineLyrics();
        //如果是 男: 女: 这种类型开头
        if (lineLyrics.endsWith("：") || lineLyrics.endsWith(":")) {
            originTempList.add(lyricsLineInfo);
        }
    }

    //开始拼接歌词
    private LyricsLineInfo mergeLyricsLine(List<LyricsLineInfo> tempList) {
        if (tempList.size() == 2) {
//            LogUtils.i("开始拼接数据:"+tempList);
            LyricsLineInfo sexSingInfo = tempList.get(0);
            LyricsLineInfo lyricsLineInfo = tempList.get(1);
            //拼接行歌词
            String sexLineLyrics = sexSingInfo.getLineLyrics();
            String lineLyrics = lyricsLineInfo.getLineLyrics();
            String appendLineLyrics = sexLineLyrics.concat(lineLyrics);
            //拼接歌词字
            String[] sexLyricsWords = sexSingInfo.getLyricsWords();
            String[] lyricsWords = lyricsLineInfo.getLyricsWords();
            String[] appendLyricsWord = new String[sexLyricsWords.length + lyricsWords.length];
            //拼接歌词字时长
            long[] sexSingInfoWordsDisInterval = sexSingInfo.getWordsDisInterval();
            long[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();
            long[] appendWordsDisInterval = new long[sexSingInfoWordsDisInterval.length + wordsDisInterval.length];
            //拼接歌词在RhythmView显示位置下标
            int[] sexWordsIndex = sexSingInfo.getWordsIndex();
            int[] wordsIndex = lyricsLineInfo.getWordsIndex();
            int[] appendWordsIndex = new int[sexWordsIndex.length + wordsIndex.length];
            //歌词开始时间和结束时间
            long startTime = sexSingInfo.getStartTime();
            long endTime = lyricsLineInfo.getEndTime();
            //拼接歌词字的绝对开始时间
            long[] sexWordsStartTime = sexSingInfo.getWordsStartTime();
            long[] wordsStartTime = lyricsLineInfo.getWordsStartTime();
            long[] appendWordsStartTime = new long[sexWordsStartTime.length + wordsStartTime.length];

            for (int i = 0; i < sexLyricsWords.length; i++) {
                appendLyricsWord[i] = sexLyricsWords[i];
                appendWordsDisInterval[i] = sexSingInfoWordsDisInterval[i];
                appendWordsIndex[i] = sexWordsIndex[i];
                appendWordsStartTime[i] = sexWordsStartTime[i];
            }
            for (int i = 0; i < lyricsWords.length; i++) {
                appendLyricsWord[sexLyricsWords.length + i] = lyricsWords[i];
                appendWordsDisInterval[sexSingInfoWordsDisInterval.length + i] = wordsDisInterval[i];
                appendWordsIndex[sexWordsIndex.length + i] = wordsIndex[i];
                appendWordsStartTime[sexWordsStartTime.length + i] = wordsStartTime[i];
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
     * 歌词是否能够一行显示，英文歌词大概率显示不全
     *
     * @param lineText 一行歌词
     * @return 是否能显示
     */
    private boolean isTextFitInOneLine(String lineText) {
        mPaint.setTextSize(mTextSize);
        mPaint.setLetterSpacing(0.1f);
        float textWidth = mPaint.measureText(lineText);
        return textWidth <= MAX_Width;
    }

    /**
     * 如果文本中英文数量超过70%，就认为是英文
     *
     * @param text 文本
     * @return 是否是英文
     */
    public boolean isMostlyEnglishText(String text) {
//        if (containsEnglishCharacters(text)){
//            return true;
//        }
        int totalCharacters = text.length();
        int englishCharacters = 0;
        // 统计文本中的英文字母数量
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c) && Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN) {
                englishCharacters++;
            }
        }
        // 计算英文字母比例，如果超过70%则认为是英文
        double englishPercentage = (double) englishCharacters / totalCharacters * 100;
        return englishPercentage >= 30;
    }

    /**
     * 包含英文和空格就认为是英文了
     *
     * @param text 文本
     * @return 是否是英文
     */
    public boolean containsEnglishCharacters(String text) {
        // 使用正则表达式匹配是否包含英文字符
        Pattern pattern = Pattern.compile("[a-zA-Z\\s]+");
        Matcher matcher = pattern.matcher(text);

        return matcher.find();
    }

    /**
     * 将超出屏幕的歌词进行分割
     *
     * @param lineLyrics 行歌词
     * @return 分割数组
     */
    private List<String> splitLyricsLine(String lineLyrics, LyricsLineInfo lyricsLineInfo) {
        List<String> result = new ArrayList<>();
        if (isTextFitInOneLine(lineLyrics)) {
            result.add(lineLyrics);
            return result;
        }
        //英文按照空格进行分割，中文全部分割
//        boolean isMostEnglish = isMostlyEnglishText(lineLyrics);
//        String[] words =isMostEnglish?lineLyrics.split("\\s+"):lineLyrics.split("");
        String[] words = lyricsLineInfo.getLyricsWords();
        float accumulatedWidth = 0f;
        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
//            String word = words[i];
            //英文的拼接最后不需要拼空格
//            String text = isMostEnglish?word.concat(i== words.length-1?"":" "):word;
            String text = words[i];
            float wordWidth = mPaint.measureText(text);
            if (accumulatedWidth + wordWidth <= MAX_Width) {
                currentLine.append(text);
                accumulatedWidth += wordWidth;
            } else {
                if (!TextUtils.isEmpty(currentLine.toString())){
                    result.add(currentLine.toString());
                }
                currentLine = new StringBuilder(text);
                accumulatedWidth = wordWidth;
            }
        }
        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }
        return result;
    }

    /**
     * 将分割好的文本组装数据
     *
     * @param splitLyricsLine               分割好的文本
     * @param lyricsLineInfo                当前行数据
     * @param transliterationLyricsLineInfo 当前行注音数据
     * @return 组装好的数据
     */
    private Pair<List<LyricsLineInfo>, List<LyricsLineInfo>> mapLyricsLineInfoList(List<String> splitLyricsLine, LyricsLineInfo lyricsLineInfo, LyricsLineInfo transliterationLyricsLineInfo) {
//        L.i(splitLyricsLine);
        List<LyricsLineInfo> originResult = new ArrayList<>();
        List<LyricsLineInfo> transliterationResult = new ArrayList<>();
        //没有进行过分割就直接返回
        if (splitLyricsLine.size() == 1) {
            LyricsUtils.splitLrcLyrics(lyricsLineInfo,mRollPaint, MAX_Width);
            originResult.add(lyricsLineInfo);
            if (transliterationLyricsLineInfo != null) {
                LyricsUtils.splitLrcLyrics(transliterationLyricsLineInfo,mRollPaint, MAX_Width);
                transliterationLyricsLineInfo.setWordsDisInterval(lyricsLineInfo.getWordsDisInterval());
                transliterationResult.add(transliterationLyricsLineInfo);
                L.i("transliterationResult:"+transliterationResult);
            }

            return new Pair<>(originResult, transliterationResult);
        }

        String[] lyricsWords = lyricsLineInfo.getLyricsWords();
        int[] wordsIndex = lyricsLineInfo.getWordsIndex();
        long[] wordsStartTime = lyricsLineInfo.getWordsStartTime();
        long[] wordsDisInterval = lyricsLineInfo.getWordsDisInterval();

        int baseIndex = 0;
        String[] tempLyricsWords;
        int[] tempWordsIndex;
        long[] tempWordsStartTime;
        long[] tempWordsDisInterval;
        long tempStartTime = lyricsLineInfo.getStartTime();


        for (String s : splitLyricsLine) {
            LyricsLineInfo tempLyricsLineInfo = new LyricsLineInfo();
            int endIndex = getEndIndex(lyricsWords, s, baseIndex);
            if (endIndex == baseIndex) {
                baseIndex = endIndex - 1;
            }
            tempWordsIndex = Arrays.copyOfRange(wordsIndex, baseIndex, endIndex);
            tempWordsStartTime = Arrays.copyOfRange(wordsStartTime, baseIndex, endIndex);
            tempWordsDisInterval = Arrays.copyOfRange(wordsDisInterval, baseIndex, endIndex);
            tempLyricsWords = Arrays.copyOfRange(lyricsWords, baseIndex, endIndex);
            long sumEndTime = sumEndTime(tempWordsDisInterval);
            long sStartTime = tempStartTime;
            tempLyricsLineInfo.setStartTime(sStartTime);
            tempLyricsLineInfo.setEndTime(sStartTime + sumEndTime);
            tempStartTime += sumEndTime;
            if (transliterationLyricsLineInfo != null) {
                LyricsLineInfo tempTlLyricsLineInfo = new LyricsLineInfo();
                String[] transliterationWords = transliterationLyricsLineInfo.getLyricsWords();
                //数据不规范是真恶心
                if (baseIndex >= transliterationWords.length || endIndex > transliterationWords.length) {
                    tempTlLyricsLineInfo.setLyricsWords(Arrays.copyOfRange(lyricsWords, baseIndex, endIndex));
                    tempTlLyricsLineInfo.setLineLyrics(sumTransliteration(Arrays.copyOfRange(lyricsWords, baseIndex, endIndex)));
                } else {
                    tempTlLyricsLineInfo.setLyricsWords(Arrays.copyOfRange(transliterationLyricsLineInfo.getLyricsWords(), baseIndex, endIndex));
                    tempTlLyricsLineInfo.setLineLyrics(sumTransliteration(Arrays.copyOfRange(transliterationLyricsLineInfo.getLyricsWords(), baseIndex, endIndex)));
                }
                LyricsUtils.splitLrcLyrics(tempTlLyricsLineInfo,mRollPaint, MAX_Width);
                transliterationLyricsLineInfo.setWordsDisInterval(lyricsLineInfo.getWordsDisInterval());
                transliterationResult.add(tempTlLyricsLineInfo);
                L.i("transliterationResult:"+tempTlLyricsLineInfo);
            }
            tempLyricsLineInfo.setLineLyrics(s);
            tempLyricsLineInfo.setWordsIndex(tempWordsIndex);
            tempLyricsLineInfo.setWordsStartTime(tempWordsStartTime);
            tempLyricsLineInfo.setWordsDisInterval(tempWordsDisInterval);
            tempLyricsLineInfo.setLyricsWords2(tempLyricsWords);
            LyricsUtils.splitLrcLyrics(tempLyricsLineInfo,mRollPaint,MAX_Width);
            originResult.add(tempLyricsLineInfo);
            baseIndex = endIndex;
        }
//        L.i("originResult:"+originResult+"\ntransliterationResult:"+transliterationResult);
        return new Pair<>(originResult, transliterationResult);
    }

    private <T> T[] copyOfRange(T[] array, int startIndex, int endIndex) {
        if (startIndex == endIndex) {
            return array.clone();
        }
        return Arrays.copyOfRange(array, startIndex, endIndex);
    }

    private long sumEndTime(long[] tempEndTime) {
        int sum = 0;
        for (long num : tempEndTime) {
            sum += num;
        }
        return sum;
    }

    private String sumTransliteration(String[] transliteration) {
        StringBuilder sb = new StringBuilder();
        for (String s : transliteration) {
            sb.append(s);
        }
        return sb.toString();
    }


    private int getEndIndex(String[] lyricsWords, String lineLyricsWords, int startIndex) {
        if (TextUtils.isEmpty(lineLyricsWords) || lyricsWords.length == 0) {
            return 0;
        }
//        L.i("lineLyricsWords:"+ lineLyricsWords);
        StringBuilder tempWords = new StringBuilder();
        for (int i = startIndex; i < lyricsWords.length; i++) {
            if (tempWords.toString().equals(lineLyricsWords)) {
                return i;
            }
            tempWords.append(lyricsWords[i]);
//            L.i("tempWord:"+tempWords.toString()+ "  lyricsWords:"+lyricsWords[i]);
        }
        return lyricsWords.length;
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

                long startTime = Long.parseLong(lineTime[0]);
                long endTime = startTime + Long.parseLong(lineTime[1]);
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
                    wordsStartTime[index] = Long.parseLong(wordsDisIntervalTemp[0]) + startTime;
                    //获取每个字的持续时间
                    wordsDisInterval[index] = Long.parseLong(wordsDisIntervalTemp[1]);
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
        List<LyricsLineInfo> transliterationLrcLineInfos = new ArrayList<>();

        List<String> tempTransliterationLrcLineInfo = new ArrayList<>();
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
            //检查是否是对唱类型
//            checkTranslateIsSexStart(tempTransliterationLrcLineInfo, lineLyrics.toString());
//            if (tempTransliterationLrcLineInfo.size() == 1) {
//                continue;
//            }
//            String mergeTemp = mergeTranslateIsSexStart(tempTransliterationLrcLineInfo);

            transliterationLrcLineInfo.setLineLyrics(lineLyrics.toString());
            transliterationLrcLineInfo.setLyricsWords(lyricsWords);

            transliterationLrcLineInfos.add(transliterationLrcLineInfo);
            tempTransliterationLrcLineInfo.clear();
        }
        // 添加音译歌词
        if (transliterationLrcLineInfos.size() > 0) {
//            L.i("transliterationLrcLineInfos.size() = " + transliterationLrcLineInfos.size());
            lyricsIfno.setTransliterationLrcLineInfos(transliterationLrcLineInfos);
        }
    }

    private String[] mergeTempArray(String[] lyricsWords, String sexStart) {
        String[] mergeTempArray = new String[lyricsWords.length + 1];
        mergeTempArray[0] = sexStart;
        for (int i = 0; i < lyricsWords.length; i++) {
            mergeTempArray[i + 1] = lyricsWords[i];
        }
        return mergeTempArray;
    }

    /**
     * 解析翻译歌词
     *
     * @param lyricsInfo
     * @param lyricContentArrayObj
     */
    private void parserTranslateLrc(LyricsInfo lyricsInfo,
                                    JSONArray lyricContentArrayObj) throws Exception {

        // 翻译歌词集合
        List<TranslateLrcLineInfo> translateLrcLineInfos = new ArrayList<>();
        List<String> tempLrcLineInfo = new ArrayList<>();

        // 获取歌词内容
        for (int j = 0; j < lyricContentArrayObj.length(); j++) {
            JSONArray lrcDataArrayObj = lyricContentArrayObj.getJSONArray(j);
            String lrcComtext = lrcDataArrayObj.getString(0);
            checkTranslateIsSexStart(tempLrcLineInfo, lrcComtext);
            if (tempLrcLineInfo.size() == 1) {
                continue;
            }
            String mergeLrcComtext = mergeTranslateIsSexStart(tempLrcLineInfo);

            // 翻译行歌词
            TranslateLrcLineInfo translateLrcLineInfo = new TranslateLrcLineInfo();
            translateLrcLineInfo.setLineLyrics(TextUtils.isEmpty(mergeLrcComtext) ? lrcComtext : mergeLrcComtext);

            translateLrcLineInfos.add(translateLrcLineInfo);
        }
        // 添加翻译歌词
        if (translateLrcLineInfos.size() > 0) {
            lyricsInfo.setTranslateLrcLineInfos(translateLrcLineInfos);
        }
    }

    private void checkTranslateIsSexStart(List<String> temp, String lrcComtext) {
        if (TextUtils.isEmpty(lrcComtext)) {
            return;
        }
        if (temp.size() == 1) {
            temp.add(lrcComtext);
            return;
        }
        String trimLrc = lrcComtext.trim();
        if (trimLrc.endsWith(":") || trimLrc.endsWith("：")) {
            temp.add(lrcComtext);
        }
    }

    private String mergeTranslateIsSexStart(List<String> temp) {
        if (temp.size() == 2) {
            return temp.get(0).concat(temp.get(1));
        }
        return "";
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
