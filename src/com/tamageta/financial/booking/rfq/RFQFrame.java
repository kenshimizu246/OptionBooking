package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.tamageta.financial.booking.gui.DateFieldEditor;
import com.tamageta.financial.booking.gui.DecimalField;
import com.tamageta.financial.booking.gui.DecimalFieldEditor;
import com.tamageta.financial.booking.rfq.data.Quote;
import com.tamageta.financial.booking.rfq.data.QuoteImpl;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;
import com.tamageta.financial.math.PriceEngineBS;

public class RFQFrame extends JFrame {
	RFQPanel rfqPanel = new RFQPanel();
	JScrollPane scrollLegs = new JScrollPane();
	StrategyTableModel strategyModel = new StrategyTableModel();
	final JTable tableLegs;
	JTable tablePrices;
	JTabbedPane tabMain = new JTabbedPane();
	JSplitPane splitMain = new JSplitPane();
	JPanel panelCntrl = new JPanel();
	JButton btnCalc = new JButton("Calc");
	
	JPanel panelSummary = new JPanel();
	
	JPopupMenu popupLeg = new JPopupMenu();
	JMenuItem miAddLeg = new JMenuItem("Add Leg");
	JMenuItem miDelLeg = new JMenuItem("Delete Leg");

    JComboBox comboSL = new JComboBox();
    JComboBox comboCP = new JComboBox();
    
	public RFQFrame(){
        comboSL.addItem("S");
        comboSL.addItem("L");
        comboCP.addItem(StrategyLeg.CALL);
        comboCP.addItem(StrategyLeg.PUT);
//        tableLegs = new JTable(strategyModel);
//            tableLegs = new JTable(strategyModel){
//			public TableCellEditor getCellEditor(int r, int c) {
//				TableColumnModel colModel = getColumnModel();
//				if(c < colModel.getColumnCount()){
//					return colModel.getColumn(c).getCellEditor();
//				}
//				return super.getCellEditor(r,c);
//			}
//        };
        //tableLegs.setDefaultEditor(Number.class, new DecimalFieldEditor());
//		tableLegs.setPreferredScrollableViewportSize(new Dimension(500, 70));
//		tableLegs.setFillsViewportHeight(true);
//		tableLegs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		TableColumnModel colModel = tableLegs.getColumnModel();
//		comboSL.setBorder(BorderFactory.createEmptyBorder());
//		TableColumn col = tableLegs.getColumnModel().getColumn(1);
//		col.setCellEditor(new DefaultCellEditor(comboSL));
//        DefaultTableCellRenderer renderer =
//                new DefaultTableCellRenderer();
//        renderer.setToolTipText("Click for combo box");
//        col.setCellRenderer(renderer);
        
//		colModel.getColumn(2).setCellEditor(new DefaultCellEditor(comboCP));
//		colModel.getColumn(3).setCellEditor(new DecimalFieldEditor());
//		colModel.getColumn(4).setCellEditor(new DecimalFieldEditor());
//		colModel.getColumn(5).setCellEditor(new DateFieldEditor());
//		colModel.getColumn(6).setCellEditor(new DecimalFieldEditor());
//		colModel.getColumn(7).setCellEditor(new DecimalFieldEditor());
//		tableLegs.setColumnModel(colModel);
		
        tableLegs = new JTable(){
			public TableCellEditor getCellEditor(int r, int c) {
				switch(c){
				case 1:
			        return new DefaultCellEditor(comboSL);
				case 2:
			        return new DefaultCellEditor(comboCP);
				case 3:
					return new DecimalFieldEditor();
			        //return new DefaultCellEditor(decimalField);
				case 4:
					return new DecimalFieldEditor();
			        //return new DefaultCellEditor(decimalField);
				case 5:
			        //return new DefaultCellEditor(new DateField( new SimpleDateFormat("yyyy-MM-dd")));
					return new DateFieldEditor();
				case 6:
				case 7:
				case 8:
				case 9:
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
				case 16:
				case 17:
				case 18:
					return new DecimalFieldEditor();
				default:
					return super.getCellEditor(c, r);
				}
			}
		};
		tableLegs.setPreferredScrollableViewportSize(new Dimension(500, 70));
		tableLegs.setFillsViewportHeight(true);
		tableLegs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableLegs.setModel(strategyModel);
		tableLegs.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent event) {
			}
			public void mouseEntered(MouseEvent event) {
			}
			public void mouseExited(MouseEvent event) {
			}
			public void mousePressed(MouseEvent event) {
				if (event.isPopupTrigger() && Quote.STRATEGY_CUSTOM.equals(rfqPanel.comboStrategy.getSelectedItem())) {
	            	miDelLeg.setEnabled(tableLegs.getRowCount() > 1);
		            popupLeg.show(event.getComponent(),
		            		event.getX(), event.getY());
		        }
			}
			public void mouseReleased(MouseEvent event) {
				if (event.isPopupTrigger() && Quote.STRATEGY_CUSTOM.equals(rfqPanel.comboStrategy.getSelectedItem())) {
	            	miDelLeg.setEnabled(tableLegs.getRowCount() > 1);
					popupLeg.show(event.getComponent(),
		            		event.getX(), event.getY());
		        }
			}
		});
		popupLeg.add(miAddLeg);
		popupLeg.add(miDelLeg);
		miAddLeg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				StrategyLeg leg = rfqPanel.getRFQ().addStrategyLeg();
				strategyModel.fireTableDataChanged();
			}
		});
		miDelLeg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int i = tableLegs.getSelectedRow();
				rfqPanel.getRFQ().getStrategies().remove(i);
				strategyModel.fireTableDataChanged();
			}
		});
        
		getContentPane().setLayout(new BorderLayout());
		splitMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitMain.add(scrollLegs, JSplitPane.TOP);
		splitMain.add(tabMain, JSplitPane.BOTTOM);
		getContentPane().add(rfqPanel, BorderLayout.NORTH);
		getContentPane().add(splitMain, BorderLayout.CENTER);
		getContentPane().add(panelCntrl, BorderLayout.SOUTH);
		scrollLegs.getViewport().add(tableLegs);
		panelCntrl.add(btnCalc);
		tabMain.addTab("Summary", panelSummary);
		btnCalc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						doCalc();
					}
				});
			}
		});
		rfqPanel.comboStrategy.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				rfqPanel.getRFQ().setStrategy((String)e.getItem());
				strategyModel.setStrategy((String)e.getItem());
			}
		});
	}
	
	public void setRFQ(Quote rfq){
		rfqPanel.setRFQ(rfq);
		for(StrategyLeg leg : rfq.getStrategies()){
			tabMain.addTab("Leg-"+leg.getId(), new PricePanel(rfq, leg));
		}
		strategyModel.setRFQ(rfq);
		strategyModel.fireTableStructureChanged();
		strategyModel.fireTableDataChanged();
	}
	
	public void doCalc(){
		Quote rfq = rfqPanel.getRFQ();
		for(StrategyLeg l : rfq.getStrategies()){
			long days = (l.getExpiry().getTime() - rfq.getTradeDate().getTime())/1000/60/60/24;
			PriceEngineBS pe = new PriceEngineBS(
						StrategyLeg.CALL.equals(l.getCallOrPut()),
						rfq.getSpotPrice(),
						l.getStrike(),
						l.getVolatility(),
						rfq.getRiskFreeRate(),
						days/365d
			);
			pe.calc();
			l.setPremium(pe.getPremium());
			l.setDelta(pe.getDelta());
			l.setGamma(pe.getGamma());
			l.setVega(pe.getVega());
			l.setTheta(pe.getTheta());
			l.setRho(pe.getRho());
		}
		strategyModel.fireTableDataChanged();
	}
	
	public static void main(String[] args) {
		Quote rfq = new QuoteImpl(){};
		rfq.setRiskFreeRate(0.001d);
		rfq.setSpotPrice(14500d);
		
		StrategyLeg leg = rfq.addStrategyLeg();
		leg.setQuantity(100);
		leg.setStrike(14000);
		
		RFQFrame frame = new RFQFrame();
		frame.setRFQ(rfq);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		Dimension d = frame.getPreferredSize();
		d.setSize(800, 480);
		frame.setPreferredSize(d);
		frame.setSize(d);
		frame.setVisible(true);
	}
}
