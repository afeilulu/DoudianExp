package cn.com.xinli.android.doudian.io;

import android.os.Parcel;
import android.os.Parcelable;

public class Recommend implements Parcelable{
	
	public Recommend(String title, 
			String icon, 
			String menuId, 
			String program, 
			String score,
			String quality,
			String poster) {
		this.title = title;
		this.icon = icon;
		this.menuId = menuId;
		this.program = program;
		this.score = score;
		this.quality = quality;
		this.poster = poster;
		
//		final File cacheDir = context.getCacheDir();
//        bitmapFile = new File(cacheDir, "r" + menuId + ".jpg");
	}
	
	public Recommend(Parcel in){
		readFromParcel(in);
	}
	
	private String title;
	private String icon;
	private String menuId;
	private String program;
	private String score;
	private String quality;
	private String poster;
	
//	private boolean isLoaded;
//	private File bitmapFile;
//	private Bitmap mBitmap;
	
	private void readFromParcel(Parcel in) {
		// We just need to read back each
		// field in the order that it was
		// written to the parcel
		title = in.readString();
		icon = in.readString();
		menuId = in.readString();
		program = in.readString();
		score = in.readString();
		quality = in.readString();
		poster = in.readString();
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// the sequence is same while readString()
		arg0.writeString(title);
		arg0.writeString(icon);
		arg0.writeString(menuId);
		arg0.writeString(program);
		arg0.writeString(score);
		arg0.writeString(quality);
		arg0.writeString(poster);
	}
	
	public static final Parcelable.Creator<Recommend> CREATOR = new Creator<Recommend>() {  
        public Recommend createFromParcel(Parcel source) {  
        	return new Recommend(source);
        }  
        public Recommend[] newArray(int size) {  
            return new Recommend[size];  
        }  
    };
    
	public String getTitle() {
		return title;
	}
	public String getIcon() {
		return icon;
	}
	public String getMenuId() {
		return menuId;
	}
	public String getProgram() {
		return program;
	}
	public String getScore(){
		return score;
	}
	public String getQuality(){
		return quality;
	}
	public String getPoster() {
		return poster;
	}
	
	/*public Bitmap getLargeBitmap(int optSize) {
        Bitmap b = null;
        try {
            b = new BitmapUtilsTask().execute(icon, "large",bitmapFile,optSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return b;

    }

    public void loadLargeBitmap() {
    	
        if (!isLoaded && !bitmapFile.exists()) {
            new BitmapUtilsTask().execute(icon, "load",bitmapFile);
            isLoaded = true;
        }
    }
    
    public void clear() {
        if (mBitmap != null)
            mBitmap.recycle();
        mBitmap = null;
    }*/

}
