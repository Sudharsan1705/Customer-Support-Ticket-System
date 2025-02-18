

import javax.servlet.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;


public class ViewTicket extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          
        Connection con = null;
 
        try {
            con = JDBCConnection.getConnection();
        	HttpSession session = request.getSession(false);
            String portal = (String)session.getAttribute("portal");
            String name = (String)(session.getAttribute("name"));
            String role = (String)(session.getAttribute("role"));
			ResultSet rs = null;

                if(role.equals("Customer")){
                    String str = "select ticket_id,subject,status_name from tickets where portal_name=? and customer_name=?;";
                    PreparedStatement p = con.prepareStatement(str);
                    p.setString(1, portal);
                    p.setString(2, name);
                    rs = p.executeQuery();
                }
                else if(role.equals("Agent")){
                    String str = "select ticket_id,subject,status_name from tickets where portal_name=? and ticket_id in (select ticket_id from admin_assign where agent_name=?)";
                    PreparedStatement p = con.prepareStatement(str);
                    p.setString(1, portal);
                    p.setString(2, name);
                    rs = p.executeQuery();
                }
                else{
                    String str = "select ticket_id,subject,status_name from tickets where portal_name=?";
                    PreparedStatement p = con.prepareStatement(str);
                    p.setString(1, portal);
                    rs = p.executeQuery();
                }
                
                JSONArray jsonArray = new JSONArray();
                  while(rs.next()){
                    Map<String, String> map = new HashMap<>();
                    map.put("ticket_id", rs.getString(1));
                    map.put("subject", rs.getString(2));
                    map.put("status", rs.getString(3));
                    JSONObject jsonObject = new JSONObject(map);
                    jsonArray.put(jsonObject);
                }
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
        
                response.getWriter().write(jsonArray.toString());
                
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
}
