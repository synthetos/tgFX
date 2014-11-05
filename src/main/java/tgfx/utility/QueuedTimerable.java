/*
 * Copyright (C) 2014 Synthetos LLC. All Rights reserved.
 * http://www.synthetos.com
 */
package tgfx.utility;

/**
 * The <code>QueuedTimerable</code> interface declares that the object contains a
 * queue of T that is used by the QueueTimerable class
 * @see QueuedTimerable
 * @author pfarrell
 */
public interface QueuedTimerable<T> {
    void addToQueue(T t);
    
}
