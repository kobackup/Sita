/**
 * This class launches the SITA program that is used
 * to read in raw data that specify a particular month's
 * flight schedule and check-in counter login records and 
 * then calculate the charges due for time spent logged in 
 * outside of each airline's designated time periods
 * (Based on the schedule of departures)
 * 
 * @author Khari
 *
 */


public class RunSita {	
	
	public static void main(String[] args) {
		
		new SitaGUI().setVisible(true);	

	}

}
