package ke.co.lightspace.yetumobile.activity.main;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

import justtide.ThermalPrinter;
import ke.co.lightspace.yetumobile.R;


public class PRINTER_Service extends IntentService {
    private ThermalPrinter thermalPrinter;
    private static final String TAG = PRINTER_Service.class.getSimpleName();
    public PRINTER_Service() {
        super(PRINTER_Service.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            String SMS = intent.getStringExtra("SMS");
            Log.e(TAG, "Printer SMS: " + SMS);

            if (SMS.contains("WDP")) {


                String SMS_Break = SMS.replace("WDP", "");

                String smsParts[] = SMS_Break.split("#");
                String customerName = smsParts[0];
                String cash = smsParts[1];
                DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
                double withdraw = Double.parseDouble(cash);

                String AccountNumber = smsParts[2];
                String Agent = smsParts[3];
                printLogo();
                printReceipt(customerName, dFormat.format(withdraw), AccountNumber, Agent);
                printBarCode(dFormat.format(withdraw));
                printSpace();

                printLogo();
                printReceipt(customerName, cash, AccountNumber, Agent);
                printBarCode(cash);
                printSpace();

            }

        }

    }

    private int printReceipt(String AccountNames, String Amount, String Account, String Agent) {

        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.setHeightAndLeft(0, 0);
        this.thermalPrinter.setLineSpacing(5);
        this.thermalPrinter.getFontCH();
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" WAKALA: " + Agent + "\n\n\n");
        this.thermalPrinter.print("===========KUTOA PESA==========\n\n");
        this.thermalPrinter.print("TAREHE: " + DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "\n\n");
        this.thermalPrinter.print("AKAUNTI: " + Account + "\n\n");
        this.thermalPrinter.print("JINA: " + AccountNames + "\n\n");
        this.thermalPrinter.print("MAELEZO: " + "KUTOA PESA" + "\n\n\n");
        this.thermalPrinter.print("KIASI: TSHS." + Amount + "\n\n\n\n");
        this.thermalPrinter.print("SAHIHI:-------------------------"+"\n\n\n");
        this.thermalPrinter.print("      Asante kwa kubenki nasi.\n\n");
        this.thermalPrinter.print("          Mucoba Bank Plc\n");
        this.thermalPrinter.print("           Benki yako,\n");
        this.thermalPrinter.print("       Kwa maendeleo yako.\n\n\n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
    }

    private int printLogo() {
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.printLogo(0, 1, BitmapFactory.decodeResource(getResources(), R.mipmap.mucobas));
        this.thermalPrinter.setStep(20);
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
    }

    private int printBarCode(String Amount) {
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.printLogo(0, 1, barcodeGenerator("W" + Amount));
        this.thermalPrinter.setStep(20);
        this.thermalPrinter.printStart();
        this.thermalPrinter.print("================================\n");
        return this.thermalPrinter.waitForPrintFinish();
    }

    private Bitmap barcodeGenerator(String data) {
        Bitmap ImageBitmap = null;
        try {
            MultiFormatWriter writer = new MultiFormatWriter();

            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            ImageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int i = 0; i < width; i++) {//width
                for (int j = 0; j < height; j++) {//height
                    ImageBitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ImageBitmap;
    }

    private int printSpace() {
        this.thermalPrinter = ThermalPrinter.getInstance();
        this.thermalPrinter.initBuffer();
        this.thermalPrinter.setGray(7);
        this.thermalPrinter.setHeightAndLeft(0, 0);
        this.thermalPrinter.setLineSpacing(5);
        this.thermalPrinter.getFontCH();
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.shiftRight(60);
        this.thermalPrinter.setStep(4);
        this.thermalPrinter.setFont(6, 1);
        this.thermalPrinter.print(" \n");
        this.thermalPrinter.printStart();
        return this.thermalPrinter.waitForPrintFinish();
    }

}
