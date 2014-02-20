
package be.norio.twunch.android.ui;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import be.norio.twunch.android.ui.fragment.SettingsFragment;

public class SettingsActivity extends BaseActivity
{

	public static void start(Context context)
	{
		context.startActivity(new Intent(context, SettingsActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
	}

}
