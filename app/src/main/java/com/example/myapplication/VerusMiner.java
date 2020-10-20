package com.example.myapplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.example.myapplication.Commander.Command;


public class VerusMiner{
    public String errors = null;
    String homePath;
    Command cmd;
    public VerusMiner(File Path,File dl){
        try {
            File path = dl;
            homePath = Path.getAbsolutePath();
            copy(new File(path, "ccminer"), new File(homePath , "/ccminer"));
            copy(new File(path, "libcrypto.so.1.1"), new File(homePath , "/libcrypto.so.1.1"));
            copy(new File(path, "libssl.so.1.1"), new File(homePath , "/libssl.so.1.1"));
            copy(new File(path, "libz.so.1"), new File(homePath , "/libz.so.1"));
            copy(new File(path, "libc++_shared.so"), new File(homePath ,"/libc++_shared.so"));
        } catch (IOException e) {
            errors = e.toString();
        }
    }

    void mine(String threads,String pass,String pool,String worker,boolean bench) {
        try {
            if(bench)
                cmd = new Command("./ccminer" ,"-a" , "verus","--benchmark","-t",threads);
            else
                cmd = new Command("./ccminer" ,"-a" , "verus","-t",threads,"-p", pass,"-o" ,pool,"-u", worker);

            cmd.setWorkingDirectory(homePath);
            cmd.setEnviron("LD_LIBRARY_PATH",homePath);
            cmd.start();
        } catch (Exception e) {
            errors = e.toString();
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


    private static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
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
}
