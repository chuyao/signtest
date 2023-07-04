package top.way2free.testdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_reboot).setOnClickListener(this);
        findViewById(R.id.bt_upgrade).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_title)).setText("Version: " + getVersionName());
    }

    private String getVersionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }

    private void reboot() {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        sendBroadcast(intent);
    }

    private void upgrade() {
        try {
            File apk = loadApk();
            PackageManager packageManager = getPackageManager();
            Class<?> aClass = Class.forName("android.app.PackageInstallObserver");
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object observerInstance = constructor.newInstance();
            Method method = packageManager.getClass().getDeclaredMethod("installPackage",
                    Uri.class, aClass, int.class, String.class );
            method.setAccessible(true);
            Uri apkUri = Uri.fromFile(apk);
            method.invoke(packageManager, apkUri, observerInstance, 2, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void upgrade1() {
        try {
            File apkFile = loadApk();
            PackageInstaller installer = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_INHERIT_EXISTING);
            int sessionId = installer.createSession(params);
            PackageInstaller.Session session = installer.openSession(sessionId);
            FileInputStream inputStream = new FileInputStream(apkFile.getPath());
            OutputStream outputStream = session.openWrite("upgrade-session", 0, -1);
            byte[] buf = new byte[4096];
            int len;
            while((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            session.fsync(outputStream);
            outputStream.close();
            inputStream.close();
            session.commit(PendingIntent.getBroadcast(this, sessionId, new Intent("android.intent.action.MAIN"), 0).getIntentSender());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File loadApk() throws IOException {
        InputStream inputStream = getAssets().open("new.apk");
        File cacheFile = new File(getApplicationContext().getFilesDir(), "new.apk");
        OutputStream outputStream = new FileOutputStream(cacheFile);
        int len = 0;
        byte[] buf = new byte[4096];
        while((len = inputStream.read(buf)) != -1) {
            outputStream.write(buf, 0, len);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        return cacheFile;
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.bt_reboot) {
            reboot();
        } else if(view.getId() == R.id.bt_upgrade) {
            if ("1.1".equals(getVersionName())) {
                Toast.makeText(this, "当前已是最新版本，请卸载后再使用本功能", Toast.LENGTH_LONG).show();
            } else {
                view.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        upgrade1();
                    }
                }).start();
            }
        }
    }
}