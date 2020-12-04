package ke.co.lightspace.yetumobile.activity.children;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;

public class ChildFatherCombined extends MyBaseActivity {
    private static final int CAMERA_REQUEST = 1888;
    @BindView(R.id.person_photo)
    ImageView imageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.save)
    Button save;
    private Uri fileUri;
    String details = null;
    String PhoneNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_combined);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        initToolbar();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String FatherImage = prefs.getString("FatherImage", null);
        String ChildImage = prefs.getString("ChildImage", null);
        details = prefs.getString("customer_details", null);
        PhoneNumber = prefs.getString("custPhone", null);

        byte[] FatherDecodedString = Base64.decode(FatherImage, Base64.DEFAULT);
        Bitmap FatherDecodedByte = BitmapFactory.decodeByteArray(FatherDecodedString, 0, FatherDecodedString.length);

        byte[] ChildDecodedString = Base64.decode(ChildImage, Base64.DEFAULT);
        Bitmap ChidDecodedByte = BitmapFactory.decodeByteArray(ChildDecodedString, 0, ChildDecodedString.length);

        Bitmap combinedBits = combineImages(FatherDecodedByte, ChidDecodedByte);

        imageView.setImageBitmap(combinedBits);
        storeImage(combinedBits);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageView.getDrawable() == null) {
                    Toast.makeText(getApplicationContext(), "Please Take Image", Toast.LENGTH_SHORT).show();
                } else {

                    startActivity(new Intent(ChildFatherCombined.this, CSignature.class)
                            .putExtra("details", fileUri.getPath())
                            .putExtra("PhoneNumber", PhoneNumber));
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {

            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            imageView.setImageBitmap(bitmap);

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;

        int width, height = 0;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth();
            height = c.getHeight() + s.getHeight();
        } else {
            width = s.getWidth();
            height = c.getHeight() + s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);


        return cs;
    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("Signature",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Signature", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Signature", "Error accessing file: " + e.getMessage());
        }
    }

    private File getOutputMediaFile() {

        File mediaFile;
        String mImageName = details + "--child.jpg";
        mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mImageName);
        fileUri = Uri.fromFile(mediaFile);
        return mediaFile;
    }
    private void initToolbar() {
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
