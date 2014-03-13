package be.norio.twunch.android.data;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateUtils;

import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.data.model.Twunches;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.NetworkStatusUpdatedEvent;
import be.norio.twunch.android.otto.TwunchesAvailableEvent;
import be.norio.twunch.android.otto.TwunchesFailedEvent;
import be.norio.twunch.android.ui.DetailsActivity;
import be.norio.twunch.android.util.PrefsUtils;
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
    private int mNotificationId = 1;
    private long[] mVibratePattern = new long[]{0, 250, 250, 250};

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
        mTwunchData.removeOldTwunches();
        return new ArrayList<Twunch>(mTwunchData.getTwunches());
    }

    public void loadTwunches(boolean force) {
        final long lastSync = mTwunchData.getTimestamp();
        mTwunchData.removeOldTwunches();
        incrementOutstandingNetworkCalls();
        mServer.loadTwunches(new Callback<Twunches>() {
            @Override
            public void success(Twunches twunches, Response response) {
                final boolean notificationEnabled = PrefsUtils.isNotificationEnabled();
                for (int i = 0; i < twunches.twunches.size(); i++) {
                    Twunch twunch = twunches.twunches.get(i);
                    if (mTwunchData.add(twunch)) {
                        if (notificationEnabled && lastSync != 0) {
                            showNotification(twunch);
                        }
                    }
                }
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

    private void showNotification(Twunch twunch) {

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setContentTitle(twunch.getTitle())
                .setContentText(twunch.getAddress())
                .setSmallIcon(R.drawable.ic_stat_hamburger)
                .setAutoCancel(true);

        if (PrefsUtils.isVibrateEnabled()) {
            builder.setVibrate(mVibratePattern);
        }
        final Uri sound = PrefsUtils.getSound();
        if (sound != null) {
            builder.setSound(sound);
        }

        // Our parent activity
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(DetailsActivity.class);
        Intent i = DetailsActivity.getIntent(mContext, twunch.getId());
        stackBuilder.addNextIntent(i);
        PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pi);

        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(mNotificationId, builder.getNotification());
        mNotificationId++;

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
