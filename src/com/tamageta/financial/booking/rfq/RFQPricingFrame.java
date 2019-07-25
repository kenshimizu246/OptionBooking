package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import com.tamageta.financial.booking.dao.CacheDao;
import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.dao.DaoException;
import com.tamageta.financial.booking.dao.DemoDao;
import com.tamageta.financial.booking.dao.SoapDao;
import com.tamageta.financial.booking.dao.UpdateEvent;
import com.tamageta.financial.booking.dao.UpdateListener;
import com.tamageta.financial.booking.gui.DateField;
import com.tamageta.financial.booking.gui.DateFieldEditor;
import com.tamageta.financial.booking.gui.DecimalFieldEditor;
import com.tamageta.financial.booking.gui.DefaultStrategyLegFieldRenderer;
import com.tamageta.financial.booking.gui.LoginDialog;
import com.tamageta.financial.booking.gui.TradeTableModel;
import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.math.PriceEngineBS;
import com.tamageta.financial.util.GuiUtility;

public class RFQPricingFrame extends JFrame implements UpdateListener{
	private Dao dao = null;
	private String userID = null;
	private String role   = "admin";
	private RFQPanel panelRFQ = new RFQPanel();
	private StrategyTableModel modelLegs = new StrategyTableModel();
	private JTable tableLegs = null;
	private AccoountDialog acctDialog = new AccoountDialog(this);
	
	private TradeTableModel tradeTableModel = new TradeTableModel();
	private JTable tableTradeIdList = new JTable(tradeTableModel);
	
	private JTabbedPane tabPrice = new JTabbedPane();
	
	private DividendTableModel modelDiv = new DividendTableModel();
	private JTable tableDiv = new JTable(modelDiv);
	private SummaryPanel begraph = new SummaryPanel();
	private AllocationPanel panelAlloc = new AllocationPanel();
	//private AccountPanel panelAcct = new AccountPanel();
	
	private JPanel panelMain = new JPanel(new BorderLayout());
	private JPanel panelBody = new JPanel();
	private JSplitPane splitMain  = new JSplitPane();
	private JSplitPane splitPrice = new JSplitPane();

    private JComboBox comboSL = new JComboBox();
    private JComboBox comboCP = new JComboBox();

    JMenuBar mbarMain = new JMenuBar();
    JMenu menuFile = new JMenu("File");
    JMenuItem miSave = new JMenuItem("Save");
    JMenuItem miQuit = new JMenuItem("Quit");
    JMenu menuOption = new JMenu("Option");
    JMenuItem miAccount = new JMenuItem("Account...");
    JMenu menuAction = new JMenu("Action");
    JMenuItem miTakeOwnership = new JMenuItem("TakeOwnership");
    JMenu menuHelp = new JMenu("Help");
    JMenuItem miAbout = new JMenuItem("About...");
    
	JPopupMenu popupLeg = new JPopupMenu();
	JMenuItem pmiAddLeg = new JMenuItem("Add Leg");
	JMenuItem pmiDelLeg = new JMenuItem("Delete Leg");
	JMenuItem pmiCalc = new JMenuItem("Calculation");
	
	JPanel panelButtons = new JPanel();
	JButton buttonNew    = new JButton("New");
	JButton buttonSubmit = new JButton("Submit");
	JButton buttonQuote  = new JButton("Quote");
	JButton buttonAffirm = new JButton("Affirm");
	JButton buttonReject = new JButton("Reject");
	JButton buttonHedge  = new JButton("Hedge");
	JButton buttonDone   = new JButton("Done");
	JButton buttonCancel = new JButton("Cancel");
	JButton buttonAmend  = new JButton("Amend");
	JButton buttonSaveAlloc = new JButton("Save Allocation");
	
	StatusPanel panelStatus = new StatusPanel();
	
	JPopupMenu popupTrade = new JPopupMenu();
	JMenuItem pmiTakeOwner    = new JMenuItem("Take Ownership");
	JMenuItem pmiReleaseOwner = new JMenuItem("Release Ownership");
	JMenuItem pmiClose = new JMenuItem("Close");
	JMenuItem pmiCopy  = new JMenuItem("Copy");
	
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
				R result = ProgressDialog.execute(RFQPricingFrame.this, title, message, this);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		public abstract R call();
	}
	
	public RFQPricingFrame(){
		setTitle("Option Booking");
		tableLegs = new JTable(modelLegs);
		
		panelRFQ.comboStrategy.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(panelRFQ.getRFQ() != null){
					panelRFQ.getRFQ().setStrategyLegs((String)e.getItem());
					modelLegs.setStrategy((String)e.getItem());
					modelLegs.fireTableDataChanged();
				}
			}
		});
		
		//tableLegs.setModel();

        comboSL.addItem("S");
        comboSL.addItem("L");
        comboCP.addItem(StrategyLeg.CALL);
        comboCP.addItem(StrategyLeg.PUT);
        
        setJMenuBar(mbarMain);
        mbarMain.add(menuFile);
        menuFile.add(miSave);
        menuFile.addSeparator();
        menuFile.add(miQuit);
        mbarMain.add(menuOption);
        menuOption.add(miAccount);
        mbarMain.add(menuAction);
        menuAction.add(miTakeOwnership);
        mbarMain.add(menuHelp);
        menuHelp.add(miAbout);
        buttonNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("New","Creating new Quote form..."){
					public Object call(){
						doNew();
						return null;
					}
				});
			}
		});
        buttonSubmit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("Submit","Submitting your response..."){
					public Object call(){
						doSend();
						return null;
					}
				});
			}
		});
        buttonAffirm.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("Affirmaiton","Affirming a trade..."){
					public Object call(){
						doAffirm();
						return null;
					}
				});
			}
		});
        buttonReject.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("Reject","Rejecting this trade..."){
					public Object call(){
						doReject();
						return null;
					}
				});
			}
		});
        buttonHedge.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("Hedge","Changing status to Hedge..."){
					public Object call(){
						doHedge();
						return null;
					}
				});
			}
		});
        buttonDone.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						doDone();
						return null;
					}
				});
			}
		});
        buttonAmend.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						doAmend();
						return null;
					}
				});
			}
		});
        buttonCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						doCancel();
						return null;
					}
				});
			}
		});
        buttonSaveAlloc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						saveAllocations();
						return null;
					}
				});
			}
        });
        miSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				commandExecutor.execute(new Command("",""){
					public Object call(){
						doSave();
						return null;
					}
				});
			}
		});
        miAccount.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doAccount();
			}
        });
        miTakeOwnership.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				doTakeOwnership();
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
		tabPrice.addTab("Dividend", new JScrollPane(tableDiv));
		tabPrice.addTab("Allocation", panelAlloc);
		tabPrice.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				if("Allocation".equals(tabPrice.getTitleAt(tabPrice.getSelectedIndex()))){
					buttonSaveAlloc.setVisible(true);
				}else{
					buttonSaveAlloc.setVisible(false);
				}
			}
		});
		
		modelLegs.addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent event){
				if(event.getColumn() > -1 && event.getFirstRow() > -1){
					//doCalc(modelLegs.getStrategyLeg(event.getFirstRow()), (event.getColumn() == 6));
				}
			}
			
		});
		tableLegs.setPreferredScrollableViewportSize(new Dimension(500, 70));
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
		tableTradeIdList.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				try {
					Quote quote = tradeTableModel.getRow(row);
					if(Quote.STATUS_IOI.equals(quote.getStatus())){
						setBackground(Color.BLUE);
					}else if(Quote.STATUS_NEW.equals(quote.getStatus())){
						setBackground(Color.YELLOW);
					}else if(Quote.STATUS_QUOTE.equals(quote.getStatus())){
						setBackground(Color.GREEN);
					}else if(Quote.STATUS_CANCEL.equals(quote.getStatus())){
						setBackground(Color.RED);
					}else{
						setBackground(Color.WHITE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
			
		});
				
		popupTrade.add(pmiTakeOwner);
		pmiTakeOwner.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doTakeOwnership();
			}
		});
		popupTrade.add(pmiReleaseOwner);
		pmiReleaseOwner.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doRleaseOwnership();
			}
		});
		popupTrade.add(pmiClose);
		pmiClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doCreateCloseQuote();
			}
		});
		popupTrade.add(pmiCopy);
		pmiCopy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				doCopyQuote();
			}
		});
		
		tableTradeIdList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableTradeIdList.getSelectionModel().addListSelectionListener(
		        new ListSelectionListener() {
		            public void valueChanged(ListSelectionEvent event) {
		                int viewRow = tableTradeIdList.getSelectedRow();
		                if (viewRow < 0) {
		                    panelRFQ.setRFQ(null);
		                    modelLegs.setRFQ(null);
		                    begraph.setRFQ(null);
		                    panelAlloc.setStrategyLegs(null);
		                    setButtons(null);
		                    //System.out.println("selected["+viewRow+"]");
		                } else {
		                    int modelRow = 
		                    	tableTradeIdList.convertRowIndexToModel(viewRow);
		                    //System.out.println("selected["+viewRow+"]["+modelRow+"]");
		                    try{
		                    	Quote q = tradeTableModel.getRow(modelRow);
		                    	panelRFQ.setRFQ(q);
		                		modelLegs.setRFQ(q);
			                    begraph.setRFQ(q);
			                    panelAlloc.setStrategyLegs(q.getStrategies());
			                    panelAlloc.setAllocations(dao.getAllocations(q));
		                		setButtons(q);
		                    }catch(Exception e){
		                    	e.printStackTrace();
		                    }
		                }
		            }
		        }
		);
		tableTradeIdList.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent event) {
				if (event.isPopupTrigger()) {
					pmiTakeOwner.setVisible(isTakeOwnership());
					pmiReleaseOwner.setVisible(isReleaseOwnership());
					pmiClose.setVisible(isDoneQuote() && !isCloseQuote());
					pmiCopy.setVisible(!isCloseQuote());
					popupTrade.show(event.getComponent(),
		            		event.getX(), event.getY());
				}
			}
		});
		setButtons(null);
		buttonNew.setEnabled(false);
		buttonNew.setVisible(false);
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
		splitMain.setDividerLocation(120);
		splitPrice.setDividerLocation(80);
		Container p = getContentPane();
		p.setLayout(new BorderLayout());
		p.add(panelMain, BorderLayout.CENTER);
		p.add(panelButtons, BorderLayout.SOUTH);
		panelMain.add(splitMain, BorderLayout.CENTER);
		panelMain.add(panelStatus, BorderLayout.SOUTH);

		splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitMain.add(new JScrollPane(tableTradeIdList), JSplitPane.LEFT);
		splitMain.add(panelBody, JSplitPane.RIGHT);
		
		panelBody.setLayout(new BorderLayout());
		panelBody.add(panelRFQ,BorderLayout.NORTH);
		panelBody.add(splitPrice, BorderLayout.CENTER);
		
		splitPrice.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPrice.add(new JScrollPane(tableLegs), JSplitPane.TOP);
		splitPrice.add(tabPrice, JSplitPane.BOTTOM);

		panelButtons.add(buttonNew);
		panelButtons.add(buttonSubmit);
		panelButtons.add(buttonAffirm);
		panelButtons.add(buttonReject);
		panelButtons.add(buttonHedge);
		panelButtons.add(buttonDone);
		panelButtons.add(buttonCancel);
		panelButtons.add(buttonAmend);
		panelButtons.add(buttonSaveAlloc);
	}
	
	public void setDao(Dao dao)throws Exception{
		if(dao != null && dao.isLogin()){
			this.dao = dao;
			dao.addUpdateListener(this);
			tradeTableModel.setDao(dao);
			panelRFQ.setDao(dao);
			//panelAcct.setDao(dao);
			//panelAlloc.setDao(dao);
			panelAlloc.setAccounts(dao.getAccounts());
	
			userID = dao.getUserId();
	
			setButtons(null);
			buttonNew.setEnabled(true);
			buttonNew.setVisible(true);
			miSave.setEnabled(true);
			acctDialog.setDao(dao);
			miAccount.setEnabled(true);
		}else{
			setButtons(null);
			buttonNew.setEnabled(false);
			buttonNew.setVisible(false);
			miSave.setEnabled(false);
			acctDialog.setDao(null);
			miAccount.setEnabled(false);
			panelAlloc.setAccounts(null);
		}
	}
	
	private void setButtons(Quote quote){
		if(quote == null 
				|| Quote.STATUS_CANCEL.equals(quote.getStatus())
				|| Quote.STATUS_REJECT.equals(quote.getStatus())){
			buttonSubmit.setEnabled(false);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(false);
			buttonDone.setEnabled(false);
			buttonHedge.setEnabled(false);
			buttonCancel.setEnabled(false);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(false);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(false);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(false);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_NEW.equals(quote.getStatus())){
			// Sales -> Submit
			buttonSubmit.setEnabled(true);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(false);
			buttonHedge.setEnabled(false);
			buttonDone.setEnabled(false);
			buttonCancel.setEnabled(true);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(true);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(false);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(true);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_IOI.equals(quote.getStatus())){
			//Trader -> Quote
			boolean t = (quote.hasTrader() && userID.equals(quote.getTrader()));
			buttonSubmit.setEnabled(t);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(t);
			buttonHedge.setEnabled(false);
			buttonDone.setEnabled(false);
			buttonCancel.setEnabled(false);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(t);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(t);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(false);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_QUOTE.equals(quote.getStatus())){
			//Sales -> Affirm/Reject
			buttonSubmit.setEnabled(false);
			buttonAffirm.setEnabled(true);
			buttonReject.setEnabled(true);
			buttonHedge.setEnabled(false);
			buttonDone.setEnabled(false);
			buttonCancel.setEnabled(false);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(false);
			buttonAffirm.setVisible(true);
			buttonReject.setVisible(true);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(false);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_AFFIRM.equals(quote.getStatus())){
			//Trader -> Hedge
			buttonSubmit.setEnabled(false);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(false);
			buttonHedge.setEnabled(true);
			buttonDone.setEnabled(false);
			buttonCancel.setEnabled(false);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(false);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(false);
			buttonHedge.setVisible(true);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(false);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_HEDGE.equals(quote.getStatus())){
			// Sales -> Done(Allocation)
			buttonSubmit.setEnabled(false);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(false);
			buttonHedge.setEnabled(false);
			buttonDone.setEnabled(true);
			buttonCancel.setEnabled(false);
			buttonAmend.setEnabled(false);
			buttonSaveAlloc.setEnabled(false);

			buttonSubmit.setVisible(false);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(false);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(true);
			buttonCancel.setVisible(false);
			buttonAmend.setVisible(false);
			buttonSaveAlloc.setVisible(false);
		}else if(Quote.STATUS_DONE.equals(quote.getStatus())){
			// Sales -> Amend/Cancel
			buttonSubmit.setEnabled(false);
			buttonAffirm.setEnabled(false);
			buttonReject.setEnabled(false);
			buttonHedge.setEnabled(false);
			buttonDone.setEnabled(false);
			buttonCancel.setEnabled(true);
			buttonAmend.setEnabled(true);
			buttonSaveAlloc.setEnabled(true);

			buttonSubmit.setVisible(false);
			buttonAffirm.setVisible(false);
			buttonReject.setVisible(false);
			buttonHedge.setVisible(false);
			buttonDone.setVisible(false);
			buttonCancel.setVisible(true);
			buttonAmend.setVisible(true);
			if("Allocation".equals(tabPrice.getTitleAt(tabPrice.getSelectedIndex()))){
				buttonSaveAlloc.setVisible(true);
			}else{
				buttonSaveAlloc.setVisible(false);
			}
		}
	}

	private void updateTradeTable(Quote quote){
		try{
			tradeTableModel.updateList();
			int idx = tradeTableModel.getIndexByTradeId(quote.getQuoteId());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void doRleaseOwnership(){
		try {
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			if(userID.equals(quote.getTrader())){
				dao.releaseOwnership(quote);
				tradeTableModel.updateList();
			}else{
				JOptionPane.showMessageDialog(this, "You are not owner for this Trade!", "Release Ownership Error", JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void doTakeOwnership(){
		try {
			final Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			if(!quote.hasTrader()){
				quote.setTrader(userID);
				dao.takeOwnership(quote);
				tradeTableModel.updateList();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private boolean isTakeOwnership(){
		try {
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			return quote != null && !quote.hasTrader() && Quote.STATUS_IOI.equals(quote.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Take Ownership Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	private boolean isReleaseOwnership(){
		try {
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			return quote != null && quote.hasTrader() && userID.equals(quote.getTrader()) && Quote.STATUS_IOI.equals(quote.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Release Ownership Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	private boolean isDoneQuote(){
		try {
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			return quote != null && Quote.STATUS_DONE.equals(quote.getStatus());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	private boolean isCloseQuote(){
		try {
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			return quote != null && Quote.CLOSE.equals(quote.getOpenClose());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	public void updated(UpdateEvent event) {
		if(UpdateEvent.TYPE_QUOTE == event.getObjectType()){
			long id = event.getObjectId();
		}
	}

	public void setRFQ(Quote rfq){
		panelRFQ.setRFQ(rfq);
		modelLegs.setRFQ(rfq);
		setButtons(rfq);
	}
	
	public boolean isLogin(){
		return (userID != null);
	}
	
	private void doNew(){
		final Quote q = dao.createNewQuote();
		q.setUserId(userID);
		
		try {
			q.setRiskFreeRate(dao.getDefaultRiskFreeRate());
		} catch (DaoException e) {
			e.printStackTrace();
		}
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				panelRFQ.setRFQ(q);
				modelLegs.setRFQ(q);
                begraph.setRFQ(q);
				panelAlloc.setStrategyLegs(null);
				setButtons(q);
			}
		});
	}
	private void doSend(){
		try{
			Quote quote   = panelRFQ.getRFQ();
			String status = quote.getStatus();
			if(Quote.STATUS_NEW.equals(status)){
				if(quote.hasOriginalQuoteId() && Quote.CLOSE.equals(quote.getOpenClose())){
					Quote original = tradeTableModel.getQuote(quote.getOriginalQuoteId());
				}
				dao.sendQuote(quote, Quote.STATUS_IOI);
			}else if(Quote.STATUS_IOI.equals(status)){
				dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_QUOTE);
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
			tradeTableModel.updateList();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doAffirm(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_QUOTE.equals(status)){
				//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_AFFIRM);
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_AFFIRM);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doReject(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_IOI.equals(status)){
				//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_REJECT);
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_REJECT);
				tradeTableModel.updateList();
			}else if(Quote.STATUS_QUOTE.equals(status)){
				//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_REJECT);
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_REJECT);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doHedge(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_AFFIRM.equals(status)){
				//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_DONE);
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_HEDGE);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doDone(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_HEDGE.equals(status)){
				//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_DONE);
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_DONE);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doCancel(){
		Quote qq = panelRFQ.getRFQ();
		if(qq == null || Quote.STATUS_NEW.equals(qq.getStatus())){
			return;
		}
		try{
			//dao.sendQuote(panelRFQ.getRFQ(), Quote.STATUS_CANCEL);
			dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_CANCEL);
			tradeTableModel.updateList();
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doAmend(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_DONE.equals(status)){
				dao.changeStatus(panelRFQ.getRFQ(), Quote.STATUS_AMEND);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doSave(){
		try{
			String status = panelRFQ.getRFQ().getStatus();
			if(Quote.STATUS_NEW.equals(status)
					|| Quote.STATUS_IOI.equals(status)){
				dao.sendQuote(panelRFQ.getRFQ(), status);
				tradeTableModel.updateList();
			}else {
				throw new Exception("Unsupported Status:"+status);
			}
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	private void doCreateCloseQuote(){
		try{
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			final Quote closeQuote = dao.createCloseQuote(quote);
			closeQuote.setUserId(userID);
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					panelRFQ.setRFQ(closeQuote);
					modelLegs.setRFQ(closeQuote);
	                begraph.setRFQ(closeQuote);
					panelAlloc.setStrategyLegs(closeQuote.getStrategies());
					setButtons(closeQuote);
				}
			});
			
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
	}
	public void doCopyQuote(){
		try{
			Quote quote = tradeTableModel.getRow(tableTradeIdList.getSelectedRow());
			final Quote copyQuote = dao.copyQuote(quote);
			copyQuote.setUserId(userID);
			
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					panelRFQ.setRFQ(copyQuote);
					modelLegs.setRFQ(copyQuote);
	                begraph.setRFQ(copyQuote);
					panelAlloc.setStrategyLegs(copyQuote.getStrategies());
					setButtons(copyQuote);
				}
			});
			
		}catch(Exception e){
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, 
					e.getMessage(), "Status Change Error!", 
					JOptionPane.ERROR_MESSAGE);
		}
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
	private void doAccount(){
		acctDialog.pack();
		acctDialog.setVisible(true);
	}
	private void doAbout(){
		//JOptionPane.showMessageDialog(this, "0.1", "About",JOptionPane.PLAIN_MESSAGE);
		doPL();
	}
	private void saveAllocations(){
		List<Allocation> allocs = panelAlloc.getAllocations();
		if(dao != null){
			try{
				dao.saveAllocations(allocs);
			}catch(Exception e){
				JOptionPane.showMessageDialog(this,
						e.getMessage(),
						e.getClass().getSimpleName(),
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	private void doPL(){
		PLModel model = new PLModel(dao);
		JFrame frame = new JFrame("P/L");
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new JScrollPane(new JTable(model)));
		frame.pack();
		frame.setVisible(true);
	}
}
