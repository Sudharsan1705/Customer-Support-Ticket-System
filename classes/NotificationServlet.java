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
import com.google.api.services.gmail.model.History;
import com.google.api.services.gmail.model.ListHistoryResponse;
import com.google.api.services.gmail.model.HistoryMessageAdded;
import com.google.api.services.gmail.model.MessagePart;

import com.google.cloud.pubsub.v1.*;

import javax.mail.Session;
import javax.mail.Address;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import org.json.JSONObject;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Flow.Subscriber;

public class NotificationServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      String pubSubMessage = builder.toString();

      JSONObject jsonMessage = new JSONObject(pubSubMessage);

      String encodedData = jsonMessage.getJSONObject("message").getString("data");

      byte[] decodedBytes = Base64.getDecoder().decode(encodedData);
      String decodedMessage = new String(decodedBytes);

      JSONObject decodedJson = new JSONObject(decodedMessage);

      String emailAddress = decodedJson.optString("emailAddress", "Unknown");
      String historyId = decodedJson.optString("historyId", "Unknown");

      Connection con = JDBCConnection.getConnection();

      String name = "";
      String str = "select name from users where mail=?";
      PreparedStatement p = con.prepareStatement(str);
      p.setString(1, emailAddress);
      ResultSet rs = p.executeQuery();
      if (rs.next()) {
        name =rs.getString(1);
      }
      name=name.trim();
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

      HttpTransport httpTransport = new NetHttpTransport();
      JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

      GoogleRefreshTokenRequest tokenRequest = new GoogleRefreshTokenRequest(
        httpTransport, jsonFactory, token, clientId, clientSecret);
      String accessToken = tokenRequest.execute().getAccessToken();

      GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);

      Gmail service = new Gmail.Builder(new NetHttpTransport(), jsonFactory, credential)
        .setApplicationName("MyApp")
        .build();
       
      java.lang.Thread.sleep(2000L);
      fetchMessagesSinceHistoryId(historyId, service, name,response);
      
     
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public void fetchMessagesSinceHistoryId(String id, Gmail service, String name,HttpServletResponse response) {
    try {
      Connection con = JDBCConnection.getConnection();
      String history = "";
      String str = "select history from history where user_name=?";
      PreparedStatement p = con.prepareStatement(str);
      p.setString(1, name);
      ResultSet rs = p.executeQuery();
      if (rs.next()) {
        history = rs.getString(1);
      }

      String up = "update history set history=? where user_name=?";
      PreparedStatement pt = con.prepareStatement(up);
      pt.setString(1, id);
      pt.setString(2, name);
      pt.executeUpdate();

      BigInteger historyId = new BigInteger(history);

      ListHistoryResponse historyResponse = service.users().history().list("me").setStartHistoryId(historyId).execute();

      if(historyResponse.getHistory() != null){

        for (History historyRecord: historyResponse.getHistory()) {
          if (historyRecord.getMessagesAdded() != null) {
            for (HistoryMessageAdded message: historyRecord.getMessagesAdded()) {
              Message emailMessage = service.users().messages().get("me", message.getMessage().getId()).execute();
              
              String fromEmail = getHeaderValue(emailMessage, "From");
              String toEmail = getHeaderValue(emailMessage, "To");
              String subject = getHeaderValue(emailMessage, "Subject");
              String body = getMessageBody(emailMessage);
              body = body.trim();
              System.out.println("From: "+fromEmail);
              System.out.println("To: "+toEmail);
              System.out.println("Subject: "+subject);
              System.out.println("Body: "+body);
              String b[] = body.split(" ");
              String val = "";
              for(int i= 0 ; i < b.length;i++){
                if(b[i].contains("On")){
                  val+=b[i].substring(0,b[i].indexOf("On"));
                  break;
                }
                val+=b[i]+" ";
              }
              body = val; 
              body = body.replaceAll("\\r", ""); 
              body = body.replaceAll("\\n", ""); 
              String messageId = getHeaderValue(emailMessage, "Message-ID");
              String reply = getHeaderValue(emailMessage, "In-Reply-To");
              String threadId = emailMessage.getThreadId();
              MessagePart image=null;
              if (emailMessage.getPayload().getParts() != null) {
                for (MessagePart part : emailMessage.getPayload().getParts()) {
                  if (isImagePart(part)) {
                    image = part;  
                    break;
                  }
                }
              }
            String mail = "";
            int in = 0;
            int cnt = 0;
            if(toEmail.contains("<")){
              
              while(in < toEmail.length()-1){
                if(cnt == 1){
                  mail+=toEmail.charAt(in);
                }
                else if(toEmail.charAt(in) == '<'){
                  cnt++;
                }
                in++;
              }
              toEmail = mail;
            }
            mail ="";
            in = 0;
            cnt = 0;
            if(fromEmail.contains("<")){
              
              while(in < fromEmail.length()-1){
                if(cnt == 1){
                  mail+=fromEmail.charAt(in);
                }
                else if(fromEmail.charAt(in) == '<'){
                  cnt++;
                }
                in++;
              }
              fromEmail = mail;
            }
            String por = "select name from portal where mail=?";
            PreparedStatement prt = con.prepareStatement(por);
            prt.setString(1, toEmail);
            ResultSet rrt = prt.executeQuery();
            String portal ="";
            if(rrt.next()){
              portal = rrt.getString(1).trim();
            }
            
            String cm = "select count(*) from message where message = ?";
            PreparedStatement cs = con.prepareStatement(cm);
            cs.setString(1, messageId);  
            ResultSet cr = cs.executeQuery();
            cr.next();
            int ct = cr.getInt(1);
            
            if(ct == 0){
              
              String upd = "update flag set bool=?";
              PreparedStatement pd = con.prepareStatement(upd);
              pd.setString(1, "true");
              pd.executeUpdate();
              
              if(reply == null && body.length()>0){
                String str1 = "insert into tickets (subject, description, customer_name, created_at, status_name, portal_name) VALUES (?, ?, ?, NOW(), ?, ?)";
                PreparedStatement insert = con.prepareStatement(str1);
                insert.setString(1, subject);
                insert.setString(2, body);
                insert.setString(3, name);
                insert.setString(4, "Pending");
                insert.setString(5, portal);
                insert.executeUpdate();
                
                String st = "select ticket_id from tickets where portal_name=? and customer_name=? and subject=? and description=?";
                PreparedStatement check = con.prepareStatement(st);
                check.setString(1, portal);
                check.setString(2, name);
                check.setString(3, subject);
                check.setString(4, body);
                ResultSet rs1 = check.executeQuery();
                int id1 = 0;
                if (rs1.next()) {
                  id1 = rs1.getInt(1);
                }
                
                
                String s1 = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
                PreparedStatement pt1 = con.prepareStatement(s1);
                pt1.setInt(1, 0);
                pt1.setString(2, toEmail);
                pt1.setString(3, fromEmail);
                pt1.setString(4, body);
                pt1.setInt(5, id1);
                int count1 = pt1.executeUpdate();
                int mid = 0;
                
                String m = "select child_id from thread where ticket_id=?";
      PreparedStatement pm = con.prepareStatement(m);
      pm.setInt(1, id1);
      ResultSet rm = pm.executeQuery();
      if (rm.next()) {
        mid = rm.getInt(1);
      }
      
      String d = "update thread set parent_id=? where ticket_id=?";
      PreparedStatement d1 = con.prepareStatement(d);
      d1.setInt(1, mid);
      d1.setInt(2, id1);
      d1.executeUpdate();
      
      String msg = "insert into message(child,message,thread_id) values(?,?,?)";
      PreparedStatement ms = con.prepareStatement(msg);
      ms.setInt(1, mid);
      ms.setString(2, messageId);
      ms.setString(3, threadId);
      ms.executeUpdate();
    }
    else{
      
      String f = "select child from message where message=?";
      PreparedStatement pf = con.prepareStatement(f);
      pf.setString(1, reply);
      ResultSet rf = pf.executeQuery();
      int parent_id = 0;
      if(rf.next()){
        parent_id = rf.getInt(1);
      }
      String st = "select ticket_id from thread where child_id=?";
      PreparedStatement p1 = con.prepareStatement(st);
      p1.setInt(1, parent_id);
      ResultSet r1 = p1.executeQuery();
      int ticket_id = 0;
      if(r1.next()){
        ticket_id = r1.getInt(1);
      }
      
      String s = "insert into thread(parent_id,from_mail,to_mail,message,time,ticket_id) values(?,?,?,?,NOW(),?)";
      
      PreparedStatement pt2 = con.prepareStatement(s);
      pt2.setInt(1, parent_id);
      pt2.setString(2, fromEmail);
      pt2.setString(3, toEmail);
      pt2.setString(4, body);
      pt2.setInt(5, ticket_id);
      int count = pt2.executeUpdate();
      
      
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
                if(image != null && image.getBody() != null){
                  byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(image.getBody().getData());
                  ByteArrayInputStream imageInputStream = new ByteArrayInputStream(decodedBytes);
                  System.out.println(child_id);
                  String str1 = "insert into image(child_id,image,time) values(?,?,NOW())";
                  PreparedStatement ip= con.prepareStatement(str1);
                  ip.setInt(1, child_id);
                  ip.setBinaryStream(2, imageInputStream,decodedBytes.length);
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
                if(image != null && image.getBody() != null){
                  byte[] decodedBytes = java.util.Base64.getUrlDecoder().decode(image.getBody().getData());
                  ByteArrayInputStream imageInputStream = new ByteArrayInputStream(decodedBytes);
                  System.out.println(child_id+" "+imageInputStream);
                  String str3 = "insert into image(child_id,image,time) values(?,?,NOW())";
                  PreparedStatement ip= con.prepareStatement(str3);
                  ip.setInt(1, child_id);
                  ip.setBinaryStream(2, imageInputStream,decodedBytes.length);
                  ip.executeUpdate();
                  
                }
                String msg = "insert into message(child,message,thread_id) values(?,?,?)";
                PreparedStatement ms = con.prepareStatement(msg);
                ms.setInt(1, child_id);
                ms.setString(2, messageId);
                ms.setString(3, threadId);
                ms.executeUpdate();
              }
            }
          }
        }
      }
    }
  }
  } catch (Exception e) {
    e.printStackTrace();
  }
}

  public boolean isImagePart(MessagePart part) {
  return part.getMimeType().startsWith("image/");
  }

  private String getHeaderValue(Message email, String headerName) {
    for (MessagePartHeader header: email.getPayload().getHeaders()) {
      if (header.getName().equals(headerName)) {
        return header.getValue();
      }
    }
    return null;
  }

  private String getMessageBody(Message message) {
    StringBuilder body = new StringBuilder();

    if (message.getPayload().getParts() != null) {
      for (MessagePart part: message.getPayload().getParts()) {
        if ("text/plain".equals(part.getMimeType())) {
          byte[] bodyData = java.util.Base64.getUrlDecoder().decode(part.getBody().getData());
          body.append(new String(bodyData));
        }
      }
    }
    return body.toString();
  }
}


// {
//   "history": [
//     {
//       "id": "123456",
//       "messagesAdded": [
//         {
//           "message": {
//             "id": "abcdef123",
//             "threadId": "1234abcd"
//           }
//         }
//       ],
//       "messagesDeleted": [
//         {
//           "message": {
//             "id": "ghijkl456",
//             "threadId": "5678efgh"
//           }
//         }
//       ],
//       "labelsAdded": [
//         {
//           "message": {
//             "id": "mnopqr789",
//             "threadId": "9101ijkl"
//           },
//           "labelIds": ["IMPORTANT", "INBOX"]
//         }
//       ],
//       "labelsRemoved": [
//         {
//           "message": {
//             "id": "stuvwx012",
//             "threadId": "3141mnop"
//           },
//           "labelIds": ["SPAM"]
//         }
//       ]
//     }
//   ],
//   "nextPageToken": "next-page-token",
//   "historyId": "123457"
// }


// {
//   "id": "abcdef12345",
//   "threadId": "12345thread",
//   "labelIds": ["INBOX", "UNREAD"],
//   "snippet": "This is a short preview of the email content...",
//   "payload": {
//     "partId": "",
//     "mimeType": "multipart/alternative",
//     "headers": [
//       { "name": "From", "value": "sender@example.com" },
//       { "name": "To", "value": "recipient@example.com" },
//       { "name": "Subject", "value": "Meeting Reminder" },
//       { "name": "Date", "value": "Mon, 25 Dec 2023 10:00:00 -0700" }
//     ],
//     "body": {
//       "size": 0
//     },
//     "parts": [
//       {
//         "partId": "0",
//         "mimeType": "text/plain",
//         "headers": [],
//         "body": {
//           "size": 1024,
//           "data": "VGhpcyBpcyBhIHBsYWluIHRleHQgbWVzc2FnZSBib2R5Lg=="
//         }
//       },
//       {
//         "partId": "1",
//         "mimeType": "text/html",
//         "headers": [],
//         "body": {
//           "size": 2048,
//           "data": "PGRpdj5UaGlzIGlzIGFuIEhUTUwgZW1haWwgYm9keS48L2Rpdj4="
//         }
//       }
//     ]
//   },
//   "sizeEstimate": 2560,
//   "historyId": "98765"
// }
