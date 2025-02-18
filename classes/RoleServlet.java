

import java.io.IOException;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.json.JSONObject;

public class RoleServlet extends HttpServlet {
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection con = null;
		try {
			    con = JDBCConnection.getConnection();
			
			    String name = request.getParameter("name");
			    String role = request.getParameter("role");

				HttpSession session = request.getSession(false);

				String portal =(String)session.getAttribute("portal");
				String userrole =(String)session.getAttribute("role");
				JSONObject jsonResponse = new JSONObject();
				PrintWriter out = response.getWriter();
				
				if(userrole.equals("Admin") && role.equals("Admin")){
					jsonResponse.put("status", "success");
					jsonResponse.put("message", "Admin cannot add admin");
					out.print(jsonResponse);
					out.flush();
				}
				else{
					String check = "select count(*) from login where user_name=?";
					PreparedStatement pts = con.prepareStatement(check);
					pts.setString(1, name);
					ResultSet rs1 = pts.executeQuery();
					
					if(rs1.next() && rs1.getInt(1) == 0){
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "Invalid username");
						out.print(jsonResponse.toString());
						out.flush();
					}
					else{
					if(role != null){
						String s = "select count(*) from roles where role_name=?";
						PreparedStatement statement = con.prepareStatement(s);
						statement.setString(1, role);
						ResultSet rs = statement.executeQuery();
						rs.next();
						int count = rs.getInt(1);
						
						if(count == 0){
							String str = "insert into roles (role_name) values (?)";
							PreparedStatement p = con.prepareStatement(str);
							p.setString(1, role);
							p.executeUpdate();
						}
					}

					
					String res = "select count(*) from portal_assign where user_name=? and portal_name=?";
					PreparedStatement stmt = con.prepareStatement(res);
					stmt.setString(1, name);
					stmt.setString(2, portal);
					ResultSet r = stmt.executeQuery();
					r.next();
					int cnt = r.getInt(1);
					
					System.out.println(cnt);
					if(cnt == 0){
						String st = "insert into portal_assign (portal_name,role_name,user_name) values (?,?,?)";
						PreparedStatement pt = con.prepareStatement(st);
						pt.setString(1, portal);
						pt.setString(2, role);
						pt.setString(3, name);
						int ct = pt.executeUpdate();
						jsonResponse.put("status", "success");
						jsonResponse.put("message", "User added into portal");
						out.print(jsonResponse);
						out.flush();
				}
				else{
					jsonResponse.put("status", "success");
					jsonResponse.put("message", "User already exists");
					out.print(jsonResponse);
					out.flush();
				}
			}
			}
			}
	        catch(Exception e){
				e.printStackTrace();
			}
		}

	}
	