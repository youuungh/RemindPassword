package com.example.passwordmanager;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.platform.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class PassCodeFragment extends Fragment implements View.OnClickListener {
    MaterialToolbar mToolbar;
    View pin_01, pin_02, pin_03, pin_04;
    Button[] buttons = new Button[10];
    Integer[] buttons_id = {R.id.btn_01, R.id.btn_02, R.id.btn_03, R.id.btn_04, R.id.btn_05,
            R.id.btn_06, R.id.btn_07, R.id.btn_08, R.id.btn_09, R.id.btn_00};
    ImageButton btn_clear;
    List<String> num_list = new ArrayList<>();
    String passCode = "";
    String num01, num02, num03, num04;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            getParentFragmentManager().popBackStack();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, true).setDuration(350));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Y, false).setDuration(350));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pass_code, container, false);

        mToolbar = view.findViewById(R.id.pin_toolbar);
        mToolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        pin_01 = view.findViewById(R.id.pin_01);
        pin_02 = view.findViewById(R.id.pin_02);
        pin_03 = view.findViewById(R.id.pin_03);
        pin_04 = view.findViewById(R.id.pin_04);

        for (int i=0; i<10; i++) {
            buttons[i] = view.findViewById(buttons_id[i]);
            buttons[i].setOnClickListener(this);
        }
        btn_clear = view.findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Log.d("num_list.size():", ""+num_list.size());
        switch (v.getId()) {
            case R.id.btn_01:
                num_list.add("1");
                passNum(num_list);
                break;
            case R.id.btn_02:
                num_list.add("2");
                passNum(num_list);
                break;
            case R.id.btn_03:
                num_list.add("3");
                passNum(num_list);
                break;
            case R.id.btn_04:
                num_list.add("4");
                passNum(num_list);
                break;
            case R.id.btn_05:
                num_list.add("5");
                passNum(num_list);
                break;
            case R.id.btn_06:
                num_list.add("6");
                passNum(num_list);
                break;
            case R.id.btn_07:
                num_list.add("7");
                passNum(num_list);
                break;
            case R.id.btn_08:
                num_list.add("8");
                passNum(num_list);
                break;
            case R.id.btn_09:
                num_list.add("9");
                passNum(num_list);
                break;
            case R.id.btn_00:
                num_list.add("0");
                passNum(num_list);
                break;
            case R.id.btn_clear:
                if (!num_list.isEmpty()) {
                    num_list.remove(num_list.size()-1);
                    clearNum(num_list);
                }
                break;
        }
    }

    private void clearNum(List<String> num_list) {
        switch (num_list.size()) {
            case 2:
                pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
                break;
            case 1:
                pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
                break;
            case 0:
                pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
                break;
        }
    }

    private void passNum(List<String> num_list) {
        if (num_list.size() == 0) {
            pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
            pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
            pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
            pin_04.setBackgroundResource(R.drawable.bg_grey_oval);
        } else {
            switch (num_list.size()) {
                case 1:
                    num01 = num_list.get(0);
                    pin_01.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 2:
                    num02 = num_list.get(1);
                    pin_02.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 3:
                    num03 = num_list.get(2);
                    pin_03.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 4:
                    num04 = num_list.get(3);
                    pin_04.setBackgroundResource(R.drawable.bg_color_oval);
                    passCode = num01 + num02 + num03 + num04;
                    Log.d("passcode:", ""+passCode);

                    PassCheckFragment passCheckFragment = new PassCheckFragment();
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    bundle.putString("PASSCODE", passCode);
                    passCheckFragment.setArguments(bundle);
                    ft.add(android.R.id.content, passCheckFragment).addToBackStack(null).commit();
                    refresh();
                    break;
            }
        }
    }

    private void refresh() {
        pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_04.setBackgroundResource(R.drawable.bg_grey_oval);

        num_list.clear();
        passCode = "";
    }
}