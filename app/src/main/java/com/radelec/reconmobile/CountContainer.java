package com.radelec.reconmobile;

public class CountContainer {
    private int chamber1HourlyCount;
    private int chamber2HourlyCount;

    public CountContainer(int ch1, int ch2)
    {
        chamber1HourlyCount = ch1;
        chamber2HourlyCount = ch2;
    }

    public int getCh1HourlyCount()
    {
        return chamber1HourlyCount;
    }

    public int getCh2HourlyCount()
    {
        return chamber2HourlyCount;
    }
}
