package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentTransaction;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialElevationScale;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.Locale;

public class EditContentActivity extends AppCompatActivity implements PassCheckFragment.Callback {
    private static final long TIME_IN_MILLIS = 10000;
    private TextInputEditText tv_title, tv_id, tv_pw, tv_memo;
    private MaterialButton button_options, button_decrypt;
    private ProgressBar progressBar;
    private String label, docId;
    private long mLastClickTime = 0;
    private long timeLeftInMillis = TIME_IN_MILLIS;
    private long endTime;
    private boolean timerRunning, favorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setEnterTransition(new MaterialElevationScale(true).setDuration(150));
        getWindow().setReturnTransition(new MaterialElevationScale(false).setDuration(150));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_content);

        MaterialToolbar mToolbar = findViewById(R.id.content_edit_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> onBackPressed());

        tv_title = findViewById(R.id.tv_title);
        tv_id = findViewById(R.id.tv_id);
        tv_pw = findViewById(R.id.tv_pw);
        tv_memo = findViewById(R.id.tv_memo);
        progressBar = findViewById(R.id.edit_progressBar);

        tv_title.setKeyListener(null);
        tv_id.setKeyListener(null);
        tv_pw.setKeyListener(null);
        tv_memo.setKeyListener(null);

        tv_title.setText(getIntent().getStringExtra("title"));
        tv_id.setText(getIntent().getStringExtra("id"));
        tv_pw.setText(getIntent().getStringExtra("pw"));
        tv_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");
        favorite = getIntent().getBooleanExtra("favorite", false);

        TextInputLayout layout_id = findViewById(R.id.content_layout_id);
        layout_id.setEndIconOnClickListener(v -> {
            Utils.copyToClipboard(getApplicationContext(), tv_id.getText().toString());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Utils.showSnack(findViewById(R.id.edit_screen), "클립보드에 복사되었습니다");
        });

        TextInputLayout layout_memo = findViewById(R.id.content_layout_memo);
        layout_memo.setEndIconOnClickListener(v -> {
            Utils.copyToClipboard(getApplicationContext(), tv_memo.getText().toString());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Utils.showSnack(findViewById(R.id.edit_screen), "클립보드에 복사되었습니다");
        });

        MaterialButton button_edit = findViewById(R.id.button_edit);
        button_edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContentActivity.class);
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
            intent.putExtra("title", tv_title.getText().toString());
            intent.putExtra("id", tv_id.getText().toString());
            intent.putExtra("memo", tv_memo.getText().toString());
            intent.putExtra("label", label);
            intent.putExtra("favorite", favorite);
            startActivity(intent, bundle);
        });

        button_options = findViewById(R.id.button_options);
        button_options.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return;
            mLastClickTime = SystemClock.elapsedRealtime();
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.content_bottom_sheet, findViewById(R.id.cbs_container));
            ImageView iv_favorite = bottomSheetView.findViewById(R.id.iv_favorite);
            TextView tv_favorite = bottomSheetView.findViewById(R.id.tv_favorite);

            DocumentReference docRef = Utils.getContentReference().document(label);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        favorite = document.getBoolean("favorite");
                        if (favorite) {
                            iv_favorite.setImageResource(R.drawable.ic_star_filled);
                            tv_favorite.setText("즐겨찾기에서 삭제");
                        } else {
                            iv_favorite.setImageResource(R.drawable.ic_star_not_filled);
                            tv_favorite.setText("즐겨찾기에 추가");
                        }
                    }
                }
            });

            bottomSheetView.findViewById(R.id.option_favorite).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
                if (favorite) {
                    deleteFavorite(docRef, label);
                } else {
                    addFavorite(docRef, label);
                }
            });

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
            if (tv_pw.getText().length() != 0) {
                if (getPassCode().length() != 0) {
                    PassCheckFragment passCheckFragment = new PassCheckFragment();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.add(android.R.id.content, passCheckFragment).addToBackStack(null).commit();
                } else {
                    Snackbar sb = Snackbar.make(findViewById(R.id.edit_screen), "먼저 비밀번호를 설정하세요", Snackbar.LENGTH_SHORT);
                    sb.setAction("설정", v12 -> {
                        Utils.showToast(this, "클릭");
                    });
                    sb.show();
                }
            } else {
                Utils.showSnack(findViewById(R.id.edit_screen), "비밀번호가 비어 있습니다");
            }
        });
    }

    @Override
    public void getCallback(boolean value) {
        if (value) {
            tv_pw.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            tv_pw.setText(Utils.decodeBase64(tv_pw.getText().toString()));
            startTimer();
        } else {
            resetTimer();
        }
    }

    private void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftInMillis;
        new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                timerRunning = true;
                button_decrypt.setEnabled(false);
                button_decrypt.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_timer));
                updateCountDown();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = TIME_IN_MILLIS;
                timerRunning = false;
                tv_pw.setTransformationMethod(PasswordTransformationMethod.getInstance());
                tv_pw.setText(Utils.encodeBase64(tv_pw.getText().toString()));
                button_decrypt.setEnabled(true);
                button_decrypt.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_unlock));
                button_decrypt.setText("비밀번호 복호화");
            }
        }.start();
    }

    private void resetTimer() {
        timeLeftInMillis = TIME_IN_MILLIS;
        updateCountDown();
    }

    private void updateCountDown() {
        int seconds = (int) (timeLeftInMillis / 1000) % 60 + 1;
        String timeLeft = String.format(Locale.getDefault(), "%2d", seconds);
        button_decrypt.setText(timeLeft);
    }

    private void onChangeInProgress(boolean inProgress) {
        button_options.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_dot_horizon_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void deleteFavorite(DocumentReference docRef, String label) {
        docRef.get().addOnSuccessListener(documentSnapshot ->
                Utils.getContentReference().document(label)
                        .update("favorite", false)
                        .addOnSuccessListener(unused -> {
                            Utils.showSnack(findViewById(R.id.edit_screen), "즐겨찾기에서 삭제되었습니다");
                        }));
    }

    private void addFavorite(DocumentReference docRef, String label) {
        docRef.get().addOnSuccessListener(documentSnapshot ->
                Utils.getContentReference().document(label)
                        .update("favorite", true)
                        .addOnSuccessListener(unused -> {
                            Utils.showSnack(findViewById(R.id.edit_screen), "즐겨찾기에 추가되었습니다");
                        }));
    }

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        button_options.setEnabled(false);
        onChangeInProgress(true);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> fromPath.delete()
                                    .addOnSuccessListener(unused1 -> finish())
                                    .addOnFailureListener(e -> {
                                        Utils.showSnack(findViewById(R.id.edit_screen), "오류, 다시 시도하세요");
                                        onChangeInProgress(false);
                                    }));
                }
            }
        });
    }

    private String getPassCode() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("MILLIS_LEFT", timeLeftInMillis);
        outState.putLong("END_TIME", endTime);
        outState.putBoolean("TIMER_STATE", timerRunning);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        timeLeftInMillis = savedInstanceState.getLong("MILLIS_LEFT");
        timerRunning = savedInstanceState.getBoolean("TIMER_STATE");

        if (timerRunning) {
            endTime = savedInstanceState.getLong("END_TIME");
            timeLeftInMillis = endTime - System.currentTimeMillis();
            startTimer();
        }
    }
}