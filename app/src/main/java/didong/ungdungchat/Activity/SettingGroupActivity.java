package didong.ungdungchat.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityGroupChatBinding;
import didong.ungdungchat.databinding.ActivitySettingGroupBinding;

public class SettingGroupActivity extends AppCompatActivity {

    ActivitySettingGroupBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    String currentGroupID, currentGroupName;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private Uri selectedImageUri;
    private StorageReference userProfileImageRef;
    private String currentProfileImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.settingToolbar);
        getSupportActionBar().setTitle("Cài đặt");

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Group_Images");

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                binding.groupImage.setImageURI(selectedImageUri);
                            }
                        }
                    }
                });
        binding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        binding.groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        retrieveGroupInfo();
    }

    private void updateSettings() {
        String name = binding.groupname.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng viết tên nhóm", Toast.LENGTH_SHORT).show();
        }
        if (selectedImageUri != null) {
            StorageReference filePath = userProfileImageRef.child(currentGroupID + ".jpg");
            filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    saveUserInfo(name, downloadUrl);
                                } else {
                                    Toast.makeText(SettingGroupActivity.this, "Không nhận được hình ảnh", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SettingGroupActivity.this, "Không nhận được hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            saveUserInfo(name, currentProfileImageUrl);
        }
    }

    private void saveUserInfo(String name, String imageUrl) {
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("name", name);
        if (imageUrl != null) {
            profileMap.put("image", imageUrl);
        }

        rootRef.child("Groups").child(currentGroupID).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingGroupActivity.this, "Hồ sơ được cập nhật thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingGroupActivity.this, "Không thể cập nhật được hồ sơ: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void retrieveGroupInfo() {
        rootRef.child("Groups").child(currentGroupID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userName = snapshot.child("name").getValue(String.class);
                            currentProfileImageUrl = snapshot.child("image").getValue(String.class);

                            if (userName != null) {
                                binding.groupname.setText(userName);
                            }

                            if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                                Picasso.get().load(currentProfileImageUrl).into(binding.groupImage);
                            }
                        } else {
                            Toast.makeText(SettingGroupActivity.this, "Vui lòng nhập thông tin hồ sơ của bạn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SettingGroupActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}