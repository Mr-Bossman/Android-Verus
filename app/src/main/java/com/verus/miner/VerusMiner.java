package com.verus.miner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.verus.miner.Commander.Command;

import android.content.Context;
import android.content.res.Resources;


public class VerusMiner{
    public String errors = null;
    String homePath;
    Command cmd;
    private Context context;

    public VerusMiner(File Path,Context current){
        this.context = current;

        try {
            homePath = Path.getAbsolutePath();
            copy(getResources().openRawResource(getResources().getIdentifier("ccminer","raw", getPackageName())), new File(homePath , "/ccminer"));
            copy(getResources().openRawResource(getResources().getIdentifier("libcpp","raw", getPackageName())), new File(homePath , "/libcrypto.so.1.1"));
            copy(getResources().openRawResource(getResources().getIdentifier("libcrypto","raw", getPackageName())), new File(homePath , "/libssl.so.1.1"));
            copy(getResources().openRawResource(getResources().getIdentifier("libssl","raw", getPackageName())), new File(homePath , "/libz.so.1"));
            copy(getResources().openRawResource(getResources().getIdentifier("libz","raw", getPackageName())), new File(homePath ,"/libc++_shared.so"));

        } catch (Exception e) {
            errors = e.toString();
        }
    }

    private String getPackageName() {
        return context.getPackageName();
    }

    private Resources getResources() {
        return context.getResources();
    }

    void mine(String threads,String pass,String pool,String worker,boolean bench) {
        try {
            Runtime.getRuntime().exec("/system/bin/chmod 777 " + homePath + "/ccminer");
            if(bench)
                cmd = new Command("./ccminer" ,"-a" , "verus","--benchmark","-t",threads);
            else
                cmd = new Command("./ccminer" ,"-a" , "verus","-t",threads,"-p", pass,"-o" ,pool,"-u", worker);

            cmd.setWorkingDirectory(homePath);
            cmd.setEnviron("LD_LIBRARY_PATH",homePath);
            cmd.start();
        }catch (Exception e) {
            errors = e.toString() + "try restarting the app... or google removed this feture";
        }
    }
    void stop(){
        cmd.end();
    }
    public String output(){
        return cmd.StdOut();
    }
    public String error(){
        return cmd.StdErr();
    }


    private static void copy(InputStream in, File dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst)) {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        }

    }
}
