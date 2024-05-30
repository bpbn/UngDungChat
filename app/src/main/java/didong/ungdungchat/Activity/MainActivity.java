package didong.ungdungchat.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
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
        binding.mainTabsPager.setAdapter(new TabsAccessorAdapter(this));
        new TabLayoutMediator(binding.mainTabs, binding.mainTabsPager, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Tin nhắn");
                    Objects.requireNonNull(getSupportActionBar()).setTitle("Tin nhắn");
                    break;
                case 1:
                    tab.setText("Nhóm");
                    break;
                case 2:
                    tab.setText("Liên hệ");
                    break;
            }
        }).attach();
        binding.mainTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Objects.requireNonNull(getSupportActionBar()).setTitle(tab.getText());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}