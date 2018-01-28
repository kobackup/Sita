import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SitaGUI extends JFrame implements ActionListener{
	private JButton chooseDataButton, chooseScheduleButton, runButton;
	private JTextField dataTextField, scheduleTextField;
	private String chosenDataFile = "";
	private String chosenScheduleFile = "";
	private Init parser;
	
	
	public SitaGUI()
	{
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Select Files");
		setLocation(200,200);
		setSize(400, 170);
		
		layoutPanels();
		
	}
	
	private void layoutPanels()
	{
		JPanel top = new JPanel(new GridLayout(2, 3, 10, 10));
		//GridLayout layout = new GridLayout(3,3);
		//setLayout(layout);
		
		JLabel dataLabel = new JLabel("Choose data file");
		dataTextField = new JTextField(35);
		chooseDataButton = new JButton("...");
		chooseDataButton.addActionListener(this);
		JPanel buttonPanel1 = new JPanel();
		buttonPanel1.add(chooseDataButton);
		top.add(dataLabel);
		top.add(dataTextField);
		top.add(buttonPanel1);
		
		JLabel scheduleLabel = new JLabel("Choose schedule file");
		scheduleTextField = new JTextField(35);
		chooseScheduleButton = new JButton("...");
		chooseScheduleButton.addActionListener(this);
		JPanel buttonPanel2 = new JPanel();
		buttonPanel2.add(chooseScheduleButton);
		top.add(scheduleLabel);
		top.add(scheduleTextField);
		top.add(buttonPanel2);
		
		
		add(top, BorderLayout.NORTH);
		
		
		/*
		JPanel top = new JPanel();
		JLabel dataLabel = new JLabel("Choose data file");
		dataTextField = new JTextField(35);
		chooseDataButton = new JButton("...");
		chooseDataButton.addActionListener(this);
		top.add(dataLabel);
		top.add(dataTextField);
		top.add(chooseDataButton);
		add(top, BorderLayout.NORTH);
		
		JPanel middle = new JPanel();
		JLabel scheduleLabel = new JLabel("Choose schedule file");
		scheduleTextField = new JTextField(35);
		chooseScheduleButton = new JButton("...");
		chooseScheduleButton.addActionListener(this);
		middle.add(scheduleLabel);
		middle.add(scheduleTextField);
		middle.add(chooseScheduleButton);
		add(middle, BorderLayout.CENTER);
		*/
		
		
		JPanel bottom = new JPanel();
		runButton = new JButton("Run");
		runButton.addActionListener(this);
		bottom.add(runButton);
		add(bottom, BorderLayout.SOUTH);
	}
	
	private String launchFileChooser(String type, String extension)
	{
		JFileChooser chooser = new JFileChooser();
		String fileType = type;
		String fileExtension = extension;
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(type, extension);
	    chooser.setFileFilter(filter);	     
	    chooser.showOpenDialog(SitaGUI.this);
	    
	    String path = "";
	    
	    try
	    {
	    	path = chooser.getSelectedFile().getCanonicalPath();
	    }
	    catch(IOException e)
	    {
	    	e.printStackTrace();
	    }
	    
	    return path;
	    
	    /*
	    int returnVal = chooser.showOpenDialog(SitaGUI.this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	            
	    } */
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == chooseDataButton)
		{			
			try
			{
				chosenDataFile = launchFileChooser("CSV", "csv").replaceAll("\\\\", "/");
			}
			catch(NullPointerException n)
			{
				chosenDataFile = "";
			}
			
			if(chosenDataFile != "")
			{
				dataTextField.setText(chosenDataFile);
			}
			
		}
		if(e.getSource() == chooseScheduleButton)
		{
			try
			{
				chosenScheduleFile = launchFileChooser("Excel", "xlsx").replaceAll("\\\\", "/");
			}
			catch(NullPointerException n)
			{
				chosenScheduleFile = "";
			}
			
			if(chosenScheduleFile != "")
			{
				scheduleTextField.setText(chosenScheduleFile);
			}
			
		}
		if(e.getSource() == runButton)
		{
			parser = new Init(chosenDataFile, chosenScheduleFile);
		}
	}

}
