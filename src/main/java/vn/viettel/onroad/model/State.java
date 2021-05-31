package vn.viettel.onroad.model;

import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.tracker.TemporaryMemory;

public class State extends TemporaryMemory.TemporaryElement<State> {
    final MatcherKState inner = new MatcherKState();

    public MatcherKState getInner() {
        return inner;
    }

    public State(String id) {
        super(id);
    }
};