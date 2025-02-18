import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;


public class AddPortalServlet extends HttpServlet {
    static Connection con = JDBCConnection.getConnection();
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String portal = request.getParameter("portal");
        String mail = request.getParameter("mail");
        System.out.println(mail);
        try{
            String checkSql = "select count(*) from portals where portal_name =?";
            PreparedStatement check = con.prepareStatement(checkSql);
            check.setString(1, portal);
            ResultSet rs = check.executeQuery();
            JSONObject jsonResponse = new JSONObject();
            PrintWriter out = response.getWriter();

            if (rs.next() && rs.getInt(1) > 0) {
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Portal already exists");
                out.print(jsonResponse);
                out.flush();
            }
            String str = "insert into portals (portal_name,portal_mail) values(?,?)";
            PreparedStatement p = con.prepareStatement(str);
            p.setString(1, portal);
            p.setString(2, mail);
            p.executeUpdate();
            jsonResponse.put("status", "success");
            jsonResponse.put("message", "Portal added successfully");
            out.print(jsonResponse);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
