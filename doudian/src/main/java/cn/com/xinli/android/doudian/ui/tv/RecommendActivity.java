package cn.com.xinli.android.doudian.ui.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.BaseActivity;

public class RecommendActivity extends BaseActivity {

	private final static String TAG = "RecommendActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blank);
		HomeTabFragment recommendFragment = (HomeTabFragment) getFragmentManager()
				.findFragmentByTag("recommend");
		
		TextView txt = (TextView) findViewById(R.id.tv_title);
		txt.setText(getIntent().getStringExtra("title"));
		
		if (recommendFragment == null) {
			recommendFragment = new HomeTabFragment();
			Bundle args = new Bundle();
			args.putString("id", getIntent().getStringExtra("id"));
			args.putInt("tabviewid", getIntent().getIntExtra("tabviewid",0));
			recommendFragment.setArguments(args);
		}
		getFragmentManager().beginTransaction()
				.add(R.id.blank_continenet, recommendFragment, "recommend")
				.disallowAddToBackStack()
				.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			Intent search = new Intent(this, SearchActivity.class);
			startActivity(search);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
}
