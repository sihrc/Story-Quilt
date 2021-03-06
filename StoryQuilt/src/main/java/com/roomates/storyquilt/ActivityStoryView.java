package com.roomates.storyquilt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;


/**
 * Created by Team Roommates
 */
public class ActivityStoryView  extends Activity {
    /**
     * Options Menu
     */
    Menu menu;

    /**
     * XML Views
     */
    EditText newPost;
    Button addButton;
    TextView storyTitle, recentPosts, quitButton, remaining;

    /**
     * User and Story Information
     */
    UserHandler userHandler;
    Story curStory;

    /**
     * Number of previous posts
     */
    Integer posts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_story);
        setup();
    }
    private void setup() {
        //Touch off keyboard
        setupUI(findViewById(R.id.parent));

        //Create the User Handler
        userHandler = new UserHandler(this);

        //Create the Story Firebase Connection
        FireHandler.create("stories", getIntent().getStringExtra("story")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //Every time the story is updated
                curStory = dataSnapshot.getValue(Story.class);
                if (curStory.checkMostRecentPoster(userHandler.user)){
                    findViewById(R.id.activity_story_button).setBackgroundColor(Color.GRAY);
                } else {
                    findViewById(R.id.activity_story_button).setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                }
                if (posts!=null && posts != curStory.pieces.size() && !curStory.checkMostRecentPoster(userHandler.user)){
                    Toast.makeText(ActivityStoryView.this, "Someone just posted!", Toast.LENGTH_SHORT).show();
                    EditText v = (EditText) findViewById(R.id.activity_story_edittext);
                    v.setText("");
                }
                posts = curStory.pieces.size();
                if (userHandler.isReader(curStory.id)) {
                    setContentView(R.layout.activity_story_following);
                    bindViewsAsReader();
                    populateViewsAsReader();
                } else {
                    bindViewsAsWriter();
                    populateViewsAsWriter();
                }
                if (menu!=null){
                    //Menu has been created, set visibility of join/leave
                    menu.findItem(R.id.join_story).setVisible((!userHandler.isReader(curStory.id))&&(!userHandler.isWriter(curStory.id)));
                    menu.findItem(R.id.leave_story).setVisible(userHandler.isWriter(curStory.id));
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
    }


    /**
     Binding Views for StoryView from XML
     */
    private void bindViewsAsWriter(){
        newPost = (EditText)findViewById(R.id.activity_story_edittext);
        remaining = (TextView) findViewById(R.id.activity_story_text_remaining);
        remaining.setText(String.valueOf(curStory.textLimit));

        newPost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //display number of words left
                if (s.toString().equals("")){
                    remaining.setText(String.valueOf(curStory.textLimit));
                } else {
                    int wordCount = s.toString().trim().split(" ").length;
                    if ((curStory.textLimit - wordCount) >= 0){
                        remaining.setText(String.valueOf(curStory.textLimit - wordCount) + " words left");
                        remaining.setTextColor(Color.BLACK);
                    } else {
                        remaining.setText(String.valueOf(curStory.textLimit - wordCount) + " words over!");
                        remaining.setTextColor(Color.RED);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        addButton = (Button)findViewById(R.id.activity_story_button);
        quitButton = (TextView)findViewById(R.id.activity_story_postsLater_textview);

        storyTitle = (TextView)findViewById(R.id.activity_story_title_textview);
        recentPosts = (TextView)findViewById(R.id.activity_story_recentPosts_textview);
        recentPosts.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     Binding Views for StoryView from XML
     */
    private void bindViewsAsReader(){
        storyTitle = (TextView)findViewById(R.id.activity_story_title_textview);
        recentPosts = (TextView)findViewById(R.id.activity_story_recentPosts_textview);
        recentPosts.setMovementMethod(new ScrollingMovementMethod());
    }

     /**
      Populating Views for StoryView from XML
     */
    private void populateViewsAsWriter(){
        storyTitle.setText(curStory.title);
        recentPosts.setText(curStory.recentPosts());
        int curNum = curStory.pieces.size() - curStory.historyLimit/curStory.textLimit;
        quitButton.setText("... "+String.valueOf(curNum>0? curNum:0)+" Posts Later ...");
        setAddButton();
        setQuitButton();
    }
    private void populateViewsAsReader(){
        storyTitle.setText(this.getTitle());
        recentPosts.setText(curStory.fullStory());
    }
    private void setAddButton(){
        //Add Button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Editable newPostText = newPost.getText();

                //Check to see if last post is by this user and don't let them if they are
                if (curStory.checkMostRecentPoster(userHandler.user)){
                    //Display Error toast stating that you can't post twice in a row.
                    Toast.makeText(ActivityStoryView.this, getString(R.string.activity_story_twiceInARow), Toast.LENGTH_SHORT).show();
                } else if (userHandler.user.email.equals("readonly")){
                    Toast.makeText(ActivityStoryView.this, "Sign in to post to a story!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (newPostText != null && !newPostText.toString().equals("")){
                        if (curStory.checkWordCount(newPostText.toString())){
                            //Add new Piece
                            Piece newPiece = new Piece(getSharedPreferences("StoryQuilt", MODE_PRIVATE).getString("email", ""), String.valueOf(System.currentTimeMillis()), newPostText.toString());
                            curStory.addPiece(newPiece);
                            //Make User a Writer if New
                            if (!userHandler.isWriter(curStory.id)){
                                userHandler.becomeWriter(curStory.id);
                                curStory.writers.add(userHandler.user.email);
                            }
                            newPost.setText("");
                            FireHandler.updateStoryInFirebase(curStory);

                        } else {
                            Toast.makeText(ActivityStoryView.this, "Please make sure your post is of appropriate length!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
    private void leaveStory() {
        //Makes you a reader in the story, instead of a writer
        new AlertDialog.Builder(ActivityStoryView.this)
                .setTitle("Become a Follower!")
                .setMessage("If you click okay, you will start following the whole story as it grows but will no longer be able to post.")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userHandler.becomeReaderFromWriter(curStory.id);
                        curStory.writers.remove(userHandler.user.email);

                        setup();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                }).show();
    }
    private void setQuitButton(){
        quitButton.setClickable(true);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveStory();
            }
        });
    }

    /**
     * Methods for handling the Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story, menu);
        menu.findItem(R.id.join_story).setVisible((!userHandler.isReader(curStory.id))&&(!userHandler.isWriter(curStory.id)));
        menu.findItem(R.id.leave_story).setVisible(userHandler.isWriter(curStory.id));
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leave_story: //Leave a new Story
                if (userHandler.user.email.equals("readonly")){
                    Toast.makeText(ActivityStoryView.this, "Sign in to join a story!", Toast.LENGTH_SHORT).show();
                } else {
                    leaveStory();
                }
                break;

            case R.id.join_story: //Join an Existing Story
                if (userHandler.user.email.equals("readonly")){
                    Toast.makeText(ActivityStoryView.this, "Sign in to join a story!", Toast.LENGTH_SHORT).show();
                } else
                if (!userHandler.isReader(curStory.id)){
                    curStory.writers.add(userHandler.user.email);
                    Toast.makeText(ActivityStoryView.this, "You have become a writer for this story!",Toast.LENGTH_SHORT).show();
                }
                break;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Methods for handling soft-keyboard behavior
     */
    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(ActivityStoryView.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus()!=null){
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
