

import javax.servlet.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import java.util.HashMap;



public class ViewUser extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
          
        Connection con = null;
 
        try {
            con = JDBCConnection.getConnection();
        	HttpSession session = request.getSession(false);
            String role = (String)session.getAttribute("role");
            String portal = (String)session.getAttribute("portal");
                String str = "select user_name,role_name from portal_assign where portal_name=?";
                PreparedStatement p = con.prepareStatement(str);
                p.setString(1, portal);
                ResultSet rs = p.executeQuery();
                
                JSONArray jsonArray = new JSONArray();
                  while(rs.next()){
                    Map<String, String> map = new HashMap<>();
                    map.put("user_name", rs.getString(1));
                    map.put("role_name", rs.getString(2));
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
