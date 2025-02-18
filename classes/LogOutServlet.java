

import javax.servlet.*;
import java.io.IOException;
import java.sql.*;
import javax.servlet.http.*;

public class LogOutServlet extends HttpServlet {
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            HttpSession session = request.getSession(false);
            if(session != null)
            session.invalidate();
            response.sendRedirect("index.jsp");
    }
}
