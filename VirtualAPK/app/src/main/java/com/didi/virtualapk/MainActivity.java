package com.didi.virtualapk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.didi.virtualapk.internal.PluginContentResolver;
import com.didi.virtualapk.internal.LoadedPlugin;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE_STORAGE = 20171222;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView)findViewById(R.id.textView);
        String cpuArch;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            cpuArch = Build.SUPPORTED_ABIS[0];
        } else {
            cpuArch = Build.CPU_ABI;
        }
        textView.setText(cpuArch);
        Log.d("ryg", "onCreate cpu arch is "+ cpuArch);
        Log.d("ryg", "onCreate classloader is "+ getClassLoader());

        if (hasPermission()) {
            Log.d(TAG,"loadPlugin");

            this.loadPlugin(this);
        } else {
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PERMISSION_REQUEST_CODE_STORAGE == requestCode) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                requestPermission();
            } else {
                this.loadPlugin(this);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean hasPermission() {

        Log.d(TAG,"hasPermission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {

        Log.d(TAG,"requestPermission");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE_STORAGE);
        }
    }


    public void onButtonClick(View v) {
        if (v.getId() == R.id.button) {
            final String pkg = "com.didi.virtualapk.demo";
            if (PluginManager.getInstance(this).getLoadedPlugin(pkg) == null) {
                Toast.makeText(this, "plugin [com.didi.virtualapk.demo] not loaded", Toast.LENGTH_SHORT).show();
                return;
            }

            // test Activity and Service
            Intent intent = new Intent();
            intent.setClassName(this, "com.didi.virtualapk.demo.MainActivity");
            startActivity(intent);

            // test ContentProvider
            Uri bookUri = Uri.parse("content://com.didi.virtualapk.demo.book.provider/book");
            LoadedPlugin plugin = PluginManager.getInstance(this).getLoadedPlugin(pkg);
            bookUri = PluginContentResolver.wrapperUri(plugin, bookUri);

            Cursor bookCursor = getContentResolver().query(bookUri, new String[]{"_id", "name"}, null, null, null);
            while (bookCursor.moveToNext()) {
                int bookId = bookCursor.getInt(0);
                String bookName = bookCursor.getString(1);
                Log.d("ryg", "query book:" + bookId + ", " + bookName);
            }
            bookCursor.close();
        } else if (v.getId() == R.id.about) {
            showAbout();
        }
    }

    private void loadPlugin(Context base) {
        PluginManager pluginManager = PluginManager.getInstance(base);
        File apk = new File(Environment.getExternalStorageDirectory(), "Test.apk");
        if (apk.exists()) {
            try {
                pluginManager.loadPlugin(apk);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_detail);
        builder.setTitle("关于");
        builder.setNegativeButton("好的", null);
        builder.show();
    }
}
