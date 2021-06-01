package vn.viettel.onroad.model;

import com.bmwcarit.barefoot.matcher.MatcherKState;
import com.bmwcarit.barefoot.tracker.TemporaryMemory;
import org.springframework.beans.factory.annotation.Value;

public class State extends TemporaryMemory.TemporaryElement<State> {
    @Value("${kstate.k:-1}")
    private int k;

    @Value("${kstate.t: -1}")
    private int t;

    final MatcherKState inner = new MatcherKState(k, t);

    public MatcherKState getInner() {
        return inner;
    }

    public State(String id) {
        super(id);
    }
};