package didong.ungdungchat.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityOtpphoneBinding;

public class OTPPhoneActivity extends AppCompatActivity {

    private ActivityOtpphoneBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpphoneBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}