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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import didong.ungdungchat.Activity.GroupChatActivity;
import didong.ungdungchat.R;
import didong.ungdungchat.databinding.ActivityMainBinding;
import didong.ungdungchat.databinding.FragmentGroupsBinding;
public class GroupsFragment extends Fragment {
    FragmentGroupsBinding binding;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listGroup = new ArrayList<>();
    private DatabaseReference GroupRef;
    public GroupsFragment() {
        // Required empty public constructor

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        binding = FragmentGroupsBinding.bind(view);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        IntializeFields();
        RetrieveAndDisplayGroup();

        binding.listGroups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });
        return view;
    }

    private void IntializeFields() {
        arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, listGroup);
        binding.listGroups.setAdapter(arrayAdapter);
    }
    private void RetrieveAndDisplayGroup() {
        GroupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }
                listGroup.clear();
                listGroup.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}