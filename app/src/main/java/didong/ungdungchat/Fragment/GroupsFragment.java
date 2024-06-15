package didong.ungdungchat.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import didong.ungdungchat.Activity.GroupChatActivity;
import didong.ungdungchat.Adapter.GroupsAdapter;
import didong.ungdungchat.Model.Groups;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityMainBinding;
import didong.ungdungchat.databinding.FragmentGroupsBinding;
public class GroupsFragment extends Fragment {
    FragmentGroupsBinding binding;
    private GroupsAdapter groupsAdapter;
    private List<Groups> listGroup = new ArrayList<>();
    private DatabaseReference groupRef, rootRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public GroupsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        binding = FragmentGroupsBinding.bind(view);

        mAuth = FirebaseAuth.getInstance();
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        rootRef = FirebaseDatabase.getInstance().getReference();
        intializeFields();
        retrieveAndDisplayGroup();

        binding.listGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Groups groups = (Groups) parent.getItemAtPosition(position);
                String currentGroupName = groups.getName();
                String currentGroupID = groups.getGroupID();
                String currentGroupImage = groups.getImage();

                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                groupChatIntent.putExtra("groupID", currentGroupID);
                groupChatIntent.putExtra("groupImage", currentGroupImage);
                startActivity(groupChatIntent);
            }
        });

        binding.listGroups.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Groups groups = (Groups) parent.getItemAtPosition(position);
                String currentGroupName = groups.getName();
                String currentGroupID = groups.getGroupID();
                CharSequence options [] = new CharSequence[]
                        {
                                "Xóa nhóm",
                                "Hủy"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                builder.setTitle("Bạn có muốn xóa nhóm?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        if(position == 0){
                            rootRef.child("Groups").child(currentGroupID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getContext(), "Đã xóa nhóm " + currentGroupName, Toast.LENGTH_SHORT ).show();
                                    }
                                }
                            });
                        } else {
                            dialog.cancel();
                        }
                    }
                });
                builder.show();
                return true;
            }
        });
        return view;
    }

    private void intializeFields() {
        groupsAdapter = new GroupsAdapter(getContext(), R.layout.groups_display_layout, listGroup);
        binding.listGroups.setAdapter(groupsAdapter);
        binding.listGroups.setDivider(null);
    }
    private void retrieveAndDisplayGroup() {
        currentUserID = mAuth.getCurrentUser().getUid();
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listGroup.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (dataSnapshot.child("members").child(currentUserID).exists()) {
                        Groups gr = dataSnapshot.getValue(Groups.class);
                        if (gr.getImage() == null) {
                            gr.setImage("default_group");
                        }
                        listGroup.add(gr);
                    }
                }
                groupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}