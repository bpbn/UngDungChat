package didong.ungdungchat.Adapter;

import static android.content.Intent.getIntent;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Model.GroupMessages;
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.CustomMessagesGroupsLayoutBinding;
import didong.ungdungchat.databinding.CustomMessagesLayoutBinding;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.GroupMessagesViewHolder> {
    private List<GroupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupMemberRef;
    private String currentGroupID;

    public GroupMessagesAdapter(List<GroupMessages> groupMessagesList, String currentGroupID) {
        this.groupMessagesList = groupMessagesList;
        this.currentGroupID = currentGroupID;  }

    @NonNull
    @Override
    public GroupMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_groups_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new GroupMessagesAdapter.GroupMessagesViewHolder(CustomMessagesGroupsLayoutBinding.bind(view));
    }
    @Override
    public void onBindViewHolder(@NonNull GroupMessagesViewHolder groupMessagesViewHolder, int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        GroupMessages groupMessages = groupMessagesList.get(i);

        String fromUserID = groupMessages.getFrom();
        String fromMessageType = groupMessages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        groupMemberRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID).child("members");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.baseline_account_circle_24).into(groupMessagesViewHolder.imgReceiver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        groupMessagesViewHolder.txtReceiverName.setVisibility(View.GONE);
        groupMessagesViewHolder.txtReceiverMess.setVisibility(View.GONE);
        groupMessagesViewHolder.imgReceiver.setVisibility(View.GONE);
        groupMessagesViewHolder.txtSenderMess.setVisibility(View.GONE);
        groupMessagesViewHolder.messageSenderPicture.setVisibility(View.GONE);
        groupMessagesViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        if(fromMessageType != null){
            if (fromMessageType.equals("text"))
            {
                if (fromUserID.equals(messageSenderId))
                {
                    groupMessagesViewHolder.txtSenderMess.setVisibility(View.VISIBLE);

                    groupMessagesViewHolder.txtSenderMess.setBackgroundResource(R.drawable.sender_messages_layout);
                    groupMessagesViewHolder.txtSenderMess.setTextColor(Color.BLACK);
                    groupMessagesViewHolder.txtSenderMess.setText(groupMessages.getMessage() + "\n \n" + groupMessages.getTime() + " - " + groupMessages.getDate());
                }
                else
                {
                    groupMessagesViewHolder.txtReceiverName.setVisibility(View.VISIBLE);
                    groupMessagesViewHolder.imgReceiver.setVisibility(View.VISIBLE);
                    groupMessagesViewHolder.txtReceiverMess.setVisibility(View.VISIBLE);

                    groupMessagesViewHolder.txtReceiverMess.setBackgroundResource(R.drawable.receiver_messages_layout);
                    groupMessagesViewHolder.txtReceiverMess.setTextColor(Color.BLACK);
                    groupMessagesViewHolder.txtReceiverMess.setText(groupMessages.getMessage() + "\n \n" + groupMessages.getTime() + " - " + groupMessages.getDate());
                    groupMemberRef.child(fromUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChild("nickname")) {
                                String nickname = snapshot.child("nickname").getValue().toString();
                                groupMessagesViewHolder.txtReceiverName.setText(nickname);
                            } else {
                                groupMessagesViewHolder.txtReceiverName.setText(groupMessages.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle possible errors.
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    public class GroupMessagesViewHolder extends RecyclerView.ViewHolder{
        public TextView txtSenderMess, txtReceiverMess, txtReceiverName;
        public CircleImageView imgReceiver;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public GroupMessagesViewHolder(@NonNull CustomMessagesGroupsLayoutBinding itemView) {
            super(itemView.getRoot());
            txtReceiverName = itemView.nameUser;
            txtSenderMess = itemView.senderMesssageGroupsText;
            txtReceiverMess = itemView.receiverMessageGroupsText;
            imgReceiver = itemView.messageGroupsProfileImage;
            messageReceiverPicture = itemView.messageGroupsReceiverImageView;
            messageSenderPicture = itemView.messageGroupsSenderImageView;
        }
    }
}
