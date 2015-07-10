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
    private int DURATION_STEP = (int) (80 * ONE_MINUTE);

    long mStartDate;
    long mEndDate;

    String mTitle;
    String Description;

    private int mColor;

    public DummyTVProgram() {
        this(Calendar.getInstance().getTime().getTime(), -1);
    }

    public DummyTVProgram(long startDate, long maxEndDate) {
        mStartDate = startDate;
        mEndDate = mStartDate + new Random().nextInt(DURATION_STEP) + MIN_DURATION;

        if (mEndDate > maxEndDate) {
            mEndDate = maxEndDate - 1000 * 60 * 5;
        }

        Random rnd = new Random();
        int count = rnd.nextInt(10);
        if (count % 5 == 0) {
            mColor = Color.RED;
        }
        if (count % 4 == 0) {
            mColor = Color.BLUE;
        }
        if (count % 3 == 0) {
            mColor = Color.GREEN;
        }
        if (count % 2 == 0) {
            mColor = Color.YELLOW;
        }
        if (mColor == 0) {
            mColor = Color.LTGRAY;
        }
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
    public long getDurationInMillis() {
        return mEndDate - mStartDate;
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
        return mColor;
    }

    @Override
    public void setDurationInSeconds(int durationInSeconds) {
        mEndDate += durationInSeconds * 1000;
    }

    public void setEndDate(long endDate) {
        this.mEndDate = endDate;
    }
}
