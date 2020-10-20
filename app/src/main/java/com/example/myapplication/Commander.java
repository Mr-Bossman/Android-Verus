package com.example.myapplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class Commander {

    private Commander() {
    }


    static private class OutputReaderThread extends Thread {

        private InputStream inputStream;
        StringBuilder output = new StringBuilder();
        private BufferedReader reader;

        OutputReaderThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                        output.capacity();
                        output.append(line).append(System.lineSeparator());
                    if (currentThread().isInterrupted()) return;
                }
            } catch (IOException e) {
            }
        }

        public StringBuilder getOutput() {
            return output;
        }

    }


    public static class Command {

        ProcessBuilder processBuilder = new ProcessBuilder();
        Process process;
        private OutputReaderThread outputHandler;
        private OutputReaderThread errorHandler;


        Map<String, String> environment = loadEnvironment();

        String workingDirectory = ".";

        Map<String, String> loadEnvironment() {
            ProcessBuilder x = new ProcessBuilder();
            return x.environment();
        }

        public void resetEnvironment() {
            environment = loadEnvironment();
            workingDirectory = ".";
        }

        public void loadEnvirons(HashMap input) {
            environment.putAll(input);
        }

        public String getEnviron(String name) {
            return environment.get(name);
        }

        public void setEnviron(String name, String value) {
            environment.put(name, value);
        }

        public boolean clearEnviron(String name) {
            return environment.remove(name) != null;
        }

        public void setWorkingDirectory(String path) {
            workingDirectory = path;
        }

        public String getWorkingDirectory() {
            return workingDirectory;
        }
        public Command(String... parameters) {
            processBuilder.command(parameters);
        }

        public int start()throws IOException {
            processBuilder.environment().putAll(environment);
            processBuilder.directory(new File(workingDirectory));
            // start the process
            process = processBuilder.start();
            if(process == null) return -1;
            // start the error reader
            outputHandler = new Commander.OutputReaderThread(process.getInputStream());
            errorHandler = new Commander.OutputReaderThread(process.getErrorStream());

            outputHandler.start();
            return 0;
        }
        public String StdOut(){
            String ret = outputHandler.getOutput().toString();
            outputHandler.output.setLength(0);
            return ret;
        }
        public String StdErr(){
            String ret = errorHandler.getOutput().toString();
            errorHandler.output.setLength(0);
            return ret;        }
        public void end(){
            if(process != null) process.destroyForcibly();
        }
    }
}