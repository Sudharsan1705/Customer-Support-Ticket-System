import java.io.IOException;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class PortalServlet extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
                
        try  {
           HttpSession session = request.getSession(false);
           if(session == null){
            response.sendRedirect("Login.jsp");
        }
        else{
            String name = (String) session.getAttribute("name");
            String role = (String) session.getAttribute("role");
            System.out.println(name+" "+role);
            if(name != null){
                JSONArray jsonArray = new JSONArray();
                 String s = null;
                 if(role!=null && role.equals("super_admin"))
                 s = "select portal_name from portals";
                 else
                 s = "select portal_name from portal_assign where user_name=?";
                 PreparedStatement p = con.prepareStatement(s);
                 if(role == null || !role.equals("super_admin"))
                 p.setString(1, name);
                 ResultSet rs = p.executeQuery();
                 while(rs.next()){
                     Map<String, String> map = new HashMap<>();
                     map.put("portal_name", rs.getString(1));
                     JSONObject jsonObject = new JSONObject(map);
                     jsonArray.put(jsonObject);
                 }
                 System.out.print(jsonArray);
                 response.setContentType("application/json");
                 response.setCharacterEncoding("UTF-8");
         
                 response.getWriter().write(jsonArray.toString());

            }
            else
            response.sendRedirect("Login.jsp");

           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
