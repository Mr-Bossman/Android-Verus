package com.verus.miner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.os.*;
import java.io.*;

import com.verus.miner.VerusMiner;

/*
how long will it run b4 i have to much data in the logall variable
bench mark doesnt work idk y
 */
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    VerusMiner miner;
    Handler handler = new Handler();
    boolean mining = false;
    String LOGAll = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        File homePath = MainActivity.this.getFilesDir();
        miner = new VerusMiner(homePath,this);
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.INTERNET,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        EditText threads = (EditText) findViewById(R.id.threads);
        EditText worker = (EditText) findViewById(R.id.worker);
        EditText address = (EditText) findViewById(R.id.address);
        EditText pool = (EditText) findViewById(R.id.pool);
        EditText pass = (EditText) findViewById(R.id.pass);

        if(readSettings(this) == null)
            saveSettings(threads.getText().toString() + '\n' + worker.getText().toString() + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(), this);

        String settings = readSettings(this);
        threads.setText(settings.split("\n")[1]);
        worker.setText(settings.split("\n")[2]);
        pool.setText(settings.split("\n")[3]);
        pass.setText(settings.split("\n")[4]);
        address.setText(settings.split("\n")[5]);

        TextView text = (TextView)findViewById(R.id.LOG);

        text.setMovementMethod(new ScrollingMovementMethod());


    }
    public void mine(View view){
        TextView text = (TextView)findViewById(R.id.LOG);

        if(mining){
            handler.removeCallbacks(textView);
            miner.stop();
            mining = false;
            text.setText("");
            text.scrollTo(0, 0);
            LOGAll = "";
        }else {
            EditText threads = (EditText)findViewById(R.id.threads);
            EditText worker = (EditText)findViewById(R.id.worker);
            EditText pool = (EditText)findViewById(R.id.pool);
            EditText pass = (EditText)findViewById(R.id.pass);
            EditText address = (EditText) findViewById(R.id.address);


            saveSettings(threads.getText().toString()  + '\n' + worker.getText().toString()  + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(),this);

            CheckBox bench = (CheckBox)findViewById(R.id.bench);
            miner.mine(threads.getText().toString(),pass.getText().toString(),pool.getText().toString(),worker.getText().toString(),bench.isChecked());
            handler.postDelayed(textView, 200);
            mining = true;

        }

    }
    // Define the code block to be executed
    private Runnable textView = new Runnable() {
        @Override
        public void run() {
            TextView text = (TextView)findViewById(R.id.LOG);
            String LOG = "";
            if(LOGAll.split("\n").length  > 100) {
                LOGAll = LOGAll.substring(LOGAll.indexOf('\n') + (LOGAll.split("\n").length - 100));
            }
            if (miner.errors != null) {
                LOG += miner.errors;
                miner.stop();
                if(text.getScrollY() == text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()){
                    text.setText(LOG);
                    text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                } else {
                    text.setText(LOG);
                }
                Log.e("test",LOG);
            } else if (!miner.error().isEmpty()) {
                miner.stop();
                LOG += miner.error();
                if(text.getScrollY() == text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()){
                    text.setText(LOG);
                    text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                } else {
                    text.setText(LOG);

                }
                Log.e("test",LOG);
            } else {
                LOG += miner.output();
                LOGAll += LOG;
                Log.e("test",LOG);
                if(text.getScrollY() >= (text.getLayout().getLineTop(text.getLineCount()) - text.getHeight())-2){
                    text.setText(LOGAll);
                    text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                } else {
                    text.setText(LOGAll);

                }
                handler.postDelayed(this, 200);
            }
        }
    };
    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void saveSettings(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String readSettings(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            return null;
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}