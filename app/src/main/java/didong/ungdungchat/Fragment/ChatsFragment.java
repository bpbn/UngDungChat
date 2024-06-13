package didong.ungdungchat.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Activity.ChatActivity;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.FragmentChatsBinding;
import didong.ungdungchat.databinding.UsersDisplayLayoutBinding;

public class ChatsFragment extends Fragment {
    FragmentChatsBinding binding;
    DatabaseReference chatsRef, usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        binding = FragmentChatsBinding.bind(view);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        binding.chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model)
                    {
                        final String usersIDs = getRef(position).getKey();
                        final String[] retImage = {"default_image"};

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            holder.userStatus.setText("online");
                                        }
                                        else if (state.equals("offline"))
                                        {
                                            SimpleDateFormat now = new SimpleDateFormat("dd/MM/yyyy");
                                            String currentDate = now.format(Calendar.getInstance().getTime());
                                            if (currentDate.equals(date)) {
                                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                                try {
                                                    long time1 = Objects.requireNonNull(sdf.parse(time)).getTime();
                                                    long time2 = Objects.requireNonNull(sdf.parse(sdf.format(Calendar.getInstance().getTime()))).getTime();
                                                    long diff = time2 - time1;
                                                    if (diff < 3600000) {
                                                        holder.userStatus.setText(diff / 60000 + " phút trước");
                                                    } else if (diff < 86400000) {
                                                        holder.userStatus.setText(diff / 3600000 + " giờ trước");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (!currentDate.equals(date)) {
                                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                                try {
                                                    long time1 = Objects.requireNonNull(sdf.parse(date)).getTime();
                                                    long time2 = Objects.requireNonNull(sdf.parse(sdf.format(Calendar.getInstance().getTime()))).getTime();
                                                    long diff = time2 - time1;
                                                    if (diff < 86400000) {
                                                        holder.userStatus.setText("Hôm qua");
                                                    } else {
                                                        holder.userStatus.setText(diff / 86400000 + " ngày trước");
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (state.equals("online")) {
                                                holder.userStatus.setText("online");
                                            }
                                        }
                                    }
                                    else
                                    {
                                        holder.userStatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
                    {
                        return new ChatsViewHolder(UsersDisplayLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
                    }
                };

        binding.chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;
        public ChatsViewHolder(UsersDisplayLayoutBinding itemView) {
            super(itemView.getRoot());
            profileImage = itemView.usersProfileImage;
            userStatus = itemView.userStatus;
            userName = itemView.userProfileName;
        }
    }
}