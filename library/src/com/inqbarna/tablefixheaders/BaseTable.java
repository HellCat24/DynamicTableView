package com.inqbarna.tablefixheaders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.inqbarna.tablefixheaders.adapters.TableAdapter;

import java.util.ArrayList;
import java.util.List;

public class BaseTable extends ViewGroup {

    public static final String TAG = "TableFixHeaders";

    private int currentX;
    private int currentY;

    private TableAdapter adapter;
    private int scrollX[];
    private int scrollY;
    //visible rows
    private int firstRow;
    //visible header column
    private int[] firstColumn;
    //items measures
    private int[][] widths;
    private int[] heights;

    private int maxRowWidth;

    //header in left top corner
    @SuppressWarnings("unused")
    private View headView;

    // upper header
    private List<View> rowViewList;
    //left header
    private List<View> columnViewList;
    //items list
    private List<List<View>> bodyViewTable;

    private int rowCount;
    private int columnCount;

    private int width;
    private int height;

    private Recycler recycler;

    private TableAdapterDataSetObserver tableAdapterDataSetObserver;
    private boolean needRelayout;

    private final int minimumVelocity;
    private final int maximumVelocity;

    private final Flinger flinger;

    private VelocityTracker velocityTracker;

    private int touchSlop;

    public BaseTable(Context context) {
        this(context, null);
    }

    public BaseTable(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.headView = null;
        this.rowViewList = new ArrayList<View>();
        this.columnViewList = new ArrayList<View>();
        this.bodyViewTable = new ArrayList<List<View>>();

        this.needRelayout = true;

        this.flinger = new Flinger(context);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public TableAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(TableAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(tableAdapterDataSetObserver);
        }

        this.adapter = adapter;
        tableAdapterDataSetObserver = new TableAdapterDataSetObserver();
        this.adapter.registerDataSetObserver(tableAdapterDataSetObserver);

        this.recycler = new Recycler(adapter.getViewTypeCount());

        scrollY = 0;
        firstRow = 0;

        needRelayout = true;
        requestLayout();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                currentX = (int) event.getRawX();
                currentY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int x2 = Math.abs(currentX - (int) event.getRawX());
                int y2 = Math.abs(currentY - (int) event.getRawY());
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true;
                }
                break;
            }
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain(); // then get one
        }
        velocityTracker.addMovement(event); // add this movement to it

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!flinger.isFinished()) { // If scrolling, then stop now
                    flinger.forceFinished();
                }
                currentX = (int) event.getRawX();
                currentY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final int x2 = (int) event.getRawX();
                final int y2 = (int) event.getRawY();
                final int diffX = currentX - x2;
                final int diffY = currentY - y2;
                currentX = x2;
                currentY = y2;

                scrollBy(diffX, diffY);
                break;
            }
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = this.velocityTracker;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                int velocityY = (int) velocityTracker.getYVelocity();

                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(getActualScrollX(), getActualScrollY(), velocityX, velocityY, getMaxScrollX(), getMaxScrollY());
                } else {
                    if (this.velocityTracker != null) { // If the velocity less than threshold
                        this.velocityTracker.recycle(); // recycle the tracker
                        this.velocityTracker = null;
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (needRelayout) {

            for (int i = 0; i < rowCount + 1; i++) {
                firstColumn[i] = 0;
                scrollX[i] = x;
            }

            scrollY = y;
            firstRow = 0;
        } else {

            throw new RuntimeException("scrollTo not supported");
          /*  int tempX;

            if(x!=0){
                for (int row = 0; row < rowCount + 1; row++) {
                    tempX = x;
                    tempX = tempX - sumArray(widths, row, 1, firstColumn[row]) - scrollX[row];
                    scrollX[row] += tempX;
                }
            }

            scrollBy(x, y - sumArray(heights, 1, firstRow) - scrollY);*/
        }
    }

    @Override
    public void scrollBy(int x, int y) {

        if (x != 0) {
            for (int row = 0; row < rowCount + 1; row++) {
                scrollX[row] += x;
            }
        }
        scrollY += y;

        if (x == 0 && y == 0 || needRelayout) {
            return;
        }

        scrollBounds();

		/*
         * TODO Improve the algorithm. Think big diagonal movements. If we are
		 * in the top left corner and scrollBy to the opposite corner. We will
		 * have created the views from the top right corner on the X part and we
		 * will have eliminated to generate the right at the Y.
		 */

        int visibleRowSize = firstRow + columnViewList.size();
        //visible/relation item row
        int itemRow = 0;

        for (int row = 0; row < rowCount + 1; row++) {

            if (scrollX[row] != 0) {
                if (scrollX[row] > 0) {

                    // update non visible views first visible column values
                    if (row < firstRow || row >= visibleRowSize) {
                        while (widths[row][firstColumn[row] + 1] < scrollX[row]) {
                            scrollX[row] -= widths[row][firstColumn[row] + 1];
                            firstColumn[row]++;
                        }
                        // update visible views first visible column values, add or delete views
                    } else if (row >= firstRow && row < visibleRowSize) {

                        while (widths[row][firstColumn[row] + 1] < scrollX[row]) {
                            removeLeft(itemRow);
                            scrollX[row] -= widths[row][firstColumn[row] + 1];
                            firstColumn[row]++;
                        }

                        int tempItemSize = bodyViewTable.get(itemRow).size();

                        while (getFilledWidth(row, tempItemSize) < width) {
                            log("getFilledWidth() " + getFilledWidth(row, tempItemSize) + " width" + width);
                            addRight(itemRow, row);
                            tempItemSize++;
                        }

                        itemRow++;
                    }
                } else {
                    if (row < firstRow && row >= visibleRowSize) {

                        if (rowViewList.isEmpty()) {
                            while (scrollX[row] < 0) {
                                firstColumn[row]--;
                                scrollX[row] += widths[row][firstColumn[row] + 1];
                            }
                        } else {
                            while (0 > scrollX[row]) {
                                firstColumn[row]--;
                                scrollX[row] += widths[row][firstColumn[row] + 1];
                            }
                        }
                    } else if (row >= firstRow && row < visibleRowSize) {

                        while (getFilledWidth(row, bodyViewTable.get(itemRow).size()) - widths[row][firstColumn[row] + bodyViewTable.get(itemRow).size()] >= width) {
                            removeRight(itemRow);
                        }

                        int tempItemSize = bodyViewTable.get(itemRow).size();

                        if (rowViewList.isEmpty()) {
                            while (scrollX[row] < 0) {
                                firstColumn[row]--;
                                scrollX[row] += widths[row][firstColumn[row] + 1];
                            }
                            while (getFilledWidth(row, tempItemSize) < width) {
                                addRight(itemRow, row);
                            }
                        } else {
                            while (0 > scrollX[row]) {
                                addLeft(itemRow, row);
                                firstColumn[row]--;
                                scrollX[row] += widths[row][firstColumn[row] + 1];
                            }
                        }

                        itemRow++;
                    }
                }
            }
        }

        if (scrollY != 0) {
            if (scrollY > 0) {
                while (heights[firstRow + 1] < scrollY) {
                    if (!columnViewList.isEmpty()) {
                        removeTop();
                    }
                    scrollY -= heights[firstRow + 1];
                    firstRow++;
                }
                while (getFilledHeight() < height) {
                    addBottom();
                }
            } else {
                while (!columnViewList.isEmpty() && getFilledHeight() - heights[firstRow + columnViewList.size()] >= height) {
                    removeBottom();
                }
                if (columnViewList.isEmpty()) {
                    while (scrollY < 0) {
                        firstRow--;
                        scrollY += heights[firstRow + 1];
                    }
                    while (getFilledHeight() < height) {
                        addBottom();
                    }
                } else {
                    while (0 > scrollY) {
                        addTop();
                        firstRow--;
                        scrollY += heights[firstRow + 1];
                    }
                }
            }
        }

        repositionViews();
    }

    /**
     * Added right view
     *
     * @param itemRow  relation (относительный) row i.e. visible row position in bodyViewTable
     * @param addedRow row to add (абсолютный)
     */
    private void addLeft(int itemRow, int addedRow) {
        //addHorizontalHeader(firstColumn[row] - 1, 0);
        int column = firstColumn[addedRow] - 1;
        List<View> list = bodyViewTable.get(itemRow);
        View view = makeView(addedRow, column, widths[itemRow][column + 1], heights[addedRow]);
        list.add(0, view);
    }

    /**
     * Added right view
     *
     * @param itemRow  relation (относительный) row i.e. visible row position in bodyViewTable
     * @param addedRow row to add (абсолютный)
     */
    private void addRight(int itemRow, int addedRow) {
        //addHorizontalHeader(firstVisibleColumn + rowItemsSize, rowViewList.size());
        List<View> list = bodyViewTable.get(itemRow);
        int rowItemsSize = list.size();

        int column = firstColumn[addedRow] + rowItemsSize;
        //log("Added row " + addedRow + "column " + column);

        View view = makeView(addedRow, column, widths[addedRow][column + 1], heights[addedRow]);
        list.add(view);
    }

    private void addHorizontalItem(int column, int row, int itemIndex) {
        //add items
        List<View> list = bodyViewTable.get(row);
        View view = makeView(row, column, widths[row][column + 1], heights[row]);
        list.add(itemIndex, view);
    }

    //add row header
    private void addHorizontalHeader(int column, int headerIndex) {
        View view = makeView(-1, column, widths[0][column + 1], heights[0]);
        rowViewList.add(headerIndex, view);
    }

    private void addTop() {
        addVerticalItem(firstRow - 1, 0);
    }

    private void addBottom() {
        final int size = columnViewList.size();
        addVerticalItem(firstRow + size, size);
    }

    private void addVerticalItem(int row, int index) {
        //add column header
        View view = makeView(row, -1, widths[0][0], heights[row + 1]);
        columnViewList.add(index, view);

        //add items
        List<View> list = new ArrayList<View>();
        final int size = rowViewList.size() + firstColumn[row];
        for (int i = firstColumn[row]; i < size; i++) {
            view = makeView(row, i, widths[i + 1][i], heights[row + 1]);
            list.add(view);
        }
        bodyViewTable.add(index, list);
    }

    private void removeLeft(int row) {
        removeLeftOrRight(0, row);
    }

    private void removeRight(int row) {
        removeLeftOrRight(bodyViewTable.get(row).size() - 1, row);
    }

    private void removeLeftOrRight(int position, int row) {
        //removeView(rowViewList.remove(position));
        removeView(bodyViewTable.get(row).remove(position));
    }

    private void removeBottom() {
        removeTopOrBottom(columnViewList.size() - 1);
    }

    private void removeTop() {
        removeTopOrBottom(0);
    }

    private void removeTopOrBottom(int position) {
        removeView(columnViewList.remove(position));
        List<View> remove = bodyViewTable.remove(position);
        for (View view : remove) {
            removeView(view);
        }
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);

        final int typeView = (Integer) view.getTag(R.id.tag_type_view);
        if (typeView != TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycler.addRecycledView(view, typeView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int w;
        final int h;

        if (adapter != null) {
            this.rowCount = adapter.getRowCount();
            this.columnCount = adapter.getMaxColumnCount();

            if (firstColumn == null) {
                firstColumn = new int[rowCount + 1];
            }

            if (scrollX == null) {
                scrollX = new int[rowCount + 1];
            }

            widths = new int[rowCount + 1][columnCount + 1];
            int maxWidth = 0;
            for (int i = -1; i < rowCount; i++) {
                maxRowWidth = 0;
                for (int j = -1; j < columnCount; j++) {
                    int itemWidth = adapter.getWidth(i, j);
                    widths[i + 1][j + 1] = itemWidth;
                    maxRowWidth += itemWidth;
                }
                if (maxWidth < maxRowWidth) {
                    maxWidth = maxRowWidth;
                }
            }

            for (int i = 0; i < rowCount; i++) {
                //TODO SET REAL HEADER SIZE
                widths[i][0] = 200;
            }

            heights = new int[rowCount + 1];
            for (int i = -1; i < rowCount; i++) {
                heights[i + 1] += adapter.getHeight(i);
            }


            if (widthMode == MeasureSpec.AT_MOST) {
                w = Math.min(widthSize, maxWidth);
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                w = maxWidth;
            } else {
                w = widthSize;
                if (maxWidth < widthSize) {
                    final float factor = widthSize / (float) maxWidth;
                    for (int i = 1; i < rowCount; i++) {
                        for (int j = 1; j < columnCount; j++) {
                            widths[i][j] = Math.round(widths[i][j] * factor);
                        }
                    }
                }
            }

            if (heightMode == MeasureSpec.AT_MOST) {
                h = Math.min(heightSize, sumArray(heights));
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                h = sumArray(heights);
            } else {
                h = heightSize;
            }
        } else {
            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                w = 0;
                h = 0;
            } else {
                w = widthSize;
                h = heightSize;
            }
        }

        if (firstRow >= rowCount || getMaxScrollY() - getActualScrollY() < 0) {
            firstRow = 0;
            scrollY = Integer.MAX_VALUE;
        }
        if (firstColumn[0] >= columnCount || getMaxScrollX() - getActualScrollX() < 0) {
            firstColumn[0] = 0;
            for (int i = 0; i < rowCount + 1; i++) {
                scrollX[i] = Integer.MAX_VALUE;
            }
        }
        setMeasuredDimension(w, h);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout = false;
            resetTable();

            if (adapter != null) {
                width = r - l;
                height = b - t;

                int left, top, right, bottom;

                right = Math.min(width, maxRowWidth);
                bottom = Math.min(height, sumArray(heights));

                headView = makeAndSetup(-1, -1, 0, 0, widths[0][0], heights[0]);

                scrollBounds();
                adjustFirstCellsAndScroll();

                left = widths[0][0] - scrollX[0];
                for (int i = firstColumn[0]; i < columnCount && left < width; i++) {
                    right = left + widths[0][i + 1];
                    final View view = makeAndSetup(-1, i, left, 0, right, heights[0]);
                    rowViewList.add(view);
                    left = right;
                }

                top = heights[0] - scrollY;
                for (int i = firstRow; i < rowCount + 1 && top < height; i++) {
                    bottom = top + heights[i + 1];
                    final View view = makeAndSetup(i, -1, 0, top, widths[i][0], bottom);
                    columnViewList.add(view);
                    top = bottom;
                }

                top = heights[0] - scrollY;
                for (int i = firstRow; i < rowCount + 1 && top < height; i++) {
                    bottom = top + heights[i + 1];
                    left = widths[i][0] - scrollX[i];
                    List<View> list = new ArrayList<View>();
                    for (int j = firstColumn[i]; j < columnCount && left < width; j++) {
                        right = left + widths[i][j + 1];
                        final View view = makeAndSetup(i, j, left, top, right, bottom);
                        list.add(view);
                        left = right;
                    }
                    bodyViewTable.add(list);
                    top = bottom;
                }
            }
        }
    }

    private void repositionViews() {
        int left, top, right, bottom, i;

        left = widths[0][0] - scrollX[0];
        i = firstRow;
        int j = firstColumn[i];
        for (View view : rowViewList) {
            right = left + widths[0][0];
            view.layout(left, 0, right, heights[0]);
            left = right;
        }

        top = heights[0] - scrollY;
        i = firstRow;
        for (View view : columnViewList) {
            bottom = top + heights[++i];
            view.layout(0, top, widths[0][0], bottom);
            top = bottom;
        }

        top = heights[0] - scrollY;
        i = firstRow;
        for (List<View> list : bodyViewTable) {
            bottom = top + heights[++i];
            j = firstColumn[i];
            left = widths[i][j] - scrollX[i];
            for (View view : list) {
                right = left + widths[i][++j];
                view.layout(left, top, right, bottom);
                left = right;
            }
            top = bottom;
        }
        invalidate();
    }


    private void scrollBounds() {
        for (int row = 0; row < rowCount + 1; row++) {
            scrollX[row] = scrollBounds(scrollX[row], firstColumn[row], widths, width, row);
            scrollY = scrollBounds(scrollY, firstRow, heights, height);
        }
    }

    private int scrollBounds(int desiredScroll, int firstCell, int sizes[], int viewSize) {
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell));
        } else {
            desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell + 1, sizes.length - 1 - firstCell) + sizes[0] - viewSize));
        }
        return desiredScroll;
    }

    private int scrollBounds(int desiredScroll, int firstCell, int sizes[][], int viewSize, int row) {
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, row, 0, firstCell));
        } else {
            desiredScroll = Math.min(desiredScroll, Math.max(0, sumArray(sizes, firstCell + 1, 0, sizes.length - 1 - firstCell) + sizes[row][0] - viewSize));
        }
        return desiredScroll;
    }

    private void adjustFirstCellsAndScroll() {
        int values[];

        for (int row = 0; row < rowCount + 1; row++) {
            values = adjustFirstCellsAndScroll(scrollX[row], firstColumn[row], widths);
            scrollX[row] = values[0];
            firstColumn[row] = values[1];
        }

        values = adjustFirstCellsAndScroll(scrollY, firstRow, heights);
        scrollY = values[0];
        firstRow = values[1];
    }

    private int[] adjustFirstCellsAndScroll(int scroll, int firstCell, int sizes[][]) {
        if (scroll == 0) {
            // no op
        } else if (scroll > 0) {
            while (sizes[firstCell + 1][0] < scroll) {
                firstCell++;
                scroll -= sizes[firstCell][0];
            }
        } else {
            while (scroll < 0) {
                scroll += sizes[firstCell][0];
                firstCell--;
            }
        }
        return new int[]{scroll, firstCell};
    }

    private int[] adjustFirstCellsAndScroll(int scroll, int firstCell, int sizes[]) {
        if (scroll == 0) {
            // no op
        } else if (scroll > 0) {
            while (sizes[firstCell + 1] < scroll) {
                firstCell++;
                scroll -= sizes[firstCell];
            }
        } else {
            while (scroll < 0) {
                scroll += sizes[firstCell];
                firstCell--;
            }
        }
        return new int[]{scroll, firstCell};
    }

    private void resetTable() {
        headView = null;
        rowViewList.clear();
        columnViewList.clear();
        bodyViewTable.clear();

        removeAllViews();
    }

    private View makeAndSetup(int row, int column, int left, int top, int right, int bottom) {
        final View view = makeView(row, column, right - left, bottom - top);
        view.layout(left, top, right, bottom);
        return view;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean ret;

        final Integer row = (Integer) child.getTag(R.id.tag_row);
        final Integer column = (Integer) child.getTag(R.id.tag_column);
        // row == null => Shadow view
        if (row == null || (row == -1 && column == -1)) {
            ret = super.drawChild(canvas, child, drawingTime);
        } else {
            canvas.save();
            if (row == -1) {
                canvas.clipRect(widths[0][column], 0, canvas.getWidth(), canvas.getHeight());
            } else if (column == -1) {
                canvas.clipRect(0, heights[0], canvas.getWidth(), canvas.getHeight());
            } else {
                canvas.clipRect(widths[row][column], heights[column], canvas.getWidth(), canvas.getHeight());
            }

            ret = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
        }
        return ret;
    }

    private View makeView(int row, int column, int w, int h) {
        final int itemViewType = adapter.getItemViewType(row, column);
        final View recycledView;
        if (itemViewType == TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycledView = null;
        } else {
            recycledView = recycler.getRecycledView(itemViewType);
        }
        final View view = adapter.getView(row, column, recycledView, this);
        view.setTag(R.id.tag_type_view, itemViewType);
        view.setTag(R.id.tag_row, row);
        view.setTag(R.id.tag_column, column);

        view.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        addTableView(view, row, column);
        return view;
    }

    private void addTableView(View view, int row, int column) {
        if (row == -1 && column == -1) {
            addView(view, getChildCount() - 4);
        } else if (row == -1 || column == -1) {
            addView(view, getChildCount() - 5);
        } else {
            addView(view, 0);
        }
    }

    private int sumArray(int array[]) {
        return sumArray(array, 0, array.length);
    }

    private int sumArray(int array[], int firstIndex, int count) {
        int sum = 0;
        count += firstIndex;
        for (int i = firstIndex; i < count; i++) {
            sum += array[i];
        }
        return sum;
    }

    private int sumArray(int array[][], int row, int startColumn, int count) {
        int sum = 0;
        count += startColumn;
        for (int i = startColumn; i < count; i++) {
            sum += array[row][i];
        }
        return sum;
    }

    public int getActualScrollX() {
        return scrollX[0] + sumArray(widths, 1, 0, firstColumn[0]);
    }

    public int getActualScrollY() {
        return scrollY + sumArray(heights, 1, firstRow);
    }

    private int getMaxScrollX() {
        return Math.max(0, maxRowWidth - width);
    }

    private int getMaxScrollY() {
        return Math.max(0, sumArray(heights) - height);
    }

    private int getFilledWidth(int row, int initialItemsCount) {
        return widths[row][0] + sumArray(widths, row, firstColumn[row] + 1, initialItemsCount) - scrollX[row];
    }

    private int getFilledHeight() {
        return heights[0] + sumArray(heights, firstRow + 1, columnViewList.size()) - scrollY;
    }

    private class TableAdapterDataSetObserver extends DataSetObserver {

        @Override
        public void onChanged() {
            needRelayout = true;
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            // Do nothing
        }
    }

    // http://stackoverflow.com/a/6219382/842697
    private class Flinger implements Runnable {
        private final Scroller scroller;

        private int lastX = 0;
        private int lastY = 0;

        Flinger(Context context) {
            scroller = new Scroller(context);
        }

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);

            lastX = initX;
            lastY = initY;
            post(this);
        }

        public void run() {
            if (scroller.isFinished()) {
                return;
            }

            boolean more = scroller.computeScrollOffset();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            int diffX = lastX - x;
            int diffY = lastY - y;
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY);
                lastX = x;
                lastY = y;
            }

            if (more) {
                post(this);
            }
        }

        boolean isFinished() {
            return scroller.isFinished();
        }

        void forceFinished() {
            if (!scroller.isFinished()) {
                scroller.forceFinished(true);
            }
        }
    }

    private void log(String message) {
        Log.d(TAG, message);
    }
}
