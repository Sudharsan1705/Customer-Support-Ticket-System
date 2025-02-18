

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetFeedback extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  Connection con = null;
 
        try {
            con = JDBCConnection.getConnection();
        	HttpSession session = request.getSession(false);
			String name = (String)session.getAttribute("name");
			String portal = (String)session.getAttribute("portal");
                String str = "select ticket_id,subject from tickets where customer_name=? and status_name=? and portal_name=? and ticket_id not in (select ticket_id from customer_feedback)";
                PreparedStatement p = con.prepareStatement(str);
                p.setString(1, name);
                p.setString(2, "Completed");
                p.setString(3, portal);
                ResultSet rs = p.executeQuery();
                
                JSONArray jsonArray = new JSONArray();
                  while(rs.next()){
                    Map<String, String> map = new HashMap<>();
                    map.put("ticket_id", rs.getString(1));
                    map.put("subject", rs.getString(2));
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
