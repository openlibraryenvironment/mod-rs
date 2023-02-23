package org.olf.rs.utils;

import org.codehaus.groovy.util.LockableObject;

/**
 * A List for topics that is thread safe with regards to removeAll and addAll and
 * and allows us to process the contents without it changing under our feet
 *
 * @author Chas
 *
 */
public class TopicList extends ArrayList<String> {

    /** The object we use for locking to make sure we are threadsafe for what we do */
    private LockableObject lockObject = new LockableObject();

    /** Keeps trsck of whether the underlying data has been modified, thriugh addAll and removeAll */
    private boolean isModified = false;

    @Override
    public boolean addAll(Collection<? extends String> collection) {
        // Obtain the lock first
        lockObject.lock();

        try {
            // Now call the parent to do its job
            boolean result =  super.addAll(collection);

            // Set the modified flag as the data has changed
            isModified = true;
        } finally {
            // Release the lock
            lockObject.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<String> collection) {
        // Obtain the lock first
        lockObject.lock();

        try {
            // Now call the parent to do its job
            boolean result =  super.removeAll(collection);

            // Set the modified flag as the data has changed
            isModified = true;
        } finally {
            // Release the lock
            lockObject.unlock();
        }
    }

    /**
     * If the underlying data has been modified we then call the processor after taking out the lock
     * @param processor The processor to be called if the data has been changed
     */
    public void process(Closure processor) {
        // If the underlying data has changed then call the processor
        if (isModified) {
            // Obtain the lock
            lockObject.lock();

            try {
                // Now we have the lock, check that it is still modified
                if (isModified) {
                    // Just call the closure with this list as a parameter
                    processor(this);

                    // Data has been processed, so reset the modified flag
                    isModified = false;
                }
            } finally {
                // This is the real reason for the try catch as we need to free the lock
                lockObject.unlock();
            }
        }
    }
}
