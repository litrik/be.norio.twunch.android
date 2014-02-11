package be.norio.twunch.android.otto;

import java.util.List;

import be.norio.twunch.android.data.model.Twunch;

public class TwunchesAvailableEvent {
    private final List<Twunch> mTwunches;

    public TwunchesAvailableEvent(List<Twunch> twunches) {
        mTwunches = twunches;
    }
}
