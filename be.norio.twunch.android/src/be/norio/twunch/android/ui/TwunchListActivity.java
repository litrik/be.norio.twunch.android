package be.norio.twunch.android.ui;

import android.os.Bundle;

public class TwunchListActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			TwunchListFragment list = new TwunchListFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, list).commit();
		}
	}

}
