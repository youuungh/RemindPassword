package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Interpolator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialElevationScale;
import com.google.android.material.transition.platform.MaterialFade;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.material.transition.platform.SlideDistanceProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Base64;
import java.util.Locale;

public class EditContentActivity extends AppCompatActivity implements PassCheckFragment.Callback {
    private static final long TIME_IN_MILLIS = 10000;
    MaterialToolbar toolbar;
    TextInputLayout tl_id, tl_memo;
    TextInputEditText tv_title, tv_id, tv_pw, tv_memo;
    MaterialButton button_edit, button_options, button_decrypt;
    ProgressBar progressBar;
    String label;
    CountDownTimer countDownTimer;
    private long timeLeftInMillis = TIME_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setEnterTransition(new MaterialElevationScale(true).setDuration(200));
        getWindow().setReturnTransition(new MaterialElevationScale(false).setDuration(300));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_content);
        toolbar = findViewById(R.id.content_edit_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        progressBar = findViewById(R.id.edit_progressBar);
        tl_id = findViewById(R.id.content_layout_id);
        tl_memo = findViewById(R.id.content_layout_memo);
        tv_title = findViewById(R.id.tv_title);
        tv_title.setKeyListener(null);
        tv_id = findViewById(R.id.tv_id);
        tv_id.setKeyListener(null);
        tv_pw = findViewById(R.id.tv_pw);
        tv_pw.setKeyListener(null);
        tv_memo = findViewById(R.id.tv_memo);
        tv_memo.setKeyListener(null);

        tv_title.setText(getIntent().getStringExtra("title"));
        tv_id.setText(getIntent().getStringExtra("id"));
        tv_pw.setText(getIntent().getStringExtra("pw"));
        tv_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");

        tl_id.setEndIconOnClickListener(v -> {
            Utils.copyToClipboard(getApplicationContext(), tv_id.getText().toString());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Utils.showSnack(findViewById(R.id.edit_screen), "클립보드에 복사되었습니다");
        });

        tl_memo.setEndIconOnClickListener(v -> {
            Utils.copyToClipboard(getApplicationContext(), tv_memo.getText().toString());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Utils.showSnack(findViewById(R.id.edit_screen), "클립보드에 복사되었습니다");
        });

        button_edit = findViewById(R.id.button_edit);
        button_edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            intent.putExtra("title", tv_title.getText().toString());
            intent.putExtra("id", tv_id.getText().toString());
            intent.putExtra("pw", tv_pw.getText().toString());
            intent.putExtra("memo", tv_memo.getText().toString());
            intent.putExtra("label", label);
            startActivity(intent, bundle);
        });

        button_options = findViewById(R.id.button_options);
        button_options.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.content_bottom_sheet, findViewById(R.id.cbs_container));
            bottomSheetView.findViewById(R.id.option_trash).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                DocumentReference fromPath = Utils.getContentReference().document(label);
                DocumentReference toPath = Utils.getTrashReference().document(label);
                moveFirebaseDocument(fromPath, toPath);
            });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        });

        button_decrypt = findViewById(R.id.button_decrypt);
        button_decrypt.setOnClickListener(v -> {
            if (getPassCode().length() != 0) {
                PassCheckFragment passCheckFragment = new PassCheckFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(android.R.id.content, passCheckFragment).addToBackStack(null).commit();
            } else {
                Utils.showSnack(findViewById(R.id.edit_screen), "먼저 비밀번호를 설정하세요");
            }
        });
    }

    @Override
    public void getCallback(boolean value) {
        if (value) {
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    tv_pw.setText(Utils.decodeBase64(tv_pw.getText().toString()));
                    button_decrypt.setEnabled(false);
                    button_decrypt.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_timer));
                    updateCountDown();
                }
                @Override
                public void onFinish() {
                    timeLeftInMillis = TIME_IN_MILLIS;
                    tv_pw.setText(Utils.encodeBase64(tv_pw.getText().toString()));
                    button_decrypt.setEnabled(true);
                    button_decrypt.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_unlock));
                    button_decrypt.setText("비밀번호 복호화");
                }
            }.start();
        }
    }

    private void updateCountDown() {
        int seconds = (int) (timeLeftInMillis / 1000) % 60 + 1;
        String timeLeft = String.format(Locale.getDefault(), "%2d", seconds);
        button_decrypt.setText(timeLeft);
    }

    private void editChangeInProgress(boolean inProgress) {
        button_options.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_dot_horizon_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        button_options.setEnabled(false);
        editChangeInProgress(true);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> fromPath.delete()
                                    .addOnSuccessListener(unused1 -> finish())
                                    .addOnFailureListener(e -> {
                                        Utils.showSnack(findViewById(R.id.edit_screen), "오류, 다시 시도하세요");
                                        editChangeInProgress(false);
                                    }));
                }
            }
        });
    }

    private String getPassCode() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }
}