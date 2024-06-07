package didong.ungdungchat.Adapter;

import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import didong.ungdungchat.Fragment.ChatsFragment;
import didong.ungdungchat.Fragment.ContactsFragment;
import didong.ungdungchat.Fragment.GroupsFragment;
import didong.ungdungchat.Fragment.RequestsFragment;

public class TabsAccessorAdapter extends FragmentStateAdapter {

    public TabsAccessorAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new ChatsFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new ContactsFragment();
            case 3:
                return new RequestsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
