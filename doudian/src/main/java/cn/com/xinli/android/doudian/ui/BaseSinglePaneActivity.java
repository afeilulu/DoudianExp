package cn.com.xinli.android.doudian.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import cn.com.xinli.android.doudian.R;

public abstract class BaseSinglePaneActivity extends BaseActivity {
    private Fragment mFragment;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlepane_empty);

        //get customeTitle from intent
        final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);

        if (savedInstanceState == null) {
        	
            //returned by sub-class
        	mFragment = onCreatePane();
        	
        	if (mFragment !=null){
        	
	        	//set Fragment arguments,it will be gotten in Fragment
	        	//getIntent return the intent that start this activity
	            mFragment.setArguments(intentToFragmentArguments(getIntent()));
	
	            getFragmentManager().beginTransaction()
	                    .add(R.id.root_container, mFragment,getFragmentTag())
	                    .commit();
            
        	}
            
        }
    }

    protected abstract Fragment onCreatePane();
    
    protected abstract String getFragmentTag();
}
