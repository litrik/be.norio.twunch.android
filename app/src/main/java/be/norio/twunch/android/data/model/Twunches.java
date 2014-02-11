package be.norio.twunch.android.data.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;


@Root(strict=false)
public class Twunches {

    @ElementList(required=true, inline=true)
    public List<Twunch> twunches;

    @Attribute
    String version;
}
