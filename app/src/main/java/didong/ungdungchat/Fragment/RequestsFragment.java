package didong.ungdungchat.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Model.Contacts;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.FragmentContactsBinding;
import didong.ungdungchat.databinding.FragmentRequestsBinding;
import didong.ungdungchat.databinding.UsersDisplayLayoutBinding;

public class RequestsFragment extends Fragment {

    FragmentRequestsBinding binding;

    private DatabaseReference ChatRequestsRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        binding = FragmentRequestsBinding.bind(view);
        binding.chatRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestsViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.AccecptIButton.setVisibility(View.VISIBLE);
                holder.CancelIButton.setVisibility(View.VISIBLE);

                final String list_user_id = getRef(position).getKey();

                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists())
                        {
                            String type = snapshot.getValue().toString();
                            if(type.equals("received")){
                                UserRef.child(list_user_id).addValueEventListener( new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("image"))
                                        {

                                            final String requestUserName = snapshot.child("name").getValue().toString();
                                            final String requestUserStatus = snapshot.child("status").getValue().toString();
                                            final String requestProfileImage = snapshot.child("image").getValue().toString();

                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);
                                            Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                        }
                                        else
                                        {
                                            final String requestUserName = snapshot.child("name").getValue().toString();
                                            final String requestUserStatus = snapshot.child("status").getValue().toString();

                                            holder.userName.setText(requestUserName);
                                            holder.userStatus.setText(requestUserStatus);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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
            public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new RequestsViewHolder(UsersDisplayLayoutBinding.bind(view));
            }
        };

        binding.chatRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageButton AccecptIButton, CancelIButton;
        public RequestsViewHolder(UsersDisplayLayoutBinding itemView) {
            super(itemView.getRoot());

            userName = itemView.userProfileName;
            userStatus = itemView.userStatus;
            profileImage = itemView.usersProfileImage;
            AccecptIButton = itemView.requestsAcceptBtn;
            CancelIButton = itemView.requestsCancelBtn;
        }
    }
}