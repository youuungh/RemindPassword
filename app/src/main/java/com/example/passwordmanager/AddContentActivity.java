package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.passwordmanager.model.Content;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class AddContentActivity extends AppCompatActivity {
    MaterialToolbar toolbar;
    TextInputEditText edt_title;
    EditText edt_id, edt_pw, edt_memo;
    ProgressBar progressBar;
    String title, id, pw, memo, label;
    FloatingActionButton fab_finish;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        progressBar = findViewById(R.id.progressBar);

        edt_title = findViewById(R.id.edt_title);
        edt_id = findViewById(R.id.edt_id);
        edt_pw = findViewById(R.id.edt_pw);
        edt_memo = findViewById(R.id.edt_memo);

        // 액션바
        toolbar = findViewById(R.id.content_add_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

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

        // 뒤로가기
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // 플로팅 버튼
        fab_finish = findViewById(R.id.fab_finish);
        fab_finish.setOnClickListener(view -> {
            String title = edt_title.getText().toString();
            String id = edt_id.getText().toString();
            String pw = edt_pw.getText().toString();
            String memo = edt_memo.getText().toString();
            Timestamp timestamp = Timestamp.now();

            if (title.isEmpty()) {
                Toast.makeText(AddContentActivity.this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 프로그래스바
            progressBar.setVisibility(View.VISIBLE);

            // firebase 데이터 저장
            Content content = new Content();
            content.setTitle(title);
            content.setId(id);
            content.setPw(pw);
            content.setMemo(memo);
            content.setTimestamp(Timestamp.now());

            saveToFirebase(content);
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void saveToFirebase(Content content) {
        DocumentReference documentReference;

        if(isEdit) {
            // 컨텐츠 업데이트
            documentReference = Utils.getContentReference().document(label);
        } else {
            // 컨텐츠 생성
            documentReference = Utils.getContentReference().document();
        }

        documentReference.set(content).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Utils.showToast(AddContentActivity.this, "저장됨");
                finish();
            } else {
                Utils.showSnack(findViewById(R.id.layout_content_add), "다시 시도하세요");
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if(isEdit) {
            inflater.inflate(R.menu.menu_option_delete, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            DocumentReference documentReference;
            documentReference = Utils.getContentReference().document(label);

            documentReference.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Utils.showToast(AddContentActivity.this, "삭제됨");
                    finish();
                } else {
                    Utils.showSnack(findViewById(R.id.layout_content_add), "다시 시도하세요");
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}