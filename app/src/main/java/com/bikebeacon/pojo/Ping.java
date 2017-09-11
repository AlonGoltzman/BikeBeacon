package com.bikebeacon.pojo;

import java.util.Locale;

import static com.bikebeacon.background.utility.Constants.EARTH_RADIUS_KM;

/**
 * Created by Alon on 9/7/2017.
 */

public class Ping {

    private double lon;
    private double lat;
    private double time;

    private double timeBetween;

    private double speed;

    public Ping(Ping previousPing, double lo, double la) {
        this(lo, la);
        timeBetween = ((time - previousPing.getTime()) / (1000 * 60 * 60));
        speed = getSpeed(previousPing, this, timeBetween);
        speed = (Double.isInfinite(speed) || Double.isNaN(speed) ? 0 : speed);
    }

    public Ping(double lo, double la) {
        lon = lo;
        lat = la;
        time = System.currentTimeMillis();
        speed = 0;
        timeBetween = 0;
    }

    private static double getSpeed(Ping start, Ping end, double timeBetweenPoints) {
        double g1 = Math.toRadians(start.lat);
        double g2 = Math.toRadians(end.lat);
        double delta1 = Math.toRadians(end.lat - start.lat);
        double delta2 = Math.toRadians(end.lon - start.lon);

        double a = (Math.sin(delta1 / 2) * Math.sin(delta1 / 2)
                + Math.cos(g1) * Math.cos(g2)
                * Math.sin(delta2 / 2) * Math.sin(delta2 / 2));
        double c = (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        double dist = EARTH_RADIUS_KM * c;
        return dist / timeBetweenPoints;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    public double getTime() {
        return time;
    }

    public double getTimeBetween() {
        return timeBetween;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "Lat: %.2f, Lon: %.2f, Speed: %.2f, Time: %.2f, Delta Time: %.2f\n", getLat(), getLon(), getSpeed(), getTime(), getTimeBetween());
    }
}
