

import javax.servlet.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.http.*;

public class PortalEnter extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession session = request.getSession(false);
            String role = (String)session.getAttribute("role");
             if(role.equals("super_admin")){
                response.sendRedirect("portals.jsp");
             }
             else{
                session.removeAttribute("role");
                response.sendRedirect("PortalView.jsp");
             }
            }
         catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
