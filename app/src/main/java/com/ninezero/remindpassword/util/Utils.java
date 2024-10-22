package com.ninezero.remindpassword.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Locale;

public class Utils {
    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showSnack(View v, String msg) {
        Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
    }

    public static void copyToClipboard(Context context, String msg) {
        android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clipData = android.content.ClipData.newPlainText("", msg);
        clipboardManager.setPrimaryClip(clipData);
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
                .collection("Trashes")
                .document("trash")
                .collection(fUser.getUid());
    }

    public static CollectionReference getFavoriteReference() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return FirebaseFirestore.getInstance()
                .collection("Users")
                .document("favorite")
                .collection(fUser.getUid());
    }

    public static String encodeBase64(String data) {
        return Base64.getEncoder().withoutPadding().encodeToString(data.getBytes());
    }

    public static String decodeBase64(String data) {
        return new String(Base64.getDecoder().decode(data));
    }

    public static void savePassCode(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences("PASSCODE_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PASSCODE", key);
        editor.apply();
    }

    public static void saveBiometric(Context context, boolean key) {
        SharedPreferences prefs = context.getSharedPreferences("BIOMETRIC_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("BIOMETRIC", key);
        editor.apply();
    }
}
