package didong.ungdungchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityForgotPasswordBinding;
import didong.ungdungchat.databinding.ActivityLoginBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;
    private ProgressDialog loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        loading = new ProgressDialog(ForgotPasswordActivity.this);
        loading.setMessage("Vui lòng đợi");

        binding.btnXacMinh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
    }
    private Boolean validateEmail()
    {
        String val = binding.forgotEmail.getText().toString();
        if(val.isEmpty())
        {
            binding.forgotEmail.setError("Vui lòng nhập email");
            return false;
        }
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!val.matches(emailPattern))
        {
            binding.forgotEmail.setError("Vui lòng nhập đúng định dạng email");
            return false;
        }
        else
        {
            binding.forgotEmail.setError(null);
            return true;
        }
    }
    private void forgotPassword()
    {
        if(!validateEmail())
        {
            return;
        }
        loading.show();

        mAuth.sendPasswordResetEmail(binding.forgotEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loading.dismiss();
                if(task.isSuccessful())
                {
                    startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                    finish();
                    Toast.makeText(ForgotPasswordActivity.this, "Vui lòng kiểm tra gmail để đổi mật khẩu", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Không thể đổi mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgotPasswordActivity.this, "Không thể đổi mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}