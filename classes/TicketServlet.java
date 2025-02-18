import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.Thread;
import com.google.api.services.gmail.model.ListMessagesResponse;

import javax.mail.Session;
import javax.mail.Address;
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

public class TicketServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String subject = request.getParameter("subject");
    String desc = request.getParameter("description");

    PreparedStatement check = null;
    PreparedStatement ck = null;
    PreparedStatement insert = null;
    Connection con = null;

    try {
      HttpSession session = request.getSession(false);
      String name = (String) session.getAttribute("name");
      String fromMail = (String) session.getAttribute("user_mail");
      String role = (String) session.getAttribute("role");
      String portal = (String) session.getAttribute("portal");

      System.out.println(session.getAttribute("user_mail"));

      con = JDBCConnection.getConnection();
      String str = "insert into tickets (subject, description, customer_name, created_at, status_name, portal_name) VALUES (?, ?, ?, NOW(), ?, ?)";
      insert = con.prepareStatement(str);
      insert.setString(1, subject);
      insert.setString(2, desc);
      insert.setString(3, name);
      insert.setString(4, "Pending");
      insert.setString(5, portal);
      int cnt = insert.executeUpdate();

      String st = "select ticket_id from tickets where portal_name=? and customer_name=? and subject=? and description=?";
      check = con.prepareStatement(st);
      check.setString(1, portal);
      check.setString(2, name);
      check.setString(3, subject);
      check.setString(4, desc);
      ResultSet rs = check.executeQuery();
      int id = 0;
      if (rs.next()) {
        id = rs.getInt(1);
      }
      String por = "select mail from portal where name=?";
      PreparedStatement prt = con.prepareStatement(por);
      prt.setString(1, portal);
      ResultSet rrt = prt.executeQuery();
      String portalMail ="";
      if(rrt.next()){
         portalMail = rrt.getString(1).trim();
      }
      System.out.println(portalMail);
      String s1 = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
      PreparedStatement pt1 = con.prepareStatement(s1);
      pt1.setInt(1, 0);
      pt1.setString(2, fromMail);
      pt1.setString(3, portalMail);
      pt1.setString(4, desc);
      pt1.setInt(5, id);
      int count1 = pt1.executeUpdate();
      int mid = 0;

      String m = "select child_id from thread where ticket_id=?";
      PreparedStatement pm = con.prepareStatement(m);
      pm.setInt(1, id);
      ResultSet rm = pm.executeQuery();
      if (rm.next()) {
        mid = rm.getInt(1);
      }

      String d = "update thread set parent_id=? where ticket_id=?";
      PreparedStatement d1 = con.prepareStatement(d);
      d1.setInt(1, mid);
      d1.setInt(2, id);
      d1.executeUpdate();
      


      String s = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
      PreparedStatement pt = con.prepareStatement(s);
      pt.setInt(1, mid);
      pt.setString(2, portalMail);
      pt.setString(3, fromMail);
      pt.setString(4, "Your ticket has been recieved.Wait for further processing.");
      pt.setInt(5, id);
      int count = pt.executeUpdate();

      JSONObject jsonResponse = new JSONObject();
      PrintWriter out = response.getWriter();
      jsonResponse.put("status", "success");
      jsonResponse.put("message", "Ticket Added Successfully");
      out.print(jsonResponse);
      out.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        con.close();
      } catch (SQLException e) {}
    }
  }

}