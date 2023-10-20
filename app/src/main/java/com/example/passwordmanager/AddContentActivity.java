package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.example.passwordmanager.model.Content;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.platform.MaterialArcMotion;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.time.Duration;

public class AddContentActivity extends AppCompatActivity {
    DocumentReference docRef;
    MaterialToolbar toolbar;
    TextInputEditText edt_title, edt_id, edt_pw, edt_memo;
    MaterialButton button_save;
    ProgressBar progressBar;
    String label;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        findViewById(android.R.id.content).setTransitionName("fab");
        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setDuration(500);
        getWindow().setSharedElementEnterTransition(transform);
        getWindow().setSharedElementReturnTransition(transform);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        toolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(null);
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

        if(label != null && !label.isEmpty()) { isEdit = true; }

        if (isEdit) {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
            edt_title.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        button_save = findViewById(R.id.button_save);
        button_save.setOnClickListener(view -> {
            InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View focusedView = getCurrentFocus();
            if (focusedView != null)
                manager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            clearFocus();

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
            Content content = new Content(title, search, id, pw, memo, timestamp);
            saveToFirebase(content);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        edt_title.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && isEdit) {
            overridePendingTransition(0, R.anim.anim_fade_out);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isEdit)
            overridePendingTransition(0, R.anim.anim_fade_out);
    }

    private void clearFocus() {
        edt_title.clearFocus();
        edt_id.clearFocus();
        edt_pw.clearFocus();
        edt_memo.clearFocus();
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
}