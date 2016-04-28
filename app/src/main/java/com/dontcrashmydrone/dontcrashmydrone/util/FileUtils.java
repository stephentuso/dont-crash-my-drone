package com.dontcrashmydrone.dontcrashmydrone.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.telecom.Call;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by stephentuso on 4/27/16.
 */
public class FileUtils {

    private Context context;

    public FileUtils(Context context) {
        this.context = context;
    }

    public File getFolder(String folderName) {
        if (folderName == null || folderName.isEmpty()) {
            return context.getFilesDir();
        }
        File folder = context.getDir(folderName, Context.MODE_PRIVATE);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

    public File getFile(String folder, String fileName) {
        File file = new File(getFolder(folder).toString(), fileName);
        return file;
    }

    public void copyAssetToFile(String assetPath, File outputFile, @Nullable Callback callback) {
        try {
            FileWriteTask task = new FileWriteTask(context.getAssets().open(assetPath), new FileOutputStream(outputFile), callback);
            task.execute();
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null)
                callback.onError();
        }
    }

    public interface Callback {
        void onComplete();
        void onError();
    }

    private static class FileWriteTask extends AsyncTask<String, Integer, String> {

        InputStream inputStream;
        OutputStream outputStream;
        Callback callback;

        private boolean errorOccurred = false;

        public FileWriteTask(InputStream input, OutputStream output, Callback callback) {
            this.inputStream = input;
            this.outputStream = output;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                int length = 0;
                byte[] buffer = new byte[64*1024]; //Read 64 kb at a time
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                errorOccurred = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (callback == null)
                return;
            if (errorOccurred) {
                callback.onError();
            } else {
                callback.onComplete();
            }
        }
    }

}
