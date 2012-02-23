package be.norio.twunch.android.ui;

import android.os.Bundle;
import be.norio.twunch.android.util.FragmentUtils;

public class TwunchListActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			TwunchListFragment fragment = new TwunchListFragment();
			fragment.setArguments(FragmentUtils.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
		}
	}

}
