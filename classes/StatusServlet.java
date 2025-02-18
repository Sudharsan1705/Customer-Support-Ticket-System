

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
import javax.swing.plaf.synth.SynthStyle;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.*;
public class StatusServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection con = null;
	     try {
	            con = JDBCConnection.getConnection();

				HttpSession session = request.getSession(false);
				String portalName = (String)(session.getAttribute("portal"));
				String userName = (String)(session.getAttribute("name"));
				String role = (String)(session.getAttribute("role"));
				ResultSet rs = null;

                if(role.equals("Customer")){
					String agent = "select status_name,count(status_name) from tickets where customer_name=? and portal_name=? group by status_name;";
					PreparedStatement ad = con.prepareStatement(agent);
					ad.setString(1, userName);
					ad.setString(2, portalName);
					rs = ad.executeQuery(); 
				}
                else if(role.equals("Agent")){
					String agent = "select status_name,count(status_name) from tickets where ticket_id in (select ticket_id from admin_assign where agent_name=?) and portal_name=? group by status_name;";
					PreparedStatement ad = con.prepareStatement(agent);
					ad.setString(1, userName);
					ad.setString(2, portalName);
					rs = ad.executeQuery(); 
				}
				else if(role.equals("Admin")){	
					String admin = "select status_name,count(status_name) from tickets where ticket_id in (select ticket_id from admin_assign) and portal_name=? group by status_name;";
					PreparedStatement ad = con.prepareStatement(admin);
					ad.setString(1, portalName);
					rs = ad.executeQuery(); 
				}
				else{
					String admin = "select status_name,count(status_name) from tickets where portal_name=? group by status_name;";
					PreparedStatement ad = con.prepareStatement(admin);
					ad.setString(1, portalName);
					rs = ad.executeQuery(); 
				}
				
			
				
				JSONArray jsonArray = new JSONArray();
				while(rs.next()){
					Map<String, String> map = new HashMap<>();
					map.put("status", rs.getString(1));
					map.put("count", Integer.toString(rs.getInt(2)));
					JSONObject jsonObject = new JSONObject(map);
					jsonArray.put(jsonObject);
				}
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");		
								
					if (jsonArray.length() == 0) {
						response.getWriter().write("[]"); 
					}
					else{
	    				response.getWriter().write(jsonArray.toString());
					}

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                con.close();
	            } catch (SQLException e) {
	            	e.printStackTrace();
	            }
	        }
	}

}
