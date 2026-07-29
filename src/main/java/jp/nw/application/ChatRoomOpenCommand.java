package jp.nw.application;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.ChatMessageEntity;
import jp.nw.model.ChatOpenLogic;

public class ChatRoomOpenCommand extends ApplicationCommand{
    
    private HttpServletRequest request = null;
	private HttpServletResponse response = null;

    private String roomId = "";

    private List<ChatMessageEntity> chatMessageEntities;

	private static final String KEY_TARGET_ROOM_ID = "targetRoomId";
	private static final String KEY_REQ = "request";
	private static final String KEY_RES = "response";
    private static final String KEY_CHATDETAIL = "chatDetail";

	public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		return true;
	}

	protected boolean doCommandData() {
		try {
			// パラメータ取得
			request.setCharacterEncoding("UTF-8");
            this.roomId = (String) this.request.getAttribute(KEY_TARGET_ROOM_ID);
			return true;
		} catch (Exception e) {
			this.logger.writeInfo("Parameter Error");
			return false;
		}

	}

	protected boolean executeCommand() {
		try {
			// ユーザー情報一覧取得処理
			ChatOpenLogic logic = new ChatOpenLogic();
			this.chatMessageEntities = logic.getChanelOpen(this.roomId, this.request);
			return true;
		} catch (Exception e) {
			this.logger.writeInfo("");
			return false;
		}
	}

	protected boolean commandOutput() {
		this.output.setValue(KEY_REQ, this.request);
		this.output.setValue(KEY_RES, this.response);

        this.output.setValue(KEY_REQ, logger);
		this.output.setValue(KEY_CHATDETAIL, this.chatMessageEntities);

		return true;
	}
}
