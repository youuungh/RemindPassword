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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.C;

import java.net.IDN;
import java.util.HashMap;
import java.util.Map;

public class AddContentActivity extends AppCompatActivity {
    FirebaseFirestore fStore;
    MaterialToolbar toolbar;
    FloatingActionButton add_finish_fab;
    EditText toolbar_add_title, content_edit_id, content_edit_pw, content_edit_memo;
    ProgressBar progressBar;
    String title, id, pw, memo, documentId;
    boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        toolbar_add_title = findViewById(R.id.toolbar_add_title);
        content_edit_id = findViewById(R.id.content_edit_id);
        content_edit_pw = findViewById(R.id.content_edit_pw);
        content_edit_memo = findViewById(R.id.content_edit_memo);

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
        documentId = getIntent().getStringExtra("documentId");

        toolbar_add_title.setText(title);
        content_edit_id.setText(id);
        content_edit_pw.setText(pw);
        content_edit_memo.setText(memo);

        if(documentId != null && !documentId.isEmpty()) {
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
        add_finish_fab = findViewById(R.id.add_finish_fab);
        add_finish_fab.setOnClickListener(view -> {
            String title = toolbar_add_title.getText().toString();
            String id = content_edit_id.getText().toString();
            String pw = content_edit_pw.getText().toString();
            String memo = content_edit_memo.getText().toString();
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
            documentReference = Utility.getCollectionReference().document(documentId);
        } else {
            // 컨텐츠 생성
            documentReference = Utility.getCollectionReference().document();
        }

        documentReference.set(content).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Utility.showToast(AddContentActivity.this, "저장됨");
                    finish();
                } else {
                    Utility.showToast(AddContentActivity.this, "다시 시도하세요");
                    progressBar.setVisibility(View.VISIBLE);
                }
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
        switch (item.getItemId()) {
            case R.id.menu_delete:

                DocumentReference documentReference;
                documentReference = Utility.getCollectionReference().document(documentId);

                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            // 삭제
                            Utility.showToast(AddContentActivity.this, "삭제됨");
                            onBackPressed();
                        } else {
                            Utility.showToast(AddContentActivity.this, "다시 시도하세요");
                        }
                    }
                });

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}