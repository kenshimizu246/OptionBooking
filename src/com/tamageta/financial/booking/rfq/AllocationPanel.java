package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.tamageta.financial.booking.dao.Dao;
import com.tamageta.financial.booking.rfq.data.Allocation;
import com.tamageta.financial.booking.rfq.data.AllocationAccount;
import com.tamageta.financial.booking.rfq.data.StrategyLeg;


public class AllocationPanel extends JPanel {
	private final AllocModel modelAlloc;
	private final JTable tableAlloc;
	
	private class AllocModel  extends AbstractTableModel {
		private static final int IDX_LEGID = 0;
		private static final int IDX_TOTAL = 1;
		private static final int IDX_ACCT  = 2;
		private static final int IDX_PERC  = 3;
		private static final int IDX_AMT   = 4;
		
		private final String[] titles = new String[]{
			"Leg ID","Leg Amt","Account","Percentage","Amount"
		};
		private class Rec {
			StrategyLeg leg;
			Vector<Allocation> allocs = new Vector<Allocation>();
			
			double getLegAmount(){
				return leg.getPremium() * leg.getQuantity();
			}
		}
		private List<Rec> recs = new Vector<Rec>();
		
		public void setStrategies(List<StrategyLeg> legs){
			recs.clear();
			if(legs == null){
				return;
			}
			for(StrategyLeg leg : legs){
				Rec rec = new Rec();
				rec.leg = leg;
				recs.add(rec);
			}
		}
		public void setAllocations(List<Allocation> allocs){
			for(Rec r : recs){
				r.allocs.removeAllElements();
			}
			for(Allocation a : allocs){
				for(Rec r : recs){
					if(r.leg.getLegId() == a.getStrategyLeg().getLegId()){
						r.allocs.add(a);
					}
				}
			}
		}
		public List<Allocation> getAllocations(){
			List<Allocation> allocs = new ArrayList<Allocation>();
			for(Rec rec : recs){
				for(Allocation a : rec.allocs){
					allocs.add(a);
				}
			}
			return allocs;
		}
		public void addAllocation(AllocationAccount account){
			for(Rec rec : recs){
				Allocation alloc = new Allocation(-1, rec.leg, account, 0);
				rec.allocs.add(alloc);
				int idx = getRowIndex(alloc);
				fireTableRowsInserted(idx, idx);
			}
			for(Rec rec : recs){
				adjustAllocations(rec);
			}
		}
		public void removeAllocation(int row){
			int idx = -1;
			Allocation alloc = getAllocation(row);
			for(Rec rec : recs){
				for(Allocation a : rec.allocs){
					if(alloc == a){
						idx = rec.allocs.indexOf(a);
					}		 
				}
			}
			if(idx < 0){
				return;
			}
			for(Rec rec : recs){
				Allocation a = rec.allocs.get(idx);
				rec.allocs.remove(a);
				adjustAllocations(rec);
			}
			fireTableDataChanged();
		}
		public Allocation getAllocation(int idx){
			int cnt = 0;
			for(Rec rec : recs){
				for(Allocation a : rec.allocs){
					if(idx == cnt++){
						return a;
					}
				}
			}
			return null;
		}
		public Rec getRec(Allocation alloc){
			for(Rec r : recs){
				for(Allocation a: r.allocs){
					if(a == alloc){
						return r;
					}
				}
			}
			return null;
		}
		public String getColumnName(int idx) {
			return titles[idx];
		}
		public int getColumnCount() {
			return titles.length;
		}
		public int getRowCount() {
			int cnt = 0;
			for(Rec rec : recs){
				cnt+= rec.allocs.size();
			}
			return cnt;
		}
		public boolean isCellEditable(int row, int col) {
			switch(col){
			case IDX_AMT:
				int cnt = 0;
				for(Rec r : recs){
					for(int i = 0; i < r.allocs.size(); i++){
						if(row == cnt++ && i == (r.allocs.size()-1)){
							return false;
						}
					}
				}
				return true;
			case IDX_PERC:
				return (recs.size() > 0 
								&& row < (recs.get(0).allocs.size() - 1));
			case IDX_ACCT:
				return (recs.size() > 0 
								&& row < recs.get(0).allocs.size());
			}
			return false;
		}
		private void adjustAllocations(Rec r){
			double amt = r.getLegAmount();
			double pct = 1;
			for(int k = 0; k < r.allocs.size(); k++){
				Allocation alloc = r.allocs.get(k);
				if(k < (r.allocs.size() - 1)){
					amt -= alloc.getAmount();
					pct -= alloc.getPercentage();
				}else{
					alloc.setAmount(amt);
					alloc.setPercentage(pct);
					int rowIdx = getRowIndex(alloc);
					fireTableCellUpdated(rowIdx, IDX_AMT);
					fireTableCellUpdated(rowIdx, IDX_PERC);
				}
			}
		}
		private int getRowIndex(Allocation alloc){
			int cnt = 0;
			for(Rec rec : recs){
				for(Allocation a : rec.allocs){
					if(a == alloc){
						return cnt;
					}
					cnt++;
				}
			}
			return -1;
		}
		public void setValueAt(Object value, int row, int col) {
			switch(col){
			case IDX_AMT:
			{
				Allocation alloc = getAllocation(row);
				Rec rec = getRec(alloc);
				alloc.setAmount((Double)value);
				alloc.setPercentage(alloc.getAmount()/rec.getLegAmount()*100);
				int rowIdx = getRowIndex(alloc);
				fireTableCellUpdated(rowIdx, IDX_AMT);
				fireTableCellUpdated(rowIdx, IDX_PERC);
				adjustAllocations(rec);
				break;
			}
			case IDX_PERC:
			{
				for(Rec rec : recs){
					Allocation alloc = rec.allocs.get(row);
					alloc.setPercentage(((Double)value) / 100);
					alloc.setAmount(rec.getLegAmount() * alloc.getPercentage());
					int rowIdx = getRowIndex(alloc);
					fireTableCellUpdated(rowIdx, IDX_AMT);
					fireTableCellUpdated(rowIdx, IDX_PERC);
					adjustAllocations(rec);
				}
				break;
			}
			case IDX_ACCT:
				for(Rec rec : recs){
					Allocation alloc = rec.allocs.get(row);
					alloc.setAccount((AllocationAccount)value);
					fireTableCellUpdated(getRowIndex(alloc), IDX_ACCT);
				}
				break;
			}
		}
		public Object getValueAt(int row, int col) {
			int cnt = 0;
			for(int l = 0; l < recs.size(); l++){
				Rec r = recs.get(l);
				for(int i = 0; i < r.allocs.size(); i++){
					if(row != cnt++){
						continue;
					}
					Allocation alloc = r.allocs.get(i);
					switch(col){
					case IDX_LEGID:
						return (i == 0 ? r.leg.getLegId() : null);
					case IDX_TOTAL:
						return (i == 0 ? r.getLegAmount() : null);
					case IDX_ACCT:
						return alloc.getAccount();
					case IDX_PERC:
						return alloc.getPercentage() * 100;
					case IDX_AMT:
						return alloc.getAmount();
					}
				}
			}
			return null;
		}
		public Class getColumnClass(int col) {
			switch(col){
			case IDX_ACCT:
				return AllocationAccount.class;
			case IDX_TOTAL:
			case IDX_PERC:
			case IDX_AMT:
				return Double.class;
			}
			return String.class;
		}
	}
	
	JComboBox comboAccounts = new JComboBox();
	public AllocationPanel(){
		modelAlloc = new AllocModel();
		tableAlloc = new JTable(modelAlloc);
		tableAlloc.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		comboAccounts.setBorder(BorderFactory.createEmptyBorder());
		TableColumn col = tableAlloc.getColumnModel().getColumn(AllocModel.IDX_ACCT);
		col.setCellEditor(new DefaultCellEditor(comboAccounts));
		col.setPreferredWidth(240);
		col = tableAlloc.getColumnModel().getColumn(AllocModel.IDX_LEGID);
		col.setPreferredWidth(80);
		col = tableAlloc.getColumnModel().getColumn(AllocModel.IDX_TOTAL);
		col.setPreferredWidth(120);
		col = tableAlloc.getColumnModel().getColumn(AllocModel.IDX_AMT);
		col.setPreferredWidth(120);
		col = tableAlloc.getColumnModel().getColumn(AllocModel.IDX_PERC);
		col.setPreferredWidth(120);
		setLayout(new BorderLayout());
		JScrollPane scrollAlloc = new JScrollPane(tableAlloc);
		add(scrollAlloc, BorderLayout.CENTER);
		
		final JPopupMenu popup = new JPopupMenu();
		JMenuItem pmiAdd = new JMenuItem("Add");
		JMenuItem pmiDel = new JMenuItem("Delete");
		
		popup.add(pmiAdd);
		popup.add(pmiDel);
		
		pmiAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modelAlloc.addAllocation(null);
			}
		});
		pmiDel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				modelAlloc.removeAllocation(tableAlloc.getSelectedRow());
			}
		});
		
		MouseAdapter ma = new MouseAdapter() {
			public void mousePressed(MouseEvent event){
				if(event.isPopupTrigger()){
					popup.show(event.getComponent(),
							event.getX(), event.getY());
				}
			}
		};
		tableAlloc.addMouseListener(ma);
		scrollAlloc.addMouseListener(ma);
	}
	public List<Allocation> getAllocations(){
		return modelAlloc.getAllocations();
	}
	public void setStrategyLegs(List<StrategyLeg> legs){
		modelAlloc.setStrategies(legs);
	}
	public void setAccounts(List<AllocationAccount> list){
		comboAccounts.removeAllItems();
		if(list == null){
			return;
		}
		for(AllocationAccount acct : list){
			comboAccounts.addItem(acct);
		}
	}
	public void setAllocations(final List<Allocation> allocs){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				modelAlloc.setAllocations(allocs);
				modelAlloc.fireTableDataChanged();
			}
		});
	}
	
	public static void main(String[] args) {
		ArrayList<AllocationAccount> accounts = new ArrayList<AllocationAccount>();
		accounts.add(new AllocationAccount("AC1", "Account One"));	
		accounts.add(new AllocationAccount("AC2", "Account Two"));
		accounts.add(new AllocationAccount("AC3", "Account Three"));	
		accounts.add(new AllocationAccount("AC4", "Account Four"));
		accounts.add(new AllocationAccount("AC5", "Account Five"));	

		List<StrategyLeg> legs = new ArrayList<StrategyLeg>();
		for(int i = 1; i < 4; i++){
			StrategyLeg leg = new StrategyLeg(null);
			leg.setLegId(i);
			leg.setLs((i % 2) == 0 ? StrategyLeg.LONG : StrategyLeg.SHORT);
			leg.setPremium(100 * ((i % 2)+1));
			leg.setQuantity(10 * ((i % 2)+1));
			legs.add(leg);
		}
		
		JFrame f = new JFrame("Allocation Panel Test");
		AllocationPanel p = new AllocationPanel();
		p.modelAlloc.setStrategies(legs);
//		p.modelAlloc.addAllocation(accounts[0]);
//		p.modelAlloc.addAllocation(accounts[1]);
		p.setAccounts(accounts);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(p, BorderLayout.CENTER);
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
