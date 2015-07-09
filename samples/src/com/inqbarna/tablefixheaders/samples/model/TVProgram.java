package com.inqbarna.tablefixheaders.samples.model;

/**
 * Created by tac on 08.07.15.
 */
public interface TVProgram {
    long getStartTime();

    long getEndTime();

    long getDurationInSeconds();

    String getTitle();

    String getDescription();

    int getColor();
}
