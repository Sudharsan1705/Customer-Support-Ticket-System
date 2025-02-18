import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONObject;

public class AddConversation extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		    String toMail = request.getParameter("toMail");
		    String message = request.getParameter("message");
		    int parent_id = Integer.parseInt(request.getParameter("id"));
			Part image = request.getPart("image"); 
			
			System.out.println(toMail);
			System.out.println(message);
			System.out.println(image);			
	        PreparedStatement check = null;
	        PreparedStatement ck = null;
	        PreparedStatement insert = null;
	        Connection con = null;
			
	         try {
				HttpSession session = request.getSession(false);
				System.out.println(session.getAttribute("ticket_id"));
			 	System.out.println(session.getAttribute("user_mail"));

			 	int ticket_id = ((Integer)session.getAttribute("ticket_id")).intValue()	;
				String fromMail = (String)session.getAttribute("user_mail");
				String name = (String)session.getAttribute("name");
				
	         	con = JDBCConnection.getConnection();
				
				String s = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
				
				PreparedStatement pt = con.prepareStatement(s);
				pt.setInt(1, parent_id);
				pt.setString(2, fromMail);
				pt.setString(3, toMail);
				pt.setString(4, message);
				pt.setInt(5, ticket_id);
				int count = pt.executeUpdate();

				
					int child_id = 0;
					String conv ="";
					if(parent_id == 0){
							String m = "select child_id,message from thread where ticket_id=? and parent_id=?";
							PreparedStatement pm = con.prepareStatement(m);
						pm.setInt(1,ticket_id);
						pm.setInt(2,parent_id);
						ResultSet rm = pm.executeQuery();
					if(rm.next()){
							child_id = rm.getInt(1);
							conv = rm.getString(2);
					}
						String d = "update thread set parent_id=? where ticket_id=? and child_id=?";
						PreparedStatement d1 = con.prepareStatement(d);
						d1.setInt(1, child_id);
						d1.setInt(2, ticket_id);
						d1.setInt(3, child_id);
						d1.executeUpdate();
					if(image != null && image.getSize() > 0){
								InputStream imageInputStream = image.getInputStream();
								System.out.println(child_id);
								String str = "insert into image(child_id,image,time) values(?,?,NOW())";
								PreparedStatement ip= con.prepareStatement(str);
							ip.setInt(1, child_id);
							ip.setBinaryStream(2, imageInputStream);
							ip.executeUpdate();
							
		    	}
					}
					else{
				   String m = "select child_id,message from thread where ticket_id=? and parent_id=? and parent_id!=child_id order by time desc limit 1";
					PreparedStatement pm = con.prepareStatement(m);
					pm.setInt(1,ticket_id);
					pm.setInt(2,parent_id);
					ResultSet rm = pm.executeQuery();
						if(rm.next()){
							child_id = rm.getInt(1);
							conv = rm.getString(2);
					}
					if(image != null && image.getSize() > 0){
								InputStream imageInputStream = image.getInputStream();
								System.out.println(child_id+" "+imageInputStream);
							String str = "insert into image(child_id,image,time) values(?,?,NOW())";
							PreparedStatement ip= con.prepareStatement(str);
							ip.setInt(1, child_id);
							ip.setBinaryStream(2, imageInputStream);
							ip.executeUpdate();
							
						}
					}
									
									JSONObject jsonResponse = new JSONObject();
									PrintWriter out = response.getWriter();
									jsonResponse.put("ticket_id", ticket_id);
									out.print(jsonResponse);
									out.flush();
								} catch (Exception e) {
									e.printStackTrace();
	        } finally {
	            try {
	                con.close();
	            } catch (SQLException e) {
				}
	        }
		}
}