import com.google.api.services.gmail.model.WatchRequest;
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
import org.json.JSONArray;

public class LoginServlet extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("username");
        String password = request.getParameter("password");
        String val = request.getParameter("value");
        PrintWriter out = response.getWriter();
		JSONObject jsonResponse = new JSONObject();
        HttpSession ss = request.getSession(false);
        String role = (String)ss.getAttribute("role");

        System.out.println(name);

            try{
                
                if(ss!= null && ss.getAttribute("name")!=null && val.equals("portal")){
                    if(role!= null && role.equals("super_admin"))
                    jsonResponse.put("redirect", "portals.jsp");
                    else
                    jsonResponse.put("redirect", "PortalView.jsp");
                }
                else if(superadmin(name,password)){
                    HttpSession session = request.getSession(false);
                    session.setAttribute("name",name);
                    session.setAttribute("role","super_admin");
                    session.setMaxInactiveInterval(30 * 60);

                    String s= "select count(*) from authentication where user_name=?";
                    PreparedStatement p = con.prepareStatement(s);
                    p.setString(1, name);
                    ResultSet rs = p.executeQuery();
                    if(rs.next() && rs.getInt(1) == 0){
                        System.out.println(rs);
                        jsonResponse.put("redirect", "authenticate");
                    }
                    else{
                    jsonResponse.put("redirect", "portals.jsp");
                   }
            }
            else if(check(name,password)){
                HttpSession session = request.getSession(false);
                session.setAttribute("name",name);
                if((String)session.getAttribute("role") != null){
                    session.removeAttribute("role");
                }
                session.setMaxInactiveInterval(30 * 60);
                String s= "select count(*) from authentication where user_name=?";
                PreparedStatement p = con.prepareStatement(s);
                p.setString(1, name);
                ResultSet rs = p.executeQuery();
    
                if(rs.next() && rs.getInt(1) == 0)
                jsonResponse.put("redirect", "authenticate");
                else{
                jsonResponse.put("redirect", "PortalView.jsp");
                }
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            System.out.println(jsonResponse.toString());
            out.print(jsonResponse.toString());
            out.flush();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
            
            public static boolean superadmin(String name,String password) throws ServletException, IOException{
                PreparedStatement check = null;
                try {
                    String checkSql = "select count(*) from super_admin where s_name = ? and password = ?";
                    check = con.prepareStatement(checkSql);
                    check.setString(1, name);
                    check.setString(2, password);
                    ResultSet rs = check.executeQuery();

            if(rs.next() && rs.getInt(1) != 0){
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean check(String name,String password) throws ServletException, IOException{
        PreparedStatement check = null;
        try {
            String checkSql = "select count(*) from login where user_name = ? and password = ?";
            check = con.prepareStatement(checkSql);
            check.setString(1, name);
            check.setString(2, password);
            ResultSet rs = check.executeQuery();
            if(rs.next() && rs.getInt(1) != 0){
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
