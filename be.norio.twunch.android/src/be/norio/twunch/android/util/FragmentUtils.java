package be.norio.twunch.android.util;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class FragmentUtils {

	public static final String ARGUMENT_URI = "_uri";

	/**
	 * Converts an intent into a {@link Bundle} suitable for use as fragment
	 * arguments.
	 */
	public static Bundle intentToFragmentArguments(Intent intent) {
		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable(ARGUMENT_URI, data);
		}

		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}

		return arguments;
	}

	/**
	 * Converts a fragment arguments bundle into an intent.
	 */
	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}

		final Uri data = arguments.getParcelable(ARGUMENT_URI);
		if (data != null) {
			intent.setData(data);
		}

		intent.putExtras(arguments);
		intent.removeExtra(ARGUMENT_URI);
		return intent;
	}

}
