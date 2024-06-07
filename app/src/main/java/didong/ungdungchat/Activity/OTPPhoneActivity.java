package didong.ungdungchat.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukeshsolanki.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

import didong.ungdungchat.databinding.ActivityOtpphoneBinding;

public class OTPPhoneActivity extends AppCompatActivity {

    private ActivityOtpphoneBinding binding;
    private FirebaseAuth mAuth;
    private String verificationId;
    private ProgressDialog loaddialog;
    private PhoneAuthProvider.ForceResendingToken mResentToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpphoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loaddialog = new ProgressDialog(this);
        loaddialog.setMessage("Sending OTP...");
        loaddialog.setCancelable(false);
        loaddialog.show();

        mAuth = FirebaseAuth.getInstance();
        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.verify.setText("Xác minh: " + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPPhoneActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
//                        signInWithPhoneAuthCredential(phoneAuthCredential);
                        loaddialog.dismiss();
                        Toast.makeText(OTPPhoneActivity.this, "Đã nhận được mã OTP", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        loaddialog.dismiss();
                        handleVerificationFailure(e);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        loaddialog.dismiss();
                        verificationId = verifyId;
                        mResentToken = forceResendingToken;
                        Toast.makeText(OTPPhoneActivity.this, "Đã gửi OTP", Toast.LENGTH_SHORT).show();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        binding.btnSendVerifycation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = binding.otpView.getText().toString();
                if (otp != null && !otp.isEmpty()) {
                    loaddialog.setMessage("Xác minh OTP...");
                    loaddialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Toast.makeText(OTPPhoneActivity.this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.otpView.post(new Runnable() {
            @Override
            public void run() {
                binding.otpView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.otpView, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        loaddialog.setMessage("Xác minh OTP...");
        loaddialog.show();

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loaddialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(OTPPhoneActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                } else {
                    handleSignInFailure(task.getException());
                }
            }
        });
    }


    private void handleVerificationFailure(FirebaseException e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            String errorCode = authException.getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equals(errorCode)) {
                Toast.makeText(OTPPhoneActivity.this, "Quá nhiều yêu cầu. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                return;
            } else if ("ERROR_QUOTA_EXCEEDED".equals(errorCode)) {
                Toast.makeText(OTPPhoneActivity.this, "Vượt chỉ tiêu SMS. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(OTPPhoneActivity.this, "Xác minh thất bại", Toast.LENGTH_LONG).show();
    }

    private void handleSignInFailure(Exception e) {
        if (e instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) e;
            String errorCode = authException.getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equals(errorCode)) {
                Toast.makeText(OTPPhoneActivity.this, "Quá nhiều yêu cầu. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Toast.makeText(OTPPhoneActivity.this, "Đăng nhập thất bại", Toast.LENGTH_LONG).show();
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
