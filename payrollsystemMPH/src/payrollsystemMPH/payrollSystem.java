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

public class payrollSystem {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");

    // Method to calculate hours worked based on time-in and time-out
    private static double calculateHoursWorked(Date timeIn, Date timeOut) {
        // Calculating the difference in milliseconds
        long difference = timeOut.getTime() - timeIn.getTime();

        // Converting milliseconds to hours
        double hoursWorked = difference / (1000.0 * 60 * 60);

        return hoursWorked;
    }

    public static void main(String[] args) throws IOException {

        // CSV file paths
        String EmployeeDetails = "src\\MotorPH _Helena's Copy - Employee Details (rev).csv";
        String AttendanceRecord = "src\\MotorPH _Helena's Copy - Copy of Attendance Record.csv";
        String HourlyRateFile = "src\\Group 11 Official File - Hourly Rate.csv";

        // Prompt for employee ID
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("\nEnter Employee ID (or type 'exit' to stop): ");
                String userInput = scanner.nextLine();

                // Exit prompt
                if (userInput.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting the program.");
                    break;
                }

                int employeeID;
                try {
                    employeeID = Integer.parseInt(userInput);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid Employee ID.");
                    continue;
                }

                // Read employee details
                boolean foundEmployeeID = false;
                String employeeLastName = "";
                String employeeFirstName = "";
                String birthday = "";
                String phoneNumber = "";
                String status = "";
                String position = "";
                String immediateSupervisor = "";
                double hourlyRate = 0.0; // Added to store hourly rate
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
                    System.out.println("Error: Employee details file not found.");
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("Error reading employee details.");
                    e.printStackTrace();
                }

                if (!foundEmployeeID) {
                    System.out.println("Employee ID not found.");
                    continue;
                }

                // Display employee details
                System.out.println("\n----------Employee Details----------");
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

                // Prompt for month
                System.out.print("\nEnter the Month (MM/YYYY): ");
                String enteredMonth = scanner.nextLine();

                // Read monthly hours worked
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

                // Display payroll details
                System.out.println("\n----------Payroll Details----------");
          
                System.out.println("\nHourly Rate: PHP " + hourlyRate);

             
   
             // Display Hours Worked for the selected month         
                System.out.println("\n- - Hours Worked for " + enteredMonth + " - -");
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
                            System.out.println(date + " : " + String.format("%.2f", hoursWorked) + " hours");
                            totalHoursForMonth += hoursWorked;
                        }
                    }
                }
                System.out.println("Total hours worked for the month: " + String.format("%.2f", totalHoursForMonth) + " hours");


                // Display Late Hours for the selected month
                System.out.println("\n- - Late Hours for " + enteredMonth + " - -");
                double totalLateHoursForMonth = 0.0;
                for (Map.Entry<String, Double> entry : lateHours.entrySet()) {
                    String date = entry.getKey();
                    // Extracting month and year from the date
                    String[] dateParts = date.split("/");
                    if (dateParts.length == 3) {
                        String monthYear = dateParts[0] + "/" + dateParts[2];
                        // Check if the date matches the entered month and year
                        if (monthYear.equals(enteredMonth)) {
                            double lateHour = entry.getValue();
                            System.out.println(date + " : " + String.format("%.2f", lateHour) + " hours late");
                            totalLateHoursForMonth += lateHour;
                        }
                    }
                }
                System.out.println("Total late hours for the month: " + String.format("%.2f", totalLateHoursForMonth) + " hours");
            }
        }
    }
}