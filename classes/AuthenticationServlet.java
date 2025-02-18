import com.google.api.client.auth.oauth2.*;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;

public class AuthenticationServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("Authenticate");

		String clientId = "759301233438-sugjneq45otpjdukscia21d42j0jd64g.apps.googleusercontent.com";
		String clientSecret = "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";
		String redirectUri = "http://localhost:8080/MyApp/verify";

		String[] scopes = {  "https://www.googleapis.com/auth/gmail.send", 
		"https://www.googleapis.com/auth/gmail.modify", 
		"https://www.googleapis.com/auth/gmail.readonly",
		"https://www.googleapis.com/auth/pubsub"};

		GoogleAuthorizationCodeRequestUrl authorizationUrl = new GoogleAuthorizationCodeRequestUrl(clientId, redirectUri, Arrays.asList(scopes)).setAccessType("offline");
		
		JSONObject jsonResponse = new JSONObject();
		String url = authorizationUrl.build()+"&prompt=consent";
		response.sendRedirect(url);
  
	}
}
