
// From https://github.com/square/otto/issues/38

package be.norio.twunch.android.otto;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class MainBus extends Bus
{
	private final Handler mainThread = new Handler(Looper.getMainLooper());

	@Override
	public void post(final Object event)
	{
		if (Looper.myLooper() == Looper.getMainLooper())
		{
			super.post(event);
		}
		else
		{
			mainThread.post(new Runnable()
			{

				@Override
				public void run()
				{
					post(event);

				}
			});
		}
	}
}