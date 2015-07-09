package com.inqbarna.tablefixheaders.samples.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.inqbarna.tablefixheaders.samples.R;
import com.inqbarna.tablefixheaders.samples.model.TVProgram;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


/**
 * Created by tac on 08.07.15.
 */
public class ChannelAdapter extends BaseTableAdapter<TVProgram> {

    protected final static SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm");

    private final int LEFT_HEADER = 0;
    private final int UPPER_HEADER = 1;
    private final int ITEM = 2;

    private final int ONE_SECOND_IN_MILLIS = 60 * 1000;
    private final float HOUR_IN_SECONDS = 60 * 60;
    private final float HALF_HOUR_IN_SECONDS = HOUR_IN_SECONDS / 2;
    private final float THREE_DAYS_IN_SECONDS = HOUR_IN_SECONDS * 24 * 3;

    private int HALF_HOUR_IN_PIXELS;
    private int PROGRAM_HEIGHT;
    private int TIME_HEIGHT;


    private int mFirstHeaderScroll;

    private List<List<TVProgram>> mBroadCast;

    private long mStartTime = Calendar.getInstance().getTimeInMillis();
    private long mEndTime = (long) (mStartTime + THREE_DAYS_IN_SECONDS);

    private int mMaxColumnCount;

    private final LayoutInflater mInflater;

    private Context mContext;


    public ChannelAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);

        HALF_HOUR_IN_PIXELS = context.getResources().getDimensionPixelSize(R.dimen.half_hour_size);
        PROGRAM_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.tv_program_height);
        TIME_HEIGHT = context.getResources().getDimensionPixelSize(R.dimen.tv_time_height);
    }

    @Override
    public int getRowCount() {
        return mBroadCast == null ? 0 : mBroadCast.size();
    }

    @Override
    public int getMaxColumnCount() {
        return mMaxColumnCount;
    }

    @Override
    public int getHorizontalHeaderWidth() {
        return HALF_HOUR_IN_PIXELS;
    }

    @Override
    public int getVerticalHeaderWidth() {
        //TODO ADD REAL SIZE
        return 200;
    }

    @Override
    public View getView(int row, int column, View converView, ViewGroup parent) {
        if (converView == null) {
            converView = mInflater.inflate(getLayoutResource(row, column), parent, false);
        }

        TextView time;

        switch (getItemViewType(row, column)) {
            case LEFT_HEADER:
                ImageView channelIcon = (ImageView) converView.findViewById(R.id.tv_channel_icon);
                switch (row) {
                    case 0:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_wh1));
                        break;
                    case 1:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_cnn));
                        break;
                    case 2:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_discovery));
                        break;
                    case 3:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_virgin));
                        break;
                    case 4:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_euronews));
                        break;
                    case 5:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_national_geographic));
                        break;
                    case 6:
                        channelIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.logo_net5));
                        break;
                }
                break;
            case UPPER_HEADER:
//                time = (TextView) converView.findViewById(R.id.txt_tv_channel_header);
//                long columnTime = (long) (mStartTime + column * HALF_HOUR_IN_SECONDS * 1000);
//                time.setText(FORMAT.format(new Date(columnTime)));
                setText(converView, getCellString(row, column));
                break;
            case ITEM:
                time = (TextView) converView.findViewById(R.id.txt_tv_program_name);
                TVProgram tvProgram = getObject(row, column);
                if (tvProgram != null) {
                    long duration = tvProgram.getDurationInSeconds();
                    time.setText("Program title Duration " + duration / 60);
                }
                View leftBorder = converView.findViewById(R.id.tv_program_left_border);
                View upperBorder = converView.findViewById(R.id.tv_program_upper_border);
                leftBorder.setBackgroundColor(tvProgram.getColor());
                upperBorder.setBackgroundColor(tvProgram.getColor());
                break;
            default:
                throw new RuntimeException("wtf?");
        }
        return converView;
    }

    public int getLayoutResource(int row, int column) {
        final int layoutResource;
        switch (getItemViewType(row, column)) {
            case LEFT_HEADER:
                layoutResource = R.layout.tv_channel_left_row;
                break;
            case UPPER_HEADER:
                layoutResource = R.layout.item_table_header;
                //layoutResource = R.layout.tv_channel_upper_header;
                break;
            case ITEM:
                layoutResource = R.layout.tv_channel_item;
                break;
            default:
                throw new RuntimeException("wtf?");
        }
        return layoutResource;
    }

    private void setText(View view, String text) {
        ((TextView) view.findViewById(android.R.id.text1)).setText(text);
    }

    public String getCellString(int row, int column) {
        return "Lorem (" + row + ", " + column + ")";
    }

    @Override
    public int getWidth(int row, int column) {
        if (row < 0) {
            return getHorizontalHeaderWidth();
        }
        if (column < 0) {
            return getVerticalHeaderWidth();
        }
        List<TVProgram> list = mBroadCast.get(row);
        if (column < list.size()) {
            return getSize(list.get(column));
        }
        return -1;
    }

    @Override
    public int getWidth(int row) {
        return 0;
    }

    private int getSize(TVProgram tvProgram) {
        long duration = tvProgram.getDurationInSeconds();
        return (int) (duration * HALF_HOUR_IN_PIXELS / HALF_HOUR_IN_SECONDS);
    }

    @Override
    public int getHeight(int row) {
        if (row < 0) {
            return TIME_HEIGHT;
        }
        return PROGRAM_HEIGHT;
    }

    @Override
    public int getItemViewType(int row, int column) {
        if (row < 0) {
            return UPPER_HEADER;
        }
        if (column < 0) {
            return LEFT_HEADER;
        }
        return ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public TVProgram getObject(int row, int column) {
        List<TVProgram> list = mBroadCast.get(row);
        if (column < list.size()) {
            return list.get(column);
        }
        return null;
    }

    @Override
    public int getItemId(int position) {
        return 0;
    }

    public void setData(List<List<TVProgram>> broadCast) {
        mBroadCast = broadCast;
        mMaxColumnCount = mBroadCast.get(0).size();
        for (int i = 1; i < mBroadCast.size(); i++) {
            if (mMaxColumnCount < mBroadCast.get(i).size()) {
                mMaxColumnCount = mBroadCast.get(i).size();
            }
        }
    }
}
