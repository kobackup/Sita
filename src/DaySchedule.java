

import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DaySchedule {
	private final int NUMBUCKETS = 30;
	private final int GRACEPERIOD = 10;
	private final int HOURLYCHARGE = 100;
	private Node[] buckets;	
	
	
	@SuppressWarnings("unchecked")
	public DaySchedule()
	{
		buckets = new Node[NUMBUCKETS];
	}
	
	public void add(String k, LocalTime v)
	{
		
		int bucketIndex = hash(k);
		
		/*
		if(buckets[bucketIndex] != null)
		{
			Node ins = new Node(k, v);
			ins.setNext(buckets[bucketIndex]);
			buckets[bucketIndex].setPrevious(ins);
			buckets[bucketIndex] = ins;			
		}
		else
		{
			buckets[bucketIndex] = new Node(k, v);
		}
		*/
		
		Node curr = buckets[bucketIndex];
		
		if(curr != null)
		{
			try
			{
				while(curr.getNext() != null)
				{
					curr = curr.getNext();
				}
				
				curr.setNext(new Node(k, v)); 
				curr.getNext().setPrevious(curr);
			}
			catch(NullPointerException e)
			{
				// do nothing
			}
		}
		else
		{
			buckets[bucketIndex] = new Node(k, v);
		}	
		
		

	}	
	
	public boolean hasAirlineCode(String code)
	{
		int bucketIndex = hash(code);
		
		Node curr = buckets[bucketIndex];
		
		while(curr != null)
		{
			if(curr.getCode().equals(code)) 
			{
				System.out.println(curr.getTime());
				return true;
			}
			curr = curr.getNext();
		}
		
		return false;
	}
	
	
	public void printTimesForCode(String code)
	{
		if(hasAirlineCode(code))
		{
			int bucketIndex = hash(code);
			
			Node curr = buckets[bucketIndex];
			
			while(curr != null)
			{
				if(curr.getCode().equals(code)) 
				{
					System.out.println("Airline: " + curr.getCode());
					System.out.println("Dept. time: " + curr.getTime());
					System.out.println();				
				}
				curr = curr.getNext();
			}
		}
		else
		{
			System.out.println("There are no records with that code");
		}
	}
	
	
	
	public String[] processRow(String[] line) //maybe return a string[] so the charge can easily be accessed to be totaled
	{
		String workstation = line[0];
		String code = line[1];
		String counter = convertCounterName(workstation);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
		LocalDateTime dateTime = LocalDateTime.parse(line[2], formatter);		
		LocalTime loginTime = dateTime.toLocalTime();
		LocalTime logoffTime = loginTime.plusMinutes(Integer.parseInt(line[3]));
		
		String[] result = new String[6];
		
		//the time span in minutes during which an airline can be 
		//logged in to check passengers in without being charged a fee
		int timeAllowed = 180;  
		
		//reduce the time allowed to 45 minutes for the gates
		switch(workstation)
		{
		case "GND1GTG001": case "GND1GTG002": case "GND1GTG003": case "GND1GTG004": 
			timeAllowed = 45;
		default: break;
		}
		
		// tally of the number of chargeable minutes
		int rowTotal = 0;
		
		if(hasAirlineCode(code))
		{
			int bucketIndex = hash(code);
			
			Node curr = buckets[bucketIndex];
			
			//int rowTotal = 0;
			
			int charge;
			
			if(isOverlappingAny(curr, loginTime, logoffTime, timeAllowed, code))
			{
				while(curr != null)
				{
					if(curr.getCode().equals(code)) 
					{
						/**
						 * Have an isOverlappingAny() method that first checks the time against all nodes in the chain.
						 * if the first is true and the nested isOverlappingThis() is false, do nothing in current node.
						 * if isOverlappingAny() is false, then just charge for the duration without restriction. 
						 */					
						
						if(isOverlapping(loginTime, logoffTime, curr.getTime(), timeAllowed)) //if it's not overlapping, it needs to be checked against other nodes as well
						{
							if(!isValidSession(loginTime, logoffTime, curr.getTime(), timeAllowed))
							{
								//if()
								/**
								 * update the nodes to also keep track of their previous node.
								 * when checking invalid logged in periods, check only until
								 * the end of the valid period for the previous node and the 
								 * beginning of the valid period of the next node, if those exist.
								 */
								if(loggedInEarly(loginTime, curr.getTime(), timeAllowed) && loggedOutLate(logoffTime, curr.getTime(), timeAllowed)) //&& rowTotal == 0
								{
									LocalTime effectiveLoginTime = loginTime;
									LocalTime effectiveLogoffTime = logoffTime;
									
									if(curr.previous != null)
									{
										//if(loginTime.compareTo(curr.previous.getTime().plusMinutes(GRACEPERIOD)) < 0)
										if(rowTotal != 0 || (curr.previous.getTime().plusMinutes(GRACEPERIOD).until(curr.getTime(), ChronoUnit.MINUTES) <= timeAllowed))
										{
											//effectiveLoginTime = curr.previous.getTime().plusMinutes(GRACEPERIOD); 
											effectiveLoginTime = curr.getTime().minusMinutes(timeAllowed+GRACEPERIOD);
										}									
									}
									
									if(curr.next != null)
									{
										if(logoffTime.compareTo(curr.next.getTime().minusMinutes(timeAllowed + GRACEPERIOD)) > 0)
										{
											effectiveLogoffTime = curr.next.getTime().minusMinutes(timeAllowed + GRACEPERIOD);
										}
									}
									/**
									 * if the rowTotal is not zero, that means I already accounted for some of the charge 
									 * and I probably shouldn't add a charge for anything before the current valid time in the 
									 * current node because this should have been charged in the previous node.
									 * The last node (where next is null) should maybe check against midnight.
									 */
									System.out.println("Effective Login time: " + effectiveLoginTime);
									System.out.println("rowTotal early calculation: " + (int)effectiveLoginTime.until(curr.getTime().minusMinutes(timeAllowed+GRACEPERIOD), ChronoUnit.MINUTES));
									System.out.println("Effective logoff time: " + effectiveLogoffTime);
									System.out.println("rowTotal late calculation: " + Math.max((int)curr.getTime().plusMinutes(GRACEPERIOD).until(effectiveLogoffTime, ChronoUnit.MINUTES), 0));
									rowTotal += (int)effectiveLoginTime.until(curr.getTime().minusMinutes(timeAllowed+GRACEPERIOD), ChronoUnit.MINUTES);
									rowTotal += Math.max((int)curr.getTime().plusMinutes(GRACEPERIOD).until(effectiveLogoffTime, ChronoUnit.MINUTES), 0);
									
								}
								
								
								
								else if(loggedInEarly(loginTime, curr.getTime(), timeAllowed) && !(loggedOutLate(logoffTime, curr.getTime(), timeAllowed)))
								{
									LocalTime effectiveLoginTime = loginTime;
									
									if(curr.previous != null)
									{
										//if(loginTime.compareTo(curr.previous.getTime().plusMinutes(GRACEPERIOD)) < 0) 
										if(rowTotal != 0  || (curr.previous.getTime().plusMinutes(GRACEPERIOD).until(curr.getTime(), ChronoUnit.MINUTES) <= timeAllowed))
										{
											//effectiveLoginTime = curr.previous.getTime().plusMinutes(GRACEPERIOD); 
											effectiveLoginTime = curr.getTime().minusMinutes(timeAllowed+GRACEPERIOD);
										}									
									}
									rowTotal += (int)effectiveLoginTime.until(curr.getTime().minusMinutes(timeAllowed+GRACEPERIOD), ChronoUnit.MINUTES);
									
								}
								
								
								
								else if(loggedOutLate(logoffTime, curr.getTime(), timeAllowed) && !(loggedInEarly(loginTime, curr.getTime(), timeAllowed)))
								{
									LocalTime effectiveLogoffTime = logoffTime;
									
									if(curr.next != null)
									{
										if(logoffTime.compareTo(curr.next.getTime().minusMinutes(timeAllowed + GRACEPERIOD)) > 0)
										{
											effectiveLogoffTime = curr.next.getTime().minusMinutes(timeAllowed + GRACEPERIOD);
										}
									}
									rowTotal += Math.max((int)curr.getTime().plusMinutes(GRACEPERIOD).until(effectiveLogoffTime, ChronoUnit.MINUTES), 0);
								}
							}						
						}
					}
					curr = curr.getNext();
				}
				
				int chargeableHours = 0;
				if(rowTotal != 0)
				{
					chargeableHours = (rowTotal/60)+1;
				}
				 
				charge = chargeableHours*HOURLYCHARGE;
				result[0] = counter;
				result[1] = code;
				result[2] = loginTime.toString();
				result[3] = logoffTime.toString();
				result[4] = Integer.toString(chargeableHours);
				result[5] = Integer.toString(charge);
				
				for(String word : result)
				{
					System.out.print(word + "  ");
				}
				System.out.println();
				System.out.println(rowTotal);
				System.out.println();
				
				return result;
				
				// create string array and return it
			}
			else //logged-in time does not intersect with any valid period
			{
				int chargeableHours = ((int)(loginTime.until(logoffTime, ChronoUnit.MINUTES))/60 +1);
				charge = chargeableHours*HOURLYCHARGE;
				result[0] = counter;
				result[1] = code;
				result[2] = loginTime.toString();
				result[3] = logoffTime.toString();
				result[4] = Integer.toString(chargeableHours);
				result[5] = Integer.toString(charge);
				
				return result;
				// create string array and return it
			}
			
			
		}
		else
		{
			System.out.println("There are no records with the code " + code);
			return null;
		}
				
		//return result;
	}
	
	
	
	/**
	 * takes the workstation name as input and 
	 * returns the corresponding counter name.
	 * @param workstation
	 * @return counter name
	 */
	private String convertCounterName(String workstation) 
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
	
	
	
	public boolean isOverlappingAny(Node node, LocalTime login, LocalTime logoff, int timeAllowed, String code)
	{
		Node curr = node;		
		while(curr != null)
		{
			if(curr.getCode().equals(code) && isOverlapping(login, logoff, curr.getTime(), timeAllowed)) 
			{
				return true;
			}
			curr = curr.next;
		}
				
		return false;
	}
	
	
	
	/**
	 * Checks whether a given time period has any overlap with 
	 * any scheduled period for that airline on that day of week
	 * @param login
	 * @param logoff
	 * @param scheduled
	 * @param timeAllowed
	 * @return
	 */
	public boolean isOverlapping(LocalTime login, LocalTime logoff, LocalTime scheduled, int timeAllowed)
	{
		return (login.isBefore(scheduled.plusMinutes(GRACEPERIOD)) && scheduled.minusMinutes(timeAllowed+GRACEPERIOD).isBefore(logoff));
	}
	
	
	public boolean isValidSession(LocalTime login, LocalTime logoff, LocalTime scheduled, int timeAllowed)
	{
		return (login.isAfter(scheduled.minusMinutes(timeAllowed+GRACEPERIOD)) && logoff.isBefore(scheduled.plusMinutes(GRACEPERIOD)));
	}
	
	
	public boolean loggedInEarly(LocalTime login, LocalTime scheduled, int timeAllowed)
	{
		return (login.isBefore(scheduled.minusMinutes(timeAllowed+GRACEPERIOD)));
	}
	
	public boolean loggedOutLate(LocalTime logoff, LocalTime scheduled, int timeAllowed)
	{
		return (logoff.isAfter(scheduled.plusMinutes(GRACEPERIOD)));
	}
	
	
	/**
	 * hash function used to assign airline codes
	 * to buckets in the DaySchedule hash table
	 * @param airlineCode
	 * @return
	 */
	private int hash(String airlineCode)
	{
		return Math.abs(airlineCode.hashCode()) % NUMBUCKETS;
	}
	
	
	/**
	 * Linked list node representing a single
	 * scheduled flight within a day's schedule.
	 * Each stores the scheduled departure time 
	 * and the relevant airline's code.
	 * @author Khari
	 *
	 */
	private static class Node {
		private LocalTime time;	
		private String code;
		private Node next;
		private Node previous;

		
		// Constructor to initialise the Node
		private Node(String newCode, LocalTime newTime)
		{
			time = newTime;
			next = null;
			previous = null;
			code = newCode;
		}

		private Node getNext() 
		{
			return next;
		}

		private void setNext(Node next) 
		{
			this.next = next;
		}
		
		private void setPrevious(Node previous)
		{
			this.previous = previous;
		}

		private LocalTime getTime() 
		{
			return time;
		}
		
		private String getCode()
		{
			return code;
		}
	}

}
