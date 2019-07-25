package com.tamageta.financial.booking.rfq;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.tamageta.financial.booking.dao.UpdateEvent;
import com.tamageta.financial.booking.dao.UpdateListener;
import com.tamageta.financial.booking.gui.DecimalField;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;

public class AnalyzerHeaderPanel extends JPanel implements UpdateListener{
	private Quote rfq;
	
	private GridBagLayout gbl = new GridBagLayout();
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private JFormattedTextField textTradeDate = new JFormattedTextField(df);
	private DecimalField textSpotPrice = new DecimalField(new DecimalFormat("###,###,###.##"));
	private DecimalField textRiskFreeRate = new DecimalField(new DecimalFormat("#.#####"));
	private JComboBox comboSellOrBuy     = new JComboBox(new String[]{Quote.SELL,Quote.BUY});
	public final JComboBox comboStrategy     = new JComboBox(Quote.STRATEGIES);
	
	public AnalyzerHeaderPanel(){
		setBorder(BorderFactory.createTitledBorder("Trade"));
		
		setLayout(gbl);
		changeEditable();
		
		Dimension d = textSpotPrice.getPreferredSize();
		d.setSize(120, d.getHeight());
		textSpotPrice.setPreferredSize(d);
		textRiskFreeRate.setPreferredSize(d);
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.insets  = new Insets(5, 5, 5, 0);
		c.anchor  = GridBagConstraints.WEST;

		c.gridy   = 0;
		c.gridx   = 0;
//		add(new JLabel("Trade Date:", JLabel.RIGHT), c);
//		c.gridx   = 1;
//		add(textTradeDate, c);

//		c.gridy++;
//		c.gridx   = 0;
//		add(new JLabel("Sell/Buy:", JLabel.RIGHT), c);
//		c.gridx   = 1;
//		add(comboSellOrBuy, c);
		
		c.gridy++;
		c.gridx   = 0;
		add(new JLabel("Spot Price:", JLabel.RIGHT), c);
		c.gridx   = 1;
		add(textSpotPrice, c);

//		c.gridy++;
		c.gridx   = 2;
		add(new JLabel("Repo Rate:", JLabel.RIGHT), c);
		c.gridx   = 3;
		add(textRiskFreeRate, c);
		
//		c.gridy++;
		c.gridx   = 4;
		add(new JLabel("Strategy:", JLabel.RIGHT), c);
		c.gridx   = 5;
		add(comboStrategy, c);
		
		textSpotPrice.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					String text = textSpotPrice.getText();
					getRFQ().setSpotPrice(Double.parseDouble(text));
				}
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void updated(UpdateEvent event) {
	}
	
	void addComponentAsLabel(Component b, int x, int y, int w) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = 1;
        gbl.setConstraints(b, gbc);
        add(b);
    }
    void addComponentAsEntry(Component b, int x, int y, int w) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = 1;
        gbl.setConstraints(b, gbc);
        add(b);
    }
    void addComponentAsButton(Component b, int x, int y, int w) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = 1;
        gbl.setConstraints(b, gbc);
        add(b);
    }
	
    private void changeEditable(){
		textTradeDate.setEditable(true);
		textSpotPrice.setEditable(true);
		textRiskFreeRate.setEditable(true);
		comboSellOrBuy.setEnabled(true);
		comboStrategy.setEnabled(true);
    }
    
	public synchronized void setRFQ(Quote rfq){
		this.rfq = rfq;
		if(rfq == null){
			textTradeDate.setValue(null);
			textSpotPrice.setValue(null);
			textSpotPrice.setValue(null);
			
			comboSellOrBuy.setSelectedIndex(0);
			comboStrategy.setSelectedIndex(0);
			textRiskFreeRate.setValue(null);
		}else{	
			textTradeDate.setValue(rfq.getTradeDate());
			textSpotPrice.setValue(rfq.getSpotPrice());
			
			comboSellOrBuy.setSelectedItem(rfq.getBuyOrSell());
			comboStrategy.setSelectedItem(rfq.getStrategy());
			textRiskFreeRate.setValue(rfq.getRiskFreeRate());
		}
		changeEditable();
	}
	
	public Quote getRFQ(){
		if(rfq != null){
			rfq.setTradeDate((Date)textTradeDate.getValue());
			if(textSpotPrice.getValue() != null){
				rfq.setSpotPrice(((Number)textSpotPrice.getValue()).doubleValue());
			}
			rfq.setBuyOrSell((String)comboSellOrBuy.getSelectedItem());
			rfq.setStrategy((String)comboStrategy.getSelectedItem());
			if(textRiskFreeRate.getValue() != null){
				rfq.setRiskFreeRate(((Number)textRiskFreeRate.getValue()).doubleValue());
			}
		}
		return rfq;
	}
	
	public static void main(String[] args){
		Quote rfq = new QuoteImpl(){};
		rfq.setUserId("ken");
	 
		final AnalyzerHeaderPanel panel = new AnalyzerHeaderPanel();
		panel.setRFQ(rfq);
		try{
			JPanel pp = new JPanel();
			pp.setLayout(new BorderLayout());
			pp.add(panel, BorderLayout.CENTER);
			
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(pp);
			frame.pack();
			frame.setVisible(true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
