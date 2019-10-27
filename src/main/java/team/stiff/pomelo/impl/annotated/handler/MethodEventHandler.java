package team.stiff.pomelo.impl.annotated.handler;

import team.stiff.pomelo.filter.EventFilter;
import team.stiff.pomelo.handler.EventHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * A basic implementation of the {@link EventHandler} used to
 * mark a method as an event listener via handle.
 *
 * @author Daniel
 * @since May 31, 2017
 */
public final class MethodEventHandler implements EventHandler {
    /**
     * The class instance and parent of the method.
     */
    private final Object listenerParent;

    /**
     * The method object that has been marked as a listener.
     */
    private final Method method;

    /**
     * A filter predicate used to test the passed method against
     * all registered filters on the handler.
     */
    private final Set<EventFilter> eventFilters;

    public MethodEventHandler(final Object listenerParent, final Method method,
            final Set<EventFilter> eventFilters) {
        this.listenerParent = listenerParent;
        if (!method.isAccessible())
            method.setAccessible(true);

        this.method = method;
        this.eventFilters = eventFilters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> void handle(final E event) {
        // iterate all event filters given to the handler
        for (final EventFilter filter : eventFilters)
            if (!filter.test(this, event))
                return;

        try {
            // invoke the listener with the current event
            method.invoke(listenerParent, event);
        } catch (final IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Object getListener() {
        return method;
    }

    @Override
    public Iterable<EventFilter> getFilters() {
        return eventFilters;
    }
}
