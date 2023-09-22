package com.example.passwordmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.passwordmanager.adapter.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

public class AddContentActivity extends AppCompatActivity {
    DocumentReference docRef;
    MaterialToolbar toolbar;
    TextInputEditText edt_title;
    EditText edt_id, edt_pw, edt_memo;
    MaterialButton button_save, button_edit;
    ProgressBar progressBar;
    String label;
    boolean isEdit = false;
    boolean isChange = false;

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

        edt_title.setText(getIntent().getStringExtra("title"));
        edt_id.setText(getIntent().getStringExtra("id"));
        edt_pw.setText(getIntent().getStringExtra("pw"));
        edt_memo.setText(getIntent().getStringExtra("memo"));
        label = getIntent().getStringExtra("label");

        if(label != null && !label.isEmpty()) isEdit = true;

        progressBar = findViewById(R.id.add_progressBar);

        button_edit = findViewById(R.id.button_edit);
        button_edit.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        button_edit.setOnClickListener(v -> {
            changeVisible(isEdit);
        });
        button_save = findViewById(R.id.button_save);
        button_save.setIcon(isEdit ? ContextCompat.getDrawable(this, R.drawable.ic_dot_horizon_bold) : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
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

            Content content = new Content(title, id, pw, memo, timestamp);

            if (isEdit && !isChange) {
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.content_bottom_sheet, findViewById(R.id.cbs_container));
                bottomSheetView.findViewById(R.id.option_trash).setOnClickListener(v -> {
                    bottomSheetDialog.dismiss();
                    DocumentReference fromPath = Utils.getContentReference().document(label);
                    DocumentReference toPath = Utils.getTrashReference().document(label);
                    moveFirebaseDocument(fromPath, toPath);
                });
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            } else if (isChange) {
                saveToFirebase(content);
            } else {
                saveToFirebase(content);
            }
        });
    }

    private void changeVisible(boolean isEdit) {
        button_edit.setVisibility(isEdit ? View.GONE : View.VISIBLE);
        button_save.setIcon(isEdit ? ContextCompat.getDrawable(this, R.drawable.ic_check_bold) : ContextCompat.getDrawable(this, R.drawable.ic_dot_horizon_bold));
        isChange = true;
    }

    private void addChangeInProgress(boolean inProgress) {
        button_save.setIcon(inProgress ? null : ContextCompat.getDrawable(this, R.drawable.ic_check_bold));
        progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

    private void saveToFirebase(Content content) {
        addChangeInProgress(true);
        if(isEdit) {
            docRef = Utils.getContentReference().document(label);
            if (isChange)
                docRef = Utils.getContentReference().document(label);
        } else {
            docRef = Utils.getContentReference().document();
        }

        docRef.set(content).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                button_save.setEnabled(false);
                finish();
            } else {
                addChangeInProgress(false);
                button_save.setEnabled(true);
                Utils.showSnack(findViewById(R.id.contentScreen), "오류, 다시 시도하세요");
            }
        });
    }

    private void moveFirebaseDocument(DocumentReference fromPath, DocumentReference toPath) {
        button_save.setEnabled(false);
        addChangeInProgress(true);
        fromPath.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null) {
                    toPath.set(documentSnapshot.getData())
                            .addOnSuccessListener(unused -> fromPath.delete()
                                    .addOnSuccessListener(unused1 -> finish())
                                    .addOnFailureListener(e -> Utils.showSnack(findViewById(R.id.contentScreen), "오류, 다시 시도하세요")));
                }
            }
        });
    }
}