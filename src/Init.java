import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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
	private Set airlineSet;
	private String dataFilepath;
	private String scheduleFilepath;
	private int lastRow;
	
	private DaySchedule[] schedules;
	
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
		airlineSet = new HashSet();
		dataFilepath = data;
		scheduleFilepath = schedule;	
		schedules = new DaySchedule[7];
		initAirlineSet();
		initDaySchedules();
		
		schedules[6].printTimesForCode("AA");
	}
	
	
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
			            	     while(match.find()) {        	    	 
					             	 String rawTimeString = match.group(1).toLowerCase();
			            	    	 String dTimeString = LocalTime.parse(rawTimeString,  DateTimeFormatter.ofPattern("h:mma")).format(DateTimeFormatter.ofPattern("HH:mm"));
			            	    	 dTime = LocalTime.parse(dTimeString);
			            	    	 System.out.println(dTime);    
			            	    	 schedules[dayIterator].add(airlineCode, dTime);
			            	    	 
			            	     }
				            }
			            }
			            
			            
			            
			            
			            /*
			            while (cellIterator.hasNext()) {
			                Cell cell = cellIterator.next(); 
		
			                switch (cell.getCellTypeEnum()) {
			                    case STRING:
			                        System.out.print(cell.getStringCellValue());
			                        break;
			                    case BOOLEAN:
			                        System.out.print(cell.getBooleanCellValue());
			                        break;
			                    case NUMERIC:
			                        System.out.print(cell.getNumericCellValue());
			                        break;			                        
			                        
			                }
			                
			                
			                System.out.print(" - ");
			            }
			            */
			            
			           // System.out.println();
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
		            	//System.out.println(contents);
		            	lastRow = nextRow.getRowNum();
		            	//System.out.println(lastRow);
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

}
