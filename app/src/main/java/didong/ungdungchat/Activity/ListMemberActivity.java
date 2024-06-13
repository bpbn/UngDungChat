package didong.ungdungchat.Activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityGroupChatBinding;
import didong.ungdungchat.databinding.ActivityListMemberBinding;
import didong.ungdungchat.databinding.ContactListGroupLayoutBinding;
import didong.ungdungchat.databinding.MemberDisplayGroupsBinding;

public class ListMemberActivity extends AppCompatActivity {

    ActivityListMemberBinding binding;
    DatabaseReference GroupRef, UsersRef, GroupMembersRef;
    FirebaseAuth mAuth;
    String currentGroupID, itemUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListMemberBinding.inflate(getLayoutInflater());
//        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        currentGroupID = getIntent().getExtras().get("groupID").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupMembersRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID).child("members");
        binding.listMember.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        initializeFields();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(GroupMembersRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ListMemberActivity.MembersViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ListMemberActivity.MembersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ListMemberActivity.MembersViewHolder holder, int i, @NonNull Contacts contacts) {
                final String usersIDs = getRef(i).getKey();
                final String[] retImage = {"default_image"};

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                String image = dataSnapshot.child("image").getValue().toString();
                                Picasso.get().load(image).into(holder.profileImage);
                            }

                            if (dataSnapshot.hasChild("name")) {
                                String name = dataSnapshot.child("name").getValue().toString();
                                String uid = dataSnapshot.child("uid").getValue().toString();
                                holder.userName.setText(name);
                                GroupMembersRef.child(uid).addValueEventListener(new ValueEventListener() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists() && snapshot.hasChild("nickname")) {
                                            String nickname = snapshot.child("nickname").getValue().toString();
                                            holder.nickname.setText(nickname);
                                        } else {
                                            holder.nickname.setText("Đặt biệt danh");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestNickName(name, uid);
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
            @NonNull
            @Override
            public ListMemberActivity.MembersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                return new ListMemberActivity.MembersViewHolder(MemberDisplayGroupsBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
            }
        };
        binding.listMember.setAdapter(adapter);
        adapter.startListening();
    }

    private void requestNickName(String userName, String userID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListMemberActivity.this, R.style.AlertDialog);
        builder.setTitle("Chỉnh sửa biệt danh ");

        final EditText nickNameField = new EditText(ListMemberActivity.this);
        GroupMembersRef.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("nickname")) {
                    String nickname = snapshot.child("nickname").getValue().toString();
                    nickNameField.setText(nickname);
                } else {
                    nickNameField.setText(userName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        builder.setView(nickNameField);

        builder.setPositiveButton("ĐẶT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nickname = nickNameField.getText().toString();
                if(TextUtils.isEmpty(nickname)){
                    Toast.makeText(ListMemberActivity.this, "Please write Nickname", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNickName(nickname, userID);
                }
            }
        });

        builder.setNeutralButton("HỦY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setNegativeButton("GỠ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeNickName(userID);
            }
        });

        builder.show();
    }

    private void createNickName(String nickname, String userID) {
        GroupMembersRef.child(userID).child("nickname").setValue(nickname).addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }
    private void removeNickName(String userID) {
        GroupMembersRef.child(userID).child("nickname").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

    private void initializeFields() {
        setSupportActionBar(binding.membersGroupChatBarLayout);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Danh sách thành viên");
    }
    public static class MembersViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName, nickname;
        String userID;
        public MembersViewHolder(MemberDisplayGroupsBinding itemView) {
            super(itemView.getRoot());
            profileImage = itemView.usersProfileImage;
            userName = itemView.userProfileName;
            nickname = itemView.userNickName;
        }
    }
}