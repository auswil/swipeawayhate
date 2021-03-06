package com.example.austinwilliams.swipeawayhate;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.austinwilliams.swipeawayhate.R.string.hate;

public class LabelActivity extends AppCompatActivity  {

    private TextView tv;
    private String text;
    private String currentKey;
    private int completed;
    private int correct;
    private final int VOTES_NEEDED = 3;
    private static final String TAG = LabelActivity.class.getSimpleName();
    private FirebaseUser user;
    private FirebaseDatabase database;
    private GestureDetectorCompat mDetector;
    private ProgressBar progress;

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";
        private final int DISTANCE = 100;
        private final int VELOCITY = 100;
        private Context context;
        private int hateVotes, normalVotes, offensiveVotes, valuesSet;

        public MyGestureListener(Context context) {
            this.context = context;
        }

        private void setValues() {
            Log.d(TAG, "Setting Values: " + valuesSet);
            if (valuesSet == 3) {
                DatabaseReference setNormal = database.getReference("finalized/" + currentKey + "/normal");
                setNormal.setValue(normalVotes);

                DatabaseReference setHate = database.getReference("finalized/" + currentKey + "/hate");
                setHate.setValue(hateVotes);

                DatabaseReference setOffensive = database.getReference("finalized/" + currentKey + "/offensive");
                setOffensive.setValue(offensiveVotes);

                DatabaseReference setText = database.getReference("finalized/" + currentKey + "/text");
                setText.setValue(tv.getText());

                DatabaseReference checkAndRemove = database.getReference("finalized/" + currentKey);
                checkAndRemove.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Log.d(TAG, "Removing tweet: " + currentKey);
                            DatabaseReference remove = database.getReference("tweets/" + currentKey);
                            remove.removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    getNextTweet();
                                    /*Query tweets = database.getReference("tweets").orderByKey().endAt(currentKey).limitToLast(1);
                                    tweets.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot posts) {
                                            advanceAfterDeletion(posts);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });*/
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        private void finalizeVotes() {
            Log.d(TAG, "Finalize votes for " + currentKey);
            hateVotes = 0;
            normalVotes = 0;
            offensiveVotes = 0;
            valuesSet = 0;

            DatabaseReference normalRef = database.getReference("labels/" + currentKey + "/normal");
            normalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int votes = dataSnapshot.getValue(int.class);
                    normalVotes = votes;
                    valuesSet++;
                    setValues();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            DatabaseReference hateRef = database.getReference("labels/" + currentKey + "/hate");
            hateRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int votes = dataSnapshot.getValue(int.class);
                    hateVotes = votes;
                    valuesSet++;
                    setValues();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

            DatabaseReference offensiveRef = database.getReference("labels/" + currentKey + "/offensive");
            offensiveRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int votes = dataSnapshot.getValue(int.class);
                    offensiveVotes = votes;
                    valuesSet++;
                    setValues();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        private void swipeRight() {
            Log.d(TAG, "SWIPE R");
            Log.d(TAG, context.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (completed < 10) {
                        DatabaseReference ref = database.getReference("example_labels/" + completed);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String label = dataSnapshot.getValue(String.class);
                                nextExample(label.equals("normal"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        if (correct > 5) {
                            final DatabaseReference ref = database.getReference("labels/" + currentKey + "/normal");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int votes = dataSnapshot.getValue(int.class);
                                    ref.setValue(votes + 1);
                                    if (votes+1 > VOTES_NEEDED) finalizeVotes();
                                    else advance();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else advance();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Set other dialog properties
            builder.setTitle(R.string.label_confirm);
            builder.setMessage(R.string.normal);


            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void swipeLeft() {
            Log.d(TAG, context.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (completed < 10) {
                        DatabaseReference ref = database.getReference("example_labels/" + completed);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String label = dataSnapshot.getValue(String.class);
                                nextExample(label.equals("hate"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        if (correct > 5) {
                            final DatabaseReference ref = database.getReference("labels/" + currentKey + "/hate");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int votes = dataSnapshot.getValue(int.class);
                                    ref.setValue(votes + 1);
                                    if (votes+1 > VOTES_NEEDED) finalizeVotes();
                                    else advance();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else advance();
                    }
                    // User clicked OK button
                    Log.d(TAG, "CLICKED OK");
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Set other dialog properties
            builder.setTitle(R.string.label_confirm);
            builder.setMessage(R.string.hate);


            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void swipeUp() {
            Log.d(TAG, context.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (completed < 10) {
                        DatabaseReference ref = database.getReference("example_labels/" + completed);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String label = dataSnapshot.getValue(String.class);
                                nextExample(false);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else advance();
                    // User clicked OK button
                    Log.d(TAG, "CLICKED OK");
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Set other dialog properties
            builder.setTitle(R.string.skip_confirm);
            builder.setMessage(R.string.skip);


            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void swipeDown() {
            Log.d(TAG, context.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            // Add the buttons
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (completed < 10) {
                        DatabaseReference ref = database.getReference("example_labels/" + completed);
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String label = dataSnapshot.getValue(String.class);
                                nextExample(label.equals("offensive"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else {
                        if (correct > 5) {
                            final DatabaseReference ref = database.getReference("labels/" + currentKey + "/offensive");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int votes = dataSnapshot.getValue(int.class);
                                    ref.setValue(votes + 1);
                                    if (votes+1 > VOTES_NEEDED) finalizeVotes();
                                    else advance();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else advance();
                    }

                    // User clicked OK button
                    Log.d(TAG, "CLICKED OK");
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Set other dialog properties
            builder.setTitle(R.string.label_confirm);
            builder.setMessage(R.string.offensive);


            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }


        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(TAG, "onFling: " + event1.toString() + event2.toString());
            float x = event2.getX() - event1.getX();
            float y = event2.getY() - event1.getY();
            if (Math.abs(x) > Math.abs(y)) {
                if (x > DISTANCE && velocityX > VELOCITY) swipeRight();
                if (x < -DISTANCE && velocityX < -VELOCITY) swipeLeft();
            }
            else {
                if (y > DISTANCE && velocityY > VELOCITY) swipeDown();
                if (y < -DISTANCE && velocityY < -VELOCITY) swipeUp();

            }
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        tv = (TextView) findViewById(R.id.tweet_text);
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        mDetector = new GestureDetectorCompat(this, new MyGestureListener(LabelActivity.this));
        progress = (ProgressBar) findViewById(R.id.indeterminateBar);
        getCorrect();
        readyToLabel();
    }

    private void readyToLabel() {
        DatabaseReference ref = database.getReference("users/" + user.getUid() + "/completed");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                completed = dataSnapshot.getValue(int.class);
                if (completed >= 10) getNextTweet();
                else getNextExample();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getCorrect() {
        DatabaseReference ref = database.getReference("users/" + user.getUid() + "/correct");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                correct = dataSnapshot.getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getNextExample() {
        DatabaseReference ref = database.getReference("examples/" + completed);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progress.setVisibility(View.INVISIBLE);
                tv.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void selectResult(DataSnapshot dataSnapshot, int stop) {
        int i = 0;
        for (DataSnapshot snap : dataSnapshot.getChildren()) {
            if (i == stop) {
                Log.d(TAG, "Setting text: " + stop + " " + snap.getValue(String.class));
                progress.setVisibility(View.INVISIBLE);
                tv.setText(snap.getValue(String.class));
                currentKey = snap.getKey();
            }
            i++;
        }
    }

    private void setText(DataSnapshot key) {
        text = key.getValue(String.class);
        Log.d(TAG, "LastKey has changed. Getting next tweet: " + currentKey);

        if (text != null) {
            Query tweets = database.getReference("tweets").orderByKey().startAt(text).limitToFirst(2);
            tweets.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString());
                    selectResult(dataSnapshot, 1);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });
        }
        else {
            Query tweets = database.getReference("tweets").orderByKey().limitToFirst(2);
            tweets.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString());
                    selectResult(dataSnapshot, 0);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                }
            });
        }
    }

    private void nextExample(boolean vote) {
        progress.setVisibility(View.VISIBLE);
        tv.setText(R.string.blank);
        DatabaseReference completeRef = database.getReference("users/" + user.getUid() + "/completed");
        completeRef.setValue(completed + 1);
        if (vote) {
            DatabaseReference correctRef = database.getReference("users/" + user.getUid() + "/correct");
            correctRef.setValue(correct + 1);
        }
    }

    private void advanceAfterDeletion(DataSnapshot ds) {
        Log.d(TAG, ds.toString());
        //only run on first result
        for (DataSnapshot snap : ds.getChildren()) {
            currentKey = snap.getKey();
            break;
        }
        advance();

    }

    private void advance() {
        Log.d(TAG, "Advancing from " + currentKey);
        tv.setText(R.string.blank);
        progress.setVisibility(View.VISIBLE);
        DatabaseReference ref = database.getReference("users/" + user.getUid() + "/lastKey");
        ref.setValue(currentKey);
    }

    private void errorText() {
        text = "ERROR";
        tv.setText(text);}
}
