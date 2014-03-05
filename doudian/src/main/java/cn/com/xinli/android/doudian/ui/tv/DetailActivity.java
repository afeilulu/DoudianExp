package cn.com.xinli.android.doudian.ui.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.BaseActivity;

public class DetailActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blank);
		DetailFragment detailFragment = (DetailFragment) getFragmentManager()
				.findFragmentByTag("detail");
		if (detailFragment == null) {
			detailFragment = new DetailFragment();
		}
        detailFragment.setArguments(intentToFragmentArguments(getIntent()));
		getFragmentManager().beginTransaction()
				.add(R.id.blank_continenet, detailFragment, "detail").commit();
	}

	public void setTitle(String title) {
		TextView tv = (TextView) findViewById(R.id.tv_title);
		tv.setText(title);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			Intent search = new Intent(this, SearchActivity.class);
			search.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(search);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		setResult(resultCode, data);
	}
}
