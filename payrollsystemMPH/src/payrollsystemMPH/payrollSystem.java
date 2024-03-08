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

// Define the main class for the payroll system
public class payrollSystem {

    // Define a date format for handling time
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("H:mm");

    // Method to calculate hours worked based on time-in and time-out
    private static double calculateHoursWorked(Date timeIn, Date timeOut) {
        // Calculating the difference in milliseconds
        long difference = timeOut.getTime() - timeIn.getTime();

        // Converting milliseconds to hours
        double hoursWorked = difference / (1000.0 * 60 * 60);

        return hoursWorked;
    }

    // Main method where the execution of the program starts
    public static void main(String[] args) throws IOException {

    	// CSV file paths
        String EmployeeDetails = "src\\Employee Details.csv";
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
                System.out.println("\n            PAYROLL DETAILS");

                System.out.println("\nHourly Rate                : PHP " + hourlyRate);

                // Display Hours Worked for the selected month
                System.out.println("\n       Hours Worked for " + enteredMonth);
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
                            System.out.println(date + "                 : " + String.format("%.2f", hoursWorked) + " hours");
                            totalHoursForMonth += hoursWorked;
                        }
                    }
                }
                System.out.println("Total hours worked         : " + String.format("%.2f", totalHoursForMonth) + " hours");

                // Display Late Hours for the selected month
                System.out.println("\n       Late Hours for " + enteredMonth);
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
                            System.out.println(date + "                 : " + String.format("%.2f", lateHour) + " hours late");
                            totalLateHoursForMonth += lateHour;
                        }
                    }
                }
                System.out.println("Total late hours           : " + String.format("%.2f", totalLateHoursForMonth) + " hours");

                // Calculate Gross Pay
                double grossPay = totalHoursForMonth * hourlyRate;
                System.out.println("\n            GROSS PAY");
                System.out.println("\nGross Pay                  : PHP " + String.format("%.2f", grossPay));

                // Calculate SSS Contribution
                double sssContribution;
                if (grossPay == 0) {
                    sssContribution = 0.00;
                } else if (grossPay < 3250) {
                    sssContribution = 135.00;
                } else if (grossPay <= 24750) {
                    int bracket = (int) Math.ceil((grossPay - 3250) / 500);
                    sssContribution = 135.00 + bracket * 22.50;
                } else {
                    sssContribution = 1125.00;
                }

                // Calculate PhilHealth Contribution
                double philHealthContribution = (grossPay * 0.03) / 2;

                // Calculate PagIBIG Contribution
                double pagIBIGContribution;
                if (grossPay >= 1500) {
                    pagIBIGContribution = grossPay * 0.02;
                } else if (grossPay >= 1000) {
                    pagIBIGContribution = grossPay * 0.01;
                } else {
                    pagIBIGContribution = 0;
                }

                // Calculate Taxable Income
                double taxableIncome = grossPay - sssContribution - pagIBIGContribution - philHealthContribution;

                // Calculate Withholding Tax
                double withholdingTax;
                if (taxableIncome <= 20832) {
                    withholdingTax = 0;
                } else if (taxableIncome <= 33333) {
                    withholdingTax = 0.2 * (taxableIncome - 20833);
                } else if (taxableIncome <= 66667) {
                    withholdingTax = 2500 + 0.25 * (taxableIncome - 33333);
                } else if (taxableIncome <= 166667) {
                    withholdingTax = 10833 + 0.3 * (taxableIncome - 66667);
                } else if (taxableIncome <= 666667) {
                    withholdingTax = 40833.33 + 0.32 * (taxableIncome - 166667);
                } else {
                    withholdingTax = 200833.33 + 0.35 * (taxableIncome - 666667);
                }

                // Calculate late deductions
                double lateDeductions = totalLateHoursForMonth * hourlyRate;

                // Display deduction details
                System.out.println("\n            DEDUCTIONS");
                System.out.println("\nSSS Contribution           : PHP " + String.format("%.2f", sssContribution));
                System.out.println("PhilHealth Contribution    : PHP " + String.format("%.2f", philHealthContribution));
                System.out.println("PagIBIG Contribution       : PHP " + String.format("%.2f", pagIBIGContribution));
                System.out.println("Taxable Income             : PHP " + String.format("%.2f", taxableIncome));
                System.out.println("Withholding Tax            : PHP " + String.format("%.2f", withholdingTax));
                System.out.println("Late Deductions            : PHP " + String.format("%.2f", lateDeductions));

                // Calculate net pay
                double netPay = grossPay - sssContribution - pagIBIGContribution - philHealthContribution - withholdingTax - lateDeductions;

                // Print net pay
                System.out.println("\n            NET PAY");
                System.out.println("\nNet Pay                    : PHP " + String.format("%.2f", netPay));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}