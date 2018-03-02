/**
 * This class reads and stores the data from both
 * the file that details the counter logins for 
 * the month and the file that details the month's
 * flight schedule.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/*
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/

public class Init {
	private Set<String> airlineSet; // a set for all airline codes that appear in the month's report
	private String dataFilepath; // filepath of the SITA report
	private String scheduleFilepath; // filepath of the flight schedule
	private int lastRow; // the last row that contains schedule info
	private String report; // the resulting billing report
	private String outputFilename = "Report.csv";
	private DaySchedule[] schedules; // array of daily schedules based on the schedule file
	int flightNumCol = 42;
	int arrDepCol = 43;
	int daysOfWeekCol = 45;
	int timeCol = 46;
	
	
	public Init(String data, String schedule)
	{
		airlineSet = new HashSet<String>();
		dataFilepath = data;
		scheduleFilepath = schedule;	
		schedules = new DaySchedule[7];
		initAirlineSet();
		initDaySchedules();	
		processCharges();
	}
	
	/**
	 * Reads in the SITA report and adds the airline codes to the set
	 */
	private void initAirlineSet()
	{
		try
		{
			FileReader reader = new FileReader(dataFilepath);
			Scanner scanner = new Scanner(reader);
			
			while(scanner.hasNextLine())
			{
				String temp = scanner.nextLine();
				String[] tempArray = temp.split(",");
				
				//exclude any blank lines at the end of CSV file
				if(tempArray[0].charAt(0) == 'W' || tempArray[0].charAt(0) == 'G')
				{
					if(tempArray[1].length() < 4)
					{
						airlineSet.add(tempArray[1]);
					}
				}								
			} 			
			scanner.close();
			reader.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		
		for(Object airline : airlineSet)
		{
			System.out.println(airline.toString());
		}		
	}
	
	
	
	
	
	/**
	 * Reads in the schedule file and adds each scheduled departure to 
	 * its corresponding DaySchedule
	 */
	
	/*
	private void initDaySchedules()
	{
		setLastRow();
		
		for(int i = 0; i < schedules.length; i++)
		{
			schedules[i] = new DaySchedule();
		}
		
		try
		 {
			FileInputStream inputStream = new FileInputStream(new File(scheduleFilepath));
	         
		        Workbook workbook = new XSSFWorkbook(inputStream);
		        Sheet firstSheet = workbook.getSheetAt(0);
		        int dayIterator = 0;
				
				//3 represents the 4th column which displays Monday's departures.
				//the loop increments by 4 to only check the departures which 
				//are in every 4th column
		        for(int i = 3; i <= 27; i+=4)
		        {
		        	Iterator<Row> iterator = firstSheet.iterator();
			        while (iterator.hasNext()) 
			        {
			            Row nextRow = iterator.next();
			            
			            if(nextRow.getRowNum() > 2 && nextRow.getRowNum() < lastRow)
			            {
			            	if(nextRow.getCell(i).getCellTypeEnum() == CellType.STRING && nextRow != null)  
				            {
			            		String[] entry = nextRow.getCell(i).toString().split(" +");
			            		String airlineCode = entry[0];
			            		LocalTime dTime;           		
			            		
			            		System.out.print(airlineCode + " ");

			            		Matcher match = Pattern.compile("\\(([^)]+)\\)").matcher(nextRow.getCell(i).toString());
		            	    	while(match.find()) 
		            	     	{        	    	 
		            	    		String rawTimeString = match.group(1).toLowerCase();
		            	    		String dTimeString = LocalTime.parse(rawTimeString,  
		            	    				DateTimeFormatter.ofPattern("h:mma")).format(DateTimeFormatter.ofPattern("HH:mm"));
		            	    		dTime = LocalTime.parse(dTimeString);
		            	    		System.out.println(dTime);    
		            	    		schedules[dayIterator].add(airlineCode, dTime);			            	    	 
		            	     	}
				            }
			            }			
			        }
			        dayIterator++;
		        }
		         
		        workbook.close();
		        inputStream.close();
		 }
		 catch (IOException e)
		 {
			 e.printStackTrace();
		 }
		
	}
	*/
	
	
	
	/**
	 * Reads in the schedule file and adds each scheduled departure to 
	 * its corresponding DaySchedule
	 */
	private void initDaySchedules()
	{
		// initialise the 7 schedule objects
		for(int i = 0; i < schedules.length; i++)
		{
			schedules[i] = new DaySchedule();
		}
		
		try
		{
			FileReader reader = new FileReader(scheduleFilepath);
			Scanner scanner = new Scanner(reader);
			
			while(scanner.hasNextLine()) 
			{
				String temp = scanner.nextLine();
				String[] tempArray = temp.split(",");
				
				// skip first line and only check the departure rows
				if(tempArray[0].charAt(0) == 'M' && tempArray[arrDepCol].equals("D"))
				{
					// get the airline code
					String[] flightNum = tempArray[flightNumCol].split(" ");
					String airlineCode = flightNum[0];
			
					// get the departure time
					String dTimeString = LocalTime.parse(tempArray[timeCol],  
    	    				DateTimeFormatter.ofPattern("HH:mm:ss")).format(DateTimeFormatter.ofPattern("HH:mm"));
					LocalTime dTime = LocalTime.parse(dTimeString);
					
					// create a schedule entry for each day specified 
					// with the current airline code and time.
					int[] days = new int[tempArray[daysOfWeekCol].length()];
					for(int i = 0; i < days.length; i++)
					{
						int day = Integer.parseInt(String.valueOf(tempArray[daysOfWeekCol].charAt(i)));
						schedules[day-1].add(airlineCode, dTime);
					}
				}
			}
			scanner.close();
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	
	
	/**
	 * Finds the row that begins with the string 'total' and sets lastRow
	 * to that row number
	 */
	
	/*
	private void setLastRow()
	{
		try
		{
			FileInputStream inputStream = new FileInputStream(new File(scheduleFilepath));
	        
	        Workbook workbook = new XSSFWorkbook(inputStream);
	        Sheet firstSheet = workbook.getSheetAt(0);       
	         
        	Iterator<Row> iterator = firstSheet.iterator();
	        while (iterator.hasNext()) 
	        {
	            Row nextRow = iterator.next();
	            
	            if(nextRow.getCell(0) != null)
	            {
	            	String contents = nextRow.getCell(0).toString();
		            
		            if(contents.startsWith("TOTAL"))
		            {
		            	lastRow = nextRow.getRowNum();
		            	break;
		            }
	            }          	            
	        }
	        
	        workbook.close();
	        inputStream.close();		        
		}
		 catch (IOException e)
		 {
			 e.printStackTrace();
		 }
		
	}
	*/

	
	
	/**
	 * For each airline code in airlineSet, this method takes each matching 
	 * line item in the SITA report, has it processed by its corresponding 
	 * DaySchedule and then produces a cumulative report file with all 
	 * resulting charges.
	 */
	private void processCharges()
	{
		report = "";
		StringBuilder builder = new StringBuilder(report);

		//Format headings for report
		builder.append(String.format("%-14s" + "," + "%-14s" + "," + "%-10s" + "," + "%-10s" + "," + "%-10s" + "," 
		+ "%-10s" + "," + "%-16s" + "," +  "%-16s" + "," + "%-10s", "DATE",  "COUNTER", "AIRLINE", "LOGIN", 
		"LOGOUT", "DURATION", "BILLED MINUTES", "BILLED HOURS", "CHARGE"));
		builder.append("\r\n");
		// builder.append("--------------------------------------------------------------------------------");
		// builder.append("\r\n");

		//process the data file one airline at a time
		for(String code : airlineSet)
		{
			int airlineTotal = 0; //tally of charges for current airline
			try
			{
				FileReader reader = new FileReader(dataFilepath);
				Scanner scanner = new Scanner(reader);
				scanner.nextLine(); //skip the header row of the data file
				
				while(scanner.hasNextLine())
				{
					String line = scanner.nextLine();
					String[] tempArray = line.split(",");
					
					//exclude any blank lines at the end of CSV file
					if(tempArray[0].charAt(0) == 'W' || tempArray[0].charAt(0) == 'G')
					{
						if(tempArray[1].equals(code)) 
						{
							DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
							LocalDateTime dateTime = LocalDateTime.parse(tempArray[2], formatter);
							int dayOfWeek = dateTime.getDayOfWeek().ordinal();

							//go to the flight schedule for the given day of week and process the current row.
							//This returns a String array with any applicable charges and other related info.
							String[] chargedItems = schedules[dayOfWeek].processRow(tempArray);
							
							//chargedItems[6] = the amount charged.
							if(chargedItems != null && Integer.parseInt(chargedItems[6]) > 0 
									&& !(chargedItems[0].charAt(0) == 'G'))
							{
								airlineTotal += Integer.parseInt(chargedItems[6]);
								int duration = Integer.parseInt(tempArray[3]);
								
								builder.append(String.format("%-14s" + "," + "%-14s" + "," + "%-10s" + "," + "%-10s" 
								+ "," + "%-10s" + "," + "%-10s" + "," + "%-16s" + "," + "%-16s" + "," + /*"$" +*/ "%-10s",
										dateTime.toLocalDate().toString(), chargedItems[0], chargedItems[1], chargedItems[2], 
										chargedItems[3], duration, chargedItems[4], chargedItems[5], chargedItems[6]));
								
								builder.append("\r\n");
							}
						}
					}
							
				} 
				
				scanner.close();
				reader.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			
			builder.append("TOTAL CHARGE FOR " + code + ": " + "," + /*"$" +*/ airlineTotal + "\r\n\r\n\r\n");
		}
		report = builder.toString();
		
		try
		{
			FileWriter writer = new FileWriter(outputFilename);
			writer.write(report);
			writer.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}

}
