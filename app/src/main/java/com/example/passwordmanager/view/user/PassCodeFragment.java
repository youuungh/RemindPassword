package com.example.passwordmanager.view.user;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.passwordmanager.R;
import com.example.passwordmanager.view.common.MainActivity;
import com.example.passwordmanager.view.user.PassCheckFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.android.material.transition.platform.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class PassCodeFragment extends Fragment implements View.OnClickListener {
    private View pin_01, pin_02, pin_03, pin_04;
    private TextView tv_error;
    private final Button[] buttons = new Button[10];
    private final Integer[] buttons_id = {R.id.btn_01, R.id.btn_02, R.id.btn_03, R.id.btn_04, R.id.btn_05,
            R.id.btn_06, R.id.btn_07, R.id.btn_08, R.id.btn_09, R.id.btn_00};
    private final List<String> num_list = new ArrayList<>();
    private String passCode = "";
    private String num01, num02, num03, num04;
    private boolean rePassCode;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (rePassCode) {
                getParentFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else {
                getParentFragmentManager().popBackStack();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null)
            rePassCode = bundle.getBoolean("NEW_PASSWORD", false);
        View view = inflater.inflate(R.layout.fragment_pass_code, container, false);

        MaterialToolbar mToolbar = view.findViewById(R.id.pin_toolbar);
        mToolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE));

        pin_01 = view.findViewById(R.id.pin_01);
        pin_02 = view.findViewById(R.id.pin_02);
        pin_03 = view.findViewById(R.id.pin_03);
        pin_04 = view.findViewById(R.id.pin_04);

        for (int i=0; i<10; i++) {
            buttons[i] = view.findViewById(buttons_id[i]);
            buttons[i].setOnClickListener(this);
        }
        ImageButton btn_clear = view.findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(this);
        tv_error = view.findViewById(R.id.pin_errorMsg);

        return view;
    }

    @Override
    public void onClick(View v) {
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

                    if (getPassCode().equals(passCode)) {
                        new Handler(Looper.getMainLooper()).postDelayed(this::misMatchPassCode, 100);
                    } else if (rePassCode) {
                        setRePassCode();
                    } else {
                        setPassCode();
                    }
                    break;
            }
        }
    }

    private void transFragments(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .add(R.id.layout_passCode, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void setRePassCode() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PassCheckFragment passCheckFragment = new PassCheckFragment();
            Bundle bundle = new Bundle();
            bundle.putString("NEW_PASSCODE", passCode);
            bundle.putBoolean("NEW_PASSWORD", true);
            passCheckFragment.setArguments(bundle);
            transFragments(passCheckFragment);
            clearPassCode();
        }, 100);
    }

    private void setPassCode() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            PassCheckFragment passCheckFragment = new PassCheckFragment();
            Bundle bundle = new Bundle();
            bundle.putString("PASSCODE", passCode);
            passCheckFragment.setArguments(bundle);
            transFragments(passCheckFragment);
            clearPassCode();
        }, 100);
    }

    private void misMatchPassCode() {
        tv_error.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> tv_error.setVisibility(View.GONE), 600);
        clearPassCode();
    }

    private void clearPassCode() {
        pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_04.setBackgroundResource(R.drawable.bg_grey_oval);
        num_list.clear();
        passCode = "";
    }

    private String getPassCode() {
        SharedPreferences pref = requireContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }

    private void ViewAnimation() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}