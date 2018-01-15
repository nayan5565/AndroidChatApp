package com.example.dev.chatapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText edtMessage;
    private Button btnSend;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseUsers;
    private RecyclerView recMessage;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtMessage = (EditText) findViewById(R.id.edtMessage);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("message");
        recMessage = (RecyclerView) findViewById(R.id.recMessage);
        recMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recMessage.setLayoutManager(linearLayoutManager);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, RegisActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        currentUser = firebaseAuth.getCurrentUser();
        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        final String messageValue = edtMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(messageValue)) {
            final DatabaseReference newPost = databaseReference.push();
            databaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("content").setValue(messageValue);
                    newPost.child("userName").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            recMessage.scrollToPosition(recMessage.getAdapter().getItemCount());

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseRecyclerAdapter<Message, MessageViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class, R.layout.single_message, MessageViewHolder.class, databaseReference
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getContent());
                viewHolder.setUserName(model.getUserName());
            }
        };
        recMessage.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View view;

        public MessageViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setContent(String content) {
            TextView message = (TextView) view.findViewById(R.id.tvMessage);
            message.setText(content);
        }

        public void setUserName(String userName) {
            TextView tvUserName = (TextView) view.findViewById(R.id.tvUserName);
            tvUserName.setText(userName);
        }
    }
}
