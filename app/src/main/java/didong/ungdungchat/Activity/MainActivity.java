package didong.ungdungchat.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import didong.ungdungchat.Adapter.TabsAccessorAdapter;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef, GroupRef;
    private String currentUserID, currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.mainAppBar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        //currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        binding.mainTabsPager.setAdapter(new TabsAccessorAdapter(this));
        new TabLayoutMediator(binding.mainTabs, binding.mainTabsPager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Tin nhắn");
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Tin nhắn");
                    break;
                case 1:
                    tab.setText("Nhóm");
                    break;
                case 2:
                    tab.setText("Liên hệ");
                    break;
            }
        }).attach();
        binding.mainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(tab.getText());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            VerifyUserExistance();
            GetUserInfo();
        }
    }

    private void VerifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    Log.d("Snapshot", snapshot.toString());
                    if(snapshot.child("name").exists())
                    {
//                        Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                     }
                    else
                    {
                        sendUserToSettingActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void sendUserToFindFriendsActivity() {
        Intent intent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(intent);
//        finish();
    }
    private void sendUserToSettingActivity() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Coding Cafe");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                }
                else {
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void GetUserInfo() {
        currentUserID = currentUser.getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
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

    private void CreateNewGroup(String groupName) {
         GroupRef = RootRef.child("Groups").child(groupName);
         //String groupKey = GroupRef.push().getKey();
         GroupRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            GroupRef.child("members").child(currentUserID).child("name").setValue(currentUserName)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(MainActivity.this,groupName + " group is Created Successful",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                        }
                    }
                });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_option)
        {
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_setting_option)
        {
            sendUserToSettingActivity();
        }
        if(item.getItemId() == R.id.main_create_groups_option)
        {
            RequestNewGroup();
        }
        if(item.getItemId() == R.id.main_find_friends_option)
        {
            sendUserToFindFriendsActivity();
        }
        return true;
    }

}