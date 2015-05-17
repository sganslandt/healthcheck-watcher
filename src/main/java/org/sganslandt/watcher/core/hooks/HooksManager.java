package org.sganslandt.watcher.core.hooks;

import com.google.common.eventbus.EventBus;

import javax.ws.rs.client.Client;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HooksManager {

    private final HooksDAO dao;
    private final EventBus eventBus;
    private final Client client;

    private final Map<String, NotifierHook> hooks;

    public HooksManager(final HooksDAO dao, final EventBus eventBus, final Client client) {
        this.dao = dao;
        this.eventBus = eventBus;
        this.client = client;
        this.hooks = new HashMap<>();
        for (String url : dao.listHooks())
            doCreateHook(url);
    }

    public void addHook(String url) {
        dao.addHook(url);
        doCreateHook(url);
    }

    private void doCreateHook(final String url) {
        NotifierHook hook = new NotifierHook(url, client);
        eventBus.register(hook);
        hooks.put(url, hook);
    }

    public Collection<String> listHooks() {
        return dao.listHooks();
    }

    public void removeHook(final String url) {
        for (Map.Entry<String, NotifierHook> e : hooks.entrySet()) {
            if (e.getKey().equals(url))
                eventBus.unregister(e.getValue());
        }
        hooks.remove(url);
        dao.removeHook(url);
    }


}
