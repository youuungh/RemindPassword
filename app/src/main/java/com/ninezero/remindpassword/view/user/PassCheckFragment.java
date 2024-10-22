package com.ninezero.remindpassword.view.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ninezero.remindpassword.R;
import com.ninezero.remindpassword.util.Utils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.platform.MaterialSharedAxis;

import java.util.ArrayList;
import java.util.List;

public class PassCheckFragment extends Fragment implements View.OnClickListener {
    private Callback callback;
    private View pin_01, pin_02, pin_03, pin_04;
    private TextView tv_error;
    private final Button[] buttons = new Button[10];
    private final Integer[] buttons_id = {R.id.chk_btn_01, R.id.chk_btn_02, R.id.chk_btn_03, R.id.chk_btn_04, R.id.chk_btn_05,
            R.id.chk_btn_06, R.id.chk_btn_07, R.id.chk_btn_08, R.id.chk_btn_09, R.id.chk_btn_00};
    private final List<String> num_list = new ArrayList<>();
    private String confirmCode = "";
    private String num01, num02, num03, num04;
    private boolean checkData;
    private boolean rePassCode;

    public interface Callback {
        void getCallback(boolean value);
    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            getParentFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        initCallback(context);
    }

    private void initCallback(Context context) {
        if (context instanceof Callback) {
            callback = (Callback) context;
        } else {
            requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setSharedAxisTransitions();
    }

    private void setSharedAxisTransitions() {
        setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
        setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
        setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            checkData = bundle.getBoolean("SET_PASSWORD", false);
            rePassCode = bundle.getBoolean("NEW_PASSWORD", false);
        }
        View view = inflater.inflate(R.layout.fragment_pass_check, container, false);

        init(view);
        setupToolbar(view);
        setupTextViews(view);
        setupButtons(view);

        return view;
    }

    private void init(View view) {
        pin_01 = view.findViewById(R.id.chk_pin_01);
        pin_02 = view.findViewById(R.id.chk_pin_02);
        pin_03 = view.findViewById(R.id.chk_pin_03);
        pin_04 = view.findViewById(R.id.chk_pin_04);
        tv_error = view.findViewById(R.id.pin_check_errorMsg);
    }

    private void setupToolbar(View view) {
        MaterialToolbar mToolbar = view.findViewById(R.id.pin_check_toolbar);
        mToolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStackImmediate());
    }

    private void setupTextViews(View view) {
        TextView tv_title = view.findViewById(R.id.tv_title);
        TextView tv_subtitle = view.findViewById(R.id.tv_subtitle);
        if (checkData) {
            tv_title.setText("현재 비밀번호 확인");
            tv_subtitle.setText("사용하고 계신 비밀번호 4자리를 입력하세요");
        } else {
            tv_title.setText("비밀번호 확인");
            tv_subtitle.setText("비밀번호 4자리를 다시 한번 입력하세요");
        }
    }

    private void setupButtons(View view) {
        for (int i = 0; i < 10; i++) {
            buttons[i] = view.findViewById(buttons_id[i]);
            buttons[i].setOnClickListener(this);
        }
        ImageButton btn_clear = view.findViewById(R.id.chk_btn_clear);
        btn_clear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chk_btn_01:
                num_list.add("1");
                passNum(num_list);
                break;
            case R.id.chk_btn_02:
                num_list.add("2");
                passNum(num_list);
                break;
            case R.id.chk_btn_03:
                num_list.add("3");
                passNum(num_list);
                break;
            case R.id.chk_btn_04:
                num_list.add("4");
                passNum(num_list);
                break;
            case R.id.chk_btn_05:
                num_list.add("5");
                passNum(num_list);
                break;
            case R.id.chk_btn_06:
                num_list.add("6");
                passNum(num_list);
                break;
            case R.id.chk_btn_07:
                num_list.add("7");
                passNum(num_list);
                break;
            case R.id.chk_btn_08:
                num_list.add("8");
                passNum(num_list);
                break;
            case R.id.chk_btn_09:
                num_list.add("9");
                passNum(num_list);
                break;
            case R.id.chk_btn_00:
                num_list.add("0");
                passNum(num_list);
                break;
            case R.id.chk_btn_clear:
                if (!num_list.isEmpty()) {
                    num_list.remove(num_list.size()-1);
                    clearNum(num_list);
                }
                break;
        }
    }

    private void clearNum(List<String> numList) {
        int size = numList.size();
        if (size > 0) {
            switch (size) {
                case 1:
                    pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
                    break;
                case 2:
                    pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
                    break;
                case 3:
                    pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
                    break;
            }
        }
    }

    private void passNum(List<String> numList) {
        int size = numList.size();
        if (size == 0) {
            resetPinBackground();
        } else if (size <= 4) {
            switch (size) {
                case 1:
                    num01 = numList.get(0);
                    pin_01.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 2:
                    num02 = numList.get(1);
                    pin_02.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 3:
                    num03 = numList.get(2);
                    pin_03.setBackgroundResource(R.drawable.bg_color_oval);
                    break;
                case 4:
                    num04 = numList.get(3);
                    pin_04.setBackgroundResource(R.drawable.bg_color_oval);
                    confirmCode = num01 + num02 + num03 + num04;

                    if (rePassCode) {
                        reRegisterPassCode();
                    } else {
                        registerPassCode();
                    }
                    break;
            }
        }
    }

    private void resetPinBackground() {
        pin_01.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_02.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_03.setBackgroundResource(R.drawable.bg_grey_oval);
        pin_04.setBackgroundResource(R.drawable.bg_grey_oval);
    }

    private void reRegisterPassCode() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String passCode = bundle.getString("NEW_PASSCODE");
                if (passCode.equals(confirmCode)) {
                    Utils.savePassCode(requireContext(), confirmCode);
                    navigateToFingerPassFragment(true);
                } else {
                    misMatchPassCode();
                }
            }
        }, 100);
    }

    private void registerPassCode() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Bundle bundle = getArguments();
            if (getPassCode().length() == 0 && bundle != null) {
                String passCode = bundle.getString("PASSCODE");
                if (passCode.equals(confirmCode)) {
                    Utils.savePassCode(requireContext(), confirmCode);
                    navigateToFingerPassFragment(false);
                } else {
                    misMatchPassCode();
                }
            } else {
                matchPassCode();
            }
        }, 100);
    }

    private void matchPassCode() {
        if (getPassCode().equals(confirmCode)) {
            if (checkData) {
                navigateToNewPassCodeFragment();
            } else {
                callback.getCallback(true);
                getParentFragmentManager().popBackStack();
            }
        } else {
            misMatchPassCode();
        }
    }

    private void misMatchPassCode() {
        tv_error.setVisibility(View.VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> tv_error.setVisibility(View.GONE), 600);
        clearPassCode();
    }

    private void clearPassCode() {
        resetPinBackground();
        num_list.clear();
        confirmCode = "";
    }

    private String getPassCode() {
        SharedPreferences pref = requireContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }

    private void navigateToFingerPassFragment(boolean isNewFingerprint) {
        FingerPassFragment fingerPassFragment = new FingerPassFragment();
        Bundle bundle_finger = new Bundle();
        bundle_finger.putBoolean("NEW_FINGERPRINT", isNewFingerprint);
        fingerPassFragment.setArguments(bundle_finger);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_passCheck, fingerPassFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToNewPassCodeFragment() {
        setEnterTransition(null);
        setReturnTransition(null);
        PassCodeFragment passCodeFragment = new PassCodeFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("NEW_PASSWORD", true);
        passCodeFragment.setArguments(bundle);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.layout_passCheck, passCodeFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}