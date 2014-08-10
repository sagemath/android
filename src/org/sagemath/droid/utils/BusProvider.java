package org.sagemath.droid.utils;

import com.squareup.otto.Bus;

/**
 * @author Nikhil Peter Raj
 */
public class BusProvider {

    private static Bus bus;

    public static Bus getInstance() {
        if (bus == null) {
            bus = new Bus();
        }

        return bus;
    }
}
