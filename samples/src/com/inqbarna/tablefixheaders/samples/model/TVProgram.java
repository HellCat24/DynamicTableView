package com.inqbarna.tablefixheaders.samples.model;

import android.graphics.Color;

/**
 * Created by tac on 08.07.15.
 */
public interface TVProgram {
    long getStartTime();

    long getEndTime();

    String getName();

    String getDescription();

    Color getColor();
}
