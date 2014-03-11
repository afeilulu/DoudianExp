package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by chen on 2/26/14.
 */
public class StreamProxy {

    private static final String TAG = "StreamProxy";
    private static StreamProxy instance = new StreamProxy();
    private Context context;
    private int statusCode = -1;
    private Process streamProxyProcess;

    public static StreamProxy getInstance(Context _context) {
        instance.context = _context;
        return instance;
    }

    public int getStatus() {
        return statusCode;
    }

    public void start() {

        boolean shouldRun = false;

        FileExtractor fe = new FileExtractor();
        switch (fe.extractFile(context, "streamproxy.conf", String.format("%s/streamproxy.conf", context.getFilesDir()))) {
            case 0:
                Log.d(TAG, "extract streamproxy.conf from asserts failed");
                break;
            case 1:
                Log.d(TAG, "extract streamproxy.conf from asserts successful");
                break;
            case 2:
                Log.d(TAG, "streamproxy.conf already exists");
                break;
        }

        final File f = context.getFilesDir();
        final String sp = String.format("%s/streamproxy", f);
        switch (fe.extractFile(context, "streamproxy", sp)) {
            case 0:
                Log.d(TAG, "extract streamproxy from asserts failed");
                break;
            case 1:
                Log.d(TAG, "extract streamproxy from asserts successful");
                shouldRun = true;
                break;
            case 2:
                Log.d(TAG, "streamproxy already exists");
                shouldRun = true;
                break;
        }

        if (shouldRun) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        streamProxyProcess = Runtime.getRuntime().exec(sp, null, f);
                        statusCode = 0;
                    } catch (IOException e) {
                        statusCode = -1;
                    }
                }
            }).start();
        }
    }

    public void stop() {
        if (streamProxyProcess != null){
            streamProxyProcess.destroy();
            statusCode = -1;
        }
    }

}
