package cn.com.xinli.android.doudian.ui.tv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.BaseActivity;

public class SearchActivity extends BaseActivity {
	private SearchFragment searchFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blank);
		searchFragment = (SearchFragment) getFragmentManager()
				.findFragmentByTag("search");
		if (searchFragment == null) {
			Bundle searchArg = new Bundle();
			searchArg.putInt("tabviewid", -1);
			searchFragment = new SearchFragment();
			searchFragment.setArguments(searchArg);
			TextView tv = (TextView) findViewById(R.id.tv_title);
			tv.setText(R.string.search_titile);
		}
		getFragmentManager().beginTransaction()
				.add(R.id.blank_continenet, searchFragment, "search").commit();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (searchFragment != null && intent != null
				&& intent.hasExtra("center")) {
//			searchFragment.Search(intent.getStringExtra("center"));
		}
	}
}
