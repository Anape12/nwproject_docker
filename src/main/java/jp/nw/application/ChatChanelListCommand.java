package jp.nw.application;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.ChatRoomEntity;
import jp.nw.entity.UserEntity;
import jp.nw.model.ChatChanelListLogic;

public class ChatChanelListCommand extends ApplicationCommand{
    
    private HttpServletRequest request = null;
	private HttpServletResponse response = null;

    private UserEntity userEntity = null;

    private List<ChatRoomEntity> chatRoomList;

	private static final String KEY_SESSIONUSER = "loginUser";
	private static final String KEY_REQ = "request";
	private static final String KEY_RES = "response";
    private static final String KEY_CHATCHANELS = "chatchanels";

	public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		return true;
	}

	protected boolean doCommandData() {
		try {
			// パラメータ取得
			request.setCharacterEncoding("UTF-8");

			HttpSession session = this.request.getSession();
			this.userEntity = (UserEntity) session.getAttribute(KEY_SESSIONUSER);

			return true;
		} catch (Exception e) {
			this.logger.writeInfo("Parameter Error");
			return false;
		}

	}

	protected boolean executeCommand() {
		try {
			// ユーザー情報一覧取得処理
			ChatChanelListLogic logic = new ChatChanelListLogic();
			this.chatRoomList = logic.getTargetChanel(this.userEntity.getUserId());
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
		this.output.setValue(KEY_CHATCHANELS, this.chatRoomList);

		return true;
	}
}
