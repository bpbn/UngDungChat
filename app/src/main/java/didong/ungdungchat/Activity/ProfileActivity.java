package didong.ungdungchat.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, Current_State;
    ActivityProfileBinding binding;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();
//        Toast.makeText(this, "User ID: " + receiverUserID, Toast.LENGTH_SHORT).show();
        Current_State = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).into(binding.visitProfileImage);
                    binding.visitUserName.setText(userName);
                    binding.visitUserStatus.setText(userStatus);

                    ManageChatRequests();
                }
                else{
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    binding.visitUserName.setText(userName);
                    binding.visitUserStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {
        ChatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserID)){
                            String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){
                                Current_State = "request_sent";
                                binding.sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received")){
                                Current_State = "request_received";
                                binding.sendMessageRequestButton.setText("Accept Chat Request");

                                binding.declineMessageRequestButton.setVisibility(View.VISIBLE);
                                binding.declineMessageRequestButton.setEnabled(true);

                                binding.declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        }else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserID)){
                                                Current_State = "friends";
                                                binding.sendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if(!senderUserID.equals(receiverUserID)){
            binding.sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    binding.sendMessageRequestButton.setEnabled(false);
                    if(Current_State.equals("new")){
                        SendChatRequests();
                    }

                    if (Current_State.equals("request_sent")){
                        CancelChatRequest();
                    }

                    if (Current_State.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (Current_State.equals("friends")){
                        RemoveSpecificContact();
                    }
                }
            });
        }else {
            binding.sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveSpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(senderUserID).child(receiverUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                binding.sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                binding.sendMessageRequestButton.setText("Send Message");

                                                binding.declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                binding.declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                ChatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    ChatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    binding.sendMessageRequestButton.setEnabled(true);
                                                                                    Current_State = "friends";

                                                                                    binding.sendMessageRequestButton.setText("Remove this Contact");

                                                                                    binding.declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    binding.declineMessageRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(senderUserID).child(receiverUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                binding.sendMessageRequestButton.setEnabled(true);
                                                Current_State = "new";
                                                binding.sendMessageRequestButton.setText("Send Message");

                                                binding.declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                binding.declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequests() {
        ChatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                binding.sendMessageRequestButton.setEnabled(true);
                                                Current_State ="request_sent";
                                                binding.sendMessageRequestButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}