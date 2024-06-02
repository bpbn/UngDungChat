package didong.ungdungchat.Activity;

import android.app.Activity;
import android.app.ProgressDialog;
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

import java.lang.reflect.Array;
import java.util.HashMap;

import didong.ungdungchat.databinding.ActivitySettingBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference();
        binding.btnSettingUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSetting();
            }
        });
        RetrieveUserInfo();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
//                        loadingBar.setTitle("Set profile image");
//                        loadingBar.setMessage("Please wait, while we are setting profile image...");
//                        loadingBar.setCanceledOnTouchOutside(false);
//                        loadingBar.show();
                        Uri selectedImageUri = data.getData();
                        binding.profileImage.setImageURI(selectedImageUri);
                        // Xử lý hình ảnh và lưu vào Firebase Storage
                        StorageReference filePath = userProfileImageRef.child("Profile_Images").child(currentUserID + ".jpg");
                        filePath.putFile(selectedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                String downloadUrl = task.getResult().toString();
                                                RootRef.child("Users").child(currentUserID).child("image")
                                                        .setValue(downloadUrl)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(SettingActivity.this, "Image saved in database successfully", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(SettingActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                Toast.makeText(SettingActivity.this, "Image updated successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(SettingActivity.this, "Failed to get download URL: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(SettingActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
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
    }

    private void UpdateSetting() {
        String name = binding.username.getText().toString();
        String status = binding.profileStatus.getText().toString();

        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Please write your user name", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Update success", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", name);
            profileMap.put("status", status);
            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(SettingActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if((snapshot.exists()) && (snapshot.hasChild("name") && snapshot.hasChild("image"))) {
                            if (snapshot.hasChild("name")) {
                                String userName = snapshot.child("name").getValue().toString();
                                String status = snapshot.child("status").getValue().toString();
                                String profileImage = snapshot.child("image").getValue().toString();
                                // Kiểm tra URL hình ảnh
                                if (!profileImage.isEmpty()) {
                                    Picasso.get().load(profileImage).into(binding.profileImage, new com.squareup.picasso.Callback() {
                                        @Override
                                        public void onSuccess() {
                                            // Xử lý thành công
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            // Xử lý khi có lỗi tải ảnh
                                            Toast.makeText(SettingActivity.this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                binding.username.setText(userName);
                                binding.profileStatus.setText(status);

//                                Picasso.get().load(profileImage).into(binding.profileImage);
                            }
                        }
                            else if((snapshot.exists()) && (snapshot.hasChild("name")))
                            {
                                String userName = snapshot.child("name").getValue().toString();
                                String status = snapshot.child("status").getValue().toString();
                                binding.username.setText(userName);
                                binding.profileStatus.setText(status);
                            }
                            else
                            {
                                Toast.makeText(SettingActivity.this, "Please set & update your profile information", Toast.LENGTH_SHORT).show();
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }


}