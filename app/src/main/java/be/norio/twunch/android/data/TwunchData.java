package be.norio.twunch.android.data;

import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.util.Util;

public class TwunchData {

    private List<Twunch> mTwunches = new ArrayList<Twunch>();
    private long mTimestamp;

    public List<Twunch> getTwunches() {
        return new ArrayList<Twunch>(mTwunches);
    }

    public void setTwunches(List<Twunch> twunches) {
        mTwunches = twunches;
        mTimestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void removeOldTwunches() {
        final long startOfToday = Util.getStartOfToday();
        for (int i = 0; i < mTwunches.size(); i++) {
            Twunch twunch = mTwunches.get(i);
            final int days = (int) ((Util.getStartOfDay(twunch.getDate()) - startOfToday) / DateUtils.DAY_IN_MILLIS);
            if (days < 0) {
                mTwunches.remove(i);
            }
        }
    }

    public boolean add(Twunch twunch) {
        boolean isNew = true;
        for (int i = 0; i < mTwunches.size(); i++) {
            Twunch t = mTwunches.get(i);
            final String id = twunch.getId();
            if (t.getId().equals(id)) {
                isNew = false;
                mTwunches.remove(i);
                break;
            }
        }
        mTwunches.add(twunch);
        return isNew;
    }
}
