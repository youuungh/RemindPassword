package com.example.passwordmanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.passwordmanager.model.Adapter;
import com.example.passwordmanager.model.Content;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    FloatingActionButton button_fab;
    RecyclerView recyclerView;
    FirebaseFirestore fStore;
    Adapter adapter;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fStore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.contents_list);
        button_fab = view.findViewById(R.id.button_fab);

        Query query = Utility.getCollectionReference().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Content> options = new FirestoreRecyclerOptions.Builder<Content>()
                .setQuery(query, Content.class).build();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new Adapter(options, this);
        recyclerView.setAdapter(adapter);

        // 플로팅 버튼
        button_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddContentActivity.class));
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

}