package com.example.passwordmanager;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {
    static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    static void showSnack(View v, String msg) {
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static String timeStampToString(Timestamp timestamp) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(timestamp.toDate());
    }

    public static CollectionReference getContentReference() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance()
                .collection("Users")
                .document("contents")
                .collection(fUser.getUid());
    }

    public static CollectionReference getTrashReference() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance()
                .collection("Users")
                .document("trash")
                .collection(fUser.getUid());
    }

    public static void savePassCode(Context context, String key) {
        Log.d("setPref:", "데이터 저장 성공");
        SharedPreferences pref = context.getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("PASSCODE", key);
        editor.commit();
    }

    public static String getPassCode(Context context) {
        Log.d("getPref:", "데이터 불러오기 성공");
        SharedPreferences pref = context.getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        return pref.getString("PASSCODE", "");
    }
}
