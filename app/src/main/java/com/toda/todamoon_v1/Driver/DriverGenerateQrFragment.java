package com.toda.todamoon_v1.Driver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v1.FirebaseStorageManager;
import com.toda.todamoon_v1.ImageDownloader;
import com.toda.todamoon_v1.R;
import com.toda.todamoon_v1.databinding.DriverGenerateQrFragmentBinding;

public class DriverGenerateQrFragment extends Fragment {

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    private static final String PREF_KEY_STORAGE_PERMISSION_GRANTED = "storage_permission_granted";
    private static final String TAG = "DriverGenerateQrFragment";
    private static final String ARG_UID = "driverUid";
    private static final String ARG_NAME = "name";
    private static final String ARG_TRICYCLE_NUMBER = "tricycleNumber";
    private static final String ARG_QR_CODE_URL = "qrCodeUrl";

    private FirebaseStorage storage;
    private StorageReference qrCodeRef;
    private String driverUid;
    private Context context;
    private DriverGenerateQrFragmentBinding binding;
    private boolean isQrCodeLoaded = false;
    private FirebaseStorageManager storageManager;
    private ImageDownloader imageDownloader;
    private ImageView qrCodeImageView;

    public DriverGenerateQrFragment() {
        // Required empty public constructor
    }

    public static DriverGenerateQrFragment newInstance(String driverUid, String name, String tricycleNumber, String qrCodeUrl) {
        DriverGenerateQrFragment fragment = new DriverGenerateQrFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, driverUid);
        args.putString(ARG_NAME, name);
        args.putString(ARG_TRICYCLE_NUMBER, tricycleNumber);
        args.putString(ARG_QR_CODE_URL, qrCodeUrl);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        storageManager = new FirebaseStorageManager();
        imageDownloader = new ImageDownloader(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DriverGenerateQrFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            driverUid = getArguments().getString(ARG_UID);
            Log.d(TAG, "Driver UID: " + driverUid);
        } else {
            Log.e(TAG, "No driver UID provided");
        }

        binding.learnMoreButton.setOnClickListener(v -> showBottomSheetDialog());
        binding.addFromGalleryButton.setOnClickListener(v -> downloadImage());

        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        loadQrCodeImage();


    }

    private void initializeStoragePath(String driverUid) {
        storage = FirebaseStorage.getInstance();
        qrCodeRef = storage.getReference().child("qrcodes/" + driverUid + ".png");
    }

    private void loadQrCodeImage() {

        Bundle args = getArguments();
        if (args != null) {
            String qrCodeUrl = args.getString(ARG_QR_CODE_URL);

            // Set user data to views
            Glide.with(DriverGenerateQrFragment.this).load(qrCodeUrl).into(qrCodeImageView);
        }
    }

    @SuppressLint("InflateParams")
    private void showBottomSheetDialog() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);

        TextView txtUID = bottomSheetView.findViewById(R.id.txtUID);
        TextView txtName = bottomSheetView.findViewById(R.id.txtName);
        TextView txtTricNumber = bottomSheetView.findViewById(R.id.txtTricNum);

        Bundle args = getArguments();
        if (args != null) {
            txtUID.setText(args.getString(ARG_UID));
            txtName.setText(args.getString(ARG_NAME));
            txtTricNumber.setText(args.getString(ARG_TRICYCLE_NUMBER));
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void downloadImage() {
        StorageReference qrCodeRef = storageManager.getQrCodeReference(driverUid);
        imageDownloader.downloadAndSaveImage(qrCodeRef);
    }

//    private void checkStoragePermissionAndDownloadImage() {
//        if (isStoragePermissionGranted()) {
//            downloadImage();
//        } else {
//            requestStoragePermission();
//        }
//    }
//
//    private boolean isStoragePermissionGranted() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
//        return sharedPreferences.getBoolean(PREF_KEY_STORAGE_PERMISSION_GRANTED, false);
//    }
//
//    private void requestStoragePermission() {
//        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
//    }
//
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                saveStoragePermissionStatus(true);
//            } else {
//                saveStoragePermissionStatus(false);
//                Toast.makeText(requireContext(), "Storage permission is required for downloading images.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void saveStoragePermissionStatus(boolean isGranted) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
//        sharedPreferences.edit().putBoolean(PREF_KEY_STORAGE_PERMISSION_GRANTED, isGranted).apply();
//    }
}
