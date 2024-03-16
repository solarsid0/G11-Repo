// Import necessary packages
package payrollsystemMPH;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

//Define the main class for the payroll system
public class payrollSystem {

 // Define a date format for handling time
 private static final SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");
 private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

 // Method to calculate hours worked based on time-in and time-out
 private static double calculateHoursWorked(Date timeIn, Date timeOut) {
     // Calculating the difference in milliseconds
     long difference = timeOut.getTime() - timeIn.getTime();

     // Converting milliseconds to hours
     double hoursWorked = difference / (1000.0 * 60 * 60);

     return hoursWorked;
 }

 // Method to get the week number based on a starting date and 7-day increment
 private static String getWeekNumber(Date startDate, Date currentDate) {
     long diffInMillies = currentDate.getTime() - startDate.getTime();
     long diffInDays = diffInMillies / (1000 * 60 * 60 * 24);
     int weekNumber = (int) (diffInDays / 7) + 1;
     return String.format("%02d", weekNumber); // Format week number with leading zeros if it has only one digit
 }

 // Main method where the execution of the program starts
 @SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {

        // CSV file paths for employee details, attendance records, and hourly rates
        String EmployeeDetails = "src\\EMployee Details.csv";
        String AttendanceRecord = "src\\Attendance Record.csv";
        String HourlyRateFile = "src\\Hourly Rate.csv";

     // Initialize employee ID outside the loop
        int employeeID = 0;
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // Prompt user for Employee ID or exit command
                System.out.print("\nEnter Employee ID (or type 'exit' to stop): ");
                String userInput = scanner.nextLine();

                // Exit prompt
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("\nExiting the program.");
                    break;
                }

                try {
                    // Parse user input to obtain employee ID
                    employeeID = Integer.parseInt(userInput);
                } catch (NumberFormatException e) {
                    System.out.println("\nInvalid input. Please enter a valid Employee ID.");
                    continue;
                }

                // Read employee details from CSV
                boolean foundEmployeeID = false;
                String employeeLastName = "";
                String employeeFirstName = "";
                String birthday = "";
                String phoneNumber = "";
                String status = "";
                String position = "";
                String immediateSupervisor = "";
                double hourlyRate = 0.0;

                try (BufferedReader detailsReader = new BufferedReader(new FileReader(EmployeeDetails))) {
                    String line;
                    boolean isFirstLine = true;
                    while ((line = detailsReader.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }
                        String[] employeeDetailsArray = line.split(",");
                        int currentEmployeeID = Integer.parseInt(employeeDetailsArray[0].trim());

                        // Check if the current line corresponds to the given employee ID
                        if (currentEmployeeID == employeeID) {
                            foundEmployeeID = true;
                            // Extract employee details
                            employeeLastName = employeeDetailsArray[1].trim();
                            employeeFirstName = employeeDetailsArray[2].trim();
                            birthday = employeeDetailsArray[3].trim();
                            phoneNumber = employeeDetailsArray[4].trim();
                            status = employeeDetailsArray[5].trim();
                            position = employeeDetailsArray[6].trim();
                            immediateSupervisor = employeeDetailsArray[7].trim();
                            for (int i = 8; i < employeeDetailsArray.length; i++) {
                                immediateSupervisor += ", " + employeeDetailsArray[i].trim();
                            }
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("\nError: Employee details file not found.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("\nError reading employee details.");
                    e.printStackTrace();
                }

                // Handle the case where the employee ID is not found
                if (!foundEmployeeID) {
                    System.out.println("\nEmployee ID not found.");
                    continue;
                }

                // Display employee details
                System.out.println("\n            EMPLOYEE DETAILS");
                System.out.println("\nEmployee Last Name         : " + employeeLastName);
                System.out.println("Employee First Name        : " + employeeFirstName);
                System.out.println("Birthday                   : " + birthday);
                System.out.println("Phone Number               : " + phoneNumber);
                System.out.println("Status                     : " + status);
                System.out.println("Position                   : " + position);
                System.out.println("Immediate Supervisor       : " + (immediateSupervisor.equals("N/A") ? "N/A" : immediateSupervisor));

                // Read hourly rate from CSV
                try (BufferedReader rateReader = new BufferedReader(new FileReader(HourlyRateFile))) {
                    String line;
                    boolean isFirstLine = true;
                    while ((line = rateReader.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }
                        String[] rateArray = line.split(",");
                        int currentEmployeeID = Integer.parseInt(rateArray[0].trim());

                        // Check if the current line corresponds to the given employee ID
                        if (currentEmployeeID == employeeID) {
                            hourlyRate = Double.parseDouble(rateArray[3].trim()); // Hourly rate is in the 4th column
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Error: Hourly rate file not found.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading hourly rate file.");
                    e.printStackTrace();
                }

                // Prompt user for the month in MM/YYYY format
                String enteredMonth = null;
                boolean validMonth = false;
                while (!validMonth) {
                    System.out.print("\nEnter the Month (MM/YYYY)  : ");
                    enteredMonth = scanner.nextLine();
                    if (enteredMonth.matches("^(0[1-9]|1[0-2])/2022$")) { // Validate month and year
                        String[] monthYear = enteredMonth.split("/");
                        int month = Integer.parseInt(monthYear[0]);
                        if (month >= 1 && month <= 12) {
                            validMonth = true;
                        } else {
                            System.out.println("\nInvalid month. Please enter a valid month (01 to 12).");
                            // Exit the loop if the month format is invalid
                            break;
                        }
                    } else {
                        System.out.println("\nInvalid month/year format. Please enter the month in MM/YYYY format for the year 2022.");
                    }
                }

                // Display payroll details
                System.out.println("\n            PAYROLL DETAILS");

                System.out.println("\nHourly Rate                : PHP " + hourlyRate + "\n");

                // Display Late Hours for the selected month
                System.out.println("\n       Hours Worked for " + enteredMonth);

                // Read monthly hours worked from attendance records
                Map<String, Double> monthlyHours = new TreeMap<>(); // Using TreeMap to sort by date
                Map<String, Double> lateHours = new HashMap<>();

                try (BufferedReader attendanceReader = new BufferedReader(new FileReader(AttendanceRecord))) {
                    String line;
                    boolean isFirstLine = true;
                    while ((line = attendanceReader.readLine()) != null) {
                        String[] row = line.split(",");
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }
                        int currentEmployeeID = Integer.parseInt(row[0].trim());
                        if (currentEmployeeID == employeeID) {
                            String date = row[1].trim();
                            try {
                                String timeInStr = row[2].trim();
                                String timeOutStr = row[3].trim();

                                // Check if the employee was absent (both time-in and time-out are "0:00")
                                if (!timeInStr.equals("0:00") && !timeOutStr.equals("0:00")) {
                                    Date timeIn = timeFormat.parse(timeInStr);
                                    Date timeOut = timeFormat.parse(timeOutStr);
                                    double hoursWorked = calculateHoursWorked(timeIn, timeOut);
                                    monthlyHours.put(date, hoursWorked);

                                    // Check for late hours
                                    Date lateThreshold = timeFormat.parse("8:10");
                                    if (timeIn.after(lateThreshold)) {
                                        double lateHour = calculateHoursWorked(lateThreshold, timeIn);
                                        lateHours.put(date, lateHour);
                                    }
                                } else {
                                    // If both time-in and time-out are "0:00", set hours worked for that day to 0
                                    monthlyHours.put(date, 0.0);
                                }

                                // Get week number for the current date
                                Date currentDate = dateFormat.parse(date);
                                String[] monthYear = enteredMonth.split("/");
                                int month = Integer.parseInt(monthYear[0]);
                                int year = Integer.parseInt(monthYear[1]);
                                if (currentDate.getMonth() + 1 == month && currentDate.getYear() + 1900 == year) {
                                    String weekNumber = getWeekNumber(dateFormat.parse("01/03/2022"), currentDate);

                                    // Get the hours worked for the current date
                                    double hoursWorkedForDate = monthlyHours.get(date);

                                    // Subtract 1 hour if the employee worked more than 8 hours
                                    if (hoursWorkedForDate > 8.0) {
                                        hoursWorkedForDate -= 1.0;
                                    }

                                    System.out.println("Week " + weekNumber + " - " + date + "       : " + String.format("%.2f", hoursWorkedForDate) + " hours");
                                }

                            } catch (ParseException e) {
                                System.out.println("Error parsing time for date: " + date);
                                e.printStackTrace();
                            } catch (ArrayIndexOutOfBoundsException e) {
                                System.out.println("Error: Incomplete data for date: " + date);
                                e.printStackTrace();
                            } catch (NumberFormatException e) {
                                System.out.println("Error: Invalid numeric data for date: " + date);
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    System.out.println("Error: Attendance record file not found.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading attendance record.");
                    e.printStackTrace();
                }

             // Display total weekly hours worked
      

                // Map to store weekly totals for the selected month
                Map<String, Double> weeklyTotals = new HashMap<>();

                // Iterate through each date and calculate weekly totals
                for (Map.Entry<String, Double> entry : monthlyHours.entrySet()) {
                    String date = entry.getKey();
                    Date currentDate;
                    try {
                        currentDate = dateFormat.parse(date);
                    } catch (ParseException e) {
                        System.out.println("Error parsing date: " + date);
                        e.printStackTrace();
                        continue;
                    }

                    // Check if the current date falls within the selected month
                    String[] monthYear = enteredMonth.split("/");
                    int month = Integer.parseInt(monthYear[0]);
                    int year = Integer.parseInt(monthYear[1]);
                    if (currentDate.getMonth() + 1 == month && currentDate.getYear() + 1900 == year) {
                        String weekNumber;
                        try {
                            weekNumber = getWeekNumber(dateFormat.parse("01/03/2022"), currentDate);
                        } catch (ParseException e) {
                            System.out.println("Error parsing date: " + date);
                            e.printStackTrace();
                            continue;
                        }

                        double hoursWorked = entry.getValue();
                        if (hoursWorked > 8.0) {
                            hoursWorked -= 1.0;
                        }
                        double currentTotal = weeklyTotals.getOrDefault(weekNumber, 0.0);
                        weeklyTotals.put(weekNumber, currentTotal + hoursWorked);
                    }
                }

                System.out.println("----------------------------------------------- ");
                
                // Display weekly totals for the selected month
                for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
                    String weekNumber = entry.getKey();
                    double totalHours = entry.getValue();
                    System.out.println("Week " + weekNumber + " Total hours worked : " + String.format("%.2f", totalHours) + " hours");
                }
                
             // Display total hours worked for the selected month
                double totalHoursForMonth = 0.0;
                for (Map.Entry<String, Double> entry : monthlyHours.entrySet()) {
                    String date = entry.getKey();
                    // Extracting month and year from the date
                    String[] dateParts = date.split("/");
                    if (dateParts.length == 3) {
                        String monthYear = dateParts[0] + "/" + dateParts[2];
                        // Check if the date matches the entered month and year
                        if (monthYear.equals(enteredMonth)) {
                            double hoursWorked = entry.getValue();
                            // Subtract 1 hour if the employee worked more than 8 hours
                            if (hoursWorked > 8.0) {
                                hoursWorked -= 1.0;
                            }
                            totalHoursForMonth += hoursWorked;
                        }
                    }
                }
                System.out.println("\nTotal hours worked for the month : " + String.format("%.2f", totalHoursForMonth) + " hours");
                
                
                
                
             // Display Late Hours for the selected month
                System.out.println("\n       Late Hours for " + enteredMonth);
                double totalLateHoursForMonth = 0.0;
                for (Map.Entry<String, Double> entry : lateHours.entrySet()) {
                    String date = entry.getKey();
                    double lateHour = entry.getValue();
                    // Extracting month and year from the date
                    String[] dateParts = date.split("/");
                    if (dateParts.length == 3) {
                        String monthYear = dateParts[0] + "/" + dateParts[2];
                        // Check if the date matches the entered month and year
                        if (monthYear.equals(enteredMonth)) {
                            // Get week number for the current date
                            String weekNumber = "";
                            try {
                                Date currentDate = dateFormat.parse(date);
                                weekNumber = getWeekNumber(dateFormat.parse("01/03/2022"), currentDate);
                            } catch (ParseException e) {
                                System.out.println("Error parsing date: " + date);
                                e.printStackTrace();
                            }
                            System.out.println("Week " + weekNumber + " - " + date + "       : " + String.format("%.2f", lateHour) + " hours late");
                            totalLateHoursForMonth += lateHour;
                        }
                    }
                }
             // Display total weekly late hours
                Map<String, Double> weeklyLateTotals = new HashMap<>();

                // Iterate through each late hour and calculate weekly totals
                for (Map.Entry<String, Double> entry : lateHours.entrySet()) {
                    String date = entry.getKey();
                    Date currentDate;
                    try {
                        currentDate = dateFormat.parse(date);
                    } catch (ParseException e) {
                        System.out.println("Error parsing date: " + date);
                        e.printStackTrace();
                        continue;
                    }

                    // Check if the current date falls within the selected month
                    String[] monthYear = enteredMonth.split("/");
                    int month = Integer.parseInt(monthYear[0]);
                    int year = Integer.parseInt(monthYear[1]);
                    if (currentDate.getMonth() + 1 == month && currentDate.getYear() + 1900 == year) {
                        String weekNumber;
                        try {
                            weekNumber = getWeekNumber(dateFormat.parse("01/03/2022"), currentDate);
                        } catch (ParseException e) {
                            System.out.println("Error parsing date: " + date);
                            e.printStackTrace();
                            continue;
                        }

                        double lateHour = entry.getValue();
                        double currentTotal = weeklyLateTotals.getOrDefault(weekNumber, 0.0);
                        weeklyLateTotals.put(weekNumber, currentTotal + lateHour);
                    }
                }
                
                System.out.println("----------------------------------------------- ");
                
                // Display weekly late totals for the selected month
                for (Map.Entry<String, Double> entry : weeklyLateTotals.entrySet()) {
                    String weekNumber = entry.getKey();
                    double totalLateHours = entry.getValue();
                    System.out.println("Week " + weekNumber + " Total late hours   : " + String.format("%.2f", totalLateHours) + " hours late");               
                }
                    System.out.println("\nTotal late hours for the month   : " + String.format("%.2f", totalLateHoursForMonth) + " hours late");
                    
                    // Calculate and display Gross Pay per week
                    System.out.println("\n            GROSS PAY PER WEEK");
                    for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
                        String weekNumber = entry.getKey();
                        double totalHours = entry.getValue();
                        double grossPayForWeek = totalHours * hourlyRate;
                        System.out.println("Week " + weekNumber + " Gross Pay: PHP " + String.format("%.2f", grossPayForWeek));
                    }

                    // Calculate and display Gross Pay for the month
                    double grossPayForMonth = totalHoursForMonth * hourlyRate;
                    System.out.println("\n            GROSS PAY FOR THE MONTH");
                    System.out.println("Gross Pay for the month: PHP " + String.format("%.2f", grossPayForMonth));
                
            }
        }
    }
}