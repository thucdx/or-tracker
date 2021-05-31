package vn.viettel.onroad;

import com.bmwcarit.barefoot.tracker.TemporaryMemory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import vn.viettel.onroad.model.State;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;


public class StatePublisher extends Thread implements TemporaryMemory.Publisher<State> {
    private static final Logger logger = LoggerFactory.getLogger(StatePublisher.class);

    private BlockingQueue<String> queue = new LinkedBlockingDeque<>();
    private ZMQ.Context context = null;
    private ZMQ.Socket socket = null;

    public StatePublisher(int port) {
        context = ZMQ.context(1);
        socket = context.socket(ZMQ.PUB);
        socket.bind("tcp://*:" + port);
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = queue.take();
                socket.send(message);
            } catch (InterruptedException e) {
                logger.warn("state publisher interrupted");
                return;
            }
        }
    }

    @Override
    public void publish(String id, vn.viettel.onroad.model.State state) {
        try {
            JSONObject json = state.getInner().toMonitorJSON();
            json.put("id", id);
            queue.put(json.toString());
        } catch (Exception e) {
            logger.error("update failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void delete(String id, long time) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("time", time);
            queue.put(json.toString());
            logger.debug("delete object {}", id);
        } catch (Exception e) {
            logger.error("delete failed: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}