package com.example.passwordmanager;

import android.content.Context;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utility {
    static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static String timeStampToString(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(timestamp.toDate());
    }

    static CollectionReference getCollectionReference() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance()
                .collection("contents")
                .document(currentUser.getUid())
                .collection("my_contents");
    }
}
