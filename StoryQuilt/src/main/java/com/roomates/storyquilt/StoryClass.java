package com.roomates.storyquilt;

/**
 * Created by chris on 12/4/13.
 */
public class StoryClass {
    String id, text, lastUpdated, title;
    int ageLimit, historyLimit, textLimit;
    long priority;
    String[] pieces;

    public StoryClass(){} //Firebase required constructor
    public StoryClass(String text, String lastUpdated, String title, int ageLimit, int historyLimit, int textLimit, String[] pieces){
        this.text = text;
        this.lastUpdated = lastUpdated;
        this.title = title;
        this.ageLimit = ageLimit;
        this.historyLimit = historyLimit;
        this.textLimit = textLimit;
        this.pieces = pieces;
    }

    //Firebase Get Methods
    public String getId(){
        return this.id;
    }
    public String getText(){
        return this.text;
    }
    public String getLastUpdated(){
        return this.lastUpdated;
    }
    public String getTitle(){
        return this.title;
    }
    public int getAgeLimit(){
        return this.ageLimit;
    }
    public int getHistoryLimit(){
        return this.historyLimit;
    }
    public int getTextLimit(){
        return this.textLimit;
    }
    public long getPriority(){
        return this.priority;
    }
    public String[] getPieces(){
        return this.pieces;
    }

    //Setting the priority based on viewers and posters
    public void setPriority(int num_viewers, int num_posters){
        this.priority = num_posters + num_viewers;
    }

    //Setting the id from Firebase
    public void setId(String value){
        this.id = value;
    }
}