package jp.nw.application;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.nw.base.ApplicationCommand;
import jp.nw.entity.UserEntity;
import jp.nw.model.UserViewLogic;

public class UserListViewCommand extends ApplicationCommand {

	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	private List<UserEntity> userList = null;

	private UserEntity loginUser = null;

	private static final String KEY_USER = "loginUser";
	private static final String KEY_REQ = "request";
	private static final String KEY_RES = "response";

	public boolean setCommandData(HttpServletRequest request, HttpServletResponse response) {

		this.request = request;
		this.response = response;

		return true;

	}

	protected boolean doCommandData() {

		try {
			// ユーザー情報一覧取得処理
			UserViewLogic userview = new UserViewLogic();
			this.userList = userview.findAll();
			this.request.setAttribute("userList", this.userList);

			return true;
		} catch (Exception e) {
			this.logger.writeInfo("SQL Error");
			return false;
		}

	}

	protected boolean executeCommand() {

		try {
			HttpSession session = this.request.getSession();
			this.loginUser = (UserEntity) session.getAttribute(KEY_USER);
			for (UserEntity userinfo : this.userList) {
				this.logger.writeInfo(userinfo.getUserId());
			}

			return true;

		} catch (Exception e) {
			this.logger.writeInfo("");
			return false;
		}
	}

	protected boolean commandOutput() {
		this.output.setValue(KEY_USER, this.loginUser);
		this.output.setValue(KEY_REQ, this.request);
		this.output.setValue(KEY_RES, this.response);

		return true;
	}

	protected boolean postCommandData() {
		this.response.setContentType("text/html; charset=Shift_JIS");
		return true;
	}

	protected boolean postExeCommand() {
		// 選択されたユーザーIDを取得
		String userId = this.request.getParameter("radiobutton");
		// ユーザー情報編集
		UserViewLogic userview = new UserViewLogic();
		List<UserEntity> userList = userview.editUserInfo(userId);
		this.request.setAttribute("userList", userList);
		HttpSession session = this.request.getSession();
		this.loginUser = (UserEntity) session.getAttribute("loginUser");

		return true;
	}

	protected boolean postCommandOutput() {
		this.output.setValue(KEY_USER, this.loginUser);
		this.output.setValue(KEY_REQ, this.request);
		this.output.setValue(KEY_RES, this.response);

		return true;
	}

}
