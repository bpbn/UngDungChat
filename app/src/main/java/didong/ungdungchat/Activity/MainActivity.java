package didong.ungdungchat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;

import didong.ungdungchat.Adapter.TabsAccessorAdapter;
import didong.ungdungchat.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.mainAppBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Ứng dụng chat");
        binding.mainTabsPager.setAdapter(new TabsAccessorAdapter(this));
        new TabLayoutMediator(binding.mainTabs, binding.mainTabsPager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Tin nhắn");
                    getSupportActionBar().setTitle("Tin nhắn");
                    break;
                case 1:
                    tab.setText("Nhóm");
                    getSupportActionBar().setTitle("Nhóm");
                    break;
                case 2:
                    tab.setText("Liên hệ");
                    getSupportActionBar().setTitle("Liên hệ");
                    break;
            }
        }).attach();
    }
}