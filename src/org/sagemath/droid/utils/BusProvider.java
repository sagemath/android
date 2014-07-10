package org.sagemath.droid.utils;

import com.squareup.otto.Bus;

/**
 * Created by Haven on 08-07-2014.
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
