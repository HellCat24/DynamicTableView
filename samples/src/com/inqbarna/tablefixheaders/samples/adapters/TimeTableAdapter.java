package com.inqbarna.tablefixheaders.samples.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.inqbarna.tablefixheaders.samples.model.TVProgram;

import java.util.Calendar;
import java.util.List;

/**
 * Created by tac on 08.07.15.
 */
public class TimeTableAdapter extends BaseTableAdapter {

    private final int LEFT_HEADER = 0;
    private final int UPPER_HEADER = 1;
    private final int ITEM = 2;

    private final int HOUR_IN_SECONDS = 60 * 60;
    private final int HALF_HOUR_IN_SECONDS = HOUR_IN_SECONDS / 2;
    private final int THREE_DAYS_IN_SECONDS = HOUR_IN_SECONDS * 24 * 3;

    private int HALF_HOUR_IN_PIXELS;

    private int mFirstHeaderScroll;

    private List<List<TVProgram>> mBroadCast;

    private long mStartTime = Calendar.getInstance().getTimeInMillis();
    private long mEndTime = mStartTime + THREE_DAYS_IN_SECONDS;

    private int mMaxColumnCount;

    public TimeTableAdapter() {

    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getMaxColumnCount() {
        return mMaxColumnCount;
    }

    @Override
    public int getHorizontalHeaderWidth() {
        return 0;
    }

    @Override
    public int getVerticalHeaderWidth() {
        return 0;
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getWidth(int row, int column) {
        return 0;
    }

    @Override
    public int getWidth(int column) {
        return 0;
    }

    @Override
    public int getHeight(int row) {
        return 0;
    }

    @Override
    public int getItemViewType(int row, int column) {
        if (row < 0) {
            return LEFT_HEADER;
        }
        if (column < 0) {
            return UPPER_HEADER;
        }
        return ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public Object getObject(int row, int column) {
        return null;
    }

    @Override
    public int getItemId(int position) {
        return 0;
    }
}
