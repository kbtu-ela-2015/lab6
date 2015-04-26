package GUI;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import RPC.Client;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.AMQP.BasicProperties;

public class Calculator extends JFrame implements ActionListener{
	// Variables
	
		 
	
	
	final int MAX_INPUT_LENGTH = 20;
	final int INPUT_MODE = 0;
	final int RESULT_MODE = 1;
	final int ERROR_MODE = 2;
	int displayMode;

	boolean clearOnNextDigit, percent;
	double lastNumber;
	String lastOperator;

	private JMenu jmenuFile, jmenuHelp;
	private JMenuItem jmenuitemExit, jmenuitemAbout;
	
	private JLabel jlbOutput;
	private JButton jbnButtons[];
	private JPanel jplMaster, jplBackSpace, jplControl;
	
	private static String message;
	private static Client action = null;

	
	
	Font f12 = new Font("Times New Roman", 0, 12);
	Font f121 = new Font("Times New Roman", 1, 12);
	
	public Calculator() 
	{
		
						

		setBackground(Color.gray);

		jplMaster = new JPanel();

		jlbOutput = new JLabel("0");
		jlbOutput.setHorizontalTextPosition(JLabel.RIGHT);
		jlbOutput.setBackground(Color.WHITE);
		jlbOutput.setOpaque(true);
		
		getContentPane().add(jlbOutput, BorderLayout.NORTH);

		jbnButtons = new JButton[23];

		JPanel jplButtons = new JPanel();			// container for Jbuttons

		for (int i=0; i<=9; i++)
		{
			jbnButtons[i] = new JButton(String.valueOf(i));
		}

		jbnButtons[10] = new JButton("+/-");
		jbnButtons[11] = new JButton("n!");
		jbnButtons[12] = new JButton("=");
		jbnButtons[13] = new JButton("/");
		jbnButtons[14] = new JButton("*");
		jbnButtons[15] = new JButton("-");
		jbnButtons[16] = new JButton("+");
		jbnButtons[17] = new JButton("sqrt");
		jbnButtons[18] = new JButton("1/x");
		jbnButtons[19] = new JButton("%");
		
		jplBackSpace = new JPanel();
		jplBackSpace.setLayout(new GridLayout(1, 1, 2, 2));

		jbnButtons[20] = new JButton("Backspace");
		jplBackSpace.add(jbnButtons[20]);

		jplControl = new JPanel();
		jplControl.setLayout(new GridLayout(1, 2, 2 ,2));

		jbnButtons[21] = new JButton(" CE ");
		jbnButtons[22] = new JButton("C");

		jplControl.add(jbnButtons[21]);
		jplControl.add(jbnButtons[22]);

		for (int i=0; i<jbnButtons.length; i++)	{
			jbnButtons[i].setFont(f12);

			if (i<10)
				jbnButtons[i].setForeground(Color.blue);
				
			else
				jbnButtons[i].setForeground(Color.red);
		}
	
		jplButtons.setLayout(new GridLayout(4, 5, 2, 2));
		
		for(int i=7; i<=9; i++)		{
			jplButtons.add(jbnButtons[i]);
		}
		
		jplButtons.add(jbnButtons[13]);
		jplButtons.add(jbnButtons[17]);
		
		for(int i=4; i<=6; i++)
		{
			jplButtons.add(jbnButtons[i]);
		}
		
		jplButtons.add(jbnButtons[14]);
		jplButtons.add(jbnButtons[18]);

		for( int i=1; i<=3; i++)
		{
			jplButtons.add(jbnButtons[i]);
		}
		
		jplButtons.add(jbnButtons[15]);
		jplButtons.add(jbnButtons[19]);
		
		jplButtons.add(jbnButtons[0]);
		jplButtons.add(jbnButtons[10]);
		jplButtons.add(jbnButtons[11]);
		jplButtons.add(jbnButtons[16]);
		jplButtons.add(jbnButtons[12]);
		
		jplMaster.setLayout(new BorderLayout());
		jplMaster.add(jplBackSpace, BorderLayout.WEST);
		jplMaster.add(jplControl, BorderLayout.EAST);
		jplMaster.add(jplButtons, BorderLayout.SOUTH);

		getContentPane().add(jplMaster, BorderLayout.SOUTH);
		requestFocus();
		
		for (int i=0; i<jbnButtons.length; i++){
			jbnButtons[i].addActionListener(this);
		}
		


		clearAll();

		addWindowListener(new WindowAdapter() {

				public void windowClosed(WindowEvent e)
				{
					System.exit(0);
				}
			}
		);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	public void actionPerformed(ActionEvent e){
		double result = 0;
	   

		
		for (int i=0; i<jbnButtons.length; i++)
		{
			if(e.getSource() == jbnButtons[i])
			{
				switch(i)
				{
					case 0:
						addDigitToDisplay(i);
						break;

					case 1:
						addDigitToDisplay(i);
						break;

					case 2:
						addDigitToDisplay(i);
						break;

					case 3:
						addDigitToDisplay(i);
						break;

					case 4:
						addDigitToDisplay(i);
						break;

					case 5:
						addDigitToDisplay(i);
						break;

					case 6:
						addDigitToDisplay(i);
						break;

					case 7:
						addDigitToDisplay(i);
						break;

					case 8:
						addDigitToDisplay(i);
						break;

					case 9:
						addDigitToDisplay(i);
						break;

					case 10:	// +/-
						processSignChange();
						break;

					case 11:	// faktorial
						faktorial();
						break;

					case 12:	// =
						processEquals();
						break;

					case 13:	// divide
						processOperator("/");
						break;

					case 14:	// *
						processOperator("*");
						break;

					case 15:	// -
						processOperator("-");
						break;

					case 16:	// +
						processOperator("+");
						break;

					case 17:	// sqrt
						if (displayMode != ERROR_MODE)
						{
							try
							{
								if (getDisplayString().indexOf("-") == 0)
									displayError("Invalid input for function!");
								
								String message = "S";
								message += Double.toString(getNumberInDisplay());

								result = Double.parseDouble(makeRequest(message));
								displayResult(result);
							}

							catch(Exception ex)
							{
								displayError("Invalid input for function!");
								displayMode = ERROR_MODE;
							}
						}
						break;

					case 18:	// 1/x
						if (displayMode != ERROR_MODE){
							try
							{
								if (getNumberInDisplay() == 0)
									displayError("Cannot divide by zero!");
								
								String message = "X";
								message += Double.toString(getNumberInDisplay());

								result = Double.parseDouble(makeRequest(message));
								displayResult(result);
	
							}
							
							catch(Exception ex)	{
								displayError("Cannot divide by zero!");
								displayMode = ERROR_MODE;
							}
						}
						break;

					case 19:	// %
						if (displayMode != ERROR_MODE){
							try	{
								String message = "P";
								message += Double.toString(getNumberInDisplay());

								result = Double.parseDouble(makeRequest(message));
								displayResult(result);
							}
	
							catch(Exception ex)	{
								displayError("Invalid input for function!");
								displayMode = ERROR_MODE;
							}
						}
						break;

					case 20:	// backspace
						if (displayMode != ERROR_MODE){
							setDisplayString(getDisplayString().substring(0,
										getDisplayString().length() - 1));
							
							if (getDisplayString().length() < 1)
								setDisplayString("0");
						}
						break;

					case 21:	// CE
						clearExisting();
						break;

					case 22:	// C
						clearAll();
						break;
				}
			}
		}
	}

	void setDisplayString(String s){
		jlbOutput.setText(s);
	}

	String getDisplayString (){
		return jlbOutput.getText();
	}

	void addDigitToDisplay(int digit){
		if (clearOnNextDigit)
			setDisplayString("");

		String inputString = getDisplayString();
		
		if (inputString.indexOf("0") == 0){
			inputString = inputString.substring(1);
		}

		if ((!inputString.equals("0") || digit > 0)  && inputString.length() < MAX_INPUT_LENGTH){
			setDisplayString(inputString + digit);
		}
		

		displayMode = INPUT_MODE;
		clearOnNextDigit = false;
	}

	void faktorial(){
		String input = getDisplayString();
		String message = "F" + input;
		setDisplayString(makeRequest(message));
	}

	void processSignChange(){
		if (displayMode == INPUT_MODE)
		{
			String input = getDisplayString();
			
			if (input.length() > 0 && !input.equals("0"))
			{
				if (input.indexOf("-") == 0)
					setDisplayString(input.substring(1));

				else
					setDisplayString("-" + input);
			}
			
		}

		else if (displayMode == RESULT_MODE)
		{
			double numberInDisplay = getNumberInDisplay();
		
			if (numberInDisplay != 0)
				displayResult(-numberInDisplay);
		}
	}

	void clearAll()	{
		setDisplayString("0");
		lastOperator = "0";
		lastNumber = 0;
		displayMode = INPUT_MODE;
		clearOnNextDigit = true;
	}

	void clearExisting(){
		setDisplayString("0");
		clearOnNextDigit = true;
		displayMode = INPUT_MODE;
	}

	double getNumberInDisplay()	{
		String input = jlbOutput.getText();
		return Double.parseDouble(input);
	}

	void processOperator(String op) {
		if (displayMode != ERROR_MODE)
		{
			double numberInDisplay = getNumberInDisplay();

			if (!lastOperator.equals("0"))	
			{
				try
				{
					double result = processLastOperator();
					displayResult(result);
					lastNumber = result;
				}

				catch (DivideByZeroException e)
				{
				}
			}
		
			else
			{
				lastNumber = numberInDisplay;
			}
			
			clearOnNextDigit = true;
			lastOperator = op;
		}
	}

	void processEquals(){
		double result = 0;

		if (displayMode != ERROR_MODE){
			try			
			{
				result = processLastOperator();
				displayResult(result);
			}
			
			catch (DivideByZeroException e)	{
				displayError("Cannot divide by zero!");
			}

			lastOperator = "0";
		}
	}

	double processLastOperator() throws DivideByZeroException {
		double result = 0;
		double numberInDisplay = getNumberInDisplay();

		if (lastOperator.equals("/"))
		{
			if (numberInDisplay == 0)
				throw (new DivideByZeroException());

			String last = Double.toString(lastNumber);
			String inD = Double.toString(numberInDisplay);			
			String message = "/" + last + "|" + inD;
			result = Double.parseDouble(makeRequest(message));
		}
			
		if (lastOperator.equals("*")){
			String last = Double.toString(lastNumber);
			String inD = Double.toString(numberInDisplay);			
			String message = "*" + last + "|" + inD;
			result = Double.parseDouble(makeRequest(message));
		}
		
		if (lastOperator.equals("-")){
			String last = Double.toString(lastNumber);
			String inD = Double.toString(numberInDisplay);			
			String message = "-" + last + "|" + inD;
			result = Double.parseDouble(makeRequest(message));
		}

		if (lastOperator.equals("+")){
			String last = Double.toString(lastNumber);
			String inD = Double.toString(numberInDisplay);			
			String message = "+" + last + "|" + inD;
			result = Double.parseDouble(makeRequest(message));
		}
		return result;
	}

	void displayResult(double result){
		setDisplayString(Double.toString(result));
		lastNumber = result;
		displayMode = RESULT_MODE;
		clearOnNextDigit = true;
	}

	void displayError(String errorMessage){
		setDisplayString(errorMessage);
		lastNumber = 0;
		displayMode = ERROR_MODE;
		clearOnNextDigit = true;
	}

	public static void main(String args[]) {
		Calculator calci = new Calculator();
		Container contentPane = calci.getContentPane();
		calci.setTitle("Java Calculator");
		calci.setSize(241, 217);
		calci.pack();
		calci.setLocation(400, 250);
		calci.setVisible(true);
		calci.setResizable(false);		
		
	}
	


public String makeRequest(String message){
	String response = null;
	try {
	      action = Client.getInstance(); 
	      System.out.println(" [x] Requesting " + message);   
	      response = action.call(message);
	      System.out.println(" [.] Result: '" + response + "'");
	    }
	    catch  (Exception e) {
	      e.printStackTrace();
	    }
	    
	   return response;
}



class DivideByZeroException extends Exception{
	public DivideByZeroException()
	{
		super();
	}
	
	public DivideByZeroException(String s)
	{
		super(s);
	}
}
}

