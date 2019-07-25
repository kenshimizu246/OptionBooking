package com.tamageta.financial.booking.rfq;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.tamageta.financial.booking.gui.DateField;
import com.tamageta.financial.booking.gui.TaskCancelEvent;
import com.tamageta.financial.booking.gui.TaskCancelListener;
import com.tamageta.financial.math.MonteCalro;
import com.tamageta.financial.math.MonteCalroDoneEvent;
import com.tamageta.financial.math.MonteCalroProgressEvent;
import com.tamageta.financial.math.MonteCalroListener;
import com.tamageta.financial.math.Probability;
import com.tamageta.financial.math.Random;

public class MonteCalroPanel extends JPanel implements MonteCalroListener{
	public static final String CALL = "Call";
	public static final String PUT  = "Put";
	private ExecutorService threadCmd = Executors.newSingleThreadExecutor();
	private DateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
	private DecimalFormat fmtDec = new DecimalFormat("###,###,###,###,###.#######");
	private JLabel labelCallOrPut = new JLabel("Call/Put:");
	private JComboBox comboCallOrPut = new JComboBox(new String[]{CALL,PUT});
	private JLabel labelTradeDate = new JLabel("Trade Date:");
	private JFormattedTextField textTradeDate = new JFormattedTextField(fmtDate);
	private JLabel labelExpiry = new JLabel("Expiry:");
	//private JFormattedTextField textExpiry = new JFormattedTextField(fmtDate);
	private DateField textExpiry = new DateField();
	private JLabel labelDays = new JLabel("Days:");
	private JFormattedTextField textDays = new JFormattedTextField(fmtDec);
	private JLabel labelSpot = new JLabel("Spot:");
	private JFormattedTextField textSpot = new JFormattedTextField(fmtDec);
	private JLabel labelStrike = new JLabel("Strike:");
	private JFormattedTextField textStrike = new JFormattedTextField(fmtDec);
	private JLabel labelRate = new JLabel("Rate:");
	private JFormattedTextField textRate = new JFormattedTextField(fmtDec);
	private JLabel labelVol = new JLabel("Volatility:");
	private JFormattedTextField textVol = new JFormattedTextField(fmtDec);
	private JLabel labelCount = new JLabel("Count:");
	private JFormattedTextField textCount = new JFormattedTextField(fmtDec);
	private JLabel labelDrift = new JLabel("Drift Value:");
	private JFormattedTextField textDrift = new JFormattedTextField(fmtDec);
	private JLabel labelCallVal = new JLabel("Call Value:");
	private JFormattedTextField textCallVal = new JFormattedTextField(fmtDec);
	private JLabel labelPremium = new JLabel("Premium:");
	private JFormattedTextField textPremium = new JFormattedTextField(fmtDec);
	
	private JProgressBar progressBar = new JProgressBar();
	private JButton btnCalc = new JButton("Calculate");
	private JButton btnCancel = new JButton("Cancel");
	private volatile boolean cancel = false;
	
	private final Random random;
	private double testValue = Math.random();
	private boolean debug = true;
	
	private Vector<TaskCancelListener> clisteners = new Vector<TaskCancelListener>();
		
	public MonteCalroPanel(){
		random = new Random(){
			private java.util.Random rdm = new java.util.Random();
			public double nextDouble() {
				return (!debug ? rdm.nextDouble() : testValue);
			}
			public double nextGaussian() {
				return (!debug ? rdm.nextGaussian() : testValue);
				//throw new UnsupportedOperationException();
			}
		};
		Calendar calendar = new GregorianCalendar();
		Dimension d = new Dimension(120, textTradeDate.getPreferredSize().height);
		textTradeDate.setPreferredSize(d);
		textExpiry.setPreferredSize(d);
		textDays.setPreferredSize(d);
		textSpot.setPreferredSize(d);
		textStrike.setPreferredSize(d);
		textRate.setPreferredSize(d);
		textVol.setPreferredSize(d);
		textCount.setPreferredSize(d);
		textDrift.setPreferredSize(d);
		textCallVal.setPreferredSize(d);
		textPremium.setPreferredSize(d);
		
		textDays.setEditable(false);
		textDrift.setEditable(false);
		textCallVal.setEditable(false);
		textPremium.setEditable(false);
		
		textTradeDate.setValue(new Date());
		textTradeDate.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				textExpiry.setTradeDate((Date)textTradeDate.getValue());
			}
		});
		calendar.add(Calendar.DAY_OF_YEAR, 61);
		textExpiry.setValue(calendar.getTime());
		
		btnCalc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				cancel = false;
				threadCmd.execute(new Runnable(){
					public void run(){
						doCalc();
						//calc();
					}
				});
			}
		});
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				fireTaskCancelEvent(new TaskCancelEvent(this));
			}
		});
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.insets  = new Insets(5, 5, 5, 0);
		c.anchor  = GridBagConstraints.WEST;

		int row = 0;
		c.gridy   = row;
		c.gridx   = 0;
		add(labelCallOrPut, c);
		c.gridx   = 1;
		add(comboCallOrPut, c);
		
		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelTradeDate, c);
		c.gridx   = 1;
		add(textTradeDate, c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelExpiry,c);
		c.gridx   = 1;
		add(textExpiry,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelDays,c);
		c.gridx   = 1;
		add(textDays,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelSpot,c);
		c.gridx   = 1;
		add(textSpot,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelSpot,c);
		c.gridx   = 1;
		add(textSpot,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelStrike,c);
		c.gridx   = 1;
		add(textStrike,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelRate,c);
		c.gridx   = 1;
		add(textRate,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelVol,c);
		c.gridx   = 1;
		add(textVol,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelCount,c);
		c.gridx   = 1;
		add(textCount,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelDrift,c);
		c.gridx   = 1;
		add(textDrift,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelCallVal,c);
		c.gridx   = 1;
		add(textCallVal,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(labelPremium,c);
		c.gridx   = 1;
		add(textPremium,c);

		c.gridy   = ++row;
		c.gridx   = 1;
		add(progressBar,c);

		c.gridy   = ++row;
		c.gridx   = 0;
		add(btnCalc,c);
		c.gridx   = 1;
		add(btnCancel,c);
	}
	
	private void doCalc(){
		Date trd = (Date)textTradeDate.getValue();
		Date exp = (Date)textExpiry.getValue();
		int days = (int)((exp.getTime() - trd.getTime())/1000/60/60/24)+1;
		
		btnCancel.setEnabled(true);
		btnCalc.setEnabled(false);

		textDays.setValue(days);

		if(debug){
			//String s = JOptionPane.showInputDialog(this, "randam value?", "randam");
			//testValue = Double.parseDouble(s);
			testValue = 0.639407;
		}
		
		progressBar.setMaximum(getCount());
		MonteCalro mc = new MonteCalro(days, 
										getSpot(), 
										getStrike(), 
										getRate(), 
										getVolatility(), 
										CALL.equals(comboCallOrPut.getSelectedItem()), 
										getCount(),
										random);
		textDrift.setValue(mc.getDrift());
		addTaskCancelListener(mc);
		mc.addMonteCalroListener(this);
		threadCmd.execute(mc);
	}
	
	public void progress(MonteCalroProgressEvent event){
		progressBar.setValue(event.getValue());
	}

	public void calculated(MonteCalroDoneEvent event){
		textCallVal.setValue(event.getOptionValue());
		textPremium.setValue(event.getPremiumValue());
		btnCancel.setEnabled(false);
		btnCalc.setEnabled(true);
	}
	
	public void addTaskCancelListener(TaskCancelListener l){
		clisteners.add(l);
	}
	public void removeTaskCancelListener(TaskCancelListener l){
		clisteners.remove(l);
	}
	private void fireTaskCancelEvent(TaskCancelEvent event){
		for(TaskCancelListener l : clisteners){
			l.cancel(event);
		}
	}
	
	private void calc(){
		Date trd = (Date)textTradeDate.getValue();
		Date exp = (Date)textExpiry.getValue();
		long days = ((exp.getTime() - trd.getTime())/1000/60/60/24)+1;
		
		textDays.setValue(days);
		
		double t1 = days/365d;
		double t2 = Math.sqrt(t1);
		
		double vol = getVolatility();
		double rate = getRate();
		double spot = getSpot();
		double strike = getStrike();
		int cnt = getCount();
		boolean isCall = CALL.equals(comboCallOrPut.getSelectedItem());

		double drift = rate - (Math.pow(vol, 2d)/2d);
		textDrift.setValue(drift);

		double avg = 0.0d;
		double sum = 0.0d;
		double min = 9999999999999999d;
		double max = 0.0d;
		
		
		progressBar.setMaximum(cnt);
		for(int i = 0; i < cnt && !cancel; i++){
			//double rn = Probability.normalCdf(Math.random());
			double rn = Probability.inverseNormal(random.nextDouble());
			double dd = Math.exp((rn * t2 * vol) + (drift * t1) + Math.log(spot)); 
			double value = Math.max(isCall ? (dd - strike) : (strike - dd), 0);
			sum += value;
			System.out.println("avg1["+value+"]:"+avg);
			avg += (value / cnt);
			System.out.println("avg2["+value+"]:"+avg);
			
			min = Math.min(min, dd);
			max = Math.max(max, dd);

			progressBar.setValue(i+1);
		}
		System.out.println("min:"+min);
		System.out.println("max:"+max);
		System.out.println("avg1:"+sum/cnt);
		System.out.println("avg2:"+avg);
		double optVal = sum/cnt;
		double premium = optVal * Math.exp(rate * t1);
		textCallVal.setValue(optVal);
		textPremium.setValue(premium);
	}
	
	
	public void setTradeDateEditable(boolean b){
		textTradeDate.setEditable(b);
	}
	public boolean isTradeDateEditable(){
		return textTradeDate.isEditable();
	}
	public void setTradeDate(Date date){
		textTradeDate.setValue(date);
	}
	public Date getTradeDate(){
		return (textExpiry.getValue() instanceof Date
				? (Date)textExpiry.getValue() : null);
	}

	public void setExpiryEditable(boolean b){
		textExpiry.setEditable(b);
	}
	public boolean isExpiryEditable(){
		return textExpiry.isEditable();
	}
	public void setExpiry(Date date){
		textExpiry.setValue(date);
	}
	public Date getExpiry(){
		return (textExpiry.getValue() instanceof Date
				?(Date)textExpiry.getValue() : null);
	}

	public void setSpotEditable(boolean b){
		textSpot.setEditable(b);
	}
	public boolean isSpotEditable(){
		return textSpot.isEditable();
	}
	public void setSpot(double spot){
		textSpot.setValue(spot);
	}
	public double getSpot(){
		return (textSpot.getValue() instanceof Number
				?((Number)textSpot.getValue()).doubleValue() : Double.NaN);
	}

	public void setStrikeEditable(boolean b){
		textStrike.setEditable(b);
	}
	public boolean isStrikeEditable(){
		return textStrike.isEditable();
	}
	public void setStrike(double strike){
		textStrike.setValue(strike);
	}
	public double getStrike(){
		return (textStrike.getValue() instanceof Number
				?((Number)textStrike.getValue()).doubleValue() : Double.NaN);
	}

	public void setRateEditable(boolean b){
		textRate.setEditable(b);
	}
	public boolean isRateEditable(){
		return textRate.isEditable();
	}
	public void setRate(double rate){
		textRate.setValue(rate);
	}
	public double getRate(){
		return (textRate.getValue() instanceof Number
				?((Number)textRate.getValue()).doubleValue() : Double.NaN);
	}

	public void setVolatilityEditable(boolean b){
		textVol.setEditable(b);
	}
	public boolean isVolatilityEditable(){
		return textVol.isEditable();
	}
	public void setVolatility(double volatility){
		textVol.setValue(volatility);
	}
	public double getVolatility(){
		return (textVol.getValue() instanceof Number
				?((Number)textVol.getValue()).doubleValue() : Double.NaN);
	}

	public void setCountEditable(boolean b){
		textCount.setEditable(b);
	}
	public boolean isCountEditable(){
		return textCount.isEditable();
	}
	public void setCount(int count){
		textCount.setValue(count);
	}
	public int getCount(){
		return (textCount.getValue() instanceof Number
				? ((Number)textCount.getValue()).intValue() : -1);
	}

	public double getCallValue(){
		return (textCallVal.getValue() instanceof Number
				?((Number)textCallVal.getValue()).doubleValue() : Double.NaN);
	}
	public double getPremium(){
		return (textPremium.getValue() instanceof Number
				?((Number)textPremium.getValue()).doubleValue() : Double.NaN);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MonteCalroPanel panelmc = new MonteCalroPanel();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(panelmc);
		panelmc.setVolatility(0.38);
		panelmc.setRate(0.06);
		panelmc.setSpot(14500);
		panelmc.setStrike(14000);
		panelmc.setCount(500000);
		frame.pack();
		frame.setVisible(true);
	}
}
