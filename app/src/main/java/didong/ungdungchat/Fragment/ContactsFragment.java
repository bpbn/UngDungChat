package didong.ungdungchat.Fragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Activity.ChatActivity;
import didong.ungdungchat.Activity.ProfileActivity;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.FragmentContactsBinding;
import didong.ungdungchat.databinding.UsersDisplayLayoutBinding;

public class ContactsFragment extends Fragment {

    private FragmentContactsBinding binding;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID, currentUserName, userName;
    private FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        binding = FragmentContactsBinding.bind(view);
        binding.contactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

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

        setupRecyclerView();

        // Listener to refresh the RecyclerView when contacts change
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName = snapshot.child("name").getValue().toString();
                    userName = currentUserName;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {
                final String[] retImage = {"default_image"};
                String userIDs = getRef(position).getKey();
                holder.chatIButton.setVisibility(View.VISIBLE);
                holder.chatIButton.setImageResource(R.drawable.baseline_chat_24);
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            final String retName = snapshot.child("name").getValue().toString();
                            if (snapshot.child("userState").hasChild("state")) {
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.hasChild("image")) {
                                String userImage = snapshot.child("image").getValue().toString();
                                String profileName = snapshot.child("name").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.baseline_account_circle_24).into(holder.profileImage);
                            } else {
                                String profileName = snapshot.child("name").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }

                            holder.chatIButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (snapshot.hasChild("image"))
                                    {
                                        retImage[0] = snapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userIDs);
                                    chatIntent.putExtra("visit_user_name", retName);
                                    chatIntent.putExtra("visit_image", retImage[0]);
                                    chatIntent.putExtra("visit_user_device_token", snapshot.child("device_token").getValue().toString());
                                    chatIntent.putExtra("user_name", userName);

                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ContactsViewHolder(UsersDisplayLayoutBinding.bind(view));
            }
        };

        binding.contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    private void searchForFriends(String searchText) {
        Query query;
        if (TextUtils.isEmpty(searchText)) {
            query = contactsRef;
        } else {
            query = usersRef.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");
        }

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(query, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> searchAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String userIDs = getRef(position).getKey();
                contactsRef.child(userIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            loadUserDetails(holder, userIDs);
                        } else {
                            holder.itemView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ContactsViewHolder(UsersDisplayLayoutBinding.bind(view));
            }
        };

        binding.contactsList.setAdapter(searchAdapter);
        searchAdapter.startListening();
    }

    private void loadUserDetails(ContactsViewHolder holder, String userIDs) {
        usersRef.child(userIDs).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.child("userState").hasChild("state")) {
                        String state = snapshot.child("userState").child("state").getValue().toString();
                        if (state.equals("online")) {
                            holder.onlineIcon.setVisibility(View.VISIBLE);
                        } else {
                            holder.onlineIcon.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                    }

                    if (snapshot.hasChild("image")) {
                        String userImage = snapshot.child("image").getValue().toString();
                        String profileName = snapshot.child("name").getValue().toString();
                        String profileStatus = snapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);
                        Picasso.get().load(userImage).placeholder(R.drawable.baseline_account_circle_24).into(holder.profileImage);
                    } else {
                        String profileName = snapshot.child("name").getValue().toString();
                        String profileStatus = snapshot.child("status").getValue().toString();

                        holder.userName.setText(profileName);
                        holder.userStatus.setText(profileStatus);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userName, userStatus;
        ImageView onlineIcon;
        ImageButton chatIButton;

        public ContactsViewHolder(UsersDisplayLayoutBinding itemView) {
            super(itemView.getRoot());
            profileImage = itemView.usersProfileImage;
            userName = itemView.userProfileName;
            userStatus = itemView.userStatus;
            onlineIcon = itemView.userOnlineStatus;
            chatIButton = itemView.requestsAcceptBtn;
        }
    }
}
