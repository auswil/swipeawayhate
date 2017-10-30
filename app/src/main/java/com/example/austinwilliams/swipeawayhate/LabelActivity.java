package com.example.austinwilliams.swipeawayhate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LabelActivity extends AppCompatActivity {

    private TextView tv;
    private String text;
    private String currentKey;
    private static final String TAG = LabelActivity.class.getSimpleName();
    FirebaseUser user;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        tv = (TextView) findViewById(R.id.tweet_text);
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advance();
            }
        });
        getNextTweet();
    }

    private void getNextTweet() {
        DatabaseReference ref = database.getReference("users/" + user.getUid() + "/lastKey");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setText(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });

    }

    private void setTweet(DataSnapshot dataSnapshot) {

    }

    private void setText(DataSnapshot dataSnapshot) {
        text = dataSnapshot.getValue(String.class);
        if (text != null) {
            tv.setText(text);
            Query tweets = database.getReference("tweets").orderByKey().startAt(text).limitToFirst(2);
            tweets.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString());
                    int i = 0;
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (i == 1) {
                            tv.setText(snap.getValue(String.class));
                            currentKey = snap.getKey();
                        }
                        i++;
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });
        }
        else {
            tv.setText("No Key Yet");
            Query tweets = database.getReference("tweets").orderByKey().limitToFirst(2);
            tweets.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString());
                    int i = 0;
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (i == 0) {
                            tv.setText(snap.getValue(String.class));
                            currentKey = snap.getKey();
                        }
                        i++;
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });
        }
    }

    private void advance() {
        DatabaseReference ref = database.getReference("users/" + user.getUid() + "/lastKey");
        ref.setValue(currentKey);
    }

    private void errorText() {
        text = "ERROR";
        tv.setText(text);}
}
