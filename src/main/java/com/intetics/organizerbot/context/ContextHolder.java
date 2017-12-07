package com.intetics.organizerbot.context;

import java.util.HashMap;
import java.util.Map;

public class ContextHolder {
    private static final String LOGTAG = "CONTEXT_HOLDER";

    private static volatile ContextHolder instance;
    private volatile Map<Long, Context> contexts;
    private volatile Map<Long, Object> editingValues;
    private volatile Map<Long, TypeOfClass> typesOfClass;

    private ContextHolder() {
        contexts = new HashMap<>();
        editingValues = new HashMap<>();
        typesOfClass = new HashMap<>();
    }

    public static ContextHolder getInstance() {
        final ContextHolder currentInstance;
        if (instance == null) {
            synchronized (ContextHolder.class) {
                if (instance == null) {
                    instance = new ContextHolder();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    public void setContext(Long id, Context context) {
        contexts.put(id, context);
    }

    public Context getContext(Long id) {
        return contexts.get(id);
    }

    public boolean contains(Long id) {
        return contexts.containsKey(id);
    }

    public void setEditingValue(Long id, Object value) {
        editingValues.put(id, value);
    }

    public Object getEditingValue(Long id) {
        return editingValues.get(id);
    }

    public boolean isEditing(Long id) {
        return editingValues.containsKey(id);
    }

    public void removeEditingValue(Long id) {
        editingValues.remove(id);
    }

    public TypeOfClass getTypeOfClass(Long id) {
        return typesOfClass.get(id);
    }

    public void setTypeOfClass(Long id, TypeOfClass type) {
        typesOfClass.put(id, type);
    }
}
