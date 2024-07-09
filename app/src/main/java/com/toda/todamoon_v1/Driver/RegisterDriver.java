package com.toda.todamoon_v1.Driver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.toda.todamoon_v1.EncryptionUtil;
import com.toda.todamoon_v1.HashPassword;
import com.toda.todamoon_v1.R;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterDriver extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    private static final int STEP_ONE = 1;
    private static final int STEP_TWO = 2;
    private static final int STEP_THREE = 3;
    private static final int STEP_FOUR = 4;

    private ImageButton step1Button, step2Button, step3Button, regButton;
    private View contactLeft, contactRight, OtherRight;
    private TextView stepTwoTextView, stepThreeTextView;
    private ProgressBar progressBar;
    private ImageButton previousButton, nextButton;
    private FrameLayout stepContentContainer;
    private View stepOneLayout, stepTwoLayout, stepThreeLayout;

    // Declare Form part one field
    private TextInputEditText firstName, middleName, lastName, age, address;

    // Declare Form part two field
    private Spinner signUpBarangay;
    private TextInputEditText plateNumber, tricycleNumber;

    // Declare Form part three field
    private TextInputEditText email, phoneNumber, password, confirmPassword;
    private CheckBox chkTermsAndCondition;
    private ScrollView scrollTermsAndCondition;
    private int currentStep = STEP_ONE; // track step for next and previous button
    private String inputEmail, inputPassword;
    private String imageProfile = "default";
    private int colorPrimaryDark, colorGray, colorWhite;

    String barangay;

    private FirebaseStorage storage;

    private Dialog loadingDialog;

    private static final String SECRET_KEY = "Todamoon_drivers"; // Replace with a 16-byte key


    private void initializeViews() {

        // FIRESTORE
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        // MULTIFORM

        step1Button = findViewById(R.id.stepOneButton);
        step2Button = findViewById(R.id.stepTwoButton);
        step3Button = findViewById(R.id.stepThreeButton);

        contactLeft = findViewById(R.id.contactLeftView);
        contactRight = findViewById(R.id.contactRightView);
        OtherRight = findViewById(R.id.OtherRightView);

        stepTwoTextView = findViewById(R.id.stepTwoTextView);
        stepThreeTextView = findViewById(R.id.stepThreeTextView);

        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        regButton = findViewById(R.id.registerButton);

        stepContentContainer = findViewById(R.id.stepContentContainer);

        // Steps layout
        stepOneLayout = LayoutInflater.from(this).inflate(R.layout.form_step_one, null);
        stepTwoLayout = LayoutInflater.from(this).inflate(R.layout.form_step_two, null);
        stepThreeLayout = LayoutInflater.from(this).inflate(R.layout.form_step_three, null);

        // Step one form field
        firstName = stepOneLayout.findViewById(R.id.signupFirstNameText);
        middleName = stepOneLayout.findViewById(R.id.signupMiddleNameText);
        lastName = stepOneLayout.findViewById(R.id.signupLastNameText);

        // Step two form field
        signUpBarangay = stepTwoLayout.findViewById(R.id.signupBarangaySpinner);
        plateNumber = stepTwoLayout.findViewById(R.id.signupPlateNumberText);
        tricycleNumber = stepTwoLayout.findViewById(R.id.signupTricycleText);

        // Step three form field
        email = stepThreeLayout.findViewById(R.id.signupEmailText);
        phoneNumber = stepThreeLayout.findViewById(R.id.signupPhoneNumberText);
        password = stepThreeLayout.findViewById(R.id.signupPasswordText);
        confirmPassword = stepThreeLayout.findViewById(R.id.signupConfirmPasswordText);
        scrollTermsAndCondition = stepThreeLayout.findViewById(R.id.termsConditionView);
    }

    private void initializeColors() {
        colorPrimaryDark = ContextCompat.getColor(this, R.color.primary_dark);
        colorGray = ContextCompat.getColor(this, R.color.light_gray);
        colorWhite = ContextCompat.getColor(this, R.color.white);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_driver);

        initializeViews();

        setupLoadingDialog();

        // Set the initial step content
        showStepContent(currentStep);

        setPrevNextButtonListener();
        spinner();


    }

    private void spinner() {
        //SPINNER
        // List of barangays
        String[] barangays = new String[]{"Select Barangay", "Barandal", "Bubuyan", "Bunggo", "Burol", "Kay-anlog", "Prinza", "Punta"};

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, barangays) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item (hint)
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;

                // Set the text color of the disabled hint to gray
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        // Drop down layout style - list view with radio button
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attaching data adapter to spinner
        signUpBarangay.setAdapter(adapter);
        // Disable the first item (hint)
    }

    private void setPrevNextButtonListener() {

        previousButton.setOnClickListener(v -> {
            if (currentStep > 1) {
                currentStep--; // Decrease currentStep by 1
                showStepContent(currentStep);
            } else {
                // If the user is at the first step, navigate back to the login page or the previous screen
                finish(); // Finish the current activity to prevent the user from navigating back to it
            }
        });

        nextButton.setOnClickListener(v -> {

            if(currentStep == STEP_ONE) {
                if(validateStepOne()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(currentStep == STEP_TWO) {
                if(validateStepTwo()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(currentStep == STEP_THREE) {
                if(validateStepThree()) {
                    currentStep++; // Increase currentStep by 1
                    showStepContent(currentStep);
                } else {
                    Toast.makeText(this, "Please fill out all fields with correct credentials.", Toast.LENGTH_SHORT).show();
                }
            }

        });

        // Show the initial step content
        showStepContent(currentStep);
    }

    private void showStepContent(int step) {
        // Update progress bar
        //updateProgressBar(step);

        // Update current step
        currentStep = step;

        // Update step buttons
        updateStepButtons();

        // Show corresponding step layout
        switch (step) {
            case STEP_ONE:
                showStepOne();
                break;
            case STEP_TWO:
                showStepTwo();
                break;
            case STEP_THREE:
                showStepThree();
                break;

        }
    }

    private void updateStepButtons() {
        step1Button.setEnabled(currentStep != STEP_ONE);
        step2Button.setEnabled(currentStep != STEP_TWO);
        step3Button.setEnabled(currentStep != STEP_THREE);
    }


    private void showStepOne() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepOneLayout);

        currentStep = 1; // Reset currentStep value to one

        step1Button.setClickable(false); // Make step1Button not clickable
        step2Button.setClickable(true); // Make step2Button clickable
        step3Button.setClickable(true); // Make step3Button clickable

        previousButton.setClickable(true); // Make previousButton not clickable
        nextButton.setClickable(true); // Make nextButton clickable

        // Reset other views colors to gray
        contactLeft.setBackgroundColor(colorGray);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepTwoTextView.setTextColor(colorGray);
        contactRight.setBackgroundColor(colorGray);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepThreeTextView.setTextColor(colorGray);
        OtherRight.setBackgroundColor(colorGray);

        //progressBar.setProgress(33);

        // Reset previous button color
        previousButton.setBackgroundResource(R.drawable.button_style);
        previousButton.setColorFilter(colorPrimaryDark);

        // Reset next button color
        nextButton.setImageResource(R.drawable.ic_forward_arrow);
        nextButton.setBackgroundColor(colorPrimaryDark);
        nextButton.setColorFilter(colorWhite);

        nextButton.setVisibility(View.VISIBLE);
        regButton.setVisibility(View.GONE);
    }

    private void showStepTwo() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepTwoLayout);

        currentStep = 2; // Reset currentStep value to two

        // Step button clickable
        step1Button.setClickable(true);
        step2Button.setClickable(false);
        step3Button.setClickable(true);

        previousButton.setClickable(true); // Make previousButton to clickable
        nextButton.setClickable(true); // Make nextButton to clickable

        contactLeft.setBackgroundColor(colorPrimaryDark);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepTwoTextView.setTextColor(colorPrimaryDark);

        // Reset other views colors to gray
        contactRight.setBackgroundColor(colorGray);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorGray));
        stepThreeTextView.setTextColor(colorGray);
        OtherRight.setBackgroundColor(colorGray);

        //progressBar.setProgress(58);

        // Change previous button color
        previousButton.setBackgroundColor(colorPrimaryDark);
        previousButton.setColorFilter(colorWhite);

        // Reset next button color
        nextButton.setImageResource(R.drawable.ic_forward_arrow); // reset button icon
        nextButton.setBackgroundColor(colorPrimaryDark);
        nextButton.setColorFilter(colorWhite);

        nextButton.setVisibility(View.VISIBLE);
        regButton.setVisibility(View.GONE);
    }


    private void showStepThree() {
        initializeColors();

        stepContentContainer.removeAllViews();
        stepContentContainer.addView(stepThreeLayout);

        currentStep = 3; // Reset currentStep value to three

        // Step button clickable
        step1Button.setClickable(true);
        step2Button.setClickable(true);
        step3Button.setClickable(false);

        previousButton.setClickable(true); // Make previousButton to clickable
        // nextButton.setClickable(false);  // Make nextButton to clickable
        nextButton.setClickable(false);

        contactLeft.setBackgroundColor(colorPrimaryDark);
        step2Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepTwoTextView.setTextColor(colorPrimaryDark);
        contactRight.setBackgroundColor(colorPrimaryDark);
        step3Button.setBackgroundTintList(ColorStateList.valueOf(colorPrimaryDark));
        stepThreeTextView.setTextColor(colorPrimaryDark);
        OtherRight.setBackgroundColor(colorPrimaryDark);

        //progressBar.setProgress(100);

        // Change previous button color
        previousButton.setBackgroundColor(colorPrimaryDark);
        previousButton.setColorFilter(colorWhite);

        // Change next button color
        nextButton.setVisibility(View.GONE);
        regButton.setVisibility(View.VISIBLE);

        regButton.setOnClickListener(v -> {
            if(validateStepThree()) {
                String emails = email.getText().toString().trim();
                String passwords = password.getText().toString().trim();

                showDialogMessage();
            } else {
                Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void showDialogMessage() {
        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.terms_and_condition, null);
        chkTermsAndCondition = dialogView.findViewById(R.id.chkTermsCondition);

        // Build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle(R.string.terms_and_condition_title)
                .setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle user agreement
                        // You can perform necessary actions here, such as enabling registration
                        currentStep = STEP_FOUR;
                        if (!chkTermsAndCondition.isChecked()) {
                            Toast.makeText(RegisterDriver.this, "Please agree to the terms and condition to continue.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Proceed with registration
                            registerDriver();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Handle user disagreement
                        // You may choose to close the app or restrict functionality
                        currentStep = STEP_FOUR;
                        Toast.makeText(RegisterDriver.this, "Registration Cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(true);

        // Show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void registerDriver() {
        HashPassword hashPass = new HashPassword();

        // Display the loading dialog before starting the registration process
        loadingDialog.show();

        String emails = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        // Hash the password before storing it
        String hashedPassword = hashPass.hashPassword(passwordInput);

        mAuth.createUserWithEmailAndPassword(emails, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            DriverGetterSetter newDriver = createDriverObject(emails, hashedPassword, uid);
                            runRegistrationTransaction(newDriver);
                        }
                    } else {
                        Toast.makeText(RegisterDriver.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }

    private DriverGetterSetter createDriverObject(String email, String password, String uid) {
        String name = firstName.getText().toString().trim() + " " + lastName.getText().toString().trim();
        barangay = (String) signUpBarangay.getSelectedItem();
        double initialBalance = 500.00; // Set the initial balance to 500
        String role = "Driver";
        boolean inQueue = false;

        return new DriverGetterSetter(
                uid,
                name,
                barangay,
                plateNumber.getText().toString().trim(),
                tricycleNumber.getText().toString().trim(),
                email,
                phoneNumber.getText().toString().trim(),
                password,
                confirmPassword.getText().toString().trim(),
                imageProfile,
                initialBalance,
                role,
                inQueue
        );
    }

    private void runRegistrationTransaction(DriverGetterSetter driver) {
        db.runTransaction(transaction -> {
            // Create references for the documents we will update
            DocumentReference driverRef = db.collection("drivers").document(driver.uid);
            DocumentReference userRef = db.collection("users").document(driver.uid);
            DocumentReference barangayRef = db.collection("barangays").document(driver.barangay);
            CollectionReference driversInBarangayRef = barangayRef.collection("drivers");
            DocumentReference barangayDriverRef = driversInBarangayRef.document(driver.uid);

            // Create a batch write for atomicity
            transaction.set(driverRef, driver);
            transaction.set(userRef, driver);

            Map<String, Object> driverInfo = new HashMap<>();
            driverInfo.put("Name", driver.name);
            driverInfo.put("TricycleNumber", driver.tricycleNumber);
            driverInfo.put("inQueue", driver.inQueue);
            transaction.set(barangayDriverRef, driverInfo);

            return null;
        }).addOnSuccessListener(aVoid -> {
            generateAndStoreQrCode(driver);
        }).addOnFailureListener(e -> {
            Toast.makeText(RegisterDriver.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        });
    }

    private void generateAndStoreQrCode(DriverGetterSetter driver) {
        try {
            String driverInfo = "Name: " + driver.name + "\nTricycle Number: " + driver.tricycleNumber + "\nIn Queue: false";
            String encryptedDriverInfo = EncryptionUtil.encrypt(driverInfo, SECRET_KEY);

            Bitmap qrCode = generateQRCode(encryptedDriverInfo);

            if (qrCode != null) {
                uploadQrCodeToStorage(driver.uid, qrCode, driver.name, driver.tricycleNumber, false);
            } else {
                Log.e("RegisterDriver", "Failed to generate QR code");
                loadingDialog.dismiss();
            }
        } catch (Exception e) {
            Log.e("RegisterDriver", "Error encrypting driver info", e);
        }
    }

    private Bitmap generateQRCode(String text) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            Log.e("RegisterDriver", "Error generating QR code", e);
            return null;
        }
    }

    private void uploadQrCodeToStorage(String driverUid, Bitmap qrCode, String driverName, String tricycleNumber, boolean inQueue) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        qrCode.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] qrCodeBytes = baos.toByteArray();

        StorageReference storageRef = storage.getReference();
        StorageReference qrCodeRef = storageRef.child("qrcodes/" + driverUid + ".png");

        qrCodeRef.putBytes(qrCodeBytes).addOnSuccessListener(taskSnapshot -> {
            qrCodeRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();
                saveQrCodeUrlToFirestore(driverUid, downloadUrl, driverName, tricycleNumber, inQueue);
            }).addOnFailureListener(e -> {
                Log.e("RegisterDriver", "Error getting download URL", e);
                loadingDialog.dismiss();
            });
        }).addOnFailureListener(e -> {
            Log.e("RegisterDriver", "Error uploading QR code", e);
            loadingDialog.dismiss();
        });
    }

    private void saveQrCodeUrlToFirestore(String driverUid, String downloadUrl, String driverName, String tricycleNumber, boolean inQueue) {
        Map<String, Object> qrCodeData = new HashMap<>();
        qrCodeData.put("driverUid", driverUid);
        qrCodeData.put("name", driverName);
        qrCodeData.put("tricycleNumber", tricycleNumber);
        qrCodeData.put("inQueue", inQueue);
        qrCodeData.put("qrCodeUrl", downloadUrl);

        db.collection("qr_code").document(driverUid).set(qrCodeData).addOnSuccessListener(aVoid -> {
            loadingDialog.dismiss();
            Intent intent = new Intent(RegisterDriver.this, LoginDriver.class);
            startActivity(intent);
            Toast.makeText(RegisterDriver.this, "Registration successful", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Log.e("RegisterDriver", "Error storing QR code URL", e);
            loadingDialog.dismiss();
        });
    }

    private void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.activity_progress_bar);
        loadingDialog.setCancelable(false);
    }

    private boolean validateStepOne() {
        // Perform validation for Step One fields
        String firstNameText = firstName.getText().toString().trim();
        String lastNameText = lastName.getText().toString().trim();

        // Check if first name contains only letters (including spaces and enye)
        boolean firstNameValid = !TextUtils.isEmpty(firstNameText) && firstNameText.matches("[a-zA-ZñÑ\\s]+");

        // Check if last name contains only letters (including spaces and enye)
        boolean lastNameValid = !TextUtils.isEmpty(lastNameText) && lastNameText.matches("[a-zA-ZñÑ\\s]+");

        // Show or hide error message based on validation result for first name
        if (!firstNameValid) {
            firstName.setError("Input a correct name.");
        } else {
            firstName.setError(null); // Clear the error
        }

        // Show or hide error message based on validation result for last name
        if (!lastNameValid) {
            lastName.setError("Input a correct name.");
        } else {
            lastName.setError(null); // Clear the error
        }

        // Return true if both first name and last name are valid
        return firstNameValid && lastNameValid;
    }


    private boolean validateStepTwo() {
        // Perform validation for Step Two fields
        return signUpBarangay.getSelectedItemPosition() != 0
                && !TextUtils.isEmpty(plateNumber.getText().toString())
                && !TextUtils.isEmpty(tricycleNumber.getText().toString());
    }

    private boolean validateStepThree() {
        // Perform validation for Step Three fields
        String emailText = email.getText().toString().trim();
        String phoneNumberText = phoneNumber.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // Check if email, password, and confirm password fields are not empty
        boolean emailValid = !TextUtils.isEmpty(emailText);
        boolean passwordValid = !TextUtils.isEmpty(passwordText);
        boolean confirmPasswordValid = !TextUtils.isEmpty(confirmPasswordText);

        // Check if phone number is exactly 11 digits
        boolean phoneNumberValid = phoneNumberText.length() == 11;

        // Check if password and confirm password match
        boolean passwordsMatch = passwordText.equals(confirmPasswordText);

        // Show error alerts if phone number is not 11 digits or passwords don't match
        if (!phoneNumberValid) {
            phoneNumber.setError("Phone number must be exactly 11 digits");
        } else {
            phoneNumber.setError(null); // Clear the error
        }

        if (!passwordsMatch) {
            Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            confirmPassword.setError("Passwords don't match");
        } else {
            confirmPassword.setError(null); // Clear the error
        }

        // Return false if any field is empty, phone number is not exactly 11 digits,
        // or passwords don't match
        return emailValid && phoneNumberValid && passwordValid && confirmPasswordValid && passwordsMatch;
    }



}