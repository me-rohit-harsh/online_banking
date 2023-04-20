package user;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class UserAllData {

	public static void main(String[] args) throws SQLException {

		// Add a connection
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "rohit");
		// create a statement
		Statement stmt = con.createStatement();
		// Write the query
		String s = "SELECT * FROM user";
		// Execute statement & store data in result set
		ResultSet rs = stmt.executeQuery(s);
		String divider = "+----------+--------------+--------------+------------+--------------+----------+";
		System.out.println(divider);
		System.out.printf("| %8s | %-12s | %-12s | %-10s | %-13s| %8s |%n", "User Id", "First Name", "Last Name"
				, "DOB","Password",
				"Balance");
		System.out.println(divider);
		while (rs.next()) {
			int id = rs.getInt("user_id");
			String Fname = rs.getString("First_Name");
			String Lname = rs.getString("Last_Name");
			String DOB = rs.getString("DOB");
			String pass = rs.getString("password");
			String bal = rs.getString("balance");
			System.out.printf("| %8s | %-12s | %-12s | %-10s | %-12s | %8s |%n", id, Fname, Lname, DOB, pass,
					bal);
		}
		System.out.println(divider);
		// Closing the connection
		con.close();
		System.out.print("**********Query Excuted***********");
	}

}
