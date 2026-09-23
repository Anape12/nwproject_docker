package jp.nw.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.nw.base.BaseModel;

/**
 * Servlet implementation class UserView
 */
@WebServlet("/EditUserView")
public class EditUserViewController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Map<String, String> postMap;
	private BaseModel logger = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EditUserViewController() {
		super();
		// TODO Auto-generated constructor stub
		this.logger = new BaseModel();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(request.getContextPath() + "/UserSecurityAdmin");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.sendRedirect(request.getContextPath() + "/UserSecurityAdmin");
	}

}
