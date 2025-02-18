import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Thread;


import javax.mail.Session;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureClassLoader;
import java.sql.*;
import java.util.*;

import javax.naming.spi.DirStateFactory.Result;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Dashboard extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String portal = request.getParameter("portal");
        try{
            HttpSession session = request.getSession(false);
            String name = (String)session.getAttribute("name");
            String role = (String)session.getAttribute("role");
            session.setAttribute("portal",portal);
            if(role == null || !role.equals("super_admin") ){
                String str = "select role_name from portal_assign where portal_name=? and user_name=?;";
                PreparedStatement check = con.prepareStatement(str);
                check.setString(1, portal);
                check.setString(2, name);
                ResultSet rs = check.executeQuery();
                if(rs.next()){
                    session.setAttribute("role",(String)rs.getString(1));
                    role = (String)(session.getAttribute("role"));
                }
                System.out.println(name+" "+(String)(session.getAttribute("role")));
            }

            String ps = "select portal_mail from portals where portal_name=?";
            PreparedStatement pt = con.prepareStatement(ps);
            pt.setString(1, portal);
            ResultSet r = pt.executeQuery();
            if(r.next()){
              session.setAttribute("portal_mail",(String)r.getString(1));
              System.out.println((String)session.getAttribute("portal_mail"));
            }

            String ps1 = "select mail from users where name=?";
            PreparedStatement pt1 = con.prepareStatement(ps1);
            pt1.setString(1, name);
            ResultSet r1 = pt1.executeQuery();
            if(r1.next()){
              session.setAttribute("user_mail",(String)r1.getString(1));
              System.out.println((String)session.getAttribute("user_mail"));
            }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("status", "success");
        if(role.equals("super_admin") || role.equals("Admin"))
        jsonResponse.put("redirectUrl", "Mainpage.jsp");
        else if(role.equals("Agent"))
        jsonResponse.put("redirectUrl", "Agent.jsp");
        else
        jsonResponse.put("redirectUrl", "Customer.jsp");


			String t = "select token from authentication where user_name=?";
			PreparedStatement tp = con.prepareStatement(t);
			tp.setString(1, name);
			ResultSet rt = tp.executeQuery();
			String token = "";
			if (rt.next()) {
				token = rt.getString(1);
			}
			String clientId = "759301233438-sugjneq45otpjdukscia21d42j0jd64g.apps.googleusercontent.com";
			String clientSecret = "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";
			
            System.out.println(token);
			
			HttpTransport httpTransport = new NetHttpTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			
		    	GoogleRefreshTokenRequest tokenRequest = new GoogleRefreshTokenRequest(
				httpTransport, jsonFactory, token, clientId, clientSecret);
				String accessToken = tokenRequest.execute().getAccessToken();
				
				GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
				
				Gmail service = new Gmail.Builder(new NetHttpTransport(), jsonFactory, credential)
				.setApplicationName("MyApp")
				.build();
				
				
				WatchRequest watchRequest = new WatchRequest()
				.setTopicName("projects/autonomous-star-444907-c6/topics/Notification")
				.setLabelIds(java.util.Collections.singletonList("INBOX"));

				
				
				service.users().watch("me", watchRequest).execute();
				System.out.println("Watch request registered.");


        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
