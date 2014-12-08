/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The <code>Timeable</code> interface declares that implementing classes
 * can be using an AsyncTimer
 * @see AsyncTimer
 * @author pfarrell
 */
public interface Timeable {
    AtomicBoolean getTimeSemaphore();
}
