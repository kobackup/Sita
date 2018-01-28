

import java.util.*;

import java.io.*;

public class DaySchedule<String, Time> {
	private final int NUMBUCKETS = 30;
	private Node<String, Time>[] buckets;	
	
	@SuppressWarnings("unchecked")
	public DaySchedule()
	{
		buckets = (Node<String, Time>[])new Node<?, ?>[NUMBUCKETS];
	}
	
	public void add(String k, Time v)
	{
		int bucketIndex = hash(k);
		
		if(buckets[bucketIndex] != null)
		{
			Node<String, Time> ins = new Node<String, Time>(k, v);
			ins.setNext(buckets[bucketIndex]);
			buckets[bucketIndex] = ins;			
		}
		else
		{
			buckets[bucketIndex] = new Node<String, Time>(k, v);
		}

	}	
	
	public boolean hasAirlineCode(String code)
	{
		int bucketIndex = hash(code);
		
		Node<String, Time> curr = buckets[bucketIndex];
		
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
			
			Node<String, Time> curr = buckets[bucketIndex];
			
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
	
	private int hash(String airlineCode)
	{
		return Math.abs(airlineCode.hashCode()) % NUMBUCKETS;
	}
	
	
	private static class Node<String, Time>{
		private Time time;	
		private String code;
		private Node<String, Time> next;

		
		// Constructor to initialise the Node
		private Node(String newCode, Time newTime)
		{
			time = newTime;
			next = null;
			code = newCode;
		}

		private Node<String, Time> getNext() 
		{
			return next;
		}

		private void setNext(Node<String, Time> next) 
		{
			this.next = next;
		}

		private Time getTime() 
		{
			return time;
		}
		
		private String getCode()
		{
			return code;
		}
	}

}
