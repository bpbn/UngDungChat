package didong.ungdungchat.Activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.type.DateTime;
import com.squareup.picasso.Picasso;

import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Adapter.MessageAdapter;
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityChatBinding;
import didong.ungdungchat.databinding.CustomChatBarBinding;

public class ChatActivity extends AppCompatActivity {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(ChatActivity.class);
    ActivityChatBinding binding;
    CustomChatBarBinding customChatBarBinding;

    boolean isKeyboardShowing = false;

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID, cUID, currentUserId, currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String saveCurrentTime, saveCurrentDate, checker = "";
    private Uri fileUri;
    private StorageTask uploadTask;
    ActivityResultLauncher<String> mStartForResult = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            fileUri = uri;
            if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).child("chats").push();

                String messagePushID = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = (Uri) task.getResult();
                            String url = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", url);
                            messageTextBody.put("name", fileUri.getLastPathSegment() + ".jpg");
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/chats/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/chats/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {
                                binding.tvMessage.setText("");
                            });
                            Long tsLong = System.currentTimeMillis()/1000;
                            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("timeStamp").setValue(-tsLong);
                            RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("timeStamp").setValue(-tsLong);
                        }
                    };
                });
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).child("chats").push();

                String messagePushID = userMessageKeyRef.getKey();

                StorageReference filePath = storageReference.child(messagePushID);
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw Objects.requireNonNull(task.getException());
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = (Uri) task.getResult();
                            String url = downloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", url);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/chats/" + messagePushID, messageTextBody);
                            messageBodyDetails.put( messageReceiverRef + "/chats/" + messagePushID, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {
                                binding.tvMessage.setText("");
                            });
                            Long tsLong = System.currentTimeMillis()/1000;
                            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("timeStamp").setValue(-tsLong);
                            RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("timeStamp").setValue(-tsLong);
                        }
                    };
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        cUID = currentUserId;
        currentUser = mAuth.getCurrentUser().getUid();
        messageSenderID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();


        messageReceiverID = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("visit_user_id")).toString();
        messageReceiverName = Objects.requireNonNull(getIntent().getExtras().get("visit_user_name")).toString();
        messageReceiverImage = Objects.requireNonNull(getIntent().getExtras().get("visit_image")).toString();


        IntializeControllers();


        customChatBarBinding.customProfileName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.baseline_account_circle_24).into(customChatBarBinding.customProfileImage);


        binding.btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendMessage();

            }
        });


        DisplayLastSeen();
        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        binding.getRoot().getWindowVisibleDisplayFrame(r);
                        int screenHeight = binding.getRoot().getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            if (!isKeyboardShowing) {
                                isKeyboardShowing = true;
                                binding.privateMessagesListOfUsers.smoothScrollToPosition( Objects.requireNonNull(binding.privateMessagesListOfUsers.getAdapter()).getItemCount());
                            }
                        }
                        else {
                            // keyboard is closed
                            if (isKeyboardShowing) {
                                isKeyboardShowing = false;
                            }
                        }
                    }
                });

    }



    private void IntializeControllers()
    {
        setSupportActionBar(binding.tbChat);

        ActionBar actionBar = getSupportActionBar();
//        Objects.requireNonNull(actionBar).setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(action_bar_view);
        customChatBarBinding = CustomChatBarBinding.bind(action_bar_view);


        messageAdapter = new MessageAdapter(messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.privateMessagesListOfUsers.setLayoutManager(linearLayoutManager);
        binding.privateMessagesListOfUsers.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }



    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = Objects.requireNonNull(dataSnapshot.child("userState").child("state").getValue()).toString();
                            String date = Objects.requireNonNull(dataSnapshot.child("userState").child("date").getValue()).toString();
                            String time = Objects.requireNonNull(dataSnapshot.child("userState").child("time").getValue()).toString();
                            SimpleDateFormat now = new SimpleDateFormat("dd/MM/yyyy");
                            String currentDate = now.format(Calendar.getInstance().getTime());
                            if (state.equals("offline")) {
                                if (currentDate.equals(date)) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
                                    try {
                                        long time1 = Objects.requireNonNull(sdf.parse(time)).getTime();
                                        long time2 = Objects.requireNonNull(sdf.parse(sdf.format(Calendar.getInstance().getTime()))).getTime();
                                        long diff = time2 - time1;
                                        if (diff < 3600000) {
                                            customChatBarBinding.customUserLastSeen.setText(diff / 60000 + " phút trước");
                                        } else if (diff < 86400000) {
                                            customChatBarBinding.customUserLastSeen.setText(diff / 3600000 + " giờ trước");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (!currentDate.equals(date)) {
                                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                    try {
                                        long time1 = Objects.requireNonNull(sdf.parse(date)).getTime();
                                        long time2 = Objects.requireNonNull(sdf.parse(sdf.format(Calendar.getInstance().getTime()))).getTime();
                                        long diff = time2 - time1;
                                        if (diff < 86400000) {
                                            customChatBarBinding.customUserLastSeen.setText("Hôm qua");
                                        } else {
                                            customChatBarBinding.customUserLastSeen.setText(diff / 86400000 + " ngày trước");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                customChatBarBinding.customUserLastSeen.setText("last seen: " + date + " " + time);
                            }
                            if (state.equals("online")) {
                                customChatBarBinding.customUserLastSeen.setText("online");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        binding.btnSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                        {
                            checker = "image";
                            mStartForResult.launch("image/*");
                        }
                        if (which == 1)
                        {
                            checker = "pdf";
                            mStartForResult.launch("application/pdf");
                        }
                        if (which == 2)
                        {
                            checker = "file";
                            mStartForResult.launch("*/*");
                        }
                    }
                });
                builder.show();
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        updateUserStatus("online");

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("chats")
                .addChildEventListener(new ChildEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        binding.privateMessagesListOfUsers.smoothScrollToPosition( Objects.requireNonNull(binding.privateMessagesListOfUsers.getAdapter()).getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    private void SendMessage() {
        String messageText = binding.tvMessage.getText().toString().trim();

        if (TextUtils.isEmpty(messageText))
        {
//            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).child("chats").push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/chats/" + messagePushID, messageTextBody);
            messageBodyDetails.put( messageReceiverRef + "/chats/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful())
                {
//                    Toast.makeText(ChatActivity.this, "Message Sent Successfully...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
                binding.tvMessage.setText("");
            });
            Long tsLong = System.currentTimeMillis()/1000;
            RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).child("timeStamp").setValue(-tsLong);
            RootRef.child("Messages").child(messageReceiverID).child(messageSenderID).child("timeStamp").setValue(-tsLong);
        }
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);
        RootRef.child("Users").child(cUID).child("userState")
                .updateChildren(onlineStateMap);

    }
}
