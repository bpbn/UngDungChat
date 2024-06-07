package didong.ungdungchat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

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

                if (phoneNumber.isEmpty()) {
                    binding.loginPhone.setError("Vui lòng nhập số điện thoại");
                    return;
                }

                if (!phoneNumber.startsWith("0")) {
                    binding.loginPhone.setError("Số điện thoại không chính xác");
                    return;
                }

                if (phoneNumber.length() < 10 || phoneNumber.length() >= 11) {
                    binding.loginPhone.setError("Số điện thoại phải có 10 chữ số");
                    return;
                }

                if(phoneNumber.startsWith("0"))
                {
                    phoneNumber = "+84" + phoneNumber.substring(1);
                }

                Intent intent = new Intent(PhoneLoginActivity.this, OTPPhoneActivity.class);
                intent.putExtra("phoneNumber", phoneNumber);
                startActivity(intent);
            }
        });
    }
}
