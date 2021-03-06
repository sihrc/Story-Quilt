package com.roomates.storyquilt;

import com.firebase.client.Firebase;


/**
 * Created by chris on 12/8/13.
 */
public class FireHandler {
    final static String url = "https://storyquilt.firebaseio.com";
    static User user;

    //Creates firebase ref given a tuple of children
    public static Firebase create(String... children) {
        Firebase firebase = new Firebase(url);
        for (String child : children){
            firebase = firebase.child(child);
        }
        return firebase;
    }

    //Pushes Story to List
    public static String pushStoryToList(Story value){
        Firebase ref = create("stories").push();
        value.setId(ref.getName());
        ref.setValue(value);
        return value.id;
    }

    //Pushes User to List
    public static void pushUserToList(User value){
        create("users", User.formatEmail(value.email)).setValue(value);
    }

    //Push Piece to a Story
    public static void  updateStoryInFirebase(Story story){
        Firebase ref = create("stories", story.id);
        ref.setValue(story);
    }

}
