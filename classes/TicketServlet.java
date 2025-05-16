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
    HttpSession session = request.getSession(false);
    String name = (String) session.getAttribute("name");
    String fromMail = (String) session.getAttribute("user_mail");
    String role = (String) session.getAttribute("role");
    String portal = (String) session.getAttribute("portal");
    
    add(fromMail,subject,desc,name,portal);
    JSONObject jsonResponse = new JSONObject();
    PrintWriter out = response.getWriter();
    jsonResponse.put("status", "success");
    jsonResponse.put("message", "Ticket Added Successfully");
    out.print(jsonResponse);
    out.flush();

  }
  
  public static void add(String fromMail,String subject,String desc,String name,String portal){
    try {
      
      PreparedStatement check = null;
      PreparedStatement ck = null;
      PreparedStatement insert = null;
      
      Connection con = null;
  
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
  
      String s = "insert into thread(from_mail,to_mail,message,time,ticket_id) values(?,?,?,NOW(),?)";
  
      String portalmail = "sudharsan1705204@gmail.com";

      String usermail=fromMail;
  
      PreparedStatement pt = con.prepareStatement(s);
      pt.setString(1, usermail);
      pt.setString(2, portalmail);
      pt.setString(3, desc);
      pt.setInt(4, id);
      int count = pt.executeUpdate();
  
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
  
      String s1 = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
      PreparedStatement pt1 = con.prepareStatement(s1);
      pt1.setInt(1, mid);
      pt1.setString(2, portalmail);
      pt1.setString(3, usermail);
      pt1.setString(4, "Your ticket has been recieved.Wait for further processing.");
      pt1.setInt(5, id);
      int count1 = pt1.executeUpdate();
  
      String clientId = "759301233438-sugjneq45otpjdukscia21d42j0jd64g.apps.googleusercontent.com";
      String clientSecret = "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";
  
      String t = "select token from authentication where user_name=?";
      PreparedStatement tp = con.prepareStatement(t);
      tp.setString(1, name);
      ResultSet rt = tp.executeQuery();
      String token = "";
      if (rt.next()) {
        token = rt.getString(1);
      }
  
      String toMail = "sudharsan1705204@gmail.com";
  
      HttpTransport httpTransport = new NetHttpTransport();
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
  
      GoogleRefreshTokenRequest tokenRequest = new GoogleRefreshTokenRequest(
        httpTransport, jsonFactory, token, clientId, clientSecret);
      String accessToken = tokenRequest.execute().getAccessToken();
  
      GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
  
      Gmail service = new Gmail.Builder(new NetHttpTransport(), jsonFactory, credential)
        .setApplicationName("MyApp")
        .build();
  
      MimeMessage email = createEmail(toMail, fromMail, Integer.toString(id), desc);
  
      Message mm = createMessageWithEmail(email);
  
      Message sentMessage = service.users().messages().send("me", mm).execute();
  
      String threadId = sentMessage.getThreadId();
  
      java.lang.Thread.sleep(4000L);
  
      Message fullMessage = service.users().messages().get("me", sentMessage.getId()).setFormat("FULL").execute();
      List < MessagePartHeader > headers = fullMessage.getPayload().getHeaders();
  
      String messageId = null;
      for (MessagePartHeader header: headers) {
        if ("Message-ID".equalsIgnoreCase(header.getName())) {
          messageId = header.getValue();
        }
      }
  
      String in = "insert into id(threadid,ticket_id,mail) values(?,?,?)";
      PreparedStatement ip = con.prepareStatement(in);
      ip.setString(1, threadId);
      ip.setInt(2, id);
      ip.setString(3, fromMail);
      ip.executeUpdate();
  
      System.out.println("Gmail-assigned Message-ID: " + messageId);
      System.out.println("Gmail-assigned Thread-ID: " + threadId);
  
      java.lang.Thread.sleep(4000L);
  
      String msg = "insert into message(child,message,thread_id) values(?,?,?)";
      PreparedStatement ms = con.prepareStatement(msg);
      ms.setInt(1, mid);
      ms.setString(2, messageId);
      ms.setString(3, threadId);
      ms.executeUpdate();
  
      String t1 = "select token from authentication where user_name=?";
      PreparedStatement tp1 = con.prepareStatement(t1);
      tp1.setString(1, "Sudharsan");
      ResultSet rt1 = tp1.executeQuery();
      String token1 = "";
      if (rt1.next()) {
        token1 = rt1.getString(1);
      }
  
      HttpTransport httpTransport1 = new NetHttpTransport();
      JsonFactory jsonFactory1 = JacksonFactory.getDefaultInstance();
  
      GoogleRefreshTokenRequest tokenRequest1 = new GoogleRefreshTokenRequest(
        httpTransport1, jsonFactory1, token1, clientId, clientSecret);
      String accessToken1 = tokenRequest1.execute().getAccessToken();
  
      System.out.println(token);
      System.out.println();
      System.out.println(token1);
  
      GoogleCredential credential1 = new GoogleCredential().setAccessToken(accessToken1);
  
      Gmail service1 = new Gmail.Builder(new NetHttpTransport(), jsonFactory1, credential1)
        .setApplicationName("MyApp")
        .build();
  
      String query = "rfc822msgid:" + messageId;
  
      ListMessagesResponse res = service1.users().messages().list("me").setQ(query).execute();
      List < Message > messages = res.getMessages();
  
      String receiverMessageId = "";
      if (messages != null && !messages.isEmpty()) {
        receiverMessageId = messages.get(0).getId();
        System.out.println("Receiver's Message ID: " + receiverMessageId);
      } else {
        System.out.println("Message not found in the receiver's account.");
      }
  
      Message m1 = service1.users().messages().get("me", receiverMessageId).execute();
  
      String threadId1 = m1.getThreadId();
  
      System.out.println(threadId1);
  
      MimeMessage replyEmail = createReplyEmail(toMail, fromMail,"Re: "+Integer.toString(id), "Your ticket has been recieved.Wait for further processing.", messageId);
  
      String str1 = "select child_id from thread where ticket_id=? and parent_id!=child_id";
      PreparedStatement p1 = con.prepareStatement(str1);
      p1.setInt(1, id);
      ResultSet rs1 = p1.executeQuery();
      if (rs1.next()) {
        mid = rs1.getInt(1);
      }
  
      Message message = createMessageWithEmail(replyEmail);
  
      message.setThreadId(threadId1);
  
      Message sentMessage1 = service1.users().messages().send("me", message).execute();
  
      System.out.println("Reply sent with Thread-ID: " + sentMessage1.getThreadId());
  
      Message fullMessage1 = service1.users().messages().get("me", sentMessage1.getId()).setFormat("FULL").execute();
      List < MessagePartHeader > headers1 = fullMessage1.getPayload().getHeaders();
  
      String messageId1 = null;
      for (MessagePartHeader header: headers1) {
        if ("Message-ID".equalsIgnoreCase(header.getName())) {
          messageId1 = header.getValue();
        }
      }
  
      System.out.println("Gmail-assigned Message-ID: " + messageId1);
      String msg1 = "insert into message(child,message,thread_id) values(?,?,?)";
      PreparedStatement ms1 = con.prepareStatement(msg1);
      ms1.setInt(1, mid);
      ms1.setString(2, messageId1);
      ms1.setString(3, threadId1);
      ms1.executeUpdate();
  
      System.out.println(sentMessage1.getId());
  
      Message message1 = service1.users().messages()
        .get("me", sentMessage1.getId())
        .setFields("historyId")
        .execute();
  
      String historyId = message1.getHistoryId().toString();
  
      System.out.println(historyId);
      String in1 = "insert into id(threadid,ticket_id,mai) values(?,?,?)";
      PreparedStatement ip1 = con.prepareStatement(in1);
      ip.setString(1, threadId1);
      ip.setInt(2, id);
      ip.setString(3, toMail);
      ip.executeUpdate();
  
      String check1 = "select count(*) from history where user_name=?";
      PreparedStatement pc = con.prepareStatement(check1);
      pc.setString(1, name);
      ResultSet rc = pc.executeQuery();
  
      if (rc.next() && rc.getInt(1) == 0) {
        String h = "insert into history(user_name,history) values(?,?)";
        PreparedStatement ph = con.prepareStatement(h);
        ph.setString(1, name);
        ph.setString(2, historyId);
        ph.executeUpdate();
      } else {
        String h = "update history set history=? where user_name=?";
        PreparedStatement ph = con.prepareStatement(h);
        ph.setString(1, historyId);
        ph.setString(2, name);
        ph.executeUpdate();
      }
  
      System.out.println("Email sent successfully");
  
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }

  public static MimeMessage createEmail(String to, String from, String subject, String bodyText) throws Exception {
    Properties props = new Properties();
    Session session = Session.getInstance(props, null);

    System.out.println(from + " " + to);

    MimeMessage email = new MimeMessage(session);
    email.setFrom(new InternetAddress(from));
    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
    email.setSubject(subject);

    MimeBodyPart body = new MimeBodyPart();
    body.setText(bodyText);

    MimeMultipart multipart = new MimeMultipart();
    multipart.addBodyPart(body);

    email.setContent(multipart);

    return email;
  }

  public static Message createMessageWithEmail(MimeMessage email) throws Exception {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    email.writeTo(buffer);
    String encodedEmail = Base64.getUrlEncoder().encodeToString(buffer.toByteArray());
    Message message = new Message();
    message.setRaw(encodedEmail);
    return message;
  }

  public static MimeMessage createReplyEmail(String from, String to, String subject, String bodyText, String originalMessageId) throws Exception {

    System.out.println("Subject " + subject);

    Properties props = new Properties();
    Session session = Session.getInstance(props, null);

    MimeMessage replyEmail = new MimeMessage(session);

    replyEmail.setFrom(new InternetAddress(from));
    replyEmail.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
    replyEmail.setSubject(subject);

    replyEmail.setHeader("In-Reply-To", originalMessageId);
    replyEmail.setHeader("References", originalMessageId);

    MimeBodyPart body = new MimeBodyPart();
    body.setText(bodyText);

    MimeMultipart multipart = new MimeMultipart("alternative");
    multipart.addBodyPart(body);
    replyEmail.setContent(multipart);

    return replyEmail;
  }

}
