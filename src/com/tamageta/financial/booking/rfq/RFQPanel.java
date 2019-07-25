package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.xml.soap.SOAPException;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.dao.SoapDao;
import com.tamageta.financial.booking.dao.UpdateEvent;
import com.tamageta.financial.booking.dao.UpdateListener;
import com.tamageta.financial.booking.gui.DecimalField;
import com.tamageta.financial.booking.gui.UnderlyingListDialog;
import com.tamageta.financial.booking.rfq.data.Client;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.Underlying;
import com.tamageta.financial.util.GuiUtility;

public class RFQPanel extends JPanel implements UpdateListener, UnderlyingListener {
	private Quote rfq;
	private Dao dao = null;
	private Underlying underlying = null;
	
	private GridBagLayout gbl = new GridBagLayout();
	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private JFormattedTextField textID = new JFormattedTextField();
	private JFormattedTextField textTradeDate = new JFormattedTextField(df);
	private JTextField textStatus = new JTextField();
	private ClientComboBox  cmbClient = new ClientComboBox();
	private UnderlyingComboBox cmbUnderlying = new UnderlyingComboBox();
	private JTextField textUnderlyingUnit = new JTextField();
	private DecimalField textSpotPrice = new DecimalField(new DecimalFormat("###,###,###.##"));
	private DecimalField textRiskFreeRate = new DecimalField(new DecimalFormat("#.#####"));
	private JComboBox comboSellOrBuy     = new JComboBox(new String[]{Quote.SELL,Quote.BUY});
	private JComboBox comboOpenClose     = new JComboBox(new String[]{Quote.OPEN,Quote.CLOSE});
	public final JComboBox comboStrategy     = new JComboBox(Quote.STRATEGIES);
	
	private UnderlyingListDialog ulDialog = new UnderlyingListDialog((Frame)SwingUtilities.getRoot(this));
	
	public RFQPanel(){
		setBorder(BorderFactory.createTitledBorder("Trade"));
		textID.setEditable(false);
		
		setLayout(gbl);
		
		int height = textID.getPreferredSize().height;
		textID.setPreferredSize(new Dimension(120,height));
		textTradeDate.setPreferredSize(new Dimension(120,height));
		textStatus.setPreferredSize(new Dimension(120,height));
		textUnderlyingUnit.setPreferredSize(new Dimension(80,height));
		textSpotPrice.setPreferredSize(new Dimension(80,height));
		textRiskFreeRate.setPreferredSize(new Dimension(80,height));

		cmbClient.setEditable(false);
		cmbUnderlying.setEditable(false);
		cmbUnderlying.addUnderlyingListener(this);
		textUnderlyingUnit.setEditable(false);
		changeEditable();
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.insets  = new Insets(5, 5, 5, 0);
		c.anchor  = GridBagConstraints.WEST;
        c.gridwidth = 1;

		c.gridy   = 0;
		c.gridx   = 0;
		add(new JLabel("Trade ID:", JLabel.RIGHT), c);
		c.gridx   = 1;
		add(textID, c);
		c.gridx   = 2;
		add(textStatus, c);
		c.gridx   = 5;
		add(new JLabel("Trade Date:", JLabel.RIGHT), c);
		c.gridx   = 6;
		add(textTradeDate, c);

		c.gridy++;
		c.gridx   = 0;
        c.gridwidth = 1;
		add(new JLabel("Client:", JLabel.LEFT), c);
		c.gridx   = 1;
        c.gridwidth = 6;
		add(cmbClient, c);

		c.gridy++;
		c.gridx   = 0;
        c.gridwidth = 1;
		add(new JLabel("Underlying:", JLabel.RIGHT), c);
		c.gridx   = 1;
        c.gridwidth = 6;
		add(cmbUnderlying, c);
		
		c.gridy++;
		c.gridx   = 3;
        c.gridwidth = 1;
		add(new JLabel("Open/Close:", JLabel.RIGHT), c);
		c.gridx   = 4;
		add(comboOpenClose, c);
		c.gridx   = 5;
		add(new JLabel("Sell/Buy:", JLabel.RIGHT), c);
		c.gridx   = 6;
		add(comboSellOrBuy, c);
		
		c.gridy++;
		c.gridx   = 0;
        c.gridwidth = 1;
		add(new JLabel("Spot Price:", JLabel.RIGHT), c);
        c.gridwidth = 1;
		c.gridx   = 1;
		add(textSpotPrice, c);
		c.gridx   = 3;
		add(new JLabel("RiskFree Rate:", JLabel.RIGHT), c);
		c.gridx   = 4;
		add(textRiskFreeRate, c);
		c.gridx   = 5;
		add(new JLabel("Strategy:", JLabel.RIGHT), c);
		c.gridx   = 6;
		add(comboStrategy, c);
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
    	if(rfq != null && Quote.STATUS_NEW.equals(rfq.getStatus())){
    		textID.setEditable(false);
    		textTradeDate.setEditable(true);
    		textStatus.setEditable(false);

    		cmbClient.setEditable(!rfq.hasOriginalQuoteId());
    		cmbClient.setEnabled(!rfq.hasOriginalQuoteId());
    		cmbUnderlying.setEditable(!rfq.hasOriginalQuoteId());
    		cmbUnderlying.setEnabled(!rfq.hasOriginalQuoteId());
    		textUnderlyingUnit.setEditable(false);
    		
    		textSpotPrice.setEditable(true);
    		textRiskFreeRate.setEditable(true);
    		comboSellOrBuy.setEnabled(!rfq.hasOriginalQuoteId());
    		comboOpenClose.setEnabled(!rfq.hasOriginalQuoteId());
    		comboStrategy.setEnabled(!rfq.hasOriginalQuoteId());
    	}else{
    		textID.setEditable(false);
    		textTradeDate.setEditable(false);
    		textStatus.setEditable(false);

    		cmbClient.setEditable(false);
    		cmbClient.setEnabled(false);
    		cmbUnderlying.setEditable(false);
    		cmbUnderlying.setEnabled(false);
    		textUnderlyingUnit.setEditable(false);
    		
    		if(rfq != null && Quote.STATUS_IOI.equals(rfq.getStatus())){
	    		textSpotPrice.setEditable(true);
	    		textRiskFreeRate.setEditable(true);
    		}else{
	    		textSpotPrice.setEditable(false);
	    		textRiskFreeRate.setEditable(false);
    		}
    		comboSellOrBuy.setEnabled(false);
    		comboOpenClose.setEnabled(false);
    		comboStrategy.setEnabled(false);
    	}
    }
    
	public synchronized void setRFQ(Quote rfq){
		this.rfq = rfq;
		if(rfq == null){
			textID.setText(null);
			
			textTradeDate.setValue(null);
			textStatus.setText(null);
			cmbClient.setClient((Client)null);
			cmbUnderlying.setUnderlying((Underlying)null);
			textSpotPrice.setValue(null);
			textUnderlyingUnit.setText(null);
			textSpotPrice.setValue(null);
			
			comboSellOrBuy.setSelectedIndex(0);
			comboOpenClose.setSelectedIndex(0);
			comboStrategy.setSelectedIndex(0);
			textRiskFreeRate.setValue(null);
		}else{
			textID.setText(String.valueOf(rfq.getQuoteId()));
	
			textTradeDate.setValue(rfq.getTradeDate());
			textStatus.setText(rfq.getStatus());
			cmbClient.setClient(rfq.getClientId());
			cmbUnderlying.setUnderlying(rfq.getUnderlyingCode());
			if(Quote.STATUS_NEW.equals(rfq.getStatus())){
				doUnderlyingSearch();
			}else{
				textSpotPrice.setValue(rfq.getSpotPrice());
			}
			
			comboSellOrBuy.setSelectedItem(rfq.getBuyOrSell());
			comboOpenClose.setSelectedItem(rfq.getOpenClose());
			comboStrategy.setSelectedItem(rfq.getStrategy());
			textRiskFreeRate.setValue(rfq.getRiskFreeRate());
		}
		changeEditable();
	}
	
	public Quote getRFQ(){
		if(rfq != null){
			rfq.setTradeDate((Date)textTradeDate.getValue());
			rfq.setClientId(cmbClient.getClientId());
			rfq.setClientName((cmbClient.getClient() != null ? cmbClient.getClient().getName() : ""));
			rfq.setUnderlyingCode(cmbUnderlying.getRic());
			if(textSpotPrice.getValue() != null){
				rfq.setSpotPrice(((Number)textSpotPrice.getValue()).doubleValue());
			}
			rfq.setBuyOrSell((String)comboSellOrBuy.getSelectedItem());
			rfq.setOpenClose((String)comboOpenClose.getSelectedItem());
			rfq.setStrategy((String)comboStrategy.getSelectedItem());
			if(textRiskFreeRate.getValue() != null){
				rfq.setRiskFreeRate(((Number)textRiskFreeRate.getValue()).doubleValue());
			}
		}
		return rfq;
	}
	
	private void doUnderlyingSearch(){
		String code = cmbUnderlying.getRic();
		
		if(dao == null || code == null || code.trim().length() < 1){
			setUnderlying(null);
			return;
		}
		try{
			Underlying underlying = dao.getUnderlying(code.toUpperCase());
			if(underlying == null){
				setUnderlying(null);
				return;
			}
			setUnderlying(underlying);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void openUnderlyingDialog(){
		try{
			ulDialog.setRics(dao.getUnderlyingRics(null));
			ulDialog.pack();
			GuiUtility.centerWindow(ulDialog, 
					((Frame)SwingUtilities.getRoot(this)).getSize(), 
					((Frame)SwingUtilities.getRoot(this)).getLocation());
			ulDialog.setVisible(true);
			if(ulDialog.isOK()){
				try{
					setUnderlying(dao.getUnderlying(ulDialog.getRic().getRic()));
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		}catch(DaoException ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		Quote rfq = new QuoteImpl(){};
		rfq.setUserId("ken");
	 
		final RFQPanel panel = new RFQPanel();
		panel.setRFQ(rfq);
		try{
			final Dao dao = new SoapDao("http://192.168.1.2:8080/edsweb-ken/quote/services");
			panel.setDao(dao);
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

	public Dao getDao() {
		return dao;
	}

	public void setDao(Dao dao) {
		this.dao = dao;
		dao.addUpdateListener(this);
		cmbClient.setDao(dao);
		cmbUnderlying.setDao(dao);
	}

	public Underlying getUnderlying() {
		return underlying;
	}

	public void setUnderlying(Underlying underlying) {
		this.underlying = underlying;
		cmbUnderlying.setUnderlying(underlying);
		if(underlying != null){
			textUnderlyingUnit.setText(Integer.toString(underlying.getMultiplier()));
			textSpotPrice.setValue(underlying.getPriceClose());
		}else{
			textUnderlyingUnit.setText("");
			textSpotPrice.setValue(null);
		}
	}
	public void underlynigChange(UnderlyingEvent event) {
		if(event.getUnderlying() != null){
			textSpotPrice.setValue(event.getUnderlying().getPriceClose());
		}else{
			textSpotPrice.setValue(null);
		}
	}
}
