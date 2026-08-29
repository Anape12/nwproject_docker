package jp.nw.listener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import jp.nw.model.AiResponseWorker;

@WebListener
public class AiWorkerLifecycle implements ServletContextListener {
    private ScheduledExecutorService executor;

    public void contextInitialized(ServletContextEvent e) {
        recoverInterruptedJobs();
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ai-response-worker");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(new AiResponseWorker(), 2, 2, TimeUnit.SECONDS);
    }

    public void contextDestroyed(ServletContextEvent e) {
        if (executor != null)
            executor.shutdownNow();
    }

    private void recoverInterruptedJobs() {
        try {
            jp.nw.parts.DBBase db = new jp.nw.parts.DBBase();
            try (java.sql.Connection c = db.getConnection();
                    java.sql.PreparedStatement p = c.prepareStatement(
                            "UPDATE ai_response_job SET status='PENDING',started_at=NULL "
                                    + "WHERE status='PROCESSING' AND started_at<NOW()-INTERVAL 5 MINUTE")) {
                p.executeUpdate();
            }
        } catch (Exception ignored) {
            // Flyway may still be creating the AI tables during the first startup.
        }
    }
}
