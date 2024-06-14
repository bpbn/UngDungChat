package didong.ungdungchat.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import didong.ungdungchat.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;
    private String currentUserID, cUID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    private Uri selectedImageUri; // Store the selected image URI
    private String currentProfileImageUrl; // Store the current profile image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.settingToolbar.getRoot());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Cài đặt tài khoản");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        loadingBar = new ProgressDialog(this);

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                selectedImageUri = data.getData();
                                binding.profileImage.setImageURI(selectedImageUri);
                            }
                        }
                    }
                });

        binding.btnSettingUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        retrieveUserInfo();
        if (mAuth.getCurrentUser() != null) {
            cUID = mAuth.getCurrentUser().getUid();
            updateUserStatus("online");
        }
    }

    private void updateSettings() {
        String name = binding.username.getText().toString();
        String status = binding.profileStatus.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng viết tên người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Vui lòng viết trạng thái", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Đang cập nhật hồ sơ");
        loadingBar.setMessage("Vui lòng đợi trong khi chúng tôi đang cập nhật hồ sơ của bạn...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(SettingActivity.this, "Vui lòng điền các thông tin trước khi cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
            }
        });
        loadingBar.show();

        if (selectedImageUri != null) {
            StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");
            filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String downloadUrl = task.getResult().toString();
                                    saveUserInfo(name, status, downloadUrl);
                                } else {
                                    Toast.makeText(SettingActivity.this, "Không nhận được hình ảnh", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SettingActivity.this, "Không nhận được hình ảnh", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        } else {
            saveUserInfo(name, status, currentProfileImageUrl);
        }
    }

    private void saveUserInfo(String name, String status, String imageUrl) {
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", currentUserID);
        profileMap.put("name", name);
        profileMap.put("status", status);
        if (imageUrl != null) {
            profileMap.put("image", imageUrl);
        }

        RootRef.child("Users").child(currentUserID).setValue(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendUserToMainActivity();
                            Toast.makeText(SettingActivity.this, "Hồ sơ được cập nhật thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Không thể cập nhật được hồ sơ", Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
                    }
                });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            RootRef.child("Users").child(currentUserID).child("device_token")
                                    .setValue(task.getResult());
                        }
                    }
                });
    }

    private void retrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String userName = snapshot.child("name").getValue(String.class);
                            String status = snapshot.child("status").getValue(String.class);
                            currentProfileImageUrl = snapshot.child("image").getValue(String.class);

                            if (userName != null) {
                                binding.username.setText(userName);
                            }

                            if (status != null) {
                                binding.profileStatus.setText(status);
                            }

                            if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                                Picasso.get().load(currentProfileImageUrl).into(binding.profileImage);
                            }
                        } else {
                            Toast.makeText(SettingActivity.this, "Vui lòng nhập thông tin hồ sơ của bạn", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SettingActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
