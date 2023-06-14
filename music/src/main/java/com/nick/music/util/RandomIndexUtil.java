package com.nick.music.util;

import java.util.Random;

/**
 * 获取随机下标
 */
public class RandomIndexUtil {

    private final Random random = new Random();
    private int index = 3;

    private RandomIndexUtil(){}

    public static RandomIndexUtil getInstance(){
        return Holder.sInstance;
    }

    public int getRandom(){
        int randomIndex = random.nextInt(10);
        if (index == randomIndex){
            return getRandom();
        }
        index = randomIndex;
        return randomIndex;
    }

    public static class Holder{
        public static final RandomIndexUtil sInstance = new RandomIndexUtil();
    }
}
