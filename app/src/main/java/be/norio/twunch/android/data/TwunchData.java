package be.norio.twunch.android.data;

import java.util.ArrayList;
import java.util.List;

import be.norio.twunch.android.data.model.Twunch;

public class TwunchData {

    private List<Twunch> mTwunches;
    private long mTwunchesTimestamp;

    public List<Twunch> getTwunches() {
        return new ArrayList<Twunch>(mTwunches);
    }

    public void setTwunches(List<Twunch> twunches) {
        mTwunches = twunches;
        mTwunchesTimestamp = System.currentTimeMillis();
    }

}
