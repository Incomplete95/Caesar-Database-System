/**
 * Created by Incomplete on 11/12/17.
 */

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SQLDriver {

    public static void main(String [] args) {
        Connection con;
        try
        {
            Class.forName(Constants.DRIVER);
            con = DriverManager.getConnection(Constants.URL,Constants.USER,Constants.PASSWORD);
            if(!con.isClosed())
                System.out.println("Succeeded connecting to the Database!");
            User user = new User(tryLogin(con), con);
            showQuarterAndYear(user);
            while(true) {
                System.out.println( "Please choose your operation: " );
                printChoices();
                Scanner scanner = new Scanner( System.in );
                String choice = scanner.nextLine();
                doOperation(con, choice.trim().toLowerCase(), user);
            }
        }
        catch(ClassNotFoundException e)
        {
            //数据库驱动类异常处理
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        }
        catch(SQLException e)
        {
            //数据库连接失败异常处理
            e.printStackTrace();
        }
        catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
        }
        finally
        {
            System.out.println("Exit System Succeed!");
        }
    }

    /**
     * Try to login into the system
     *
     * @param con
     *              The database connection
     * @return
     *              The map storing user's name and id
     */
    private static Map<String, String> tryLogin(Connection con) {
        Statement statement;
        ResultSet resTrySearchLoginName;
        Map<String, String> map = new HashMap<>();
        String correctPassword = null;
        try {
            statement = con.createStatement();
            while (map.get(Constants.NAME) == null) {
                Scanner scanner = new Scanner( System.in );
                System.out.print( "Please input your name: " );
                //studentName = scanner.nextLine();
                String trySearchLoginName = Constants.SELECT +
                        Constants.ALL +
                        Constants.FROM +
                        "student" +
                        Constants.WHERE +
                        "Name='" +
                        scanner.nextLine() +
                        Constants.SINGLE_QUOTE;
                resTrySearchLoginName = statement.executeQuery(trySearchLoginName);

                while(resTrySearchLoginName.next()) {
                    map.put(Constants.NAME, resTrySearchLoginName.getString(Constants.NAME));
                    map.put(Constants.ID, resTrySearchLoginName.getString(Constants.ID));
                    correctPassword = resTrySearchLoginName.getString("Password");
                    System.out.println("Welcome, " + map.get(Constants.NAME) + "!");
                }
                if (map.get(Constants.NAME) == null) {
                    System.out.println("Student name doesn't exist in system.");
                    System.out.println("Please check the spelling of both first name and last name.");
                    System.out.println("If spelling is correct, please contact system administrator immediately.");
                }
                resTrySearchLoginName.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        int failCount = 0, totalChances = 5;
        while(failCount < totalChances) {
            Scanner scanner = new Scanner( System.in );
            System.out.print("Please input your password: ");
            String password = scanner.nextLine();
            if (password.equals(correctPassword)) {
                System.out.println("Login success!");
                return map;
            } else {
                failCount ++;
                System.out.println("Incorrect password, you have " +
                        (totalChances - failCount) +
                        " more times to try login");
            }
        }
        System.exit(0);
        return null;
    }

    /**
     * print current year and quarter
     *
     * @param user
     *              The instance of user
     */
    private static void showQuarterAndYear(User user) {
        user.refreshTime();
        System.out.println("Year: " + user.year);
        System.out.println("Quarter: " + user.quarter);
    }

    /**
     * Print all the choices
     */
    private static void printChoices() {
        for (String choice : Constants.CHOICES_SET) {
            System.out.println("* " + choice);
        }
    }

    /**
     * Execute the core operations
     *
     * @param con
     *              The database connection
     * @param choice
     *              User's choice
     * @param user
     *              The instance of user
     */
    private static void doOperation(Connection con, String choice, User user) {
        if (Constants.CHOICES_SET.contains(choice)) {
            try {
               switch (choice) {
                   case "personal details":
                       printPersonalInfoOp(user);
                       break;
                   case "transcript":
                       printTransciptsOp(user);
                       break;
                   case "enroll":
                       enrollOp(user);
                       break;
                   case "withdraw":
                       withdrawOp(user);
                       break;
                   default:
                       con.close();
                       System.out.println("Exit System Succeed!");
                       System.exit(0);
               }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot recognize your choice, please re-enter.");
        }
    }

    /**
     * The operations for printing personal information
     *
     * @param user
     *              The instance of user
     */
    private static void printPersonalInfoOp(User user) {
        user.printInfo();
        System.out.println("Do you want to change password or address?");
        System.out.println("* Password(p)");
        System.out.println("* Address(a)");
        System.out.println("* No(n)");
        Scanner scanner = new Scanner( System.in );
        String infoToChange = scanner.nextLine().trim().toLowerCase();
        infoToChange = infoToChange.substring(0, 1).toUpperCase() + infoToChange.substring(1);
        if (!(infoToChange.equals("No") || infoToChange.equals("n"))) {
            if (infoToChange.startsWith("P")) {
                infoToChange = "Password";
            } else {
                infoToChange = "Address";
            }
            scanner = new Scanner( System.in );
            System.out.print("Please input the new " + infoToChange + " you want to change:");
            String newInfo = scanner.nextLine();
            user.changeInfo(newInfo, infoToChange);
        }
    }

    /**
     * The operations for printing transcripts
     *
     * @param user
     *              The instance of user
     */
    private static void printTransciptsOp(User user) {
        try {
            Statement statement = user.createStatement();
            String Id = user.getId();
            String genTranscriptsQuery =
                    Constants.SELECT +
                    "UoSCode, Grade" +
                    Constants.FROM +
                    "transcript" +
                    Constants.WHERE +
                    "StudId" +
                    Constants.EQUAL +
                    Constants.SINGLE_QUOTE +
                    user.getId() +
                    Constants.SINGLE_QUOTE;
            ResultSet resultSet = statement.executeQuery(genTranscriptsQuery);
            System.out.println("Course ID \t Grades");
            while(resultSet.next()) {
                System.out.println(resultSet.getString("UoSCode") +
                    " \t " +
                    resultSet.getString("Grade"));
            }
            while(true) {
                System.out.println("Please choose your operation:");
                System.out.println("* View details of course(v)");
                System.out.println("* Back to previous page(b)");
                Scanner scanner = new Scanner( System.in );
                String choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("view") || choice.equals("v")) {
                    viewCourseDetails(user);
                } else {
                    break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print the details of one course
     *
     * @param user
     *              The instance of user
     */
    private static void viewCourseDetails(User user) {
        System.out.print("Please input the course ID you want to view(Caution: Case sensitive):");
        Scanner scanner = new Scanner( System.in );
        String courseId = scanner.nextLine().trim();
        
    }

    /**
     * The operations for enrolling
     *
     * @param user
     *              The instance of user
     */
    private static void enrollOp(User user) {

    }

    /**
     * The operations for withdrawing
     *
     * @param user
     *              The instance of user
     */
    private static void withdrawOp(User user) {

    }
}