import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.function.Predicate;

import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try{
		Connection con = JDBCConnection.getConnection();
		response.setContentType("text/event-stream");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Cache-Control", "no-cache"); 
		response.setHeader("Connection", "keep-alive");
		PrintWriter out = response.getWriter();
			String upd = "select bool from flag";
			PreparedStatement pd = con.prepareStatement(upd);
			ResultSet rs = pd.executeQuery();
			if(rs.next() && rs.getString(1).equals("true")){
				System.out.println("test");
				out.write("data: added.\n\n");
			    out.flush();
				 String upd1 = "update flag set bool=?";
				PreparedStatement pd1 = con.prepareStatement(upd1);
				pd1.setString(1, "false");
				pd1.executeUpdate();
			}
			java.lang.Thread.sleep(5000L);
			}catch(Exception e){
				e.printStackTrace();
			}
				
	}


}
