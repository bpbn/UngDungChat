package didong.ungdungchat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Fragment.ChatsFragment;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityAddMemberBinding;
import didong.ungdungchat.databinding.ActivityGroupChatBinding;
import didong.ungdungchat.databinding.ContactListGroupLayoutBinding;
import didong.ungdungchat.databinding.UsersDisplayLayoutBinding;

public class AddMemberActivity extends AppCompatActivity {

    ActivityAddMemberBinding binding;
    DatabaseReference ContactsRef, UsersRef, GroupMembersRef;
    FirebaseAuth mAuth;
    String currentUserID, currentGroupID;
    private List<Contacts> contactsList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMemberBinding.inflate(getLayoutInflater());
        //EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupID = getIntent().getExtras().get("groupID").toString();
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupMembersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID);
        binding.listContact.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, AddMemberActivity.ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int i, @NonNull Contacts contacts) {
                final String usersIDs = getRef(i).getKey();
                final String[] retImage = {"default_image"};
                // Kiểm tra xem người dùng có trong danh sách thành viên của nhóm không
                GroupMembersRef.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild(usersIDs)) {
                            UsersRef.child(usersIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild("image")) {
                                            retImage[0] = dataSnapshot.child("image").getValue().toString();
                                            Picasso.get().load(retImage[0]).into(holder.profileImage);
                                        }

                                        final String retName = dataSnapshot.child("name").getValue().toString();
                                        holder.userName.setText(retName);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        } else {
                            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                            params.height = 0;
                            params.width = 0;
                            holder.itemView.setLayoutParams(params);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Xử lý khi có lỗi xảy ra
                    }
                });
            }
            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                return new ContactsViewHolder(ContactListGroupLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            }
        };
        binding.listContact.setAdapter(adapter);
        adapter.startListening();
    }



    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName;
        public ContactsViewHolder(ContactListGroupLayoutBinding itemView) {
            super(itemView.getRoot());
            profileImage = itemView.usersProfileImage;
            userName = itemView.userProfileName;
        }
    }
}