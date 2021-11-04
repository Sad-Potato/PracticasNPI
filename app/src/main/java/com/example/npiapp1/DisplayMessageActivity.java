package com.example.npiapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;


public class DisplayMessageActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    InputImage image;
    private SensorManager sensorManager;
    private List<Sensor> deviceSensors;

    BarcodeScannerOptions options =
            new BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE)
                    .build();


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            System.out.println("Error");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Despues de echar la foto obtenemos la imagen en forma de bitmap
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            // Con esto lo único que hacemos es mostrar la imagen (thumnail) en el
            // widget
            ImageView imageView=findViewById(R.id.imageTest);
            imageView.setImageBitmap(imageBitmap);
            // Pasamos el bitmap a un formato InputImage para
            // usarla con barcode scanning
            int rotationDegree = 0;
            image=InputImage.fromBitmap(imageBitmap, rotationDegree);
            // Escaneamos con los parametros que hemos definido
            BarcodeScanner scanner = BarcodeScanning.getClient(options);

            TextView textView = findViewById(R.id.textView);

            // Procesamos el qr
            Task<List<Barcode>> result = scanner.process(image)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // Task completed successfully
                            // [START_EXCLUDE]
                            // [START get_barcodes]
                            for (Barcode barcode: barcodes) {
                                Rect bounds = barcode.getBoundingBox();
                                Point[] corners = barcode.getCornerPoints();

                                String rawValue = barcode.getRawValue();

                                int valueType = barcode.getValueType();
                                // See API reference for complete list of supported types
                                switch (valueType){
                                    case Barcode.TYPE_WIFI:
                                        String ssid = barcode.getWifi().getSsid();
                                        String password = barcode.getWifi().getPassword();
                                        int type = barcode.getWifi().getEncryptionType();
                                    break;
                                    case Barcode.TYPE_URL:
                                        String title = barcode.getUrl().getTitle();
                                        String url = barcode.getUrl().getUrl();
                                        textView.setText(url);
                                    break;
                                    default:
                                        String text=barcode.getRawValue();
                                        textView.setText(text);
                                    break;
                                }
                            }
                            // [END get_barcodes]
                            // [END_EXCLUDE]
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        }
        else{
            IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            TextView textView = findViewById(R.id.textView3);
            textView.setText(intentResult.getContents() + " " + intentResult.getFormatName());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        //TextView textView = findViewById(R.id.textView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

    }

    public void qr_android(View view){
        dispatchTakePictureIntent();
    }

    public void qr_zxing(View view){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.initiateScan();
    }

}