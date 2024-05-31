package didong.ungdungchat.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    ActivityProfileBinding binding;

    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
//        Toast.makeText(this, "User ID: " + receiverUserID, Toast.LENGTH_SHORT).show();

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.exists()) && (snapshot.hasChild("image"))){
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).into(binding.visitProfileImage);
                    binding.visitUserName.setText(userName);
                    binding.visitUserStatus.setText(userStatus);
                }
                else{
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    binding.visitUserName.setText(userName);
                    binding.visitUserStatus.setText(userStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}