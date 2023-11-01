package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.transition.ArcMotion;
import android.transition.Explode;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.example.passwordmanager.model.Content;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.Hold;
import com.google.android.material.transition.platform.MaterialArcMotion;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.android.material.transition.platform.MaterialElevationScale;
import com.google.android.material.transition.platform.MaterialFade;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import com.google.android.material.transition.platform.SlideDistanceProvider;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.Duration;

public class AddContentActivity extends AppCompatActivity {
    DocumentReference docRef;
    MaterialToolbar toolbar;
    TextInputEditText edt_title, edt_id, edt_pw, edt_memo;
    MaterialButton button_save;
    ProgressBar progressBar;
    String label, docId;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        com.google.android.material.transition.platform.MaterialFadeThrough enter = new MaterialFadeThrough();
        enter.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.BOTTOM));
        enter.setDuration(500);
        getWindow().setEnterTransition(enter);
        com.google.android.material.transition.platform.MaterialFadeThrough exit = new MaterialFadeThrough();
        exit.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
        exit.setDuration(650);
        getWindow().setReturnTransition(exit);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        toolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        progressBar = findViewById(R.id.add_progressBar);
        edt_title = findViewById(R.id.edt_title);
        edt_id = findViewById(R.id.edt_id);
        edt_pw = findViewById(R.id.edt_pw);
        edt_memo = findViewById(R.id.edt_memo);

        edt_title.setText(getIntent().getStringExtra("title"));
        edt_id.setText(getIntent().getStringExtra("id"));
        edt_pw.setText(getIntent().getStringExtra("pw"));
        edt_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");

        if(label != null && !label.isEmpty()) {
            isEdit = true;
            edt_title.requestFocus();
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
        } else {
            edt_title.requestFocus();
        }

        button_save = findViewById(R.id.button_save);
        button_save.setOnClickListener(view -> {
            hideSoftKeyboard();
            String title = edt_title.getText().toString();
            String search = title.toLowerCase();
            String id = edt_id.getText().toString();
            String pw = edt_pw.getText().toString();
            String memo = edt_memo.getText().toString();
            Timestamp timestamp = Timestamp.now();

            if (title.isEmpty()) {
                Utils.showSnack(findViewById(R.id.add_screen), "제목을 입력하세요");
                return;
            }
            Content content = new Content(title, search, id, pw, memo, docId, timestamp);
            saveToFirebase(content);
        });
    }

    private void clearFocus() {
        edt_title.clearFocus();
        edt_id.clearFocus();
        edt_pw.clearFocus();
        edt_memo.clearFocus();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView != null)
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        clearFocus();
    }

    private void addChangeInProgress(boolean inProgress) {
        button_save.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void saveToFirebase(Content content) {
        addChangeInProgress(true);
        if(isEdit) {
            docRef = Utils.getContentReference().document(label);
        } else {
            docRef = Utils.getContentReference().document();
        }
        docRef.set(content).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                button_save.setEnabled(false);
                if (isEdit) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                finish();
            } else {
                addChangeInProgress(false);
                button_save.setEnabled(true);
                Utils.showSnack(findViewById(R.id.add_screen), "오류, 다시 시도하세요");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideSoftKeyboard();
    }
}