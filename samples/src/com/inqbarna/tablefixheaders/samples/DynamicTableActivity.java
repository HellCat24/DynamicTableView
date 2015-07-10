package com.inqbarna.tablefixheaders.samples;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.inqbarna.tablefixheaders.DynamicTableView;
import com.inqbarna.tablefixheaders.samples.adapters.ChannelAdapter;
import com.inqbarna.tablefixheaders.samples.adapters.SampleTableAdapter;
import com.inqbarna.tablefixheaders.samples.model.DummyTVProgram;
import com.inqbarna.tablefixheaders.samples.model.TVProgram;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class DynamicTableActivity extends Activity {

    int size = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_table);

        final DynamicTableView tableFixHeaders = (DynamicTableView) findViewById(R.id.table);
        //tableFixHeaders.setAdapter(new MyAdapter(this));
        generateDummyData(tableFixHeaders);

    }

    private void generateDummyData(DynamicTableView dynamicTableView) {
        List<List<TVProgram>> mBroadCast = new ArrayList();

        for (int i = 0; i < 20; i++) {
            mBroadCast.add(new ArrayList<TVProgram>());
        }

        Random random = new Random();

        long startTime = Calendar.getInstance().getTimeInMillis();
        //72 hours from start
        long maxEndTime = startTime + 60 * 60 * 24 * 3 * 1000;

        for (List<TVProgram> list : mBroadCast) {
            int columnCount = random.nextInt(5) + 70;
            list.add(new DummyTVProgram(startTime, maxEndTime));
            for (int i = 1; i < columnCount; i++) {
                list.add(new DummyTVProgram(list.get(i - 1).getEndTime(), maxEndTime));
            }
        }

        ((DummyTVProgram) mBroadCast.get(0).get(mBroadCast.get(0).size() - 1)).setEndDate(maxEndTime);

        ChannelAdapter channelAdapter = new ChannelAdapter(this, startTime);
        channelAdapter.setData(mBroadCast);

        dynamicTableView.setAdapter(channelAdapter);
    }


    public class MyAdapter extends SampleTableAdapter {

        private final int height;

        int widths[][];

        public MyAdapter(Context context) {
            super(context);

            height = 100;

            generateRandomWidth();
        }

        private void generateRandomWidth() {
            widths = new int[getRowCount()][getMaxColumnCount()];

            Random rnd = new Random();

            int columnCount;

            for (int i = 0; i < getRowCount(); i++) {
                columnCount = 135 + rnd.nextInt(9);
                for (int j = 0; j < columnCount; j++) {
                    widths[i][j] = rnd.nextInt(100) + 400;
                }
            }
        }

        @Override
        public int getRowCount() {
            return size;
        }

        @Override
        public int getMaxColumnCount() {
            return 144;
        }

        @Override
        public int getHorizontalHeaderWidth() {
            return 500;
        }

        @Override
        public int getVerticalHeaderWidth() {
            return 100;
        }


        @Override
        public int getWidth(int row, int column) {
            return widths[row][column];
        }

        @Override
        public int getHeight(int row) {
            return height;
        }

        @Override
        public String getCellString(int row, int column) {
            return "Lorem (" + row + ", " + column + ")";
        }

        @Override
        public int getLayoutResource(int row, int column) {
            final int layoutResource;
            switch (getItemViewType(row, column)) {
                case 0:
                    layoutResource = R.layout.item_table_header;
                    break;
                case 1:
                    layoutResource = R.layout.item_table;
                    break;
                default:
                    throw new RuntimeException("wtf?");
            }
            return layoutResource;
        }

        @Override
        public int getItemViewType(int row, int column) {
            if (row < 0) {
                return 0;
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
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
}
