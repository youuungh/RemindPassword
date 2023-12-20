package com.example.passwordmanager.view.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.passwordmanager.R;
import com.example.passwordmanager.util.Utils;
import com.example.passwordmanager.model.Content;
import com.example.passwordmanager.view.common.MainActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.transition.platform.MaterialFadeThrough;
import com.google.android.material.transition.platform.SlideDistanceProvider;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class AddContentActivity extends AppCompatActivity {
    private TextInputEditText edt_title, edt_id, edt_pw, edt_memo;
    private MaterialButton button_save;
    private ProgressBar progressBar;
    private CheckBox checkBoxSaveData;
    private String label, stored_pw;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        configureEnterExitTransitions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        initUI();
        setupToolbar();
        setupSaveButton();
    }

    private void configureEnterExitTransitions() {
        MaterialFadeThrough enter = new MaterialFadeThrough();
        enter.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.BOTTOM));
        getWindow().setEnterTransition(enter);

        MaterialFadeThrough exit = new MaterialFadeThrough();
        exit.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
        getWindow().setReturnTransition(exit);
    }

    private void setupToolbar() {
        MaterialToolbar mToolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> onBackPressed());
    }

    private void initUI() {
        LinearLayout layout_save_data = findViewById(R.id.layout_save_data);
        edt_title = findViewById(R.id.edt_title);
        edt_id = findViewById(R.id.edt_id);
        edt_pw = findViewById(R.id.edt_pw);
        edt_memo = findViewById(R.id.edt_memo);
        button_save = findViewById(R.id.button_save);
        progressBar = findViewById(R.id.add_progressBar);
        checkBoxSaveData = findViewById(R.id.checkbox_save_data);

        edt_title.setText(getIntent().getStringExtra("title"));
        edt_id.setText(getIntent().getStringExtra("id"));
        edt_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");

        if(label != null && !label.isEmpty()) {
            isEdit = true;
            edt_title.requestFocus();
            stored_pw = Utils.decodeBase64(getIntent().getStringExtra("pw"));

            if (!stored_pw.isEmpty()) {
                layout_save_data.setVisibility(View.VISIBLE);
                edt_pw.setEnabled(false);
                edt_pw.setText(getIntent().getStringExtra("pw"));
                checkBoxSaveData.setChecked(true);
            }
            checkBoxSaveData.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isChecked) {
                    edt_pw.setText(null);
                    edt_pw.setEnabled(true);
                } else {
                    edt_pw.setText(getIntent().getStringExtra("pw"));
                    edt_pw.setEnabled(false);
                }
            });
            configureEditToolbar();
        } else {
            edt_title.requestFocus();
        }
    }

    private void configureEditToolbar() {
        MaterialToolbar mToolbar = findViewById(R.id.content_add_toolbar);
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
    }

    private void setupSaveButton() {
        button_save.setOnClickListener(view -> {
            hideSoftKeyboard();
            processSaveButton();
        });
    }

    private void processSaveButton() {
        String title = edt_title.getText().toString();
        String id = edt_id.getText().toString();
        String pw = checkBoxSaveData.isChecked() ? stored_pw : edt_pw.getText().toString();
        String memo = edt_memo.getText().toString();
        Timestamp timestamp = Timestamp.now();
        boolean favorite = getIntent().getBooleanExtra("favorite", false);

        if (title.isEmpty()) {
            Utils.showSnack(findViewById(R.id.add_screen), "제목을 입력하세요");
            return;
        }

        Content content = new Content(title, title.toLowerCase(), id, Utils.encodeBase64(pw), memo, "", timestamp, favorite);

        if (favorite) {
            editFavToFirebase(content);
        } else {
            saveToFirebase(content);
        }
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

    private void onChangeInProgress(boolean inProgress) {
        button_save.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void saveToFirebase(Content content) {
        onChangeInProgress(true);
        DocumentReference docRef;
        if (isEdit) {
            docRef = Utils.getContentReference().document(label);
        } else {
            docRef = Utils.getContentReference().document();
        }
        content.setDocId(docRef.getId());
        docRef.set(content).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                button_save.setEnabled(false);
                navigateToMainActivity();
            } else {
                onChangeInProgress(false);
                button_save.setEnabled(true);
                Utils.showSnack(findViewById(R.id.add_screen), "오류, 다시 시도하세요");
            }
        });
    }

    private void editFavToFirebase(Content content) {
        onChangeInProgress(true);
        DocumentReference docRef = Utils.getFavoriteReference().document(label);
        content.setDocId(docRef.getId());
        docRef.set(content).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                button_save.setEnabled(false);
                navigateToMainActivity();
            } else {
                onChangeInProgress(false);
                button_save.setEnabled(true);
                Utils.showSnack(findViewById(R.id.add_screen), "오류, 다시 시도하세요");
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent, bundle);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideSoftKeyboard();
    }
}