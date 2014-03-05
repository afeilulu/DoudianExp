package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FileExtractor {
	public static final String TAG = "FileExtractor";

	public int extractFile(Context ctx, String assetFile, String saveToFile)
	{
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
	    Log.d(TAG, "copyStream("+ in + " ," + out+ ")");
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
};
