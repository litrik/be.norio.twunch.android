
package be.norio.twunch.android.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import be.norio.twunch.android.otto.BusProvider;
import butterknife.ButterKnife;

public class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }
}
