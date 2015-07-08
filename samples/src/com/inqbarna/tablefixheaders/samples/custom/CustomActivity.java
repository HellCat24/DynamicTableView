package com.inqbarna.tablefixheaders.samples.custom;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.inqbarna.tablefixheaders.CustomTableFixHeaders;
import com.inqbarna.tablefixheaders.samples.R;
import com.inqbarna.tablefixheaders.samples.adapters.SampleTableAdapter;

import java.util.Random;

public class CustomActivity extends Activity {

    int size = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_table);

        CustomTableFixHeaders tableFixHeaders = (CustomTableFixHeaders) findViewById(R.id.table);
        tableFixHeaders.setAdapter(new MyAdapter(this));
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
            for (int i = 0; i < getRowCount(); i++) {
                for (int j = 0; j < getMaxColumnCount(); j++) {
                    widths[i][j] = rnd.nextInt(300) + 500;
                }
            }
            widths[getRowCount() - 1][getMaxColumnCount() - 1] = 500;
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
            return 50;
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
                    layoutResource = R.layout.item_upper_header;
                    break;
                case 1:
                    layoutResource = R.layout.left_row;
                    break;
                case 2:
                    layoutResource = R.layout.item_programm;
                    break;
                default:
                    throw new RuntimeException("wtf?");
            }
            return layoutResource;
        }

        @Override
        public View getView(int row, int column, View converView, ViewGroup parent) {
            if (converView == null) {
                converView = getLayoutInflater().inflate(getLayoutResource(row, column), parent, false);
            }
            return converView;
        }

        @Override
        public int getItemViewType(int row, int column) {
            if (row < 0) {
                return 0;
            }
            if (column < 0) {
                return 1;
            }
            return 2;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemId(int position) {
            return 0;
        }
    }
}
