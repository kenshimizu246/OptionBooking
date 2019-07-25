package com.tamageta.financial.booking;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import com.tamageta.financial.booking.dao.UpdateEvent;
import com.tamageta.financial.booking.dao.UpdateListener;
import com.tamageta.financial.booking.gui.DateFieldEditor;
import com.tamageta.financial.booking.gui.DecimalFieldEditor;
import com.tamageta.financial.booking.gui.DefaultStrategyLegFieldRenderer;
import com.tamageta.financial.booking.rfq.AnalyzerHeaderPanel;
import com.tamageta.financial.booking.rfq.DividendTableModel;
import com.tamageta.financial.booking.rfq.ProgressDialog;
import com.tamageta.financial.booking.rfq.StrategyTableModel;
import com.tamageta.financial.booking.rfq.SummaryPanel;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.math.PriceEngineBS;
import com.tamageta.financial.util.GuiUtility;

public class OptionCalculatorFrame extends JFrame implements UpdateListener{
	private AnalyzerHeaderPanel panelRFQ = new AnalyzerHeaderPanel();
	private StrategyTableModel modelLegs = new StrategyTableModel();
	private JTable tableLegs = null;
	
	private JTabbedPane tabPrice = new JTabbedPane();
	
	private DividendTableModel modelDiv = new DividendTableModel();
	private JTable tableDiv = new JTable(modelDiv);
	private SummaryPanel begraph = new SummaryPanel();
	
	private JPanel panelMain = new JPanel(new BorderLayout());
	private JPanel panelBody = new JPanel();
	private JSplitPane splitPrice = new JSplitPane();

    private JComboBox comboSL = new JComboBox();
    private JComboBox comboCP = new JComboBox();

    JMenuBar mbarMain = new JMenuBar();
    JMenu menuFile = new JMenu("File");
    JMenuItem miSave = new JMenuItem("Save");
    JMenuItem miQuit = new JMenuItem("Quit");
    JMenu menuHelp = new JMenu("Help");
    JMenuItem miAbout = new JMenuItem("About...");
    
	JPopupMenu popupLeg = new JPopupMenu();
	JMenuItem pmiAddLeg = new JMenuItem("Add Leg");
	JMenuItem pmiDelLeg = new JMenuItem("Delete Leg");
	JMenuItem pmiCalc = new JMenuItem("Calculation");
	
	JPopupMenu popupTrade = new JPopupMenu();
	JMenuItem pmiReleaseOwner = new JMenuItem("Release Ownership");
	
	private ExecutorService commandExecutor = Executors.newSingleThreadExecutor();
	private abstract class Command<R> implements Runnable, Callable<R> {
		private String title;
		private String message;
		public Command(String title, String message){
			this.title = title;
			this.message = message;
		}
		public void run(){
			try{
				R result = ProgressDialog.execute(OptionCalculatorFrame.this, title, message, this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public abstract R call();
	}
	
	public OptionCalculatorFrame(){
		setTitle("Option Pricing V1.0");
		tableLegs = new JTable(modelLegs);
		
		panelRFQ.comboStrategy.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(panelRFQ.getRFQ() != null){
					panelRFQ.getRFQ().setStrategyLegs((String)e.getItem());
					modelLegs.setStrategy((String)e.getItem());
					modelLegs.fireTableDataChanged();
					for(StrategyLeg l : panelRFQ.getRFQ().getStrategies()){
						l.addDataUpdateHandler(begraph);
					}
				}
			}
		});
		
        comboSL.addItem("S");
        comboSL.addItem("L");
        comboCP.addItem(StrategyLeg.CALL);
        comboCP.addItem(StrategyLeg.PUT);
        
        setJMenuBar(mbarMain);
        mbarMain.add(menuFile);
        menuFile.add(miSave);
        menuFile.addSeparator();
        menuFile.add(miQuit);
        mbarMain.add(menuHelp);
        menuHelp.add(miAbout);

        miSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						//doSave();
						return null;
					}
				});
			}
		});
        miAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						doAbout();
						return null;
					}
				});
			}
		});
        
        popupLeg.add(pmiCalc);
        popupLeg.addSeparator();
		popupLeg.add(pmiAddLeg);
		popupLeg.add(pmiDelLeg);
		pmiCalc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//doCalc();
			}
		});
		
		tabPrice.addTab("Summary", begraph);
		//tabPrice.addTab("Dividend", new JScrollPane(tableDiv));
		
		modelLegs.addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent event){
				if(event.getColumn() > -1 && event.getFirstRow() > -1){
					//doCalc(modelLegs.getStrategyLeg(event.getFirstRow()), (event.getColumn() == 6));
				}
			}
			
		});
		tableLegs.setPreferredScrollableViewportSize(new Dimension(500, 120));
		tableLegs.setFillsViewportHeight(true);
		tableLegs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		tableLegs.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent event) {
				if (event.isPopupTrigger()) {
					if(Quote.STRATEGY_CUSTOM.equals(panelRFQ.comboStrategy.getSelectedItem())){
		            	pmiAddLeg.setEnabled(true);
		            	pmiDelLeg.setEnabled(tableLegs.getRowCount() > 1);
					}else{
		            	pmiAddLeg.setEnabled(false);
		            	pmiDelLeg.setEnabled(false);
					}
					popupLeg.show(event.getComponent(),
		            		event.getX(), event.getY());
		        }
			}
			public void mouseReleased(MouseEvent event) {
				if (event.isPopupTrigger()) {
					if(Quote.STRATEGY_CUSTOM.equals(panelRFQ.comboStrategy.getSelectedItem())){
		            	pmiAddLeg.setEnabled(true);
		            	pmiDelLeg.setEnabled(tableLegs.getRowCount() > 1);
					}else{
		            	pmiAddLeg.setEnabled(false);
		            	pmiDelLeg.setEnabled(false);
					}
					popupLeg.show(event.getComponent(),
		            		event.getX(), event.getY());
		        }
			}
		});

		miSave.setEnabled(false);
		
		Enumeration<TableColumn> cols = tableLegs.getColumnModel().getColumns();
		while(cols.hasMoreElements()){
			cols.nextElement().setCellRenderer(new DefaultStrategyLegFieldRenderer());
		}
		comboSL.setBorder(BorderFactory.createEmptyBorder());
		TableColumn col = tableLegs.getColumnModel().getColumn(1);
		col.setCellEditor(new DefaultCellEditor(comboSL));
		col.setPreferredWidth(20);
		
		col = tableLegs.getColumnModel().getColumn(2);
		col.setCellEditor(new DefaultCellEditor(comboCP));
		col.setPreferredWidth(40);
        
		col = tableLegs.getColumnModel().getColumn(3);
		col.setCellEditor(new DecimalFieldEditor());
        
		col = tableLegs.getColumnModel().getColumn(4);
		col.setCellEditor(new DecimalFieldEditor());


		col = tableLegs.getColumnModel().getColumn(5);
		col.setCellEditor(new DateFieldEditor());

		col = tableLegs.getColumnModel().getColumn(6);
		col.setCellEditor(new DecimalFieldEditor());

		col = tableLegs.getColumnModel().getColumn(7);
		col.setCellEditor(new DecimalFieldEditor());
		
		setPreferredSize(new Dimension(800,600));
		splitPrice.setDividerLocation(120);
		Container p = getContentPane();
		p.setLayout(new BorderLayout());
		p.add(panelMain, BorderLayout.CENTER);
		panelMain.add(panelBody, BorderLayout.CENTER);
		
		panelBody.setLayout(new BorderLayout());
		panelBody.add(panelRFQ,BorderLayout.NORTH);
		panelBody.add(splitPrice, BorderLayout.CENTER);
		
		splitPrice.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPrice.add(new JScrollPane(tableLegs), JSplitPane.TOP);
		splitPrice.add(tabPrice, JSplitPane.BOTTOM);
	}
	

	public void updated(UpdateEvent event) {
		if(UpdateEvent.TYPE_QUOTE == event.getObjectType()){
			long id = event.getObjectId();
		}
	}

	public void setRFQ(Quote rfq){
		panelRFQ.setRFQ(rfq);
		modelLegs.setRFQ(rfq);
		begraph.setRFQ(rfq);
	}
	
	private void doCalc(){
		Quote rfq = panelRFQ.getRFQ();
		for(StrategyLeg leg : rfq.getStrategies()){
			doCalc(leg, true);
		}
	}
	private void doCalc(StrategyLeg leg, boolean byVolatility){
		Quote rfq = panelRFQ.getRFQ();
		if(rfq.getSpotPrice() <= 0
				|| rfq.getRiskFreeRate() <= 0
				|| rfq.getTradeDate() == null
				){
			return;
		}
		if(leg.getExpiry() == null
				|| Double.isNaN(leg.getStrike())
				|| (byVolatility && Double.isNaN(leg.getVolatility()))
				|| (!byVolatility && Double.isNaN(leg.getPremium()))
				){
			return;
		}
		long days = (leg.getExpiry().getTime() - rfq.getTradeDate().getTime())/1000/60/60/24;
		PriceEngineBS pe;
		if(byVolatility){
			pe = new PriceEngineBS(
						StrategyLeg.CALL.equals(leg.getCallOrPut()),
						rfq.getSpotPrice(),
						leg.getStrike(),
						leg.getVolatility(),
						rfq.getRiskFreeRate(),
						days/365d
			);
		}else{
			pe = new PriceEngineBS(
					StrategyLeg.CALL.equals(leg.getCallOrPut()),
					rfq.getSpotPrice(),
					leg.getStrike(),
					leg.getPremium(),
					rfq.getRiskFreeRate(),
					days/365d,
					true
			);
		}
		pe.calc();
		leg.setVolatility(pe.getVolatility());
		leg.setPremium(pe.getPremium());
		leg.setDelta(pe.getDelta());
		leg.setGamma(pe.getGamma());
		leg.setVega(pe.getVega());
		leg.setTheta(pe.getTheta());
		leg.setRho(pe.getRho());
		leg.setVolga(pe.getVolga());
		leg.setVanna(pe.getVanna());
		leg.setCharm(pe.getCharm());
		leg.setColor(pe.getColor());
		leg.setDualDelta(pe.getDualDelta());
		leg.setDualGamma(pe.getDualGamma());
		
		//modelLegs.fireTableDataChanged();
	}
	private void doAbout(){
		JOptionPane.showMessageDialog(this, "0.1", "About",JOptionPane.PLAIN_MESSAGE);
	}
	public static void main(String[] args) {
		Quote rfq = new QuoteImpl(){};
		rfq.setRiskFreeRate(0.001d);
		rfq.setSpotPrice(100d);
		rfq.setStatus(Quote.STATUS_ANALYSIS);
		
		OptionCalculatorFrame frame = new OptionCalculatorFrame();
		frame.setRFQ(rfq);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		Dimension d = frame.getPreferredSize();
		d.setSize(800, 580);
		frame.setPreferredSize(d);
		frame.setSize(d);
		frame.setVisible(true);
	}
}

