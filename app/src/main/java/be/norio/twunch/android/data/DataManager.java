package be.norio.twunch.android.data;

import android.content.Context;
import android.location.Location;
import android.text.format.DateUtils;

import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.data.model.Twunches;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.NetworkStatusUpdatedEvent;
import be.norio.twunch.android.otto.TwunchesAvailableEvent;
import be.norio.twunch.android.otto.TwunchesFailedEvent;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.SimpleXMLConverter;
import retrofit.http.GET;

public class DataManager {

    private static DataManager instance = null;
    private final Context mContext;
    private final TwunchServer mServer;
    private final TwunchData mTwunchData;
    private int mOutstandingNetworkCalls = 0;
    private Location mLocation;

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    protected DataManager() {
        mContext = PrefsUtils.getContext();
        mServer = new RestAdapter.Builder().setServer("http://twunch.be/").setConverter(new SimpleXMLConverter()).build().create(TwunchServer.class);
        if (PrefsUtils.isDataAvailable()) {
            mTwunchData = PrefsUtils.getData();
        } else {
            mTwunchData = new TwunchData();
            loadTwunches(true);
        }
        BusProvider.getInstance().register(this);
    }

    public Twunch getTwunch(String id) {
        final List<Twunch> twunches = mTwunchData.getTwunches();
        for (int i = 0; i < twunches.size(); i++) {
            final Twunch twunch = twunches.get(i);
            if (id.equals(twunch.getId())) {
                return twunch;
            }
        }
        return null;
    }

    public List<Twunch> getTwunches() {
        final long startOfToday = Util.getStartOfToday();
        final List<Twunch> twunches = new ArrayList<Twunch>(mTwunchData.getTwunches());
        boolean listUpdated = false;
        for (int i = 0; i < twunches.size(); i++) {
            Twunch twunch = twunches.get(i);
            final int days = (int) ((Util.getStartOfDay(twunch.getDate()) - startOfToday) / DateUtils.DAY_IN_MILLIS);
            if (days < 0) {
                twunches.remove(i);
                listUpdated = true;
            }
        }
        if (listUpdated) {
            mTwunchData.setTwunches(twunches);
            PrefsUtils.setData(mTwunchData);
        }
        return twunches;
    }

    public void loadTwunches(boolean force) {
        long lastSync = mTwunchData.getTimestamp();
        long now = (new Date()).getTime();
        if (!force && lastSync != 0 && (now - lastSync < DateUtils.HOUR_IN_MILLIS)) {
            return;
        }
        incrementOutstandingNetworkCalls();
        mServer.loadTwunches(new Callback<Twunches>() {
            @Override
            public void success(Twunches twunches, Response response) {
                mTwunchData.setTwunches(twunches.twunches);
                updateLocation(mLocation);
                PrefsUtils.setData(mTwunchData);
                BusProvider.getInstance().post(new TwunchesAvailableEvent(mTwunchData.getTwunches()));
                decrementOutstandingNetworkCalls();
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                retrofitError.printStackTrace();
                decrementOutstandingNetworkCalls();
                BusProvider.getInstance().post(new TwunchesFailedEvent());
            }
        });
    }

    private void incrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls++;
        BusProvider.getInstance().post(new NetworkStatusUpdatedEvent(mOutstandingNetworkCalls));
    }

    private void decrementOutstandingNetworkCalls() {
        mOutstandingNetworkCalls--;
        BusProvider.getInstance().post(new NetworkStatusUpdatedEvent(mOutstandingNetworkCalls));
    }

    @Produce
    public TwunchesAvailableEvent produceTwunches() {
        return new TwunchesAvailableEvent(getTwunches());
    }

    @Produce
    public NetworkStatusUpdatedEvent produceNetworkStatusUpdatedEvent() {
        return new NetworkStatusUpdatedEvent(mOutstandingNetworkCalls);
    }

    interface TwunchServer {
        @GET("/events.xml?when=future")
        void loadTwunches(Callback<Twunches> callback);
    }

    public void updateLocation(Location location) {
        if (location == null) {
            return;
        }
        mLocation = location;
        final List<Twunch> twunches = mTwunchData.getTwunches();
        final Location twunchLocation = new Location("");
        for (int i = 0; i < twunches.size(); i++) {
            Twunch twunch = twunches.get(i);
            if (twunch.hasLocation()) {
                twunchLocation.setLatitude(twunch.getLatitude());
                twunchLocation.setLongitude(twunch.getLongitude());
                twunch.setDistance(twunchLocation.distanceTo(location));
            } else {
                twunch.setDistance(Float.MAX_VALUE);
            }
        }
    }

}
