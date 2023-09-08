package com.example.passwordmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.passwordmanager.model.Content;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class AddContentActivity extends AppCompatActivity {
    DocumentReference documentReference;
    MaterialToolbar toolbar;
    TextInputEditText edt_title;
    EditText edt_id, edt_pw, edt_memo;
    MaterialButton button_save;
    ProgressBar progressBar;
    String title, id, pw, memo, label;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        toolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(null);

        edt_title = findViewById(R.id.edt_title);
        edt_id = findViewById(R.id.edt_id);
        edt_pw = findViewById(R.id.edt_pw);
        edt_memo = findViewById(R.id.edt_memo);

        // 데이터 받아오기
        title = getIntent().getStringExtra("title");
        id = getIntent().getStringExtra("id");
        pw = getIntent().getStringExtra("pw");
        memo = getIntent().getStringExtra("memo");
        label = getIntent().getStringExtra("label");

        edt_title.setText(title);
        edt_id.setText(id);
        edt_pw.setText(pw);
        edt_memo.setText(memo);

        if(label != null && !label.isEmpty()) {
            isEdit = true;
        }

        progressBar = findViewById(R.id.add_progressBar);

        button_save = findViewById(R.id.button_save);
        button_save.setIcon(isEdit ? ContextCompat.getDrawable(this, R.drawable.ic_trash_bold) : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        button_save.setOnClickListener(view -> {
            String title = edt_title.getText().toString();
            String id = edt_id.getText().toString();
            String pw = edt_pw.getText().toString();
            String memo = edt_memo.getText().toString();
            Timestamp timestamp = Timestamp.now();

            if (title.isEmpty()) {
                Utils.showSnack(findViewById(R.id.contentScreen), "제목을 입력하세요");
                return;
            }

            // firebase 데이터 저장
            Content content = new Content();
            content.setTitle(title);
            content.setId(id);
            content.setPw(pw);
            content.setMemo(memo);
            content.setTimestamp(Timestamp.now());

            if (isEdit) {
                deleteFromFirebase();
            } else {
                saveToFirebase(content);
            }
        });
    }

    private void addChangeInProgress(boolean inProgress) {
        button_save.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void saveToFirebase(Content content) {
        addChangeInProgress(true);
        if(isEdit) {
            // 컨텐츠 업데이트
            documentReference = Utils.getContentReference().document(label);
        } else {
            // 컨텐츠 생성
            documentReference = Utils.getContentReference().document();
        }

        documentReference.set(content).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                finish();
            } else {
                addChangeInProgress(false);
                Utils.showSnack(findViewById(R.id.contentScreen), "다시 시도하세요");
            }
        });
    }

    private void deleteFromFirebase() {
        documentReference = Utils.getContentReference().document(label);
        documentReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                finish();
            } else {
                Utils.showSnack(findViewById(R.id.contentScreen), "다시 시도하세요");
            }
        });
    }
}