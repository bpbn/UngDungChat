package didong.ungdungchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import didong.ungdungchat.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityRegisterBinding binding;
    private ProgressDialog loading;
    private DatabaseReference Rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        Rootref = FirebaseDatabase.getInstance().getReference();
        loading =new ProgressDialog(this);
        binding.alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });
        binding.btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }

        });
    }
    private void CreateNewAccount() {
        String email = binding.registerEmail.getText().toString();
        String password = binding.registerPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            binding.registerEmail.setError("Vui lòng nhập email");
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            binding.registerPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        else
        {
            loading.setTitle("Đang tạo tài khoản mới");
            loading.setMessage("Vui lòng đợi...");
            loading.setCanceledOnTouchOutside(true);
            loading.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {

                                String currentUserId = mAuth.getCurrentUser().getUid();
                                Rootref.child("Users").child(currentUserId).setValue("");

                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful()) {
                                                    Rootref.child("Users").child(currentUserId).child("device_token")
                                                            .setValue(task.getResult());
                                                }
                                            }
                                        });
                                mAuth.getCurrentUser().sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
//                                                    sendUserToMainActivity();
                                                    Toast.makeText(RegisterActivity.this, "Tài khoản được tạo thành công. Vui lòng xác minh email của bạn.", Toast.LENGTH_SHORT).show();
                                                    loading.dismiss();
                                                }

                                            }
                                        });

//                                sendUserToMainActivity();
                                sendUserToLoginActivity();
                                Toast.makeText(RegisterActivity.this, "Tạo mới tài khoản thành công", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                            else
                            {
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Không thể tạo tài khoản", Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    });

        }
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }


}