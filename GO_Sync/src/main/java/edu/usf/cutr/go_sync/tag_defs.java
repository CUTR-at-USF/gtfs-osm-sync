package edu.usf.cutr.go_sync;

//
public class tag_defs {

    //https://developers.google.com/transit/gtfs/reference/
    //https://wiki.openstreetmap.org/wiki/Proposed_features/Public_Transport
    //https://wiki.openstreetmap.org/wiki/GTFS/Mapping_to_OSM_tags
    //https://wiki.openstreetmap.org/wiki/GTFS

    //https://developers.google.com/transit/gtfs/reference#agencytxt
    public final static String GTFS_AGENCY_NAME_KEY = "agency_name";
    public final static String GTFS_AGENCY_ID_KEY = "agency_id";
    public final static String OSM_NETWORK_KEY = "network";

    //https://developers.google.com/transit/gtfs/reference#stoptxt
    public final static String GTFS_STOP_ID_KEY = "stop_id";
    public final static String OSM_STOP_ID_KEY = "gtfs_id"; // Alternatives are: gtfs:stop_id & gtfs:id

    public final static String GTFS_STOP_NAME_KEY = "stop_name";
    public final static String OSM_STOP_NAME_KEY = "name";

    public final static String GTFS_STOP_DESC_KEY = "stop_desc";
    public final static String OSM_STOP_DESC_KEY = "description";

    public final static String GTFS_ZONE_KEY  = "zone_id";
    public final static String  OSM_ZONE_KEY  = "transport:zone";

    public final static String GTFS_STOP_URL_KEY = "stop_url";
    public final static String OSM_STOP_URL_KEY = "url";

    public final static String GTFS_WHEELCHAIR_KEY = "wheelchair_boarding";
    public final static String OSM_WHEELCHAIR_KEY = "wheelchair";

    public final static String GTFS_STOP_TYPE_KEY = "location_type";
    public final static String OSM_STOP_TYPE_KEY = "public_transport";

    public final static String GTFS_PLATFORM_CODE_KEY = "platform_code";
    public final static String OSM_PLATFORM_CODE_KEY = "local_ref";

    //	public final static String GTFS_OPERATOR_KEY = "operator";

    //https://developers.google.com/transit/gtfs/reference#routestxt
    public final static String GTFS_ROUTE_ID_KEY = "route_id";
    public final static String OSM_ROUTE_ID_KEY = "gtfs:route_id";

    public final static String GTFS_ROUTE_NUM_KEY = "route_short_name";
    public final static String OSM_ROUTE_NUM_KEY = "ref"; // Alternative is gtfs:name

    public final static String GTFS_ROUTE_NAME_KEY = "route_long_name";
    public final static String OSM_ROUTE_NAME_KEY = "gtfs:name";

    public final static String GTFS_ROUTE_DESC_NAME_KEY = "route_desc";
    public final static String OSM_ROUTE_DESC_NAME_KEY = "description";

    public final static String GTFS_COLOR_KEY   = "route_color";
    public final static String GTFS_COLOUR_KEY  = "route_colour";
    public final static String  OSM_COLOUR_KEY  = "colour";

    public final static String GTFS_ROUTE_URL_KEY = "route_url";
    public final static String OSM_ROUTE_URL_KEY = "url";

    public final static String GTFS_NAME_KEY = "name";
    public final static String ROUTE_KEY = "ref";
    public final static String OSM_ROUTE_TYPE_KEY = "route";

    //https://developers.google.com/transit/gtfs/reference#tripstxt
    public final static String GTFS_SERVICE_ID_KEY  = "service_id";
    // No mapping to OSM for GTFS_SERVICE_ID_KEY

    public final static String GTFS_SHAPE_ID_KEY = "shape_id";
    public final static String OSM_SHAPE_ID_KEY = "gtfs:shape_id";

    public final static String GTFS_TRIP_ID_KEY     = "trip_id";
    public final static String OSM_TRIP_ID_KEY     = "gtfs:trip_id:sample"; // or gtfs:shape_id if possible

    //https://developers.google.com/transit/gtfs/reference#stop_timestxt
    public final static String GTFS_SEQUENCE_KEY = "stop_sequence";
    public final static String GTFS_PICKUP_TYPE_KEY = "pickup_type";
    public final static String GTFS_DROP_OFF_TYPE_KEY = "drop_off_type";
    public final static String GTFS_DEPARTURE_TIME_KEY = "departure_time";
    public final static String GTFS_ARRIVAL_TIME_KEY = "arrival_time";

    public enum primative_type {
        NODE, RELATION, WAY
    }

}

/*
public enum tag_def {


    GTFS_STOP_ID_KEY(12323)
}*/