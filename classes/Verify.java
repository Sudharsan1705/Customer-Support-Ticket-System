import com.google.api.client.json.JsonFactory;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.*;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.*;
import com.fasterxml.jackson.core.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;

public class Verify extends HttpServlet {
	static Connection con = JDBCConnection.getConnection();
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String clientId = "759301233438-sugjneq45otpjdukscia21d42j0jd64g.apps.googleusercontent.com";
	String clientSecret = "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";
	String code = request.getParameter("code");
    try{
		HttpSession session = request.getSession(false);
		String name="";
		String role="";
		if(session != null){
			name = (String)session.getAttribute("name");
			role = (String)session.getAttribute("role");
		}
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
			new NetHttpTransport(),jsonFactory,
				"https://oauth2.googleapis.com/token",
				clientId,clientSecret,code,"http://localhost:8080/MyApp/verify"
		).execute();
		
		String accessToken = tokenResponse.getAccessToken();
		String refreshToken = tokenResponse.getRefreshToken();
		
		System.out.println("Access Token: " + accessToken);
		System.out.println("Refresh Token: " + refreshToken);
		
		String str = "insert into authentication(user_name,code,token) values(?,?,?);";
        PreparedStatement p = con.prepareStatement(str);
		p.setString(1, name);
		p.setString(2, code);
		p.setString(3, refreshToken);
		p.executeUpdate();


		String url ="";
		
		if(role!=null && role.equals("super_admin"))
		url="http://localhost:8080/MyApp/portals.jsp";
		else
		url="http://localhost:8080/MyApp/PortalView.jsp";
		System.out.println(url);
		response.sendRedirect(url);
	}
	catch(Exception e){
       e.printStackTrace();
	}
	}
}

