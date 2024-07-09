package com.toda.todamoon_v1;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseStorageManager {
    private FirebaseStorage storage;

    public FirebaseStorageManager() {
        storage = FirebaseStorage.getInstance();
    }

    public StorageReference getQrCodeReference(String driverUid) {
        return storage.getReference().child("qrcodes/" + driverUid + ".png");
    }
}
