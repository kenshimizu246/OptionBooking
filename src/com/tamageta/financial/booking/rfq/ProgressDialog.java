package com.tamageta.financial.booking.rfq;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author yuanying
 * 
 * @param <R>
 */
public class ProgressDialog<R> extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4556558246742108136L;

	private static final Log log = LogFactory.getLog(ProgressDialog.class);

	//private static final String CANCEL_TEXT = UIManager
	//		.getString("OptionPane.cancelButtonText");
	private static final String CANCEL_TEXT = "Cancel";

	final private FutureTask<R> result;

	final private String message;

	private boolean canceled;

	@SuppressWarnings("unchecked")
	public ProgressDialog(Frame owner, String title, String message,
			Callable<R> call) throws HeadlessException {
		super(owner, title, true);

		this.initialize(owner);

		InvocationHandler handler = new CallableHandler(call);
		Callable<R> p = (Callable<R>) Proxy.newProxyInstance(Callable.class
				.getClassLoader(), new Class[] { Callable.class }, handler);
		this.result = new FutureTask(p);
		this.message = message;
		new Thread(this.result).start();
		this.setVisible(true);
	}

	private void initialize(Frame owner) {
		Container contentPane = this.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(this.createOptionPane(), BorderLayout.CENTER);

		this.pack();
		this.setLocationRelativeTo(owner);

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				if (ProgressDialog.this.result != null) {
					ProgressDialog.this.cancel();
				}
			}
		});
	}

	private JOptionPane createOptionPane() {
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		Object[] messageList = { this.message, bar };
		JButton cancelBtn = new JButton(CANCEL_TEXT);
		cancelBtn.addActionListener(this);
		return new JOptionPane(messageList, JOptionPane.INFORMATION_MESSAGE,
				JOptionPane.DEFAULT_OPTION, null, new Object[] { cancelBtn },
				null);
	}

	public void actionPerformed(ActionEvent e) {
		this.cancel();
	}

	/**
	 * 
	 * @return
	 */
	public synchronized boolean isCanceled() {
		return canceled;
	}

	/**
	 * 
	 * 
	 */
	public synchronized void cancel() {
		if (this.result != null) {
			this.result.cancel(true);
		}
		this.setVisible(false);
		this.canceled = true;
	}

	public R getResult() throws Exception {
		return this.result.get();
	}

	private class CallableHandler implements InvocationHandler {

		private Callable<R> call;

		public CallableHandler(Callable<R> call) {
			this.call = call;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			Exception e1 = null;
			Object rtn = null;
			try {
				rtn = method.invoke(this.call, args);
			} catch (Exception e) {
				e1 = e;
			}
			if ("call".equals(method.getName())) {
				log.debug("call is finished");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						ProgressDialog.this.setVisible(false);
						// ProgressDialog.this.dispose();
					}
				});
			}
			if (e1 != null) {
				throw e1;
			}

			return rtn;
		}

	}

	public static <R> R execute(Frame owner, String title, String message,
			Callable<R> callback) throws Exception {
		ProgressDialog<R> dialog = new ProgressDialog<R>(owner, title, message,
				callback);

		return dialog.getResult();
	}
}
