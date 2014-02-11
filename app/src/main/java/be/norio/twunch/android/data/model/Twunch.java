package be.norio.twunch.android.data.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

@Root(strict = false)
public class Twunch {

    private static SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMdd'T'HHmmss'Z'");

    @Element
    String id;
    @Element(required = false)
    String title;
    @Element(required = false)
    String address;
    @Element(required = false)
    String note;
    @Element(required = false)
    String date;
    @Element(required = false)
    String map;
    @Element(required = false)
    String link;
    @Element(required = false)
    double latitude;
    @Element(required = false)
    double longitude;
    @ElementList
    List<String> participants;
    @Element(required = false)
    boolean closed;
    @Element(required = false)
    boolean sponsored;

    float distance;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAddress() {
        return address;
    }

    public String getNote() {
        return note;
    }

    public long getDate() {
        try {
            return sdf.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    public String getMap() {
        return map;
    }

    public String getLink() {
        return link;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isSponsored() {
        return sponsored;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
