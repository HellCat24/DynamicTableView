package com.inqbarna.tablefixheaders.samples.model;

import android.graphics.Color;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by tac on 08.07.15.
 */
public class DummyTVProgram implements TVProgram {

    private long ONE_SECOND_IN_MILLIS = 1000;
    private long ONE_MINUTE = ONE_SECOND_IN_MILLIS * 60;

    private int MIN_DURATION = (int) (20 * ONE_MINUTE);
    private int DURATION_STEP = (int) (50 * ONE_MINUTE);

    long mStartDate;
    long mEndDate;

    String mTitle;
    String Description;

    private Color mColor;

    public DummyTVProgram() {
        mStartDate = Calendar.getInstance().getTime().getTime();
        mEndDate = mStartDate + new Random().nextInt(DURATION_STEP) + MIN_DURATION;
    }

    public DummyTVProgram(long startDate) {
        mStartDate = startDate;
        mEndDate = mStartDate + new Random().nextInt(DURATION_STEP) + MIN_DURATION;
    }

    @Override
    public long getStartTime() {
        return mStartDate;
    }

    @Override
    public long getEndTime() {
        return mEndDate;
    }

    @Override
    public long getDurationInSeconds() {
        return (mEndDate - mStartDate) / 1000;
    }

    @Override
    public String getTitle() {
        return "Stub Title";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public int getColor() {
        int color = Color.BLACK;
        if (getDurationInSeconds() % 5 == 0) {
            return Color.RED;
        }
        if (getDurationInSeconds() % 3 == 0) {
            return Color.GREEN;
        }
        if (getDurationInSeconds() % 2 == 0) {
            return Color.YELLOW;
        }
        return color;
    }
}
