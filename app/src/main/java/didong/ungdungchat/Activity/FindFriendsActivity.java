package didong.ungdungchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityFindFriendsBinding;
import didong.ungdungchat.databinding.UsersDisplayLayoutBinding;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;

    private DatabaseReference UsersRef, ContactRef, ChatRequestsRef, RootRef;
    private FirebaseAuth mAuth;
    private String currentUserID, cUID;
    private List<String> contactsList= new ArrayList<>();
    private List<String> requestsSentList = new ArrayList<>();

    ActivityFindFriendsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindFriendsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo FirebaseAuth và lấy thông tin người dùng hiện tại
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = currentUser.getUid();
        }
        cUID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        binding.findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(binding.findFriendsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Tìm Bạn Bè");

        fetchContactsAndRequestsSent();

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForFriends(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchForFriends(newText);
                return false;
            }
        });
        updateUserStatus("online");
    }

    private void fetchContactsAndRequestsSent() {
        // Lấy danh sách UID đã kết bạn
        ContactRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contactsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String contactID = snapshot.getKey();
                    contactsList.add(contactID);
                }
                // Sau khi lấy danh sách, cập nhật danh sách hiển thị
                updateFriendsList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi
            }
        });

        // Lấy danh sách UID đã gửi lời mời kết bạn
        ChatRequestsRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestsSentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String receiverID = snapshot.getKey();
                    requestsSentList.add(receiverID);
                }
                // Sau khi lấy danh sách, cập nhật danh sách hiển thị
                updateFriendsList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi
            }
        });
    }

    private void updateFriendsList() {
        // Tạo query để lấy danh sách người dùng từ UsersRef
        Query query = UsersRef.orderByChild("name");

        // Tạo FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query, Contacts.class)
                .build();

        // Tạo FirebaseRecyclerAdapter
        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                        String userIDs = getRef(position).getKey();

                        // Kiểm tra nếu userIDs không nằm trong danh sách contactsList và requestsSentList và không phải là user đang đăng nhập
                        if (!contactsList.contains(userIDs) && !requestsSentList.contains(userIDs) && !userIDs.equals(currentUserID)) {
                            holder.userName.setText(model.getName());
                            holder.userStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();
                                    Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        } else {
                            // Nếu là bạn bè hoặc đã gửi lời mời kết bạn, ẩn item này đi
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        return new FindFriendViewHolder(UsersDisplayLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
                    }
                };

        binding.findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    private void searchForFriends(String searchText) {
        Query query;
        if (TextUtils.isEmpty(searchText)) {
            query = UsersRef.orderByChild("name");
        } else {
            query = UsersRef.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        }

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(query, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                        String userIDs = getRef(position).getKey();

                        if (!contactsList.contains(userIDs) && !requestsSentList.contains(userIDs) && !userIDs.equals(currentUserID)) {
                            holder.userName.setText(model.getName());
                            holder.userStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();
                                    Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        return new FindFriendViewHolder(UsersDisplayLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
                    }
                };
        binding.findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(UsersRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Contacts model) {
                        String userIDs = getRef(position).getKey();

                        if (!contactsList.contains(userIDs) && !requestsSentList.contains(userIDs) && !userIDs.equals(currentUserID)) {
                            holder.userName.setText(model.getName());
                            holder.userStatus.setText(model.getStatus());
                            Picasso.get().load(model.getImage()).into(holder.profileImage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();
                                    Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                        }
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        return new FindFriendViewHolder(UsersDisplayLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
                    }
                };
        binding.findFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
        updateUserStatus("online");
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        public FindFriendViewHolder(@NonNull UsersDisplayLayoutBinding itemView) {
            super(itemView.getRoot());
            profileImage = itemView.usersProfileImage;
            userStatus = itemView.userStatus;
            userName = itemView.userProfileName;

        }
    }
    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);
        RootRef.child("Users").child(cUID).child("userState")
                .updateChildren(onlineStateMap);

    }
}