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
import java.lang.Process;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.verus.miner.VerusMiner;

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
        Log.e("test",homePath.getAbsolutePath());

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
        Button button = (Button)findViewById(R.id.button);

        if(readSettings(this) == null)
            saveSettings(threads.getText().toString() + '\n' + worker.getText().toString() + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(), this);

        String settings = readSettings(this);
        threads.setText(settings.split("\n")[1]);
        worker.setText(settings.split("\n")[2]);
        pool.setText(settings.split("\n")[3]);
        pass.setText(settings.split("\n")[4]);
        address.setText(settings.split("\n")[5]);

        TextView text = (TextView)findViewById(R.id.LOG);
        try {
            Scanner scanner = new Scanner( new File("/proc/cpuinfo") );
            String output = scanner.useDelimiter("\\A").next();
            scanner.close(); //
            String ver = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("/system/bin/uname -a ").getInputStream())).readLine();

            int count = 0;
            int pos = output.indexOf("aes");
            while (pos > -1) {
                ++count;
                pos = output.indexOf("aes", ++pos);
            }
            if(count > 0) {
                threads.setText(String.valueOf(count));
                text.setText("You have " + String.valueOf(count) +" avaliable threads...");
                Log.e("test", String.valueOf(count) + " threads");

            } else {
                threads.setText("None");
                text.setText("Your cpu doesnt have AES...");
                Log.e("test", "cpu doesnt have aes");
                button.setClickable(false);
            }
            if(!ver.contains("aarch64") && !ver.contains("armv8")){
                if(ver.contains("armv7")&& count > 0){
                    Log.e("test", "armv7l but realy armv8 ?");
                    text.setText("We are working on getting your cpu working...");
                    button.setClickable(false);
                } else if (count > 0){
                    text.setText("unknown cpu \nNum of AES: " + String.valueOf(count) + "/n" + ver );
                    Log.e("test", "unknown cpu \nNum of AES: " + String.valueOf(count) + "/n" + ver );
                    button.setClickable(false);
                }
            } else {
                if(count == 0){
                    text.setText("Wrong kernel version");
                    Log.e("test","Wrong kernel version" );
                    button.setClickable(false);
                }
            }

        }catch (Exception e){
            text.setText(e.toString());
            Log.e("test",e.toString());
        }
        text.setMovementMethod(new ScrollingMovementMethod());


    }
    public void mine(View view){
        TextView text = (TextView)findViewById(R.id.LOG);
        Button button = (Button)findViewById(R.id.button);
        if(mining){
            handler.removeCallbacks(textView);
            miner.stop();
            mining = false;
            text.setText("");
            text.scrollTo(0, 0);
            LOGAll = "";
            button.setText("Start");
        }else {
            EditText threads = (EditText)findViewById(R.id.threads);
            EditText worker = (EditText)findViewById(R.id.worker);
            EditText pool = (EditText)findViewById(R.id.pool);
            EditText pass = (EditText)findViewById(R.id.pass);
            EditText address = (EditText) findViewById(R.id.address);


            saveSettings(threads.getText().toString()  + '\n' + worker.getText().toString()  + '\n' + pool.getText().toString() + '\n' + pass.getText().toString() + '\n' + address.getText().toString(),this);
            CheckBox bench = (CheckBox)findViewById(R.id.bench);
            miner.mine(threads.getText().toString(),pass.getText().toString(),pool.getText().toString(),worker.getText().toString(),address.getText().toString(),bench.isChecked());
            handler.postDelayed(textView, 200);
            mining = true;
            button.setText("Stop");
        }

    }
    // Define the code block to be executed
    private Runnable textView = new Runnable() {
        @Override
        public void run() {
            Button button = (Button)findViewById(R.id.button);
            TextView text = (TextView)findViewById(R.id.LOG);
            String LOG = "";
            if(LOGAll.split("\n").length  > 100) {
                LOGAll = LOGAll.substring(LOGAll.indexOf('\n') + (LOGAll.split("\n").length - 100));
            }
            if (miner.errors != null) {
                LOG += miner.errors + miner.error() + miner.output();
                miner.stop();
                if(text.getScrollY() == text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()){
                    text.setText(LOG);
                    text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                } else {
                    text.setText(LOG);
                }
                mining = false;
                button.setText("Start");
                Log.e("test",LOG);
                handler.removeCallbacks(textView);

            }
            if (!miner.error().isEmpty()) {
                LOG += miner.error() + miner.output();
                miner.stop();
                if(text.getScrollY() == text.getLayout().getLineTop(text.getLineCount()) - text.getHeight()){
                    text.setText(LOG);
                    text.scrollTo(0, text.getLayout().getLineTop(text.getLineCount()) - text.getHeight());
                } else {
                    text.setText(LOG);
                }
                mining = false;
                button.setText("Start");
                Log.e("test",LOG);
                handler.removeCallbacks(textView);
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