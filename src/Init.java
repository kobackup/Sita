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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Init {
	private Set<String> airlineSet; // a set for all airline codes that appear in the month's report
	private String dataFilepath; // filepath of the SITA report
	private String scheduleFilepath; // filepath of the flight schedule
	private int lastRow; // the last row that contains schedule info
	private String report; // the resulting billing report
	private String outputFilename = "Report.txt";
	
	private DaySchedule[] schedules; // array of daily schedules based on the schedule file
	
	/*
	private DaySchedule monday;
	private DaySchedule tuesday;
	private DaySchedule wednesday;
	private DaySchedule thursday;
	private DaySchedule friday;
	private DaySchedule saturday;
	private DaySchedule sunday;
	*/
	
	public Init(String data, String schedule)
	{
		airlineSet = new HashSet<String>();
		dataFilepath = data;
		scheduleFilepath = schedule;	
		schedules = new DaySchedule[7];
		initAirlineSet();
		initDaySchedules();
		
		//schedules[6].printTimesForCode("LI");
		
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
				if(tempArray[1].length() < 4)
				{
					airlineSet.add(tempArray[1]);
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
	
	
	/**
	 * Finds the row that begins with the string 'total' and sets lastRow
	 * to that row number
	 */
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
	
	
	
	/**
	 * takes the workstation name as input and 
	 * returns the corresponding counter name.
	 * @param workstation
	 * @return counter name
	 */
	
	/*
	private String convertCounterName(String workstation) // move this to DaySchedule
	{
		switch(workstation)
		{
		case "GND1CKB001": case "GND1CKR002":
			return "Counter 1";
		case "GND1CKB003": case "GND1CKR004":
			return "Counter 2";
		case "GND1CKB005": case "GND1CKR006":
			return "Counter 3";
		case "GND1CKB007": case "GND1CKR008":
			return "Counter 4";
		case "GND1CKB013": case "GND1CKR014":
			return "Counter 7";
		case "GND1CKB017": case "GND1CKR018":
			return "Counter 9";
		case "GND1CKB023": case "GND1CKR024":
			return "Counter 12";
		case "GND1CKR010":
			return "Counter 5";
		case "GND1CKR012":
			return "Counter 6";
		case "GND1CKR016":
			return "Counter 8";
		case "GND1CKR020":
			return "Counter 10";
		case "GND1CKR022":
			return "Counter 11";
		case "GND1CKR026":
			return "Counter 13";
		case "GND1GTG001":
			return "Gate 1";
		case "GND1GTG002":
			return "Gate 2";
		case "GND1GTG003":
			return "Gate 3";
		case "GND1GTG004":
			return "Gate 4";
		default:
			return "Invalid workstation";
		}
		
	}
	*/
	
	
	private void processCharges()
	{
		report = "";
		StringBuilder builder = new StringBuilder(report);
		builder.append(String.format("%-14s" + "%-22s" + "%-10s" + "%-10s" + "%-10s" + "%-6s" + "%-10s", 
				"DATE",  "COUNTER", "AIRLINE", "START", "END", "HOURS", "CHARGE"));
		builder.append("\r\n\r\n");
		for(String code : airlineSet)
		{
			int airlineTotal = 0;
			try
			{
				FileReader reader = new FileReader(dataFilepath);
				Scanner scanner = new Scanner(reader);
				scanner.nextLine();
				
				while(scanner.hasNextLine())
				{
					String line = scanner.nextLine();
					String[] tempArray = line.split(",");
					if(tempArray[1].equals(code)) 
					{
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
						LocalDateTime dateTime = LocalDateTime.parse(tempArray[2], formatter);
						int dayOfWeek = dateTime.getDayOfWeek().ordinal();
						String[] chargedItems = schedules[dayOfWeek].processRow(tempArray);
						
						if(chargedItems != null && Integer.parseInt(chargedItems[5]) > 0)
						{
							airlineTotal += Integer.parseInt(chargedItems[5]);
							
							// add to report string builder
							
							//builder.append(dateTime.toLocalDate() + "   ");
//							for(String word: chargedItems)
//							{
//								builder.append(word + "   ");
//							}
							
							builder.append(String.format("%-14s" + "%-22s" + "%-10s" + "%-10s" + "%-10s" + "%-6s" + "%-10s",
									dateTime.toLocalDate().toString(), chargedItems[0], chargedItems[1], chargedItems[2], 
									chargedItems[3], chargedItems[4], chargedItems[5]));
							
							builder.append("\r\n\r\n");
						}
						
//						for(String cell : tempArray)
//						{
//							System.out.print(cell + "     ");
//						}
//						System.out.println();
						
						/*
						String workstation = tempArray[0];
						String counter = convertCounterName(workstation);
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
						LocalDateTime dateTime = LocalDateTime.parse(tempArray[2], formatter);
						int dayOfWeek = dateTime.getDayOfWeek().ordinal();
						LocalTime loginTime = dateTime.toLocalTime();
						LocalTime logoffTime = loginTime.plusMinutes(Integer.parseInt(tempArray[3]));
						*/
					}					
					
				} 
				
				scanner.close();
				reader.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			} 
			
			builder.append("TOTAL CHARGE FOR " + code + ": " + "$" + airlineTotal + "\r\n\r\n\r\n");
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
