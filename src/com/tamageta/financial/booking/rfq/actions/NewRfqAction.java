package com.tamageta.financial.booking.rfq.actions;

import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class NewRfqAction extends AbstractAction {
	private final ExecutorService commandExecutor;
	
	public NewRfqAction(ExecutorService commandExecutor){
	  this.putValue(Action.NAME, "New");
	  this.commandExecutor = commandExecutor;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
