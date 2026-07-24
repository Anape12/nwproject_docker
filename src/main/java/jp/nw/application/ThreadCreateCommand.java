package jp.nw.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.base.ApplicationCommand;
import jp.nw.base.BaseModel;
import jp.nw.entity.UserEntity;
import jp.nw.model.ThreadCreateLogic;

public class ThreadCreateCommand extends ApplicationCommand {

	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	// private Map<String, String> threadInfos = null;
	private Map<String, Object> threadInfos = null;

	private BaseModel baseModel = null;

	private List<UserEntity> userList = null;

	private UserEntity loginUser = null;

	private static final String KEY_TITLE = "title";
	private static final String KEY_TARGET_USER = "author_id";
	private static final String KEY_CONTENT = "thread_content";
	private static final String KEY_SESSIONUSER = "loginUser";
	private static final String KEY_REQ = "request";
	private static final String KEY_RES = "response";

	public ThreadCreateCommand() {
		baseModel = new BaseModel();
	}

	public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
		return true;
	}

	protected boolean doCommandData() {
		try {
			// パラメータ取得
			request.setCharacterEncoding("UTF-8");

			Map<String, Object> threadMap = this.baseModel.getParameter(request, response);

			HttpSession session = this.request.getSession();
			UserEntity loginUser = (UserEntity) session.getAttribute(KEY_SESSIONUSER);

			threadInfos = new HashMap<>();
			threadInfos.put(KEY_TITLE, threadMap.get(KEY_TITLE));
			threadInfos.put(KEY_TARGET_USER, loginUser.getUserId());
			threadInfos.put(KEY_CONTENT, threadMap.get("content"));
			return true;
		} catch (Exception e) {
			this.logger.writeInfo("Parameter Error");
			return false;
		}

	}

	protected boolean executeCommand() {
		try {
			// ユーザー情報一覧取得処理
			ThreadCreateLogic logic = new ThreadCreateLogic();
			logic.insertThread(threadInfos);
			return true;
		} catch (Exception e) {
			this.logger.writeInfo("");
			return false;
		}
	}

	protected boolean commandOutput() {
		this.output.setValue(KEY_REQ, this.request);
		this.output.setValue(KEY_RES, this.response);

		return true;
	}
}
