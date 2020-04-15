package com.example.fitnessapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

public class UserProfile extends AppCompatActivity implements View.OnClickListener {
    TextView username;
    TextView email;
    Button signOutButton;
    ImageView profilePicture;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser firebaseUser;

    StorageReference profilePictureReference;
    DocumentReference userReference;

    String userId;

    DateModelAdapter dateModelAdapter;

    static final int REQUEST_IMAGE_CAPTURE = 10001;
    static final int SELECT_IMAGE_REQUEST = 10002;

    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.display_username);
        email = findViewById(R.id.display_email);
        signOutButton = findViewById(R.id.multipleUseButton);
        profilePicture = findViewById(R.id.profilePicture);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        firebaseFirestore = FirebaseFirestore.getInstance();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();

            // Make a reference to the users document in Firebase.
            userReference = firebaseFirestore.collection("users")
                    .document(userId);

            // Make a reference to the profile pictures storage.
            profilePictureReference = FirebaseStorage.getInstance().getReference()
                    .child("profile_pictures");

            // Listen to the data in the FireBase database.
            userReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (!documentSnapshot.getString("profileImageURL")
                                .equals("default")) {
                            // If the user's profile picture URL is not default, load the profile
                            // picture.
                            Glide.with(UserProfile.this)
                                    .load(documentSnapshot.getString("profileImageURL"))
                                    .into(profilePicture);
                        }
                        // Set the username from the database onto the profile.
                        username.setText(documentSnapshot.getString("username"));
                        // Set the email from the database on the profile.
                        email.setText(documentSnapshot.getString("email"));
                    }
                }
            });

            signOutButton.setOnClickListener(this);
            profilePicture.setOnClickListener(this);

            setUpDateRecyclerView();
        } else {
            Intent signInActivity = new Intent(this, SignIn.class);
            startActivity(signInActivity);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Go back to the previous activity.
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.multipleUseButton:
                // Sign out of Firebase if the user clicks sign out.
                firebaseAuth.signOut();
                // Send user back to sign in activity when they sign out.
                Intent signInActivity = new Intent(getApplicationContext(), SignIn.class);
                startActivity(signInActivity);
                finish();
                break;
            case R.id.profilePicture:
                // Function that provides different methods of updating the profile picture.
                changeProfilePicture();
                break;
        }
    }

    /**
     * Alert Dialog with options of taking a picture, choosing from the gallery, and cancelling.
     */
    private void changeProfilePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Picture");
        builder.setPositiveButton("Take Picture", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If take picture is selected, call take photo function.
                takePhoto();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Choose From Gallery",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // If choose from gallery is selected, call select photo function.
                        selectPhotoFromGallery();
                        dialog.cancel();
                    }
                });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    /**
     * Take profile picture.
     */
    private void takePhoto() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Select a picture from the device gallery.
     */
    private void selectPhotoFromGallery() {
        Intent choosePicture = new Intent(Intent.ACTION_GET_CONTENT);
        choosePicture.setType("image/*");
        startActivityForResult(choosePicture, SELECT_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap = null;

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK
                && data != null && data.getExtras() != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            // Set the profile picture.
            profilePicture.setImageBitmap(bitmap);
        }

        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Set the profile picture.
        profilePicture.setImageBitmap(bitmap);
        // Upload the profile picture to Firebase.
        if (bitmap != null) {
            uploadProfilePicture(bitmap);
        } else {
            Toast.makeText(this, "Failed to upload image.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfilePicture(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        profilePictureReference.child(userId + ".jpeg").putBytes(byteArrayOutputStream
                .toByteArray()).addOnSuccessListener(
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the picture's URL.
                        getDownloadURL(profilePictureReference.child(userId + ".jpeg"));
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Display error message if upload fails.
                Toast.makeText(UserProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Get the profile picture's download URL.
     *
     * @param storageReference profile picture storage reference.
     */
    private void getDownloadURL(StorageReference storageReference) {
        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Set the profile image URL in the document.
                        userReference.update("profileImageURL", uri.toString());
                    }
                });
    }

    private void setUpDateRecyclerView() {
        // Initialize collection reference that contains dates.
        CollectionReference dateReference = userReference.collection("shared_workout");

        // Query the dates in descending order.
        Query dateQuery = dateReference.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<DateModel> options =
                new FirestoreRecyclerOptions.Builder<DateModel>()
                        .setQuery(dateQuery, DateModel.class).build();

        dateModelAdapter = new DateModelAdapter(options);
        RecyclerView dateRecyclerView = findViewById(R.id.listOfDates);
        dateRecyclerView.setHasFixedSize(true);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dateRecyclerView.setAdapter(dateModelAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        dateModelAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dateModelAdapter.stopListening();
    }

    public UserProfile getContext() {
        return UserProfile.this;
    }

    static public Context getmContext() {
        return mContext;
    }

}
