package be.norio.twunch.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import be.norio.twunch.android.AboutActivity;
import be.norio.twunch.android.R;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.iosched.util.GoogleAnalyticsSessionManager;

public abstract class BaseActivity extends FragmentActivity {

	protected final String GA_VAR_KLANTID = "KLANTID";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
		GoogleAnalyticsTracker.getInstance().trackPageView(getPageName());
		if (!(this instanceof TwunchListActivity)) {
			getSupportActionBar().setHomeButtonEnabled(true);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GoogleAnalyticsTracker.getInstance().dispatch();
		GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();
	}

	protected String getPageName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		case R.id.menuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void goHome() {
		Intent intent = new Intent(this, TwunchListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}