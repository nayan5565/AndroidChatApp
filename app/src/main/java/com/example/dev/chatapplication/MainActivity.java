package com.example.dev.chatapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtMessage;
    private Button btnSend;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseUsers;
    private RecyclerView recMessage;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FloatingActionButton fab;
    public static String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("message");
        recMessage = (RecyclerView) findViewById(R.id.recMessage);
        recMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(true);
        recMessage.setLayoutManager(linearLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();

        Log.e("name", name);
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, RegisActivity.class));
                    finish();
                }
            }
        };

        FirebaseDatabase.getInstance().getReference().child("message").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.e("FIREBASE","added:"+s);
                recMessage.scrollToPosition(recMessage.getAdapter().getItemCount() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.e("FIREBASE","changed:"+s);
                recMessage.scrollToPosition(recMessage.getAdapter().getItemCount() - 1);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.e("FIREBASE","remmoved");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.e("FIREBASE","Moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FIREBASE","onCancelled");
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            currentUser = firebaseAuth.getCurrentUser();
            databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());


            final String messageValue = edtMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageValue)) {
                final DatabaseReference newPost = databaseReference.push();
                databaseUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        recMessage.scrollToPosition(recMessage.getAdapter().getItemCount() - 1);
                        newPost.child("content").setValue(messageValue);
                        newPost.child("userName").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    edtMessage.setText("");
                                    recMessage.scrollToPosition(recMessage.getAdapter().getItemCount() - 1);
                                }

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        }


    }

    private String getToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        String day = sdf.format(new Date());
        return day;
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        final FirebaseRecyclerAdapter<Message, MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class, R.layout.single_message, MessageViewHolder.class, databaseReference
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                Log.e("VH"," data "+model.getContent());
                String user = name;
                if (user.equals(name)) {
                    viewHolder.setContent(model.getContent(), "");
                    viewHolder.setUserName(model.getUserName(), "");
                    viewHolder.setDate(getToday(), "");
                    viewHolder.tvUserName.setTextColor(Color.RED);
//                    viewHolder.addMessageBox("You:-\n" +model.getContent(), model.getUserName(), getToday(), 1, MainActivity.this);
                } else {
                    viewHolder.tvUserName.setTextColor(Color.GREEN);
                    viewHolder.setContent("", model.getContent());
                    viewHolder.setUserName("", model.getUserName());
                    viewHolder.setDate("", getToday());
//                    viewHolder.addMessageBox("with:-\n" + model.getContent(), model.getUserName(), getToday(), 2, MainActivity.this);
                }

            }
        };
        recMessage.setAdapter(firebaseRecyclerAdapter);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                firebaseRecyclerAdapter.notifyDataSetChanged();
//                recMessage.scrollToPosition(recMessage.getAdapter().getItemCount() - 1);
//            }
//        }, 100);

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvUserName, tvUserName2;
        public LinearLayout layout;

        public MessageViewHolder(View itemView) {
            super(itemView);
            view = itemView;

        }

        public void setContent(String content, String content2) {
            TextView message = (TextView) view.findViewById(R.id.tvMessage);
            TextView message2 = (TextView) view.findViewById(R.id.tvMessage2);
            message.setText(content);
            message2.setText(content2);
        }

        public void setUserName(String userName, String userName2) {
            tvUserName = (TextView) view.findViewById(R.id.tvUserName);
            tvUserName2 = (TextView) view.findViewById(R.id.tvUserName2);

            tvUserName.setText(userName);
            tvUserName2.setText(userName2);
        }

        public void setDate(String date, String date4) {
            TextView date2 = (TextView) view.findViewById(R.id.tvDate);
            TextView date3 = (TextView) view.findViewById(R.id.tvDate2);
            date2.setText(date);
            date3.setText(date4);
        }

        public void addMessageBox(String message, String user, String date, int type, Context context) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            TextView textView = new TextView(context);
            TextView textView2 = new TextView(context);
            TextView textView3 = new TextView(context);
            textView.setText(message);
            textView2.setText(user);
            textView3.setText(date);

            LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp2.weight = 1.0f;

            if (type == 1) {
                lp2.gravity = Gravity.RIGHT;
//                textView.setBackgroundResource(R.drawable.bubble_in);
            } else {
                lp2.gravity = Gravity.LEFT;
//                textView.setBackgroundResource(R.drawable.bubble_out);
            }
            textView.setLayoutParams(lp2);
            textView2.setLayoutParams(lp2);
            textView3.setLayoutParams(lp2);
            layout.addView(textView);
            layout.addView(textView2);
            layout.addView(textView3);
//            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    }


}
