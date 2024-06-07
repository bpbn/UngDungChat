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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private ProgressDialog loading;
    ActivityLoginBinding binding;
    GoogleSignInOptions gOptions;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loading = new ProgressDialog(this);
        binding.needNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();

            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        binding.btnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPhoneActivity();
            }
        });

        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gOptions);

        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
//                sendUserToGoogleActivity();
            }
        });
    }
    private void SignInGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                FirebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập thất bại.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void FirebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful()) {
                                                    UsersRef.child(currentUserID).child("device_token")
                                                            .setValue(task.getResult());
                                                }
                                            }
                                        });
                                sendUserToMainActivity();
                                loading.dismiss();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                loading.dismiss();
                            }
                        }
                    }
                });
    }

    private void AllowUserToLogin() {
        String email = binding.loginEmail.getText().toString().trim();
        String password = binding.loginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.loginEmail.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.loginPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        loading.setTitle("Đăng nhập");
        loading.setMessage("Vui lòng đợi...");
        loading.setCanceledOnTouchOutside(true);
        loading.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loading.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user.isEmailVerified()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful()) {
                                                    UsersRef.child(currentUserID).child("device_token")
                                                            .setValue(task.getResult());
                                                }
                                            }
                                        });
                                sendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Vui lòng xác minh email của bạn.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String errorMessage;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                errorMessage = "Không tìm thấy địa chỉ email tương thích.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "Email hoặc mật khẩu không hợp lệ.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "Email đã tồn tại.";
                            } catch (FirebaseAuthEmailException e) {
                                errorMessage = "Định dạng email không hợp lệ.";
                            } catch (Exception e) {
                                errorMessage = "Xác thực không thành công: ";
                            }
                            Log.e("AuthError", errorMessage, task.getException());
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }



    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void sendUserToPhoneActivity() {
        Intent intent = new Intent(this, PhoneLoginActivity.class);
        startActivity(intent);
    }

}