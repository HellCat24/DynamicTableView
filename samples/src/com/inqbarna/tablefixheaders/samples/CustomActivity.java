package com.inqbarna.tablefixheaders.samples;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.inqbarna.tablefixheaders.DynamicTableView;
import com.inqbarna.tablefixheaders.samples.adapters.ChannelAdapter;
import com.inqbarna.tablefixheaders.samples.adapters.SampleTableAdapter;
import com.inqbarna.tablefixheaders.samples.model.DummyTVProgram;
import com.inqbarna.tablefixheaders.samples.model.TVProgram;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomActivity extends Activity {

    int size = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_table);

        DynamicTableView tableFixHeaders = (DynamicTableView) findViewById(R.id.table);
        tableFixHeaders.setAdapter(new MyAdapter(this));
        //generateDummyData(tableFixHeaders);
    }

    private void generateDummyData(DynamicTableView dynamicTableView) {
        List<List<TVProgram>> mBroadCast = new ArrayList();

        for (int i = 0; i < 15; i++) {
            mBroadCast.add(new ArrayList<TVProgram>());
        }

        Random random = new Random();
        TVProgram tvProgram = new DummyTVProgram();

        for (List<TVProgram> list : mBroadCast) {
            int columnCount = random.nextInt(5) + 20;
            list.add(tvProgram);
            for (int i = 1; i < columnCount; i++) {
                list.add(new DummyTVProgram(list.get(0).getStartTime()));
            }
        }

        ChannelAdapter channelAdapter = new ChannelAdapter(this);
        channelAdapter.setData(mBroadCast);

        dynamicTableView.setAdapter(channelAdapter);
    }


    public class MyAdapter extends SampleTableAdapter {

        public int NO_ITEM = -1;

        private final int width;
        private final int height;

        int widths[][];

        public MyAdapter(Context context) {
            super(context);

            Resources resources = context.getResources();

            width = 200;
            height = 100;

            generateRandomWidth();
        }

        private void generateRandomWidth() {
            widths = new int[getRowCount()][getMaxColumnCount()];

            Random rnd = new Random();

            int columnCount;

            for (int i = 0; i < getRowCount(); i++) {
                columnCount = 6 + rnd.nextInt(3);
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
            return 10;
        }

        @Override
        public int getHorizontalHeaderWidth() {
            return 100;
        }

        @Override
        public int getVerticalHeaderWidth() {
            return 500;
        }


        @Override
        public int getWidth(int row, int column) {
            if (row < 0) {
                return getVerticalHeaderWidth();
            }
            if (column < 0) {
                return getHorizontalHeaderWidth();
            }
            return widths[row][column];
        }

        @Override
        public int getWidth(int column) {
            return width;
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
