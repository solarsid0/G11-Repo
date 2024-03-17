/* 

== Notes about the code ==
1) Not all employee details were displayed from the original data base sheet as to censor the other sensitive information of the employees. (Assuming the details will be shown on a system and to managers.)
2) There is -1 hour in the total hours worked of employees since 1 hour is for break time and break time isn't paid.
3) The monthly deductions (including taxable income), were divided into 4 weeks to get the weekly amounts.
4) Phone allowance values were added in taxable income as they're taxable according to TRAIN Law. Clothing allowance were also added for those employees that have more than Php 500 clothing allowance (exceeded the Php 6k per annum limit).
Reference: https://taxacctgcenter.ph/tax-exempt-de-minimis-benefits-to-employees/
6) Starting day for week number counting is every Monday.
Reference: https://www.generalblue.com/2022-philippines-calendar?weekstart=monday


*/

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
		    
		    // Constants for SSS calculation
		    private static final double MONTHLY_SSS_THRESHOLD = 3250;
		    private static final double MONTHLY_SSS_BASE_CONTRIBUTION = 135;
		    private static final double WEEKS_IN_MONTH = 4;
		    
		    // Main method where the execution of the program starts
		    @SuppressWarnings("deprecation")
			public static void main(String[] args) throws IOException {

	     // CSV file paths for employee details, attendance records, and hourly rates
		    String EmployeeDetails = "src\\Employee Details.csv";
		    String AttendanceRecord = "src\\Attendance Record.csv";
		    String HourlyRateFile = "src\\Hourly Rate.csv";
		    String AllowancesFile = "src\\Allowances & Subsidy.csv";
		        

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
	             double riceSubsidy = 0.0;
	             double phoneAllowance = 0.0;
	             double clothingAllowance = 0.0;

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

	             //Read allowances and subsidy file
	             try (BufferedReader allowancesReader = new BufferedReader(new FileReader(AllowancesFile))) {
	                 String line;
	                 boolean isFirstLine = true;
	                 while ((line = allowancesReader.readLine()) != null) {
	                     if (isFirstLine) {
	                         isFirstLine = false;
	                         continue;
	                     }
	                     String[] allowanceArray = line.split(",");
	                     int currentEmployeeID = Integer.parseInt(allowanceArray[0].trim());
	                     if (currentEmployeeID == employeeID) {
	                         riceSubsidy = Double.parseDouble(allowanceArray[1].trim());
	                         phoneAllowance = Double.parseDouble(allowanceArray[2].trim());
	                         clothingAllowance = Double.parseDouble(allowanceArray[3].trim());
	                         break;
	                     }
	                 }
	             } catch (FileNotFoundException e) {
	                 System.out.println("Error: Allowances file not found.");
	                 e.printStackTrace();
	             } catch (IOException e) {
	                 System.out.println("Error reading allowances file.");
	                 e.printStackTrace();
	             }
	             
	             

	             // Display payroll details
	             System.out.println("\n            PAYROLL DETAILS");
	             System.out.println("\nHourly Rate                : PHP " + hourlyRate + "\n");
	             System.out.println("Rice Subsidy               : PHP " + String.format("%.2f", riceSubsidy));
	             System.out.println("Phone Allowance            : PHP " + String.format("%.2f", phoneAllowance));
	             System.out.println("Clothing Allowance         : PHP " + String.format("%.2f", clothingAllowance));
	             

	             // Display Late Hours for the selected month
	             System.out.println("\n       HOURS WORKED FOR " + enteredMonth);
	             System.out.println();

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

	                                 // Check for late hours; Beyond 8:10 is late
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
	                              // Subtract 1 hour if the employee worked less than 8 hours but greater than 0
	                              if (hoursWorkedForDate > 0.0) {
	                                  if (hoursWorkedForDate > 8.0 || (hoursWorkedForDate < 8.0 && hoursWorkedForDate > 0.0)) {
	                                      hoursWorkedForDate -= 1.0;
	                                  }
	                              } else {
	                                  hoursWorkedForDate = 0.0;
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
	                     
	                     // Subtract 1 hour if the employee worked more than 8 hours
	                     // Subtract 1 hour if the employee worked less than 8 hours but greater than 0
	                     if (hoursWorked > 0.0) {
	                         if (hoursWorked > 8.0 || (hoursWorked < 8.0 && hoursWorked > 0.0)) {
	                             hoursWorked -= 1.0;
	                         }
	                     } else {
	                         hoursWorked = 0.0;
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
	                         // Subtract 1 hour if the employee worked less than 8 hours but greater than 0
	                         if (hoursWorked > 0.0) {
	                             if (hoursWorked > 8.0 || (hoursWorked < 8.0 && hoursWorked > 0.0)) {
	                                 hoursWorked -= 1.0;
	                             }
	                         } else {
	                             hoursWorked = 0.0;
	                         }
	                         totalHoursForMonth += hoursWorked;
	                     }
	                 }
	             }
	             System.out.println("\nTotal hours worked for the month : " + String.format("%.2f", totalHoursForMonth) + " hours");

	             // Display Late Hours for the selected month
	             System.out.println("\n       LATE HOURS FOR " + enteredMonth);
	             System.out.println();
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
	                     double roundedLateHour = Math.round(lateHour * 100.0) / 100.0;
	                     weeklyLateTotals.put(weekNumber, currentTotal + roundedLateHour);
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
	                 System.out.println();
	                 for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
	                     String weekNumber = entry.getKey();
	                     double totalHours = entry.getValue();
	                     double grossPayForWeek = totalHours * hourlyRate;
	                     System.out.println("Week " + weekNumber + " Gross Pay          : PHP " + String.format("%.2f", grossPayForWeek));
	                 }

	                 // Calculate and display Gross Pay for the month
	                 double grossPayForMonth = totalHoursForMonth * hourlyRate;
	                 System.out.println("\n            GROSS PAY FOR THE MONTH");
	                 System.out.println("\nGross Pay for the month    : PHP " + String.format("%.2f", grossPayForMonth));

	                 // Calculate monthly contributions
	                 double monthlySSSContribution = calculateMonthlySSSContribution(grossPayForMonth);
	                 double monthlyPhilHealthContribution = calculateMonthlyPhilHealthContribution(grossPayForMonth);
	                 double monthlyPagIBIGContribution = calculateMonthlyPagIBIGContribution(grossPayForMonth);
	              // Add phone allowance and clothing allowance to calculate monthly taxable income
	                 double monthlyTaxableIncome = calculateMonthlyTaxableIncome(grossPayForMonth, monthlySSSContribution, monthlyPhilHealthContribution, monthlyPagIBIGContribution, phoneAllowance, clothingAllowance);
	                 double monthlyWithholdingTax = calculateMonthlyWithholdingTax(monthlyTaxableIncome);
	                 double monthlyLateDeductions = totalLateHoursForMonth * hourlyRate;
	    
	                 // Calculate and display deductions for each week
	                 System.out.println("\n            DEDUCTIONS PER WEEK");

	                 for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
	                     String weekNumber = entry.getKey();
	                     double totalHours = entry.getValue();
	                     // Calculate for weekly deductions
	                     double weeklyLateHours = weeklyLateTotals.getOrDefault(weekNumber, 0.0);                    
	                     double grossPayForWeek = totalHours * hourlyRate;
	                     double sssContribution = calculateWeeklySSSContribution(grossPayForWeek, monthlySSSContribution);
	                     double philHealthContribution = calculateWeeklyPhilHealthContribution(grossPayForWeek, monthlyPhilHealthContribution);
	                     double pagIBIGContribution = calculateWeeklyPagIBIGContribution(grossPayForWeek, monthlyPagIBIGContribution);
	                     double weeklyTaxableIncome = calculateWeeklyTaxableIncome(monthlyTaxableIncome);
	                     double weeklyWithholdingTax = calculateWeeklyWithholdingTax(monthlyWithholdingTax);
	                     double weeklyLateDeductions = weeklyLateHours * hourlyRate;
	                      
	                     // Display weekly deductions
	                     System.out.println("\nWeek " + weekNumber + " Deductions");
	                     System.out.println("SSS Contribution           : PHP " + String.format("%.2f", sssContribution));
	                     System.out.println("PhilHealth Contribution    : PHP " + String.format("%.2f", philHealthContribution));
	                     System.out.println("Pag-IBIG Contribution      : PHP " + String.format("%.2f", pagIBIGContribution));
	                     System.out.println("Taxable Income             : PHP " + String.format("%.2f", weeklyTaxableIncome));
	                     System.out.println("Withholding Tax            : PHP " + String.format("%.2f", weeklyWithholdingTax));
	                     System.out.println("Late Deductions            : PHP " + String.format("%.2f", weeklyLateDeductions));
	                 }

	                 // Display monthly deductions
	                 System.out.println("\n            DEDUCTIONS FOR THE MONTH");
	                 System.out.println("\nSSS Contribution           : PHP " + String.format("%.2f", monthlySSSContribution));
	                 System.out.println("PhilHealth Contribution    : PHP " + String.format("%.2f", monthlyPhilHealthContribution));
	                 System.out.println("Pag-IBIG Contribution      : PHP " + String.format("%.2f", monthlyPagIBIGContribution));
	                 System.out.println("Taxable Income             : PHP " + String.format("%.2f", monthlyTaxableIncome));
	                 System.out.println("Withholding Tax            : PHP " + String.format("%.2f", monthlyWithholdingTax));
	                 System.out.println("Late Deductions            : PHP " + String.format("%.2f", monthlyLateDeductions));

	                 // Calculate monthly net pay
	                 double monthlyNetPay = grossPayForMonth - monthlySSSContribution - monthlyPhilHealthContribution
	                         - monthlyPagIBIGContribution - monthlyWithholdingTax - monthlyLateDeductions;
	                 monthlyNetPay = Math.round(monthlyNetPay * 100.0) / 100.0; // Round to nearest hundredth

	                 // Display Net Pay per week
	                 System.out.println("\n            NET PAY PER WEEK");
	                 System.out.println();
	                 for (Map.Entry<String, Double> entry : weeklyTotals.entrySet()) {
	                     String weekNumber = entry.getKey();
	                     double totalHours = entry.getValue();
	                     double weeklyLateHours = weeklyLateTotals.getOrDefault(weekNumber, 0.0);
	                     double grossPayForWeek = totalHours * hourlyRate;

	                     // Iterate weekly deductions for net pay
	                     double sssContribution = calculateWeeklySSSContribution(grossPayForWeek, monthlySSSContribution);
	                     double philHealthContribution = calculateWeeklyPhilHealthContribution(grossPayForWeek, monthlyPhilHealthContribution);
	                     double pagIBIGContribution = calculateWeeklyPagIBIGContribution(grossPayForWeek, monthlyPagIBIGContribution);
	                     double weeklyWithholdingTax = calculateWeeklyWithholdingTax(monthlyWithholdingTax);
	                     double weeklyLateDeductions = weeklyLateHours * hourlyRate;

	                     // Calculate weekly net pay
	                     double weeklyNetPay = grossPayForWeek - sssContribution - philHealthContribution
	                                           - pagIBIGContribution - weeklyWithholdingTax - weeklyLateDeductions;

	                     // Display weekly net pay
	                     System.out.println("Week " + weekNumber + " Net Pay            : PHP " + String.format("%.2f", weeklyNetPay));
	                 }

	                 // Display monthly net pay
	                 System.out.println("\n            NET PAY FOR THE MONTH");
	                 System.out.println();
	                 System.out.println("Monthly Net Pay            : PHP " + String.format("%.2f", monthlyNetPay));
	             }
	     }
		    }
	             // Calculate monthly SSS contribution based on gross pay
	             private static double calculateMonthlySSSContribution(double grossPay) {
	                 if (grossPay == 0) {
	                     return 0.00;
	                 } else if (grossPay < MONTHLY_SSS_THRESHOLD) {
	                     return MONTHLY_SSS_BASE_CONTRIBUTION;
	                 } else {
	                     double contribution = ((Math.round((grossPay - MONTHLY_SSS_THRESHOLD) / 499.0) * 22.5) + MONTHLY_SSS_BASE_CONTRIBUTION);
	                     return Double.parseDouble(String.format("%.2f", contribution));
	                 }
	             }

	             // Calculate weekly SSS contribution based on gross pay and monthly contribution
	             private static double calculateWeeklySSSContribution(double grossPay, double monthlySSSContribution) {
	                 return monthlySSSContribution / WEEKS_IN_MONTH;
	             }
	         
	//Calculate monthly PhilHealth contribution based on gross pay
	private static double calculateMonthlyPhilHealthContribution(double grossPay) {
	 return Math.round((grossPay * 0.03) / 2 * 100) / 100.0;
	}

	//Calculate weekly PhilHealth contribution based on gross pay and monthly contribution
	private static double calculateWeeklyPhilHealthContribution(double grossPay, double monthlyPhilHealthContribution) {
	 return monthlyPhilHealthContribution / WEEKS_IN_MONTH;
	}
	//Calculate monthly Pag-IBIG contribution based on gross pay
	private static double calculateMonthlyPagIBIGContribution(double grossPay) {
	 double pagIBIGRate = (grossPay > 1500) ? 0.02 : ((grossPay >= 1000) ? 0.01 : 0);
	 return Math.round(grossPay * pagIBIGRate * 100) / 100.0;
	}

	//Calculate weekly Pag-IBIG contribution based on gross pay and monthly contribution
	private static double calculateWeeklyPagIBIGContribution(double grossPay, double monthlyPagIBIGContribution) {
	 return monthlyPagIBIGContribution / WEEKS_IN_MONTH;
	}
	
	// Calculate monthly taxable income including phone and clothing allowances
	private static double calculateMonthlyTaxableIncome(double grossPay, double monthlySSSContribution, double monthlyPhilHealthContribution, double monthlyPagIBIGContribution, double phoneAllowance, double clothingAllowance) {
	    return Math.round((grossPay - monthlySSSContribution - monthlyPhilHealthContribution - monthlyPagIBIGContribution + phoneAllowance + (clothingAllowance > 500 ? clothingAllowance : 0)) * 100) / 100.0;
	}

	//Calculate weekly taxable income based on monthly taxable income
	private static double calculateWeeklyTaxableIncome(double monthlyTaxableIncome) {
	 return monthlyTaxableIncome / WEEKS_IN_MONTH;
	}
	//Calculate monthly withholding tax
	private static double calculateMonthlyWithholdingTax(double monthlyTaxableIncome) {
	 double withholdingTax = 0.0;
	 if (monthlyTaxableIncome <= 20832) {
	     withholdingTax = 0.0;
	 } else if (monthlyTaxableIncome <= 33333) {
	     withholdingTax = 0.2 * (monthlyTaxableIncome - 20833);
	 } else if (monthlyTaxableIncome <= 66667) {
	     withholdingTax = 2500 + 0.25 * (monthlyTaxableIncome - 33333);
	 } else if (monthlyTaxableIncome <= 166667) {
	     withholdingTax = 10833 + 0.3 * (monthlyTaxableIncome - 66667);
	 } else if (monthlyTaxableIncome <= 666667) {
	     withholdingTax = 40833.33 + 0.32 * (monthlyTaxableIncome - 166667);
	 } else {
	     withholdingTax = 200833.33 + 0.35 * (monthlyTaxableIncome - 666667);
	 }
	 return Math.round(withholdingTax * 100) / 100.0;
	}

	//Calculate weekly withholding tax based on monthly value
	private static double calculateWeeklyWithholdingTax(double monthlyWithholdingTax) {
	 return monthlyWithholdingTax / WEEKS_IN_MONTH;
	}
	}