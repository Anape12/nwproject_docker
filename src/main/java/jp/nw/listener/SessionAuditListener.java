package jp.nw.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import jp.nw.entity.UserEntity;
import jp.nw.model.AuditLogLogic;

@WebListener
public class SessionAuditListener implements HttpSessionListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        if (Boolean.TRUE.equals(event.getSession().getAttribute("intentionalLogout")))
            return;
        UserEntity user = (UserEntity) event.getSession().getAttribute("loginUser");
        if (user != null)
            AuditLogLogic.record(user.getUserId(), "AUTH", "SESSION_EXPIRED", "USER",
                    user.getUserId(), true, null, null, "セッションタイムアウトまたは無効化");
    }
}
