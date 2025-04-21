package com.example.tutorapp2.Register;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RegisterPagerAdapter extends FragmentStateAdapter {

    public RegisterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new RegisterStudentFragment();
        } else {
            return new RegisterParentFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
