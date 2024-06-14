package didong.ungdungchat.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivitySettingBinding;
import didong.ungdungchat.databinding.LayoutChangePasswordBinding;

public class SettingActivity extends AppCompatActivity {

    ActivitySettingBinding binding;
    private String currentUserID, cUID;
    LayoutChangePasswordBinding changePasswordBinding;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;
    private Dialog dialog;
    private Uri selectedImageUri; // Store the selected image URI
    private String currentProfileImageUrl; // Store the current profile image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.settingToolbar);
        dialog = new Dialog(SettingActivity.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        binding.btnChangPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
                changePasswordBinding.passwordOld.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        showPassword(changePasswordBinding.passwordOld,event);
                        return false;
                    }
                });
                changePasswordBinding.passwordNew.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        showPassword(changePasswordBinding.passwordNew,event);
                        return false;
                        }
                });
                changePasswordBinding.passConfirm.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        showPassword(changePasswordBinding.passConfirm, event);
                        return false;
                    }

                });
            }
        });


    }
    private boolean isPasswordVisible = false;

    private void changePassword() {
        dialog.setContentView(R.layout.layout_change_password);
        dialog.setTitle("Đổi mật khẩu");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show(); // Hiển thị Dialog

        // Gắn changePasswordBinding với layout_change_password
        changePasswordBinding = LayoutChangePasswordBinding.bind(dialog.findViewById(R.id.change_layout_password));

        changePasswordBinding.btnchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldpass = changePasswordBinding.passwordOld.getText().toString();
                String newpass = changePasswordBinding.passwordNew.getText().toString();
                String confirmpass = changePasswordBinding.passConfirm.getText().toString();

                if(TextUtils.isEmpty(oldpass))
                {
                    changePasswordBinding.passwordOld.setError("Vui lòng nhập mật khẩu cũ");
                }
                else if(oldpass.length() < 6)
                {
                    changePasswordBinding.passwordOld.setError("Mật khẩu cũ phải có ít nhất 6 ký tự");
                }
                else if(TextUtils.isEmpty(newpass))
                {
                    changePasswordBinding.passwordNew.setError("Vui lòng nhập mật khẩu mới");
                }
                else if(newpass.length() < 6)
                {
                    changePasswordBinding.passwordNew.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
                }
                else if(TextUtils.isEmpty(confirmpass))
                {
                    changePasswordBinding.passConfirm.setError("Vui lòng nhập lại mật khẩu mới");
                }
                else if(!newpass.equals(confirmpass))
                {
                    changePasswordBinding.passConfirm.setError("Mật khẩu mới không trùng với mật khẩu nhập lại");
                }
                else {
                    updatePassword(oldpass, newpass);
                }

            }
        });
    }
    private void showPassword(TextInputEditText editText, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                // Thay đổi trạng thái hiển thị mật khẩu
                if (editText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_vpn_key_24, 0);
                } else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_vpn_key_off_24, 0);
                }
                editText.setSelection(editText.getText().length()); // Di chuyển con trỏ tới cuối
            }
        }
    }


    private void updatePassword(String oldpass, String newpass) {
        loadingBar.show();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldpass);

        user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                loadingBar.dismiss();
                user.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(SettingActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                        dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        Toast.makeText(SettingActivity.this, "Mật khẩu cũ không chính xác", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(SettingActivity.this, "Đổi mật khẩu không thành công", Toast.LENGTH_SHORT).show();
            }
        });
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
