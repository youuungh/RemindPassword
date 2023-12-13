package com.example.passwordmanager.view.navigation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    private String label, docId;
    private boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);
        MaterialFadeThrough enter = new MaterialFadeThrough();
        enter.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.BOTTOM));
        getWindow().setEnterTransition(enter);
        MaterialFadeThrough exit = new MaterialFadeThrough();
        exit.setSecondaryAnimatorProvider(new SlideDistanceProvider(Gravity.TOP));
        getWindow().setReturnTransition(exit);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        MaterialToolbar mToolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> onBackPressed());

        TextInputLayout layout_pw = findViewById(R.id.content_layout_pw);
        edt_title = findViewById(R.id.edt_title);
        edt_id = findViewById(R.id.edt_id);
        edt_pw = findViewById(R.id.edt_pw);
        edt_memo = findViewById(R.id.edt_memo);
        progressBar = findViewById(R.id.add_progressBar);

        edt_title.setText(getIntent().getStringExtra("title"));
        edt_id.setText(getIntent().getStringExtra("id"));
        edt_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");

        if(label != null && !label.isEmpty()) {
            isEdit = true;
            edt_title.requestFocus();
            layout_pw.setHint("새로운 비밀번호 설정");
            mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
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
            boolean favorite = getIntent().getBooleanExtra("favorite", false);

            if (title.isEmpty()) {
                Utils.showSnack(findViewById(R.id.add_screen), "제목을 입력하세요");
                return;
            }

            Content content = new Content(title, search, id, Utils.encodeBase64(pw), memo, docId, timestamp, favorite);

            if (favorite) {
                editFavToFirebase(content);
            } else {
                saveToFirebase(content);
            }
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

    private void onChangeInProgress(boolean inProgress) {
        button_save.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void saveToFirebase(Content contents) {
        onChangeInProgress(true);
        DocumentReference docRef;
        if(isEdit) {
            docRef = Utils.getContentReference().document(label);
        } else {
            docRef = Utils.getContentReference().document();
        }
        contents.setDocId(docRef.getId());
        docRef.set(contents).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                button_save.setEnabled(false);
                Intent intent = new Intent(this, MainActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent, bundle);
                finish();
            } else {
                onChangeInProgress(false);
                button_save.setEnabled(true);
                Utils.showSnack(findViewById(R.id.add_screen), "오류, 다시 시도하세요");
            }
        });
    }

    private void editFavToFirebase(Content contents) {
        onChangeInProgress(true);
        DocumentReference docRef = Utils.getFavoriteReference().document(label);
        contents.setDocId(docRef.getId());
        docRef.set(contents).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                button_save.setEnabled(false);
                Intent intent = new Intent(this, MainActivity.class);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(this).toBundle();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent, bundle);
            } else {
                onChangeInProgress(false);
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