package didong.ungdungchat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import didong.ungdungchat.databinding.ActivityPhoneLoginBinding;

public class PhoneLoginActivity extends AppCompatActivity {

    private ActivityPhoneLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginPhone.requestFocus();
        binding.btnSendVerifycation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = binding.loginPhone.getText().toString().trim();

                // Validate the phone number
                if (phoneNumber.isEmpty()) {
                    binding.loginPhone.setError("Phone number is required");
                    return;
                }

                // Check if the phone number starts with the country code
                if (!phoneNumber.startsWith("+")) {
                    binding.loginPhone.setError("Phone number must include country code and start with +");
                    return;
                }

                // Check the length of the phone number (including country code)
                if (phoneNumber.length() < 10) {
                    binding.loginPhone.setError("Phone number is too short");
                    return;
                }

                Intent intent = new Intent(PhoneLoginActivity.this, OTPPhoneActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });
    }
}
