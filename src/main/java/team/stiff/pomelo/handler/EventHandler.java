package team.stiff.pomelo.handler;

import team.stiff.pomelo.filter.EventFilter;

/**
 * The event handler is a container that will assist in the
 * process of handling the event.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public interface EventHandler {

    /**
     * Invoked when the listener needs to handle
     * an event.
     */
    <E> void handle(final E event);

    /**
     * The object of the event listener.
     *
     * @return parent of listener
     */
    Object getListener();

    /**
     * All of the filters that will be tested on the
     * handler when being dispatched.
     *
     * @return iterable filters
     */
    Iterable<EventFilter> getFilters();
}
