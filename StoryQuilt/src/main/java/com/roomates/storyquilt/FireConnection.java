package com.roomates.storyquilt;

import com.firebase.client.Firebase;

/**
 * Created by chris on 12/8/13.
 */
public class FireConnection {
    final static String url = "https://storyquilt.firebaseio.com";

    public static Firebase create(String... children) {
        Firebase firebase = new Firebase(url);
        for (String child : children){
            firebase = firebase.child(child);
        }
        return firebase;
    }
}