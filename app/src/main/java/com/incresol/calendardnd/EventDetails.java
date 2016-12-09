package com.incresol.calendardnd;

/**
 * Created by incresol-026 on 8/11/16.
 */

public class EventDetails  {



    String EVENT_ID;
    String EVENT_SUMMARY;
    String EVENT_DESCRIPTION;
    long START_TIME;

    public long getEND_TIME() {
        return END_TIME;
    }

    public void setEND_TIME(long END_TIME) {
        this.END_TIME = END_TIME;
    }

    public long getSTART_TIME() {
        return START_TIME;
    }

    public void setSTART_TIME(long START_TIME) {
        this.START_TIME = START_TIME;
    }

    long END_TIME;
    Integer SWITCH_STATE;
    String LOCATION;

    public String getEVENT_ID() {
        return EVENT_ID;
    }

    public void setEVENT_ID(String EVENT_ID) {
        this.EVENT_ID = EVENT_ID;
    }

    public String getEVENT_SUMMARY() {
        return EVENT_SUMMARY;
    }

    public void setEVENT_SUMMARY(String EVENT_SUMMARY) {
        this.EVENT_SUMMARY = EVENT_SUMMARY;
    }

    public String getEVENT_DESCRIPTION() {
        return EVENT_DESCRIPTION;
    }

    public void setEVENT_DESCRIPTION(String EVENT_DESCRIPTION) {
        this.EVENT_DESCRIPTION = EVENT_DESCRIPTION;
    }



    public Integer getSWITCH_STATE() {
        return SWITCH_STATE;
    }

    public void setSWITCH_STATE(Integer SWITCH_STATE) {
        this.SWITCH_STATE = SWITCH_STATE;
    }

    public String getLOCATION() {
        return LOCATION;
    }

    public void setLOCATION(String LOCATION) {
        this.LOCATION = LOCATION;
    }
}
