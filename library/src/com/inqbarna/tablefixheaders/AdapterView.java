package com.inqbarna.tablefixheaders;

import android.content.Context;
import android.view.View;
import android.widget.Adapter;

/**
 * Created by tac on 30.06.15.
 */
public class AdapterView extends android.widget.AdapterView {


    public AdapterView(Context context) {
        super(context);
    }

    @Override
    public Adapter getAdapter() {
        return null;
    }

    @Override
    public void setAdapter(Adapter adapter) {

    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int position) {

    }
}
