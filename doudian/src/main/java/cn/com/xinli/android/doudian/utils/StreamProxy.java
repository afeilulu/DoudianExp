package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamProxy {

    private static final String TAG = "StreamProxy";
    private final static int READ_STATUS = 1;
    public int mPid;
    public String mHost;
    private OnStateListener mListener;
    private Handler mHandler;
    private boolean mShouldRun;
    private Process mProcess;
    private File mFilesDir;
    private InputStream mReader;
    private byte[] mBuffer = new byte[512];
    private int mCursor;
    private boolean mStopByUser;
    private File mProcessDir;

    public StreamProxy(Context ctx, OnStateListener listener) {
        mListener = listener;
        mCursor = 0;
        mPid = 0;
        mStopByUser = false;
        mShouldRun = extractStreamProxy(ctx);
        setOnStateListener(listener);
    }

    public void setOnStateListener(OnStateListener listener) {
        mListener = listener;
        if (mListener == null) return;

        if (mHandler == null) mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case READ_STATUS:
                        if (readStatus()) {
                            //Log.d(TAG, "readStatus go on");
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(READ_STATUS), 1000);
                        } else {
                            //Log.d(TAG, "readStatus stopped");
                        }
                        break;
                }
            }
        };
    }

    public void start() {
        if (mShouldRun) {
            mStopByUser = false;
            try {
                Log.d(TAG, "try to run streamproxy");
                mProcess = Runtime.getRuntime().exec(String.format("%s/streamproxy", mFilesDir), null, mFilesDir);
                mReader = mProcess.getInputStream();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(READ_STATUS), 200);

            } catch (IOException e) {
                e.printStackTrace();
                if (mListener != null) {
                    mListener.onStarted(false, 4);
                }
                mReader = null;
                mProcess = null;
                if (mHandler != null) mHandler.removeMessages(READ_STATUS);
            }
        } else if (mListener != null) {
            mListener.onStarted(false, 2);
        }
    }

    public void stop() {
        mStopByUser = true;
        if (mProcess != null) {
            mProcess.destroy();
        }
        if (mHandler != null) mHandler.removeMessages(READ_STATUS);
    }

    public boolean ready() {
        return mHost != null;
    }

    private int extractFile(Context ctx, String assetFile, String saveToFile) {
        Log.d(TAG, "extract Assets (" + assetFile + " -> " + saveToFile);
        File outputFile = new File(saveToFile);

        if (outputFile.exists()) {
            return 2;
        }

        OutputStream out;

        try {
            out = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return 0;
        }

        int copyResult = 0;

        InputStream in = null;

        try {
            in = ctx.getAssets().open(assetFile);
            copyResult = copyStream(in, out);
            Log.d(TAG, "copy " + assetFile + " - " + copyResult);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            // close input stream(file)
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {

            }
        }
        // close output stream (file)

        try {
            if (out != null)
                out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // add file execute permission
        File fs = new File(saveToFile);
        try {
            fs.setExecutable(true, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "extractFile return " + copyResult);
        return copyResult;
    }

    private int copyStream(InputStream in, OutputStream out) {
        Log.d(TAG, "copyStream(" + in + " ," + out + ")");
        try {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0)
                out.write(buf, 0, len);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    private boolean extractStreamProxy(Context ctx) {
        int ret = 0;
        mFilesDir = ctx.getFilesDir();
        String sp = String.format("%s/streamproxy", mFilesDir);
        switch (ret = extractFile(ctx, "streamproxy", sp)) {
            case 0:
                Log.d(TAG, "extract streamproxy from asserts failed");
                break;
            case 1:
                Log.d(TAG, "extract streamproxy from asserts successful");
                break;
            case 2:
                Log.d(TAG, "streamproxy already exists");
                break;
        }
        switch (extractFile(ctx, "streamproxy.conf", String.format("%s/streamproxy.conf", mFilesDir))) {
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

        return ret != 0;
    }

    private boolean parseStatusLine(String line) {
        boolean bGoOn = true;
        if (line.startsWith("pid ")) { /* pid 123 port 123 */

            String[] tags = line.split("\\s+");
            if (tags.length >= 4) {
                mPid = Integer.valueOf(tags[1]);
                mHost = tags[3];
                mProcessDir = new File(String.format("/proc/%d", mPid));
            }
            Log.d(TAG, "start streamproxy success, got pid - " + mPid + " host - " + mHost);

            if (mListener != null) mListener.onStarted(true, mPid);
        } else if (line.startsWith("exit ")) {
            mPid = 0;
            mHost = null;
            mProcessDir = null;
            bGoOn = false;
            if (line.startsWith("exit by listen failed")) {
                Log.d(TAG, "start streamproxy failed because server port was used");
                if (mListener != null) mListener.onStarted(false, 3);
            } else if (line.startsWith("exit by error")) {
                Log.d(TAG, "streamproxy crashed, restart it now");
                start();
            } else if (line.startsWith("exit by signal")) {
                Log.d(TAG, "streamproxy was killed by other, restart");
                if (mStopByUser) {
                    if (mListener != null) mListener.onExit(true);
                } else {
                    start();
                }

            }
        } else {
            Log.d(TAG, "status line - " + line);
            if (mListener != null) mListener.onProxyStatus(line);
        }

        return bGoOn;
    }

    private boolean readStatus() {
        boolean bGoOn = false;
        try {
            int can_read = mReader.available();
            if (can_read == 0) {
                //Log.d(TAG, "reader not ready");
                if (mProcessDir != null && !mProcessDir.exists()) {
                    start();
                    return false;
                }
                return true;
            }
            int capacity = mBuffer.length - mCursor;
            if (capacity == 0) {
                Log.d(TAG, "too long status line(exceed 512bytes), skip");
                mCursor = 0;
                capacity = mBuffer.length;
            }
            if (can_read > capacity) can_read = capacity;
            int n = mReader.read(mBuffer, mCursor, can_read);
            //Log.d(TAG, "read " + n + "bytes");
            if (n > 0) {
                mCursor += n;
                int newline_start = 0;
                int i = 0;
                for (; i < mCursor; i++) {
                    if (mBuffer[i] == '\n') {
                        String line = new String(mBuffer, newline_start, i - newline_start);
                        //Log.d(TAG, "got one line - " + line);
                        bGoOn = parseStatusLine(line);
                        newline_start = i + 1;
                        if (!bGoOn) {
                            mCursor = 0;
                            return false;
                        }
                    }
                }
                if (newline_start == mCursor) {
                    mCursor = 0;
                } else {
                    /* compact */
                    for (i = 0; newline_start < mCursor; i++, newline_start++) {
                        mBuffer[i] = mBuffer[newline_start];
                    }
                    mCursor = i;
                }
            }
            bGoOn = true;
        } catch (IOException e) {
            Log.d(TAG, "read streamproxy failed, check streamproxy alive");
            if (mProcessDir != null && !mProcessDir.exists()) {
                start();
            }
            e.printStackTrace();
            bGoOn = false;
        }

        return bGoOn;
    }

    public interface OnStateListener {
        /*
         * success = true, extra is the server pid
         * success = false, extra indicates the error
         *     2 - file not found, 3 - listen failed, 4 - could not execute
         */
        public void onStarted(boolean success, int extra);

        public void onExit(boolean normal);

        public void onProxyStatus(String statusLine);
    }
}