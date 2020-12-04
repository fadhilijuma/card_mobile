package ke.co.lightspace.yetumobile.activity.newaccount;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ke.co.lightspace.yetumobile.R;
import ke.co.lightspace.yetumobile.activity.existing.UploadExisting;
import ke.co.lightspace.yetumobile.activity.main.MyBaseActivity;

public class NSignature extends MyBaseActivity {

    signature mSignature;
    LinearLayout mContent;
    Button clear, save;
    String PhoneNumber;
    String faceDetails;
    private Uri fileUri;
    String LastName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mysignature);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_actionbar);
        setSupportActionBar(toolbar);

        initToolbar();

        Intent input = getIntent();
        PhoneNumber = input.getStringExtra("Phone");
        faceDetails = input.getStringExtra("details");
        LastName = input.getStringExtra("LastName");

        save = (Button) findViewById(R.id.save);
        save.setEnabled(false);
        clear = (Button) findViewById(R.id.clear);
        mContent = (LinearLayout) findViewById(R.id.mysignature);

        mSignature = new signature(this, null);
        mContent.addView(mSignature);

        save.setOnClickListener(onButtonClick);
        clear.setOnClickListener(onButtonClick);

    }

    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == clear) {
                mSignature.clear();
            } else if (v == save) {
                mSignature.save();
            }
        }
    };

    public class signature extends View {
        static final float STROKE_WIDTH = 10f;
        static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        Paint paint = new Paint();
        Path path = new Path();

        float lastTouchX;
        float lastTouchY;
        final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear() {
            path.reset();
            invalidate();
            save.setEnabled(false);
        }

        public void save() {
            Bitmap returnedBitmap = Bitmap.createBitmap(mContent.getWidth(),
                    mContent.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            Drawable bgDrawable = mContent.getBackground();
            if (bgDrawable != null)
                bgDrawable.draw(canvas);
            else
                canvas.drawColor(Color.WHITE);
            mContent.draw(canvas);

            try {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                        PhoneNumber + "-AZXidimage.jpg");

                OutputStream fOut;

                fOut = new FileOutputStream(file);
                returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, fOut);

                fileUri = Uri.fromFile(file);

                startActivity(new Intent(NSignature.this, UploadActivity.class)
                        .putExtra("face", faceDetails)
                        .putExtra("ID", fileUri.getPath())
                        .putExtra("LastName", LastName)
                        .putExtra("isImage", true));

            }catch (Exception ex){
                Log.d("Exception",ex.getMessage());
            }


        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            save.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
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
