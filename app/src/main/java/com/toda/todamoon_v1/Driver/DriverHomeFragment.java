package com.toda.todamoon_v1.Driver;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.toda.todamoon_v1.DateTime;
import com.toda.todamoon_v1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverHomeFragment extends Fragment {

    private RecyclerView transactionRecyclerView;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList;
    private double balance;
    private TextView balanceTextView;

    private static final String TAG = "DriverHomeFragment";

    public DriverHomeFragment() {
        // Required empty public constructor
    }

    public static DriverHomeFragment newInstance(double balance) {
        DriverHomeFragment fragment = new DriverHomeFragment();
        Bundle args = new Bundle();
//        args.putDouble("balance", balance);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            balance = getArguments().getDouble("balance", 0.0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.driver_home_fragment, container, false);

        // Initialize UI components
        initUIComponents(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh balance and transactions when fragment is resumed
        fetchBalanceFromFirestore();
        fetchTransactionsFromFirestore();
    }

    private void fetchBalanceFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("drivers").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Double newBalance = documentSnapshot.getDouble("balance");
                            if (newBalance != null) {
                                balance = newBalance;
                                balanceTextView.setText(String.format(Locale.getDefault(), "₱%.2f", balance));
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch balance: ", e);
                    });
        }
    }

    private void fetchTransactionsFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference transactionsRef = db.collection("drivers").document(userId)
                    .collection("transactions");

            // Modify the query to order by timestamp field
            transactionsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.e(TAG, "Failed to fetch transactions: ", e);
                                return;
                            }

                            if (snapshots != null) {
                                transactionList.clear();
                                for (QueryDocumentSnapshot doc : snapshots) {
                                    Transaction transaction = doc.toObject(Transaction.class);
                                    transactionList.add(transaction);
                                }
                                transactionAdapter.notifyDataSetChanged();
                            }
                        }
                    });
        }
    }

    private void initUIComponents(View view) {
        // Initialize and set up the transaction list
        initTransactionList();

        // Set up the adapter
        transactionAdapter = new TransactionAdapter(getActivity(), transactionList);

        // Find the RecyclerView and set the adapter
        transactionRecyclerView = view.findViewById(R.id.transaction_history_list);
        transactionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        transactionRecyclerView.setAdapter(transactionAdapter);

        // Add RecyclerView divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(transactionRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        transactionRecyclerView.addItemDecoration(dividerItemDecoration);

        // Display the balance
        balanceTextView = view.findViewById(R.id.accountBalance);
        balanceTextView.setText(String.format(Locale.getDefault(), "₱%.2f", balance));

        TextView dateTextView = view.findViewById(R.id.txtDate);
        DateTime.startDateTimeUpdater(dateTextView);

    }

    private void initTransactionList() {
        transactionList = new ArrayList<>();
    }
}
