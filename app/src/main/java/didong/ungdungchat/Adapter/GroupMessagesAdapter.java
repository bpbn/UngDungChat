package didong.ungdungchat.Adapter;

import static android.content.Intent.getIntent;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import didong.ungdungchat.Activity.ListMemberActivity;
import didong.ungdungchat.Model.GroupMessages;
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.CustomMessagesGroupsLayoutBinding;
import didong.ungdungchat.databinding.CustomMessagesLayoutBinding;

public class GroupMessagesAdapter extends RecyclerView.Adapter<GroupMessagesAdapter.GroupMessagesViewHolder> {
    private List<GroupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupRef;
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
        String message = groupMessages.getMessage();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID);
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
                groupRef.child("messages").child(groupMessages.getMessageID()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists() && snapshot.hasChild("revoke")) {
                            String revoke = snapshot.child("revoke").getValue().toString();

                            if(revoke.equals(messageSenderId) && revoke.equals(fromUserID)){
                                groupMessagesViewHolder.txtSenderMess.setVisibility(View.GONE);
                            } else if(revoke.equals(messageSenderId) && !revoke.equals(fromUserID)){
                                groupMessagesViewHolder.txtReceiverName.setVisibility(View.GONE);
                                groupMessagesViewHolder.txtReceiverMess.setVisibility(View.GONE);
                                groupMessagesViewHolder.imgReceiver.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if (fromUserID.equals(messageSenderId))
                {
                    groupMessagesViewHolder.txtSenderMess.setVisibility(View.VISIBLE);
                    groupMessagesViewHolder.txtSenderMess.setBackgroundResource(R.drawable.sender_messages_layout);
                    if(message.equals("Thu hồi")){
                        groupMessagesViewHolder.txtSenderMess.setTextColor(Color.GRAY);
                        groupMessagesViewHolder.txtSenderMess.setText("Bạn đã thu hồi một tin nhắn");
                    } else{
                        groupMessagesViewHolder.txtSenderMess.setTextColor(Color.BLACK);
                        groupMessagesViewHolder.txtSenderMess.setText(groupMessages.getMessage() + "\n \n" + groupMessages.getTime() + " - " + groupMessages.getDate());
                    }

                } else {
                    groupMessagesViewHolder.txtReceiverName.setVisibility(View.VISIBLE);
                    groupMessagesViewHolder.imgReceiver.setVisibility(View.VISIBLE);
                    groupMessagesViewHolder.txtReceiverMess.setVisibility(View.VISIBLE);

                    groupMessagesViewHolder.txtReceiverMess.setBackgroundResource(R.drawable.receiver_messages_layout);
                    if(message.equals("Thu hồi")){
                        groupMessagesViewHolder.txtReceiverMess.setTextColor(Color.GRAY);
                        groupMessagesViewHolder.txtReceiverMess.setText("Tin nhắn đã bị thu hồi");
                    } else{
                        groupMessagesViewHolder.txtReceiverMess.setTextColor(Color.BLACK);
                        groupMessagesViewHolder.txtReceiverMess.setText(groupMessages.getMessage() + "\n \n" + groupMessages.getTime() + " - " + groupMessages.getDate());
                    }
                    groupRef.child("members").child(fromUserID).addValueEventListener(new ValueEventListener() {
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

        if(fromUserID.equals(messageSenderId)){
            groupMessagesViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(groupMessages.getType().equals("text")){
                        CharSequence options [] = new CharSequence[]
                                {
                                        "Thu hồi",
                                        "Gỡ ở phía bạn",
                                        "Hủy"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupMessagesViewHolder.itemView.getContext(), R.style.AlertDialog);
                        builder.setTitle("Bạn muốn gỡ tin nhắn này ở phía ai?");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if(position == 0){
                                    deleteMessage(i, groupMessagesViewHolder);
                                } else if (position == 1) {
                                    deleteMessageForMe(i, groupMessagesViewHolder);
                                }else {
                                    dialog.cancel();
                                }
                            }
                        });

                        builder.show();
                    }
                    return true;
                }
            });
        } else {
            groupMessagesViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(groupMessages.getType().equals("text")){
                        CharSequence options [] = new CharSequence[]
                                {
                                        "Gỡ ở phía bạn",
                                        "Hủy"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(groupMessagesViewHolder.itemView.getContext(), R.style.AlertDialog);

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {
                                if(position == 0){
                                    deleteMessageForMe(i, groupMessagesViewHolder);
                                } else {
                                    dialog.cancel();
                                }
                            }
                        });

                        builder.show();
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    private void deleteMessage(int position, GroupMessagesViewHolder holder){
        groupRef.child("messages").child(groupMessagesList.get(position).getMessageID()).child("message").setValue("Thu hồi").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    groupMessagesList.get(position).setMessage("Thu hồi");
                    notifyItemChanged(position);
                }
            }
        });
    }

    private void deleteMessageForMe(int position, GroupMessagesViewHolder holder){
        groupRef.child("messages").child(groupMessagesList.get(position).getMessageID()).child("revoke").setValue(mAuth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    notifyItemChanged(position);
                }
            }
        });
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
