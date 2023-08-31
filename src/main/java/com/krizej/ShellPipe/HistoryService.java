package com.krizej.ShellPipe;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;


public class HistoryService implements PersistentStateComponent<HistoryService.State> {
    static HistoryService service = new HistoryService();

    static class State {
        public List<String> history = new ArrayList<>();
    }

    private State myState = new State();

    public static HistoryService getInstance() {
        return service;

    }
    
    public void addString(String s) {
        if(s == null) return;
        if(s.trim().isEmpty()) return;
        myState.history.remove(s);
        myState.history.add(s);
    }

    public String getString(int idx) {
        if(myState.history.size() == 0)
            return "";
        return myState.history.get(myState.history.size() - idx - 1);
    }

    public int size() {
        return myState.history.size();
    }


    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.myState = state;
    }

    @Override
    public void noStateLoaded() {
        PersistentStateComponent.super.noStateLoaded();
    }

    @Override
    public void initializeComponent() {
        PersistentStateComponent.super.initializeComponent();
    }
}
