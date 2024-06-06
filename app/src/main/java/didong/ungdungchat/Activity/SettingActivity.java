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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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
    private Uri selectedImageUri; // Store the selected image URI
    private String currentProfileImageUrl; // Store the current profile image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
    }

    private void updateSettings() {
        String name = binding.username.getText().toString();
        String status = binding.profileStatus.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please write your user name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(status)) {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingBar.setTitle("Updating Profile");
        loadingBar.setMessage("Please wait while we are updating your profile...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(SettingActivity.this, "Please complete all fields before updating your profile", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(SettingActivity.this, "Failed to get download URL: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(SettingActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SettingActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                        loadingBar.dismiss();
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
                            Toast.makeText(SettingActivity.this, "Please set your profile information", Toast.LENGTH_SHORT).show();
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
}
