package org.klyx.flux;

import org.klyx.flux.dispatch.TickScheduler;
import org.slf4j.Logger;

public interface FluxPlatform {
    Logger logger();

    TickScheduler tickScheduler();
}
