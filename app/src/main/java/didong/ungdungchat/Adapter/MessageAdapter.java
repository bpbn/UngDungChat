package didong.ungdungchat.Adapter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import didong.ungdungchat.Model.Messages;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.CustomMessagesLayoutBinding;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, rootRef;


    public MessageAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(CustomMessagesLayoutBinding.bind(view));
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, @SuppressLint("RecyclerView") int i) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String message = messages.getMessage();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.baseline_account_circle_24).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text")) {
            rootRef.child("Messages")
                    .child(messages.getFrom())
                    .child(messages.getTo())
                    .child("chats")
                    .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChild("revoke")) {
                                String revoke = snapshot.child("revoke").getValue().toString();

                                if (revoke.equals(messageSenderId) && revoke.equals(fromUserID)) {
                                    messageViewHolder.senderMessageText.setVisibility(View.GONE);
                                } else if (revoke.equals(messageSenderId) && !revoke.equals(fromUserID)) {
                                    messageViewHolder.receiverMessageText.setVisibility(View.GONE);
                                    messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setPadding(20, 20, 20, 20);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                rootRef.child("Messages")
                        .child(messages.getFrom())
                        .child(messages.getTo())
                        .child("chats")
                        .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("message")) {
                                    String mess = snapshot.child("message").getValue().toString();

                                    if (mess.equals("Thu hồi")) {
                                        messageViewHolder.senderMessageText.setTextColor(Color.GRAY);
                                        messageViewHolder.senderMessageText.setText("Bạn đã thu hồi một tin nhắn");
                                    } else {
                                        messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                                        messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setPadding(20, 20, 20, 20);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                rootRef.child("Messages")
                        .child(messages.getFrom())
                        .child(messages.getTo())
                        .child("chats")
                        .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("message")) {
                                    String mess = snapshot.child("message").getValue().toString();

                                    if (mess.equals("Thu hồi")) {
                                        messageViewHolder.receiverMessageText.setTextColor(Color.GRAY);
                                        messageViewHolder.receiverMessageText.setText("Tin nhắn đã bị thu hồi");
                                    } else {
                                        messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                                        messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        } else if (fromMessageType.equals("image")) {
            rootRef.child("Messages")
                    .child(messages.getFrom())
                    .child(messages.getTo())
                    .child("chats")
                    .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChild("revoke")) {
                                String revoke = snapshot.child("revoke").getValue().toString();

                                if (revoke.equals(messageSenderId) && revoke.equals(fromUserID)) {
                                    messageViewHolder.senderMessageImageLayout.setVisibility(View.GONE);
                                    messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
                                } else if (revoke.equals(messageSenderId) && !revoke.equals(fromUserID)) {
                                    messageViewHolder.receiverMessageImageLayout.setVisibility(View.GONE);
                                    messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);
                                    messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            if (fromUserID.equals(messageSenderId)) {
                rootRef.child("Messages")
                        .child(messages.getFrom())
                        .child(messages.getTo())
                        .child("chats")
                        .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("message")) {
                                    String mess = snapshot.child("message").getValue().toString();

                                    if (mess.equals("Thu hồi")) {
                                        messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                                        messageViewHolder.senderMessageText.setPadding(20, 20, 20, 20);

                                        messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                                        messageViewHolder.senderMessageText.setTextColor(Color.GRAY);
                                        messageViewHolder.senderMessageText.setText("Bạn đã thu hồi một tin nhắn");
                                    } else {
                                        messageViewHolder.senderMessageImageLayout.setVisibility(View.VISIBLE);
                                        messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);

                                        Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                                        messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                                        messageViewHolder.senderMessageTime.setText(messages.getTime() + " - " + messages.getDate());
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                rootRef.child("Messages")
                        .child(messages.getFrom())
                        .child(messages.getTo())
                        .child("chats")
                        .child(messages.getMessageID()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists() && snapshot.hasChild("message")) {
                                    String mess = snapshot.child("message").getValue().toString();

                                    if (mess.equals("Thu hồi")) {
                                        messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                                        messageViewHolder.receiverMessageText.setPadding(20, 20, 20, 20);

                                        messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                                        messageViewHolder.receiverMessageText.setTextColor(Color.GRAY);
                                        messageViewHolder.receiverMessageText.setText("Tin nhắn đã bị thu hồi");
                                    } else {
                                        messageViewHolder.receiverMessageImageLayout.setVisibility(View.VISIBLE);
                                        messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);

                                        Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                                        messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                                        messageViewHolder.receiverMessageTime.setText(messages.getTime() + " - " + messages.getDate());
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext(), R.style.AlertDialog);
                        builder.setTitle("Bạn muốn xem ảnh này?");
                        builder.setPositiveButton("Xem ảnh", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(userMessagesList.get(i).getMessage()));
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
            }
        }
        else {
            if (fromUserID.equals(messageSenderId)) {
                messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageImageLayout.setVisibility(View.VISIBLE);
                messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.baseline_insert_drive_file_24);
                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageTime.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext(), R.style.AlertDialog);
                        builder.setTitle("Bạn muốn tải xuống tệp này?");

                        builder.setMessage(userMessagesList.get(i).getName());
                        builder.setPositiveButton("Tải xuống", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(userMessagesList.get(i).getMessage()));
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageImageLayout.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.baseline_insert_drive_file_24);
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageTime.setText(messages.getTime() + " - " + messages.getDate());
                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext(), R.style.AlertDialog);
                        builder.setTitle("Bạn muốn tải xuống tệp này?");
                        builder.setMessage(userMessagesList.get(i).getName());
                        builder.setPositiveButton("Tải xuống", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(userMessagesList.get(i).getMessage()));
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                });
            }
        }

        if (fromUserID.equals(messageSenderId)) {

            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Thu hồi",
                                    "Gỡ ở phía bạn",
                                    "Hủy"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext(), R.style.AlertDialog);
                    builder.setTitle("Bạn muốn gỡ tin nhắn này ở phía ai?");

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0) {
                                deleteMessage(i, messageViewHolder);
                            } else if (position == 1) {
                                deleteMessageForMe(i, messageViewHolder);
                            } else {
                                dialog.cancel();
                            }
                        }
                    });

                    builder.show();
                    return true;
                }
            });
        } else {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Gỡ ở phía bạn",
                                    "Hủy"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext(), R.style.AlertDialog);

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int position) {
                            if (position == 0) {
                                deleteMessageForMe(i, messageViewHolder);
                            } else {
                                dialog.cancel();
                            }
                        }
                    });

                    builder.show();
                    return true;
                }
            });
        }
    }
    @Override
    public int getItemCount ()
    {
        return userMessagesList.size();
    }

    private void deleteMessage ( int position, MessageViewHolder holder){
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child("chats")
                .child(userMessagesList.get(position).getMessageID())
                .child("message").setValue("Thu hồi").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child("chats")
                                    .child(userMessagesList.get(position).getMessageID())
                                    .child("message").setValue("Thu hồi");
                        }
                        userMessagesList.get(position).setMessage("Thu hồi");
                        notifyItemChanged(position);
                    }
                });
    }

    private void deleteMessageForMe ( int position, MessageViewHolder holder){
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child("chats")
                .child(userMessagesList.get(position).getMessageID())
                .child("revoke").setValue(mAuth.getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child("chats")
                                    .child(userMessagesList.get(position).getMessageID())
                                    .child("revoke").setValue(mAuth.getCurrentUser().getUid());
                        }
                        notifyItemChanged(position);
                    }
                });
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText, senderMessageTime, receiverMessageTime;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;
        public LinearLayout senderMessageImageLayout, receiverMessageImageLayout;


        public MessageViewHolder(@NonNull CustomMessagesLayoutBinding itemView) {
            super(itemView.getRoot());

            senderMessageText = itemView.senderMesssageText;
            receiverMessageText = itemView.receiverMessageText;
            receiverProfileImage = itemView.messageProfileImage;
            messageReceiverPicture = itemView.messageReceiverImageView;
            messageSenderPicture = itemView.messageSenderImageView;
            senderMessageTime = itemView.senderMessageTime;
            receiverMessageTime = itemView.receiverMessageTime;
            senderMessageImageLayout = itemView.senderMessageImageLayout;
            receiverMessageImageLayout = itemView.receiverMessageImageLayout;
        }
    }
}

