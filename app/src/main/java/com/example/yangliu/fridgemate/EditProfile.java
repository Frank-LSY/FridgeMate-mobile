package com.example.yangliu.fridgemate;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cocosw.bottomsheet.BottomSheet;
import com.example.yangliu.fridgemate.authentication.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.yangliu.fridgemate.R.id.local;

public class EditProfile extends AppCompatActivity {

    private ConstraintLayout mEditFormView;
    private ProgressBar mProgressView;


    private EditText name;
    private CircleImageView profilePhoto;
    private TextView email;
    private Button saveBtn;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // Return to login screen if cannot verify user identity
        if(user == null){
            Toast.makeText(getApplication(), R.string.error_load_data,
                    Toast.LENGTH_LONG).show();
            mAuth.signOut();

            Intent i = new Intent(EditProfile.this, LoginActivity.class);
            startActivity(i);
            finish();
        }

        mEditFormView = findViewById(R.id.edit_profile_form);
        mProgressView = findViewById(R.id.progress);

        saveBtn = findViewById(R.id.save_user_profile);
        name = findViewById(R.id.user_name);
        profilePhoto = findViewById(R.id.profile_image);
        email = findViewById(R.id.email);

        name.setInputType(InputType.TYPE_CLASS_TEXT);

        // TODO:: DATABASE get profile pic
        name.setText(user.getDisplayName());
//        profilePhoto.setImageBitmap();
        email.setText(user.getEmail());

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showProgress(true);

                String displayName = String.valueOf(name.getText());
                if (profilePhoto.getDrawingCache() != null) {
                    Bitmap profileImg  = Bitmap.createScaledBitmap(profilePhoto.getDrawingCache(),
                        200,200, true);
                }

                // TODO:: DATABASE save profile picture, implement change email
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplication(),R.string.profile_updated,
                                        Toast.LENGTH_LONG).show();
                                }

                                Intent i = new Intent(EditProfile.this, MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
            }
        });
    }


        public void selectPhoto(View view) {

            new BottomSheet.Builder(this).title("Options").sheet(R.menu.menu_profile_photo).listener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case R.id.local://从相册里面取照片
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryIntent.setType("image/*");
                            startActivityForResult(galleryIntent, LOAD_IMAGE_REQUEST);
                            break;
                        case R.id.camera://调用相机拍照
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (checkSelfPermission(android.Manifest.permission.CAMERA)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(new String[]{Manifest.permission.CAMERA},
                                            MY_CAMERA_PERMISSION_CODE);
                                } else {
                                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                                }
                            }
                            break;

                    }
                }
            }).show();
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.delete_account:
                // TODO:: DATABASE: deactivate user account
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // photo editing
    private static String path = "";//sd路径
    private Bitmap head;
    private static final int CAMERA_REQUEST = 1888;
    private static final int LOAD_IMAGE_REQUEST = 1889;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO:: better fetching the photo
        switch  (requestCode) {
            //从相册里面取相片的返回结果
            case LOAD_IMAGE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    profilePhoto.setImageURI(selectedImageUri);
                }
                break;

            case CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    // TODO
                    Matrix matrix = new Matrix();
                    // clockwise by 90 degrees becaues some weird issue on emulator
                    matrix.postRotate(90);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(photo  , 0, 0, photo.getWidth(), photo  .getHeight(), matrix, true);
                    profilePhoto.setImageBitmap(rotatedBitmap);
                }

                break;
            default:
                Toast.makeText(getApplicationContext(), "You have not selected and image", Toast.LENGTH_SHORT).show();

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mEditFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mEditFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
