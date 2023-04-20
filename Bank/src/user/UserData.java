package user;

import java.time.Year;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.Connection;

public class UserData {
	public static void Make_Connection() throws SQLException {
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "rohit");
	}

	public static void DOB_val(Connection con, String Fname, String Lname) throws SQLException {
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your DOB (dd/mm/yyyy): ");
		String input = sc.nextLine();
		String[] parts = input.split("/");
		int day, month, year;
		try {
			day = Integer.parseInt(parts[0]);
			month = Integer.parseInt(parts[1]);
			year = Integer.parseInt(parts[2]);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid input format.");
			main(parts);
			return;
		}
		if (day < 1 || day > 31 || month < 1 || month > 12 || year < 0) {
			System.out.println("Error:Invalid DOB. Please re-enter the DOB in the format ");
			DOB_val(con, Fname, Lname);
		}
		if (month == 2) {
			if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
				if (day < 1 || day > 29) {
					System.out.println("Error:Invalid DOB. Please re-enter the DOB in the format ");
					DOB_val(con, Fname, Lname);
				}
			} else {
				if (day < 1 || day > 28) {
					System.out.println("Error:Invalid DOB. Please re-enter the DOB in the format ");
					DOB_val(con, Fname, Lname);
				}
			}
		} else if (month == 4 || month == 6 || month == 9 || month == 11) {
			if (day < 1 || day > 30) {
				System.out.println("Error:Invalid DOB. Please re-enter the DOB in the format ");
				DOB_val(con, Fname, Lname);
			}
		}
		Year currentYear = Year.now();
		if (currentYear.getValue() == year || currentYear.getValue() > year) {
			if (currentYear.getValue() - year == 10 || currentYear.getValue() - year > 10) {
				System.out.print("Your Address : ");
				String Address = sc.nextLine();
				System.out.print("Create a password : ");
				String MyPass = sc.next();
				// create a statement
				Statement stmt = con.createStatement();
				// Write the query
				String s = "INSERT INTO user(First_Name,Last_Name,DOB,password,Address) VALUES('" + Fname + "','"
						+ Lname
						+ "','" + input + "','"
						+ MyPass + "','" + Address + "')";
				// Execute statement
				stmt.execute(s);
				// Running query to get the newly added user's id
				String id = "SELECT user_id FROM user ORDER BY user_id DESC LIMIT 1;";
				// store data in result set
				ResultSet rs = stmt.executeQuery(id);
				int user_id = 0;
				while (rs.next()) {
					user_id = rs.getInt("user_id");
				}
				System.out.println("Your User Id is " + user_id + " You can use it to login back");
				Registred_user(con);
			} else {
				System.out.println("You must be at least 10 years old.");
				main(parts);
				return;
			}
		} else {
			System.out.println("Invalid year: year of birth can't be in the future.");
			DOB_val(con, Fname, Lname);
		}
	}

	public static void Send_money(int reg_id, Connection con) throws SQLException {
		System.out.print("Enter the recipient's ID : ");
		Scanner sc = new Scanner(System.in);
		int rec_id = sc.nextInt();
		if (rec_id == reg_id) {
			System.out.println("You can not transfer money in your own");
			Send_money(reg_id, con);
		} else {
			Make_Connection();
			// Create a statement
			Statement send = con.createStatement();
			// Running query to get the user's id
			String get_id = "SELECT user_id FROM user ORDER BY user_id;";
			// Excuting the statement & store data
			ResultSet RS = send.executeQuery(get_id);
			int Hcount = 0;
			while (RS.next()) {
				int user_id = RS.getInt("user_id");
				if (user_id == rec_id) {
					Hcount++;
					System.out.print("Enter the ammount to send : ");
					Float money = sc.nextFloat();
					if (money > 0) {
						if (money < Available_Bal(reg_id, con)) {
							// Create a statement
							Statement Tmoney = con.createStatement();
							// Running query to get the user's id
							String get_money = "SELECT Balance FROM user WHERE user_id= '" + rec_id + "'";
							// Execute the statement and store the value
							RS.close();
							RS = Tmoney.executeQuery(get_money);
							while (RS.next()) {
								Float rec_money = RS.getFloat("Balance");
								rec_money = rec_money + money;
								Deposite_Money(rec_id, rec_money, con);
							}
							// Create a statement
							Statement Rmoney = con.createStatement();
							// Writing the query to update the balance of the sender
							String UBal = "UPDATE user SET Balance='" + (Available_Bal(reg_id, con) - money)
									+ "' WHERE user_id='" + reg_id + "'";
							// Execute the statement
							Rmoney.execute(UBal);
							Main_page(reg_id, con);
						} else {
							System.out.println("Insufficient Balance!!");
							Main_page(reg_id, con);
						}
					} else {
						System.out.println("Money can not be in negative");
						Main_page(reg_id, con);
					}
				}
			}
			if (Hcount == 0) {
				System.out.println("Incorrect Id please check and try again!");
				// Send_money(reg_id, con);
				// Return to the send page
				Main_page(reg_id, con);
			}

		}
	}

	public static void Add_user(Connection con) throws SQLException {
		Make_Connection();
		System.out.print("Enter your First Name : ");
		Scanner sc = new Scanner(System.in);
		String Fname = sc.nextLine();
		System.out.print("Enter your Last Name : ");
		String Lname = sc.nextLine();
		DOB_val(con, Fname, Lname);

	}

	public static void Deposite_Money(int reg_id, Float Total, Connection con) throws SQLException {
		Make_Connection();
		// create a statement
		Statement UpdateBal = con.createStatement();
		// Write query to fetch data
		String UBal = "UPDATE user SET Balance='" + Total + "' WHERE user_id='" + reg_id + "'";
		// Excute query
		UpdateBal.execute(UBal);
		System.out.println("******Succesful******");
	}

	public static float Available_Bal(int reg_id, Connection con) throws SQLException {
		Make_Connection();
		// Create a ststement
		Statement ViewBal = con.createStatement();
		// Write query to fetch data
		String ViewBalance = "Select Balance FROM user WHERE user_id='" + reg_id + "' ";

		ResultSet RS = ViewBal.executeQuery(ViewBalance);
		while (RS.next()) {
			Float TotalBalance = RS.getFloat("Balance");
			return TotalBalance;
		}
		return 0;
	}

	public static void delete_ac(int reg_id, Connection con) throws SQLException {
		Make_Connection();
		// create a statement
		Statement del = con.createStatement();
		// Write the query
		String Delete = "DELETE FROM user WHERE user_id='" + reg_id + "';";
		// Execute statement
		del.execute(Delete);
		System.out.println("******Succesful******");
	}

	public static void Main_page(int reg_id, Connection con) throws SQLException {
		// Create a connection
		Make_Connection();

		System.out.println(
				"a) Cash Deposite  \nb) View Balance \nc) Withdraw Money \ns) Send Money \nd) Previous Transaction \nf) Upadte Name\ng) Change Password\nh) Delete Account \ne) Exit ");
		Scanner sc = new Scanner(System.in);
		String Update = sc.next();
		switch (Update) {
			case "s":
				Send_money(reg_id, con);
				break;
			case "a":
				// create a statement
				Statement Bal = con.createStatement();
				// Write query to fetch data
				String balance = "SELECT Balance FROM user WHERE user_id='" + reg_id + "';";
				// execute the statement
				ResultSet RS = Bal.executeQuery(balance);
				Float Total = 0.00f;
				while (RS.next()) {
					Total = RS.getFloat("Balance");
				}
				System.out.print("Add money to add in your account : ");
				Float AddBal = sc.nextFloat();
				if (AddBal > 0) {
					Total = Total + AddBal;
					Deposite_Money(reg_id, Total, con);
					System.out.println("Total balance is :" + Available_Bal(reg_id, con));
					Main_page(reg_id, con);
				} else {
					System.out.println("Money can not be in negative");
					Main_page(reg_id, con);
				}
				break;
			case "b":
				System.out.println("Availabe Balance is : " + Available_Bal(reg_id, con));
				Main_page(reg_id, con);
				break;
			case "c":
				System.out.print("Enter the amount : ");
				Float amount = sc.nextFloat();
				if (amount > 0) {
					if (amount < Available_Bal(reg_id, con)) {
						Float updatedBal = Available_Bal(reg_id, con) - amount;
						Deposite_Money(reg_id, updatedBal, con);
						System.out.println("Availabe balance is :" + Available_Bal(reg_id, con));
					} else {
						System.out.println("Insufficient Balance!!");
					}
					Main_page(reg_id, con);
				} else {
					System.out.println("Amount can not be in negative");
					Main_page(reg_id, con);
				}
				break;
			case "f":
				System.out.print("Enter your First Name : ");
				String Fname = sc.next();
				System.out.print("Enter your Last Name :");
				String Lname = sc.next();
				// create a statement
				Statement Upstmt = con.createStatement();
				// Write the query
				String UName = "UPDATE user SET First_Name='" + Fname + "',Last_Name='" + Lname
						+ "' WHERE user_id='" + reg_id + "'";
				// Execute statement
				Upstmt.execute(UName);
				System.out.println("******Succesful******");
				Main_page(reg_id, con);
				break;
			case "g":
				System.out.print("Enter your new password : ");
				String pass = sc.next();
				// create a statement
				Statement UpPass = con.createStatement();
				// Write the query
				String UPass = "UPDATE user SET password='" + pass + "' WHERE user_id='"
						+ reg_id + "'";
				// Execute statement
				UpPass.execute(UPass);
				System.out.println("******Succesful******");
				Main_page(reg_id, con);
				break;
			case "h":
				System.out.println("Are you sure to delete your bank account");
				System.out.print("Y/N\nTell us your desicion : ");
				String des = sc.next();
				switch (des) {
					case "Y":
						delete_ac(reg_id, con);
						break;
					case "y":
						delete_ac(reg_id, con);
						break;
					case "N":
						System.out.println("We glad to have you :)");
						Main_page(reg_id, con);
						break;
					case "n":
						System.out.println("We glad to have you :)");
						Main_page(reg_id, con);
						break;
					default:
						System.out.println("Choose a correct option to proceed");
						Main_page(reg_id, con);
				}
				break;
			case "e":
				break;
			default:
				System.out.println("Choose a correct option to proceed");
		}
	}

	public static void Registred_user(Connection con) throws SQLException {
		System.out.print("Enter you user id : ");
		Scanner sc = new Scanner(System.in);
		int reg_id = sc.nextInt();
		// Add a connection
		Make_Connection();
		// create a statement
		Statement stmt = con.createStatement();
		// Running query to get the user's id
		String id = "SELECT user_id FROM user ORDER BY user_id;";
		// Excuting the statement
		ResultSet RS = stmt.executeQuery(id);
		// store data in resultset
		int user_id = 0;
		int count = 0;
		while (RS.next()) {
			user_id = RS.getInt("user_id");
			if (user_id == reg_id) {
				count++;
				// create a statement
				Statement stmtB = con.createStatement();
				// writing the query to fetch the desired details
				String name = "SELECT First_Name FROM user WHERE user_id ='" + reg_id + "';";
				// Excuting the statement
				RS.close();
				RS = stmtB.executeQuery(name);
				// Storing the data
				String F_Name = "";
				while (RS.next()) {
					F_Name = RS.getString("First_Name");
				}
				System.out.println("**********Welcome Back " + F_Name + "**********");
				System.out.print("Enter the Password :");
				String pass = sc.next();
				String bj = "SELECT password FROM user WHERE user_id = " + reg_id + ";";
				// Execute statement & store data in resultset

				RS = stmt.executeQuery(bj);
				while (RS.next()) {
					String UserPassword = RS.getString("password");
					if (pass.equals(UserPassword)) {
						Main_page(reg_id, con);
					} else {
						System.out.println("Incorrect password please try again");
					}
				}

			}
		}
		if (count == 0) {
			System.out.println("You are not registered please register your self");
			Add_user(con);
		}
		return;
	}

	public static void main(String[] args) throws SQLException {

		// Add a connection
		Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "rohit");
		// Taking input from the user First_Name & Last_Name

		Scanner sc = new Scanner(System.in);
		System.out.println("a) New User \nb) Registred User \ne) Exit");
		String type = sc.next();
		switch (type) {
			case "a":
				System.out.println("**********Welcome**********");
				Add_user(con);
				Registred_user(con);
				break;
			case "b":
				Registred_user(con);
				break;
			case "e":
				break;
			default:
				System.out.println("Choose a correct option to proceed");
		}

		// Closing the connection
		con.close();

		System.out.print("********** Thank You For Choosing Us :) ***********");
	}

}
