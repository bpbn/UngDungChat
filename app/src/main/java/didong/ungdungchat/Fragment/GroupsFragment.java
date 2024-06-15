package didong.ungdungchat.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private DatabaseReference GroupRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public GroupsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        binding = FragmentGroupsBinding.bind(view);

        mAuth = FirebaseAuth.getInstance();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
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
        return view;
    }

    private void intializeFields() {
        groupsAdapter = new GroupsAdapter(getContext(), R.layout.groups_display_layout, listGroup);
        binding.listGroups.setAdapter(groupsAdapter);
        binding.listGroups.setDivider(null);
    }
    private void retrieveAndDisplayGroup() {
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listGroup.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    if (dataSnapshot.child("members").child(currentUserID).exists()) {
                        Groups gr = dataSnapshot.getValue(Groups.class);
                        if (gr.getImage() == null) {
                            gr.setImage(getContext().getPackageName() + R.drawable.baseline_groups_24);
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