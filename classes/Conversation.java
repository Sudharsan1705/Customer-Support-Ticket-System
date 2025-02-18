import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class Conversation extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            int id = Integer.parseInt(request.getParameter("ticket_id"));
                
        try  {
                HttpSession session = request.getSession(false);
                session.setAttribute("ticket_id",id);
                JSONArray jsonArray = new JSONArray();
                 String s = "select from_mail,to_mail,message,child_id from thread where ticket_id=? order by time desc";
                 PreparedStatement p = con.prepareStatement(s);
                 p.setInt(1, id);
                 ResultSet rs = p.executeQuery();
                 while(rs.next()){
                     Map<String, String> map = new HashMap<>();
                     map.put("from_mail", rs.getString(1));
                     map.put("to_mail", rs.getString(2));
                     map.put("message", rs.getString(3));
                     map.put("child_id", Integer.toString(rs.getInt(4)));
                     int cid = rs.getInt(4);
                     String iq = "select image from image where child_id = ?";
                        PreparedStatement im = con.prepareStatement(iq);
                        im.setInt(1, cid);
                        ResultSet ir = im.executeQuery();

                        if (ir.next()) {
                            byte[] imageBytes = ir.getBytes("image");
                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                            map.put("image", base64Image); 
                        }
                     JSONObject jsonObject = new JSONObject(map);
                     jsonArray.put(jsonObject);
                 }
                 response.setContentType("application/json");
                 response.setCharacterEncoding("UTF-8");
         
                 response.getWriter().write(jsonArray.toString());
            }

         catch (Exception e) {
            e.printStackTrace();
        }
    }
}
