package didong.ungdungchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import didong.ungdungchat.Adapter.GroupMessagesAdapter;
import didong.ungdungchat.Adapter.MessageAdapter;
import didong.ungdungchat.Model.GroupMessages;
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityGroupChatBinding;

public class GroupChatActivity extends AppCompatActivity {

    ActivityGroupChatBinding binding;
    boolean isKeyboardShowing = false;
    private GroupMessagesAdapter groupMessagesAdapter;
    List<GroupMessages> groupMessagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    private String currentGroupName,currentGroupID, currentUserID, currentUserName, currentDate, currentTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        //EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName,Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        InitializeFields();
        GetUserInfo();

        binding.sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                binding.inputGroupMessage.setText("");
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
                if(groupMessages != null){
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
    }

    private void InitializeFields() {
        setSupportActionBar(binding.groupChatBarLayout);
        Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroupName);

        groupMessagesAdapter = new GroupMessagesAdapter(groupMessagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.groupsMessagesListOfUsers.setLayoutManager(linearLayoutManager);
        binding.groupsMessagesListOfUsers.setAdapter(groupMessagesAdapter);
    }
    private void GetUserInfo() {
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
//        if(item.getItemId() == R.id.miListMembers)
//        {
//
//        }
//        if(item.getItemId() == R.id.miCaiDat)
//        {
//
//        }
//        if(item.getItemId() == R.id.miLogOut)
//        {
//
//        }
        return true;
    }

    private void sendUserToAddMemberActivity() {
        Intent intent = new Intent(GroupChatActivity.this, AddMemberActivity.class);
        intent.putExtra("groupID", currentGroupID);
        startActivity(intent);
    }
}