package didong.ungdungchat.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import didong.ungdungchat.Adapter.GroupMessagesAdapter;
import didong.ungdungchat.Adapter.MessageAdapter;
import didong.ungdungchat.Model.GroupMessages;
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityGroupChatBinding;
import didong.ungdungchat.databinding.CustomChatBarBinding;
import didong.ungdungchat.databinding.CustomGroupChatBarBinding;
import didong.ungdungchat.databinding.FragmentGroupsBinding;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    CustomGroupChatBarBinding groupChatBarBinding;
    boolean isKeyboardShowing = false;
    private GroupMessagesAdapter groupMessagesAdapter;
    List<GroupMessages> groupMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef, RootRef;
    private String currentGroupName,currentGroupID, currentGroupImage, currentUserID, currentUserName, currentDate, currentTime, cUID, checker = "", currentBackgroundImageUrl;
    private Uri fileUri;
    private StorageTask uploadTask;
    ActivityResultLauncher<String> mStartForResult = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        if (uri != null) {
            fileUri = uri;
            if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                String messageKey = GroupNameRef.child(currentGroupID).child("messages").push().getKey();
                GroupMessageKeyRef = GroupNameRef.child(currentGroupID).child("messages").child(messageKey);

                StorageReference filePath = storageReference.child(messageKey + "." + "jpg");
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

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("from", currentUserID);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("message", url);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("messageID", messageKey);
                            GroupMessageKeyRef.updateChildren(messageInfoMap);
                        }
                    };
                });
            } else {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                String messageKey = GroupNameRef.child(currentGroupID).child("messages").push().getKey();
                GroupMessageKeyRef = GroupNameRef.child(currentGroupID).child("messages").child(messageKey);

                StorageReference filePath = storageReference.child(messageKey);
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

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                                messageInfoMap.put("from", currentUserID);
                                messageInfoMap.put("type", checker);
                                messageInfoMap.put("name", currentUserName);
                                messageInfoMap.put("message", url);
                                messageInfoMap.put("date", currentDate);
                                messageInfoMap.put("time", currentTime);
                                messageInfoMap.put("messageID", messageKey);
                            GroupMessageKeyRef.updateChildren(messageInfoMap);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();
        currentGroupImage = getIntent().getExtras().get("groupImage").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName,Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        cUID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();
        getUserInfo();
        groupChatBarBinding.customGroupName.setText(currentGroupName);
        Picasso.get().load(currentGroupImage).placeholder(R.drawable.baseline_groups_24).into(groupChatBarBinding.customGroupImage);

        retrieveBackgroundImage();
        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                binding.inputGroupMessage.setText("");
            }
        });

        binding.sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Hình ảnh ",
                                "File PDF",
                                "Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Chọn file");
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
                                binding.groupsMessagesListOfUsers.smoothScrollToPosition( Objects.requireNonNull(binding.groupsMessagesListOfUsers.getAdapter()).getItemCount());
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

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.child(currentGroupID).child("messages").addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                GroupMessages groupMessages = snapshot.getValue(GroupMessages.class);
                if(groupMessages != null && !groupMessagesList.contains(groupMessages)){
                    groupMessagesList.add(groupMessages);
                    groupMessagesAdapter.notifyDataSetChanged();
                    binding.groupsMessagesListOfUsers.smoothScrollToPosition( Objects.requireNonNull(binding.groupsMessagesListOfUsers.getAdapter()).getItemCount());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        updateUserStatus("online");
    }

    private void InitializeFields() {
        setSupportActionBar(binding.groupChatBarLayout);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.custom_group_chat_bar, null);
        actionBar.setCustomView(action_bar_view);
        groupChatBarBinding = CustomGroupChatBarBinding.bind(action_bar_view);

        groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesList, currentGroupID);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.groupsMessagesListOfUsers.setLayoutManager(linearLayoutManager);
        binding.groupsMessagesListOfUsers.setAdapter(groupMessagesAdapter);

        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        currentDate = currentDateFormat.format(calendarDate.getTime());

        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
        currentTime = currentTimeFormat.format(calendarTime.getTime());
    }
    private void getUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void SaveMessageInfoToDatabase() {
        String message = binding.inputGroupMessage.getText().toString();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(GroupChatActivity.this,"Please write message first...", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            String messageKey = GroupNameRef.child(currentGroupID).child("messages").push().getKey();
            GroupMessageKeyRef = GroupNameRef.child(currentGroupID).child("messages").child(messageKey);
            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("from", currentUserID);
                messageInfoMap.put("type", "text");
                messageInfoMap.put("name", currentUserName);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
                messageInfoMap.put("messageID", messageKey);
            GroupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu_group_chat, menu);
        if(menu instanceof MenuBuilder){
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.miAddMember)
        {
            sendUserToAddMemberActivity();
        }
        if(item.getItemId() == R.id.miListMembers)
        {
            showListMember();
        }
        if(item.getItemId() == R.id.miCaiDat)
        {
            settingGroup();
        }
        if(item.getItemId() == R.id.miLogOut)
        {
            removeUserFromGroup();
        }
        return true;
    }

    private void sendUserToAddMemberActivity() {
        Intent intent = new Intent(GroupChatActivity.this, AddMemberActivity.class);
        intent.putExtra("groupID", currentGroupID);
        startActivity(intent);
    }

    private void removeUserFromGroup() {
        GroupNameRef.child(currentGroupID).child("members").child(currentUserID).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(GroupChatActivity.this, "You have been removed from the group", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(GroupChatActivity.this, FragmentGroupsBinding.class);
                startActivity(intent);
            } else {
                Toast.makeText(GroupChatActivity.this, "Error occurred while removing from the group", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showListMember() {
        Intent intent = new Intent(GroupChatActivity.this, ListMemberActivity.class);
        intent.putExtra("groupID", currentGroupID);
        startActivity(intent);
    }

    private void settingGroup() {
        Intent intent = new Intent(GroupChatActivity.this, SettingGroupActivity.class);
        intent.putExtra("groupName", currentGroupName);
        intent.putExtra("groupID", currentGroupID);
        startActivity(intent);
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

    private void retrieveBackgroundImage() {
        GroupNameRef.child(currentGroupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentBackgroundImageUrl = snapshot.child("backgroundImage").getValue(String.class);
                    if (currentBackgroundImageUrl != null && !currentBackgroundImageUrl.isEmpty()) {
                        Picasso.get().load(currentBackgroundImageUrl).into(new com.squareup.picasso.Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                binding.groupChatLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                            }
                            @Override
                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                Toast.makeText(GroupChatActivity.this, "Không thể tải hình nền", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onPrepareLoad(Drawable placeHolderDrawable) {
                                // Do nothing
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupChatActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}