package com.nick.music.view;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.TimeUtils;
import com.nick.music.model.LyricsInfo;
import com.xyz.base.utils.L;
import com.nick.music.model.LyricsLineInfo;
import com.nick.music.util.ColorUtils;
import com.nick.music.util.LyricsUtils;

import java.util.List;
import java.util.TreeMap;


/**
 * @Description: 多行歌词:歌词行号和view所在位置关联,Scroller只做动画处理，不去移动view
 * @param:
 * @return:
 * @throws
 * @author: zhangliangming
 * @date: 2018-04-21 20:28
 */
public class ManyLyricsView extends AbstractLrcView {

    /**
     * 初始
     */
    private final int TOUCHEVENTSTATUS_INIT = 0;

    /**
     * 滑动越界
     */
    private final int TOUCHEVENTSTATUS_OVERSCROLL = 1;
    /**
     * 快速滑动
     */
    private final int TOUCHEVENTSTATUS_FLINGSCROLL = 2;

    /**
     * 触摸状态
     */
    private int mTouchEventStatus = TOUCHEVENTSTATUS_INIT;

    /////////////////////////////////////////////////
    /**
     * 画时间线指示器
     ***/
    private Paint mPaintIndicator;
    /**
     * 画线
     */
    private Paint mPaintLine;

    /**
     * 画线颜色
     */
    private int mPaintLineColor = Color.WHITE;

    /**
     * 绘画播放按钮
     */
    private Paint mPaintPlay;
    /**
     * 播放按钮区域
     */
    private Rect mPlayBtnRect;

    /**
     * 是否在播放按钮区域
     */
    private boolean isInPlayBtnRect = false;
    /**
     * 播放按钮区域字体大小
     */
    private int mPlayRectSize = 25;
    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     * 滚动类
     */
    private Scroller mScroller;
    /**
     * Y轴移动的时间
     */
    private int mDuration = 350;

    /**
     * 额外歌词加倍：Y轴移动的时间
     */
    private boolean isExtraDoubleDuration = false;

    ///////////////////////////////////////////////////
    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;
    /**
     * 视图y中间
     */
    private float mCentreY = 0;
    /**
     * 颜色渐变梯度
     */
    private int mMaxAlpha = 255;
    private int mMinAlpha = 50;
    //渐变的高度
    private int mShadeHeight = 0;
    /**
     * 记录手势
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    //用于判断拦截
    private int mInterceptX = 0;
    private int mInterceptY = 0;
    /**
     * 单行Lrc歌词换行间隙
     */
    private int mLrcVerticalSpace = 5;
    /**
     * 触摸最后一次的坐标
     */
    private int mLastY;
    /**
     * 是否直接拦截
     */
    private boolean mIsTouchIntercept = false;

    /**
     * 是否允许触摸
     */
    private boolean mTouchAble = true;

    /**
     * 是否绘画时间线
     */
    private boolean mIsDrawIndicator = true;

    //////////////////////////////////////////////////////

    /**
     * 还原歌词视图
     */
    private final int RESETLRCVIEW = 1;
    /**
     *
     */
    private int mResetDuration = 3000;

    /**
     * Handler处理滑动指示器隐藏和歌词滚动到当前播放的位置
     */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case RESETLRCVIEW:
                    if (mScroller.computeScrollOffset()) {
                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {

                        mIsTouchIntercept = false;
                        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                        int lyricsLineNum = getLyricsLineNum();
                        int deltaY = getLineAtHeightY(lyricsLineNum) - mScroller.getFinalY();
                        mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
                        invalidateView();
                    }

                    break;
            }
        }
    };

    /**
     * 歌词快进事件
     */
    private OnLrcClickListener mOnLrcClickListener;
    /**
     * 指示器
     */
    private OnIndicatorListener mOnIndicatorListener;
    /**
     * 暂停刷新flag
     */
    private boolean mForcePause = false;

    ValueAnimator mCenterLyricsEnlargeAnimator = ValueAnimator.ofFloat(mSMFontSize, mFontSize);
    ValueAnimator mCenterLyricsSmallerAnimator = ValueAnimator.ofFloat(mFontSize, mSMFontSize);

    public ManyLyricsView(Context context) {
        super(context);
        init(context);
    }

    public ManyLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    /**
     * @throws
     * @Description: 初始
     * @param:
     * @return:
     * @author: zhangliangming
     * @date: 2018-04-21 9:08
     */
    private void init(Context context) {

        //初始化
        mScroller = new Scroller(context, new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        //画指时间示器
        mPaintIndicator = new Paint();
        mPaintIndicator.setDither(true);
        mPaintIndicator.setAntiAlias(true);

        //画线
        mPaintLine = new Paint();
        mPaintLine.setDither(true);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStyle(Paint.Style.FILL);


        //绘画播放按钮
        mPaintPlay = new Paint();
        mPaintPlay.setDither(true);
        mPaintPlay.setAntiAlias(true);
        mPaintPlay.setStrokeWidth(2);


        setGotoSearchTextColor(Color.WHITE);
        setGotoSearchTextPressedColor(ColorUtils.parserColor("#0288d1"));

        //获取屏幕宽度
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        int screensWidth = displayMetrics.widthPixels;

        //设置歌词的最大宽度
        int textMaxWidth = screensWidth / 3 * 2;
        setTextMaxWidth(textMaxWidth);

        //设置画笔大小
        mPaintIndicator.setTextSize(mPlayRectSize);
        mPaintLine.setTextSize(mPlayRectSize);
        mPaintPlay.setTextSize(mPlayRectSize);

        Paint paint = getPaint();
        Paint paintHL = getPaintHL();
        mCenterLyricsEnlargeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            paint.setTextSize(value);
            paintHL.setTextSize(value);
        });
        mCenterLyricsEnlargeAnimator.setDuration(500);
        Paint lastUpLinePaint = getLastUpLinePaint();
        mCenterLyricsSmallerAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            lastUpLinePaint.setTextSize(value);
        });
        mCenterLyricsSmallerAnimator.setDuration(500);
    }

    public void setData(TreeMap<Integer, LyricsLineInfo> treeMap,List<LyricsLineInfo> translateLrcLineInfos,List<LyricsLineInfo> transliterationLrcLineInfos,int lyricsInfoType,long position){
//        L.i("setData: "+position);
        mForcePause = true;
        setVisibility(INVISIBLE);
        mScroller.startScroll(0, mScroller.getFinalY(), 0, -mScroller.getFinalY(), 200);
        mHandler.postDelayed(()->{
            mForcePause = false;
            setVisibility(VISIBLE);
        },1200);
//        L.i(String.format("getFinalY:%s ",mScroller.getFinalY()));

        super.setData(treeMap,translateLrcLineInfos,transliterationLrcLineInfos,lyricsInfoType);

        if (position>0){
            mHandler.postDelayed(()->{
                mForcePause = false;
                updateManyLrcView(position);
            },150);
        }

    }

    @Override
    protected void onDrawLrcView(Canvas canvas) {
        if (mShadeHeight == 0) {
            mShadeHeight = getHeight() / 4;
        }
        drawManyLrcView(canvas);
    }

    @Override
    protected void updateView(long playProgress) {
        updateManyLrcView(playProgress);
    }

    public float setCenterTextSize(float size){
        float fs = mFontSize;
        L.i("setCenterTextSize: "+ size);
        Paint paint = getPaint();
        Paint paintHL = getPaintHL();
        paint.setTextSize(size);
        paintHL.setTextSize(size);
        mFontSize = size;
        invalidateView();
        return fs;
    }

    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawManyLrcView(Canvas canvas) {

        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        if (lrcLineInfos.isEmpty()){
            return;
        }
        Paint paint = getPaint();
        Paint paintSM = getSMPaint();
        Paint paintHL = getPaintHL();
        Paint lastUpLinePaint = getLastUpLinePaint();
        Paint extraLrcPaint = getExtraLrcPaint();
        Paint extraLrcPaintHL = getExtraLrcPaintHL();
        int lyricsLineNum = getLyricsLineNum();
        int splitLyricsLineNum = getSplitLyricsLineNum();
        int splitLyricsWordIndex = getSplitLyricsWordIndex();
        int extraSplitLyricsLineNum = getExtraSplitLyricsLineNum();
        int extraSplitLyricsWordIndex = getExtraSplitLyricsWordIndex();
        float spaceLineHeight = getSpaceLineHeight();
        float extraLrcSpaceLineHeight = getExtraLrcSpaceLineHeight();
        float lyricsWordHLTime = getLyricsWordHLTime();
        float translateLyricsWordHLTime = getTranslateLyricsWordHLTime();


        //获取中间位置
        mCentreY = (getHeight() + LyricsUtils.getTextHeight(paintSM)) * 0.5f + getLineAtHeightY(lyricsLineNum) - mOffsetY;
//        L.i(String.format("mCentreY: %s, height: %s, TextHeight:%s, LineAtHeightY: %s , mOffsetY:%s",mCentreY,getHeight(),LyricsUtils.getTextHeight(paintSM),getLineAtHeightY(lyricsLineNum),mOffsetY));

        //画当前行歌词
        //获取分割后的歌词列表
        LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(lyricsLineNum);
        if (lyricsLineInfo==null){
            return;
        }
        List<LyricsLineInfo> splitLyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        float lineBottomY = drawCenterLyrics(canvas, paint, paintHL, splitLyricsLineInfos, splitLyricsLineNum, splitLyricsWordIndex, spaceLineHeight, lyricsWordHLTime, mCentreY);

        //未超出下视图
        if (lineBottomY + spaceLineHeight < getHeight()) {
            //画额外歌词
            lineBottomY = drawDownExtraLyrics(canvas, extraLrcPaint, extraLrcPaintHL, lyricsLineNum, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, lyricsWordHLTime, translateLyricsWordHLTime, lineBottomY);
        }

        //画当前行下面的歌词
        for (int i = lyricsLineNum + 1; i < lrcLineInfos.size(); i++) {

            //超出下视图
            if (lineBottomY + spaceLineHeight > getHeight()*1.2f) {
                break;
            }

            LyricsLineInfo downLyricsLineInfo = lrcLineInfos.get(i);
            if (downLyricsLineInfo==null){
                continue;
            }
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = downLyricsLineInfo.getSplitLyricsLineInfos();
            lineBottomY = drawDownLyrics(canvas, extraLrcPaint, extraLrcPaintHL, lyricsLineInfos, -1, -2, spaceLineHeight, -1, lineBottomY);

            //超出下视图
            if (lineBottomY + spaceLineHeight > getHeight()*1.2f) {
                break;
            }


            //画额外歌词
            lineBottomY = drawDownExtraLyrics(canvas, extraLrcPaint, extraLrcPaintHL, i, -1, -2, extraLrcSpaceLineHeight, -1, -1, lineBottomY);
        }


        // 画当前歌词之前的歌词
        float lineTopY = mCentreY;
        for (int i = lyricsLineNum - 1; i >= 0; i--) {
            if (lineTopY < 0) {
                break;
            }
            LyricsLineInfo upLyricsLineInfo = lrcLineInfos.get(i);
            if (upLyricsLineInfo==null){
                continue;
            }
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = upLyricsLineInfo.getSplitLyricsLineInfos();
            if (i == lyricsLineNum - 1){
                lineTopY = drawUpExtraLyrics(canvas, lastUpLinePaint, lyricsLineInfos, i, extraLrcSpaceLineHeight, lineTopY);
            }else {
                lineTopY = drawUpExtraLyrics(canvas, extraLrcPaint, lyricsLineInfos, i, extraLrcSpaceLineHeight, lineTopY);
            }

        }

//        //绘画时间、播放按钮等
//        if (mIsTouchIntercept || mTouchEventStatus != TOUCHEVENTSTATUS_INIT) {
//
//            //画当前时间
//            int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
//            long startTime = lrcLineInfos.get(scrollLrcLineNum).getStartTime();
//
//            if (mIsDrawIndicator) {
//                drawIndicator(canvas, startTime);
//            }
//
//            //指示器回调
//            if (mOnIndicatorListener != null) {
//                mOnIndicatorListener.indicatorVisibleToUser(true, startTime);
//            }
//        } else {
//
//            //指示器回调
//            if (mOnIndicatorListener != null) {
//                mOnIndicatorListener.indicatorVisibleToUser(false, -1);
//            }
//        }
    }

    /**
     * 向下绘画动感歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param splitLyricsLineNum   分隔歌词行索引
     * @param splitLyricsWordIndex 分隔歌词字索引
     * @param spaceLineHeight      空行高度
     * @param lyricsWordHLTime     歌词高亮时间
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawCenterLyrics(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float fristLineTextY) {
        if (mLyricsinfoType == LyricsInfo.DYNAMIC){
            return drawCenterKrc(canvas, paint, paintHL, splitLyricsLineInfos, splitLyricsLineNum, splitLyricsWordIndex, spaceLineHeight, lyricsWordHLTime, fristLineTextY);
        }else {
            return drawCenterLrc(canvas, paint, paintHL, splitLyricsLineInfos, splitLyricsLineNum, splitLyricsWordIndex, spaceLineHeight, lyricsWordHLTime, fristLineTextY);
        }
    }

    private float drawCenterLrc(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float firstLineTextY){
        //获取数据
        int[] paintHLColors = getPaintHLColors();
        //
        float lineBottomY = 0;

        //当前文件一句歌词的高度，这一句可能会变成多句，因为超出屏幕自动换行
        //这里是总文本高度
        float totalHeight = LyricsUtils.getTextHeight(paint)*splitLyricsLineInfos.size();
        float textHeight = LyricsUtils.getTextHeight(paint);
        float startY = firstLineTextY - totalHeight/2 + textHeight;

        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {
            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            //你可能好奇为什么这里用mFontSize,因为上面的TextHeight仅仅只是显示这个文字的高度，而mFontSize是显示这个文字的方格
            //显示一个文字占用高度应该以方格高度来算
            float textY = startY + i * (mFontSize + mLrcVerticalSpace);
            LyricsUtils.drawText(canvas, paintHL, paintHLColors, text, textX, textY);
        }
        lineBottomY = firstLineTextY + totalHeight  + spaceLineHeight;
//        canvas.drawLine(0, firstLineTextY, 720, firstLineTextY, paint);
//        L.i(String.format("drawCenterLrc firstLineTextY:%s lineBottomY:%s",firstLineTextY,lineBottomY));
        return lineBottomY;
    }

    private float drawCenterKrc(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float fristLineTextY){
        //获取数据
        int[] paintColors = getPaintColors();
        int[] paintHLColors = getPaintHLColors();
        Paint smPaint = getSMPaint();

        //
        float lineBottomY = 0;

        //歌词和空行高度
        float lineHeight = LyricsUtils.getTextHeight(smPaint) + spaceLineHeight;
        //往下绘画歌词
        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {

            String text = splitLyricsLineInfos.get(i).getLineLyrics();

            lineBottomY = fristLineTextY + i * lineHeight ;

//            //超出上视图
//            if (lineBottomY < lineHeight) {
//                continue;
//            }
            //超出下视图
            if (lineBottomY + spaceLineHeight > getHeight()) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineBottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineBottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);
            paintHL.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            //
//            L.i(String.format("i:%s splitLyricsLineNum:%s",i,splitLyricsLineNum));
            if (i < splitLyricsLineNum) {
                LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineBottomY);
                LyricsUtils.drawText(canvas, paintHL, paintHLColors, text, textX, lineBottomY);

            } else if (i == splitLyricsLineNum) {
                //绘画动感歌词
                float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(LyricsInfo.DYNAMIC, paint, splitLyricsLineInfos.get(i), splitLyricsWordIndex, lyricsWordHLTime);
//                L.i(String.format("text: %s  lineLyricsHLWidth: %s",text,lineLyricsHLWidth));
                LyricsUtils.drawDynamicText(canvas, paint, paintHL, paintColors, paintHLColors, text, lineLyricsHLWidth, textX, lineBottomY);

            } else if (i > splitLyricsLineNum) {
                LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineBottomY);
            }

//            canvas.drawLine(0, lineBottomY - getTextHeight(paint), 720, lineBottomY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineBottomY, 720, lineBottomY, paint);
        }
        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineBottomY = fristLineTextY + lineHeight * (splitLyricsLineInfos.size());

        return lineBottomY;
    }

    /**
     * 向下绘画动感歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param splitLyricsLineNum   分隔歌词行索引
     * @param splitLyricsWordIndex 分隔歌词字索引
     * @param spaceLineHeight      空行高度
     * @param lyricsWordHLTime     歌词高亮时间
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawDownLyrics(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float fristLineTextY) {
        if (mLyricsinfoType == LyricsInfo.DYNAMIC){
            return drawDownLyricsKrc(canvas, paint, paintHL, splitLyricsLineInfos, splitLyricsLineNum, splitLyricsWordIndex, spaceLineHeight, lyricsWordHLTime, fristLineTextY);
        }else {
            return drawDownLyricsLrc(canvas, paint, paintHL, splitLyricsLineInfos, splitLyricsLineNum, splitLyricsWordIndex, spaceLineHeight, lyricsWordHLTime, fristLineTextY);
        }
    }

    private float drawDownLyricsLrc(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float firstLineTextY){
        int[] paintColors = getPaintColors();
        float lineBottomY = 0;
        //画布要比实际View要大一些，这样未进入到View的字幕会提前绘制好
        float canvasHeight = getHeight()*1.2f;
        float totalHeight = LyricsUtils.getTextHeight(paint)*splitLyricsLineInfos.size();
        float textHeight = LyricsUtils.getTextHeight(paint);
        float startY = firstLineTextY - totalHeight/2 + textHeight;
        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {
            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            lineBottomY = firstLineTextY + i * textHeight;
            if (lineBottomY + spaceLineHeight > canvasHeight) {
                break;
            }
            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineBottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineBottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }
            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            float textY = startY + i * (mSMFontSize+mLrcVerticalSpace);
            LyricsUtils.drawText(canvas, paint, paintColors, text, textX, textY);
        }

        lineBottomY = firstLineTextY + totalHeight  + spaceLineHeight;

        return lineBottomY;
    }

    private float drawDownLyricsKrc(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, float spaceLineHeight, float lyricsWordHLTime, float fristLineTextY){
        //获取数据
        int[] paintColors = getPaintColors();
        int[] paintHLColors = getPaintHLColors();
        Paint smPaint = getSMPaint();

        //
        float lineBottomY = 0;

        //画布要比实际View要大一些，这样未进入到View的字幕会提前绘制好
        float canvasHeight = getHeight()*1.2f;

        //歌词和空行高度
        float lineHeight = LyricsUtils.getTextHeight(smPaint) + spaceLineHeight;
        //往下绘画歌词
        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {

            String text = splitLyricsLineInfos.get(i).getLineLyrics();

            lineBottomY = fristLineTextY + i * lineHeight;

//            //超出上视图
//            if (lineBottomY < lineHeight) {
//                continue;
//            }
            //超出下视图
            if (lineBottomY + spaceLineHeight > canvasHeight) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineBottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineBottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);
            paintHL.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            //
            if (i < splitLyricsLineNum) {
                LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineBottomY);
                LyricsUtils.drawText(canvas, paintHL, paintHLColors, text, textX, lineBottomY);

            } else if (i == splitLyricsLineNum) {
                //绘画动感歌词
                float lineLyricsHLWidth = LyricsUtils.getLineLyricsHLWidth(LyricsInfo.DYNAMIC, paint, splitLyricsLineInfos.get(i), splitLyricsWordIndex, lyricsWordHLTime);
//                L.i(String.format("text: %s  lineLyricsHLWidth: %s",text,lineLyricsHLWidth));
                LyricsUtils.drawDynamicText(canvas, paint, paintHL, paintColors, paintHLColors, text, lineLyricsHLWidth, textX, lineBottomY);

            } else if (i > splitLyricsLineNum) {
                LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineBottomY);
            }

//            canvas.drawLine(0, lineBottomY - getTextHeight(paint), 720, lineBottomY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineBottomY, 720, lineBottomY, paint);
        }
        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineBottomY = fristLineTextY + lineHeight * (splitLyricsLineInfos.size());

        return lineBottomY;
    }


    /**
     * 绘画向下的额外歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param lyricsLineNum
     * @param extraSplitLyricsLineNum
     * @param extraSplitLyricsWordIndex
     * @param extraLrcSpaceLineHeight
     * @param lyricsWordHLTime
     * @param translateLyricsWordHLTime
     * @param lineBottomY
     * @return
     */
    private float drawDownExtraLyrics(Canvas canvas, Paint paint, Paint paintHL, int lyricsLineNum, int extraSplitLyricsLineNum, int extraSplitLyricsWordIndex, float extraLrcSpaceLineHeight, float lyricsWordHLTime, float translateLyricsWordHLTime, float lineBottomY) {
        //获取数据
        int extraLrcStatus = getExtraLrcStatus();
        float spaceLineHeight = getSpaceLineHeight();
        int translateDrawType = getTranslateDrawType();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();

        //
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = translateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += extraLrcSpaceLineHeight - spaceLineHeight;
                if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC && translateDrawType == AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC) {
                    lineBottomY = drawDownLyrics(canvas, paint, paintHL, translateSplitLyricsLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, translateLyricsWordHLTime, lineBottomY);
                } else {
                    //画lrc歌词
                    int splitLyricsLineNum = -1;
                    //高亮绘画lrc歌词
                    if (getTranslateDrawLrcColorType() == AbstractLrcView.TRANSLATE_DRAW_LRC_COLOR_HL) {
                        splitLyricsLineNum = extraSplitLyricsLineNum;
                    }
                    lineBottomY = drawDownLyrics(canvas, paint, paintHL, translateSplitLyricsLineInfos, splitLyricsLineNum, -2, extraLrcSpaceLineHeight, -1, lineBottomY);
                }
                lineBottomY += spaceLineHeight - extraLrcSpaceLineHeight;
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = transliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += extraLrcSpaceLineHeight - spaceLineHeight;
                lineBottomY = drawDownLyrics(canvas, paint, paintHL, transliterationSplitLrcLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, lyricsWordHLTime, lineBottomY);
                lineBottomY += spaceLineHeight - extraLrcSpaceLineHeight;
            }
        }
        return lineBottomY;
    }

    /**
     * 绘画向上的额外歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos
     * @param lyricsLineNum
     * @param extraLrcSpaceLineHeight
     * @param lineTopY                @return
     */
    private float drawUpExtraLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, int lyricsLineNum, float extraLrcSpaceLineHeight, float lineTopY) {
        //获取数据
        int extraLrcStatus = getExtraLrcStatus();
        float spaceLineHeight = getSpaceLineHeight();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();

        //
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = translateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (LyricsUtils.getTextHeight(paint) + spaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, translateSplitLyricsLineInfos, extraLrcSpaceLineHeight, lineTopY);
                if (lineTopY < 0) {
                    return lineTopY;
                }
                lineTopY -= (LyricsUtils.getTextHeight(paint) + extraLrcSpaceLineHeight);
                //
                lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, spaceLineHeight, lineTopY);
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = transliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (LyricsUtils.getTextHeight(paint) + spaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, transliterationSplitLrcLineInfos, extraLrcSpaceLineHeight, lineTopY);

                if (lineTopY < 0) {
                    return lineTopY;
                }

                lineTopY -= (LyricsUtils.getTextHeight(paint) + extraLrcSpaceLineHeight);

                //
                lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, spaceLineHeight, lineTopY);
            }
        } else {
            lineTopY = drawUpLyrics(canvas, paint, splitLyricsLineInfos, spaceLineHeight, lineTopY);
        }
        return lineTopY;
    }

    /**
     * 向上绘画歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param spaceLineHeight      空行高度
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawUpLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, float spaceLineHeight, float fristLineTextY) {
        if (mLyricsinfoType == LyricsInfo.DYNAMIC){
            fristLineTextY -= (LyricsUtils.getTextHeight(paint) + spaceLineHeight);
            return drawUpLyricsKrc(canvas, paint, splitLyricsLineInfos, spaceLineHeight, fristLineTextY);
        }else {
            return drawUpLyricsLrc(canvas, paint, splitLyricsLineInfos, spaceLineHeight, fristLineTextY);
        }
    }

    private float drawUpLyricsKrc(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, float spaceLineHeight, float firstLineTextY){
        int[] paintColors = getPaintColors();
        Paint smPaint = getSMPaint();

        float lineTopY = firstLineTextY;
        //歌词和空行高度
        float lineHeight = LyricsUtils.getTextHeight(smPaint) + spaceLineHeight;
        for (int i = splitLyricsLineInfos.size() - 1; i >= 0; i--) {
            if (i != splitLyricsLineInfos.size() - 1) {
                lineTopY -= lineHeight;
            }

            //超出上视图
            if (lineTopY < 0) {
                return -1;
            }
//            if (lineTopY < lineHeight) {
//                return -1;
//            }
//            //超出下视图
////            if (lineTopY + spaceLineHeight > getHeight()) {
////                continue;
////            }

            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineTopY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineTopY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineTopY);


//            canvas.drawLine(0, lineTopY - getTextHeight(paint), 720, lineTopY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineTopY, 720, lineTopY, paint);

        }

        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineTopY = firstLineTextY - lineHeight * (splitLyricsLineInfos.size() - 1);
        return lineTopY;
    }

    private float drawUpLyricsLrc(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, float spaceLineHeight, float firstLineTextY){
        int[] paintColors = getPaintColors();
        Paint smPaint = getSMPaint();
        float lineTopY = firstLineTextY;
        float totalHeight = LyricsUtils.getTextHeight(smPaint)*splitLyricsLineInfos.size();
        float textHeight = LyricsUtils.getTextHeight(smPaint);
        //歌词和空行高度
        float lineHeight = textHeight + spaceLineHeight;
        for (int i = splitLyricsLineInfos.size() - 1; i >= 0; i--) {
            if (i != splitLyricsLineInfos.size() - 1) {
                lineTopY -= (mSMFontSize+mLrcVerticalSpace);
            }else {
                lineTopY -= lineHeight;
            }
            //超出上视图
            if (lineTopY < 0) {
                return -1;
            }
            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineTopY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineTopY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);

            float textWidth = LyricsUtils.getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            LyricsUtils.drawText(canvas, paint, paintColors, text, textX, lineTopY);

        }

        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineTopY = firstLineTextY - totalHeight - spaceLineHeight;
        return lineTopY;
    }

    /**
     * 绘画时间、播放按钮等
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas, long startTime) {
        String timeString = TimeUtils.millis2String(startTime);
        int textHeight = LyricsUtils.getTextHeight(mPaintIndicator);
        float textWidth = LyricsUtils.getTextWidth(mPaintIndicator, timeString);
        int padding = 10;
        float textX = padding;
        float textY = (getHeight() + textHeight) / 2;
        canvas.drawText(timeString, textX, textY, mPaintIndicator);

        mPaintPlay.setStyle(Paint.Style.STROKE);
        //圆形矩形
        if (mPlayBtnRect == null)
            mPlayBtnRect = new Rect();
        //圆半径
        int circleR = mPlayRectSize;
        int linePadding = padding * 2;
        int rectR = getWidth() - linePadding;
        int rectL = rectR - circleR * 2;
        int rectT = getHeight() / 2;
        int rectB = rectT + circleR * 2;
        mPlayBtnRect.set(rectL - padding, rectT - padding, rectR + padding, rectB + padding);

        //画圆
        int cx = rectL + (rectR - rectL) / 2;
        int cy = rectT;
        canvas.drawCircle(cx, cy, circleR, mPaintPlay);

        //画三角形
        Path trianglePath = new Path();
        float startX = cx + circleR / 2;
        float startY = rectT;
        trianglePath.moveTo(startX, startY);// 此点为多边形的起点
        float pleftX = startX - (float) circleR / 4 * 3;
        float ptopY = startY - circleR * (float) Math.sqrt(3) / 4;
        float pbomY = startY + circleR * (float) Math.sqrt(3) / 4;
        trianglePath.lineTo(pleftX, ptopY);
        trianglePath.lineTo(pleftX, pbomY);
        trianglePath.close();// 使这些点构成封闭的多边形
        if (isInPlayBtnRect) {
            mPaintPlay.setStyle(Paint.Style.FILL);
        } else {
            mPaintPlay.setStyle(Paint.Style.STROKE);
        }
        canvas.drawPath(trianglePath, mPaintPlay);

        //画线
        int lineH = 2;
        float lineY = (getHeight() - lineH) / 2;
        float lineLeft = textX + textWidth + linePadding;
        float lineR = rectL - linePadding;
        LinearGradient linearGradientHL = new LinearGradient(lineLeft, lineY + lineH, lineR, lineY + lineH, new int[]{ColorUtils.parserColor(mPaintLineColor, 255), ColorUtils.parserColor(mPaintLineColor, 0), ColorUtils.parserColor(mPaintLineColor, 0), ColorUtils.parserColor(mPaintLineColor, 255)}, new float[]{0f, 0.2f, 0.8f, 1f}, Shader.TileMode.CLAMP);
        mPaintLine.setShader(linearGradientHL);
        canvas.drawRect(lineLeft, lineY, lineR, lineY + lineH, mPaintLine);

    }

    public void setPauseFlag(boolean pauseFlag){
        mForcePause = pauseFlag;
    }

    /**
     * 更新歌词视图
     *
     * @param playProgress
     */
    public void updateManyLrcView(long playProgress) {
        if (mForcePause){
            return;
        }


        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        if (lrcLineInfos.isEmpty()){
            return;
        }

        int lyricsLineNum = getLyricsLineNum();

        //
        int newLyricsLineNum = LyricsUtils.getLineNumber(LyricsInfo.DYNAMIC, lrcLineInfos, playProgress, 0);

        if (newLyricsLineNum != lyricsLineNum) {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_INIT && !mIsTouchIntercept) {
                //初始状态
                int duration = mDuration;
                if (isExtraDoubleDuration) {
                    duration = duration * getLineSizeNum(lyricsLineNum);
                }
                int deltaY = getLineAtHeightY(newLyricsLineNum) - mScroller.getFinalY();
//                L.i(String.format("newLyricsLineNum:%s ,lyricsLineNum:%s ,getFinalY:%s ,deltaY:%s",newLyricsLineNum,lyricsLineNum,mScroller.getFinalY(),deltaY));
                mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, duration);
                centerLyricsScaleAnimaStart();



            }
            lyricsLineNum = newLyricsLineNum;
            setLyricsLineNum(lyricsLineNum);
        }
        updateSplitData(playProgress);
        invalidateView();

    }

    private void centerLyricsScaleAnimaStart(){
        if (getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC){
            mCenterLyricsSmallerAnimator.start();
            mCenterLyricsEnlargeAnimator.start();
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int lrcStatus = getLrcStatus();
        if (!mTouchAble || lrcStatus != AbstractLrcView.LRCSTATUS_LRC)
            return true;
        obtainVelocityTracker(event);
        int actionId = event.getAction();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:

                mLastY = (int) event.getY();
                mInterceptX = (int) event.getX();
                mInterceptY = (int) event.getY();

                //发送还原
                mHandler.removeMessages(RESETLRCVIEW);


                if (mPlayBtnRect != null && isPlayClick(event)) {
                    isInPlayBtnRect = true;
                    invalidateView();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                int deltaX = mInterceptX - curX;
                int deltaY = mInterceptY - curY;

                if (mIsTouchIntercept || (Math.abs(deltaY) > mTouchSlop && Math.abs(deltaX) < mTouchSlop)) {
                    mIsTouchIntercept = true;

                    int dy = mLastY - curY;

                    //创建阻尼效果
                    float finalY = mOffsetY + dy;

                    if (finalY < getTopOverScrollHeightY() || finalY > getBottomOverScrollHeightY()) {
                        dy = dy / 2;
                        mTouchEventStatus = TOUCHEVENTSTATUS_OVERSCROLL;


                    }

                    mScroller.startScroll(0, mScroller.getFinalY(), 0, dy, 0);
                    invalidateView();

                }

                mLastY = curY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //判断是否在滑动和是否点击了播放按钮
                if (isInPlayBtnRect) {

                    mHandler.removeMessages(RESETLRCVIEW);

                    if (mOnLrcClickListener != null) {

                        //获取当前滑动到的歌词播放行
                        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);

                        //Log.d("ManyLyricsView", "click scrollLrcLineNum = " + scrollLrcLineNum);

                        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
                        long startTime = lrcLineInfos.get(scrollLrcLineNum).getStartTime();
                        //加多100ms，确保可以定位到当前行
                        mOnLrcClickListener.onLrcPlayClicked(startTime + 100);

                    }
                    mIsTouchIntercept = false;
                    mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                    isInPlayBtnRect = false;
                    invalidateView();
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                    int yVelocity = (int) velocityTracker.getYVelocity();
                    int xVelocity = (int) velocityTracker.getXVelocity();

                    if (Math.abs(yVelocity) > mMinimumVelocity) {

                        int startX = 0;
                        int startY = mScroller.getFinalY();
                        int velocityX = -xVelocity;
                        int velocityY = -yVelocity;
                        int minX = 0;
                        int maxX = 0;

                        //
                        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
                        int lrcSumHeight = getLineAtHeightY(lrcLineInfos.size());
                        int minY = -getHeight() / 4;
                        int maxY = lrcSumHeight + getHeight() / 4;
                        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                        invalidateView();

                        mTouchEventStatus = TOUCHEVENTSTATUS_FLINGSCROLL;

                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {

                        if (mTouchEventStatus == TOUCHEVENTSTATUS_OVERSCROLL) {
                            resetLrcView();
                        } else {
                            //发送还原
                            mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);

                        }
                    }
                }
                releaseVelocityTracker();

                mLastY = 0;
                mInterceptX = 0;
                mInterceptY = 0;

                break;
            default:
        }

        return true;
    }

    /**
     * 判断是否是播放按钮点击
     *
     * @param event
     * @return
     */
    private boolean isPlayClick(MotionEvent event) {
        if (mPlayBtnRect == null) return false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        return mPlayBtnRect.contains(x, y);

    }

    /**
     * 判断该行总共有多少行歌词（原始歌词 + 分隔歌词）
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineSizeNum(int lyricsLineNum) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();


        //
        int lineSizeNum = 0;
        LyricsLineInfo lyricsLineInfo = lrcLineInfos
                .get(lyricsLineNum);
        //获取分割后的歌词列表
        List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        lineSizeNum += lyricsLineInfos.size();

        //判断是否有翻译歌词或者音译歌词
        if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += tempTranslateLrcLineInfos.size();
            }
        } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += tempTransliterationLrcLineInfos.size();
            }
        }

        return lineSizeNum;
    }


    /**
     * 获取所在歌词行的高度
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineAtHeightY(int lyricsLineNum) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        Paint smPaint = getSMPaint();
        Paint extraLrcPaint = getExtraLrcPaint();
        float spaceLineHeight = getSpaceLineHeight();
        float extraLrcSpaceLineHeight = getExtraLrcSpaceLineHeight();
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();

        //
        int lineAtHeightY = 0;
        for (int i = 0; i < lyricsLineNum; i++) {
            LyricsLineInfo lyricsLineInfo = lrcLineInfos.get(i);
            //获取分割后的歌词列表
            if (lyricsLineInfo==null){
                continue;
            }
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            if (mLyricsinfoType == LyricsInfo.DYNAMIC){
                lineAtHeightY += (LyricsUtils.getTextHeight(smPaint) + spaceLineHeight) * lyricsLineInfos.size();
            }else {
                lineAtHeightY += LyricsUtils.getTextHeight(smPaint) * lyricsLineInfos.size() + spaceLineHeight;
            }

            //判断是否有翻译歌词或者音译歌词
            if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTranslateLrcLineInfos.size();
                }
            } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTransliterationLrcLineInfos.size();
                }
            }
        }
        return lineAtHeightY;
    }

    /**
     * 获取滑动的当前行
     *
     * @return
     */
    private int getScrollLrcLineNum(float offsetY) {
        //获取数据
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        Paint paint = getPaint();
        Paint extraLrcPaint = getExtraLrcPaint();
        float spaceLineHeight = getSpaceLineHeight();
        float extraLrcSpaceLineHeight = getExtraLrcSpaceLineHeight();
        int extraLrcStatus = getExtraLrcStatus();
        List<LyricsLineInfo> translateLrcLineInfos = getTranslateLrcLineInfos();
        List<LyricsLineInfo> transliterationLrcLineInfos = getTransliterationLrcLineInfos();


        //
        int scrollLrcLineNum = -1;
        int lineHeight = 0;
        for (int i = 0; i < lrcLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = lrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineHeight += (LyricsUtils.getTextHeight(paint) + spaceLineHeight) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (translateLrcLineInfos != null && translateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTranslateLrcLineInfos = translateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTranslateLrcLineInfos.size();
                }
            } else if (extraLrcStatus == AbstractLrcView.EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (transliterationLrcLineInfos != null && transliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> tempTransliterationLrcLineInfos = transliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (LyricsUtils.getTextHeight(extraLrcPaint) + extraLrcSpaceLineHeight) * tempTransliterationLrcLineInfos.size();
                }
            }

            if (lineHeight > offsetY) {
                scrollLrcLineNum = i;
                break;
            }
        }
        if (scrollLrcLineNum == -1) {
            scrollLrcLineNum = lrcLineInfos.size() - 1;
        }
        return scrollLrcLineNum;
    }


    /**
     * @param event
     */

    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }


    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据
            mOffsetY = mScroller.getCurrY();

            invalidateView();
        } else {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_FLINGSCROLL) {
                resetLrcView();
            }
        }
    }

    /**
     * 还原歌词视图
     */
    private void resetLrcView() {

        if (mOffsetY < 0) {

            int deltaY = -mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();
        } else if (mOffsetY > getBottomOverScrollHeightY()) {
            TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();

            int deltaY = getLineAtHeightY(lrcLineInfos.size
                    () - 1) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();

        }
    }

    /**
     * 获取底部越界
     *
     * @return
     */
    private float getBottomOverScrollHeightY() {
        TreeMap<Integer, LyricsLineInfo> lrcLineInfos = getLrcLineInfos();
        if (lrcLineInfos == null) return 0;
        return getLineAtHeightY(lrcLineInfos.size());
    }

    /**
     * 获取顶部越界高度
     *
     * @return
     */
    private float getTopOverScrollHeightY() {
        return 0;
    }


    /**
     * 指示线颜色
     *
     * @param mPaintLineColor
     */
    public void setPaintLineColor(int mPaintLineColor) {
        this.mPaintLineColor = mPaintLineColor;
    }

    public void setTouchAble(boolean mTouchAble) {
        this.mTouchAble = mTouchAble;
    }


    /**
     * 初始歌词数据
     */
    public void initLrcData() {
        mScroller.setFinalY(0);
        mOffsetY = 0;
        mCentreY = 0;
        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
        super.initLrcData();
    }


    /**
     * 设置默认颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int[] paintColor) {
        setPaintColor(paintColor, false);
    }


    /**
     * 设置高亮颜色
     *
     * @param paintHLColor
     */
    public void setPaintHLColor(int[] paintHLColor) {
        setPaintHLColor(paintHLColor, false);
    }

    /**
     * 设置高亮颜色
     *
     * @param paintHLColor     至少两种颜色
     * @param isInvalidateView 是否更新视图
     */
    public void setPaintHLColor(int[] paintHLColor, boolean isInvalidateView) {
        mPaintIndicator.setColor(paintHLColor[0]);
        mPaintPlay.setColor(paintHLColor[0]);
        super.setPaintHLColor(paintHLColor, isInvalidateView);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        setTypeFace(typeFace, false);
    }

    /**
     * 设置字体文件
     *
     * @param typeFace
     * @param isInvalidateView 是否更新视图
     */
    public void setTypeFace(Typeface typeFace, boolean isInvalidateView) {
        if (isInvalidateView) {
            setTypeFace(typeFace, false);
            resetScrollerFinalY();
        }
        super.setTypeFace(typeFace, isInvalidateView);
    }

    /**
     * //字体大小、额外歌词显示或者空行大小改变，则对歌词的位置进行修改
     * 重置scroller的finaly
     */
    private void resetScrollerFinalY() {
        int lyricsLineNum = getLyricsLineNum();
        //字体大小、额外歌词显示或者空行大小改变，则对歌词的位置进行修改
        mOffsetY = getLineAtHeightY(lyricsLineNum);
        mScroller.setFinalY((int) mOffsetY);
    }


    /**
     * 设置空行高度
     *
     * @param spaceLineHeight
     */
    public void setSpaceLineHeight(float spaceLineHeight) {
        setSpaceLineHeight(spaceLineHeight, false);
    }

    /**
     * 设置额外空行高度
     *
     * @param extraLrcSpaceLineHeight
     */
    public void setExtraLrcSpaceLineHeight(float extraLrcSpaceLineHeight) {
        setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight, false);
    }

    /**
     * 设置额外歌词的显示状态
     *
     * @param extraLrcStatus
     */
    public void setExtraLrcStatus(int extraLrcStatus) {
        super.setExtraLrcStatus(extraLrcStatus);
        resetScrollerFinalY();
        super.setExtraLrcStatus(extraLrcStatus, true);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setFontSize(float fontSize) {
        setFontSize(fontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param isReloadData 是否重新加载数据及刷新界面
     */
    public void setFontSize(float fontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setFontSize(fontSize, false);
            resetScrollerFinalY();
        }

        super.setFontSize(fontSize, isReloadData);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     */
    public void setExtraLrcFontSize(float extraLrcFontSize) {
        setExtraLrcFontSize(extraLrcFontSize, false);
    }

    /**
     * 设置额外字体大小
     *
     * @param extraLrcFontSize
     * @param isReloadData     是否重新加载数据及刷新界面
     */
    public void setExtraLrcFontSize(float extraLrcFontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setExtraLrcFontSize(extraLrcFontSize, false);
            resetScrollerFinalY();
        }
        super.setExtraLrcFontSize(extraLrcFontSize, isReloadData);
    }

    /**
     * 设置歌词解析器
     *
     */
    public void setLyricsReader() {
        super.setLyricsReader();
        int extraLrcType = getExtraLrcType();
        //翻译歌词以动感歌词形式显示
        if (extraLrcType == AbstractLrcView.EXTRALRCTYPE_BOTH || extraLrcType == AbstractLrcView.EXTRALRCTYPE_TRANSLATELRC) {
            super.setTranslateDrawType(AbstractLrcView.TRANSLATE_DRAW_TYPE_DYNAMIC);
        }
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     */
    public void setSize(int fontSize, int extraFontSize) {
        setSize(fontSize, extraFontSize, false);
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraFontSize 额外歌词字体
     * @param isReloadData  是否重新加载数据及刷新界面
     */
    public void setSize(int fontSize, int extraFontSize, boolean isReloadData) {
        if (isReloadData) {
            super.setSize(fontSize, extraFontSize, false);
            resetScrollerFinalY();
        }
        super.setSize(fontSize, extraFontSize, isReloadData);
    }

    /**
     * 是否绘画时间指示器
     *
     * @param isDrawIndicator
     */
    public void setIsDrawIndicator(boolean isDrawIndicator) {
        this.mIsDrawIndicator = isDrawIndicator;
    }

    /**
     * 设置指示器字体大小
     *
     * @param fontSize
     */
    public void setIndicatorFontSize(int fontSize) {
        mPlayBtnRect = null;
        this.mPlayRectSize = fontSize;
        mPaintIndicator.setTextSize(mPlayRectSize);
        mPaintLine.setTextSize(mPlayRectSize);
        mPaintPlay.setTextSize(mPlayRectSize);
        invalidateView();
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setExtraDoubleDuration(boolean extraDoubleDuration) {
        isExtraDoubleDuration = extraDoubleDuration;
    }

    /**
     * 设置歌词点击事件
     *
     * @param onLrcClickListener
     */
    public void setOnLrcClickListener(OnLrcClickListener onLrcClickListener) {
        this.mOnLrcClickListener = onLrcClickListener;
    }

    /**
     * 设置指示器监听事件
     *
     * @param onIndicatorListener
     */
    public void setOnIndicatorListener(OnIndicatorListener onIndicatorListener) {
        this.mOnIndicatorListener = onIndicatorListener;
    }

    public float getTextSize(){
        return getPaint().getTextSize();
    }

    /**
     * 歌词事件
     */
    public interface OnLrcClickListener {
        /**
         * 歌词快进播放
         *
         * @param progress
         */
        void onLrcPlayClicked(long progress);

    }

    /**
     * 指示器事件
     */
    public interface OnIndicatorListener {
        /**
         * 指示器是否可视
         *
         * @param isVisibleToUser   对用户是否可视
         * @param scrollLrcProgress 滑动时的播放进度
         */
        void indicatorVisibleToUser(boolean isVisibleToUser, long scrollLrcProgress);
    }

}
