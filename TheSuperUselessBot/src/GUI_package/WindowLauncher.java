package GUI_package;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import main_package.TwitchBot;

public class WindowLauncher  extends JFrame { 
	
	TwitchBot tb;

	public WindowLauncher() {
		super("TheSuperUselessBot");
		
		WindowListener l = new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				if(tb!=null) // Si il n'a pas �t� instanci�
					tb.deconnexion();
				System.exit(0);
			}
		};
		
		JMenuBar menuBar = new JMenuBar();
		JMenu commandesMenu = new JMenu("Commandes");
		JMenu jeuxMenu = new JMenu("Jeux");
		JMenu outilsMenu = new JMenu("Outils");
		
		menuBar.add(commandesMenu);
		menuBar.add(jeuxMenu);
		menuBar.add(outilsMenu);
		this.setJMenuBar(menuBar);
		
		this.addWindowListener(l);
		

		
		
		
//		JPanel panneauPrinc = new JPanel();
//		panneauPrinc.setLayout(new BoxLayout(panneauPrinc, BoxLayout.Y_AXIS));
//		
//		JPanel panneau1 = new JPanel();
//		panneau1.add(new JLabel("Test1"));
//		panneau1.add(new JLabel("Test2"));
//		JPanel panneau2 = new JPanel();
//		panneau1.add(new JLabel("Test1"));
//		panneau1.add(new JLabel("Test2"));
//		
//		panneauPrinc.add(panneau1);
//		panneauPrinc.add(panneau2);
//		
//		this.setContentPane(panneauPrinc);
		
		JPanel parent = new JPanel();
	    parent.setLayout(new BoxLayout(parent,BoxLayout.Y_AXIS));
	    add(parent);

	    JPanel Checks = new JPanel(); //set up panel
	    JLabel CLabel = new JLabel("Label with text");
	    Checks.setBackground(Color.red);
	    parent.add(Checks);


	   JPanel Transactions = new JPanel();
	   Transactions.setToolTipText("Electronic Transactions");
	   Transactions.setBackground(Color.blue);
	   parent.add(Transactions);
		
		
		this.setSize(400,600);
		this.setVisible(true);
		
//		Configuration config = new Configuration();
//		
//		try {
//			config.setupConfig("ressources/config_bot.ini");
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		try {
//			config.setupCommands("ressources/command_list.json");
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		tb = new TwitchBot(config);
	}
	
	public static void main(String [] args){
		WindowLauncher frame = new WindowLauncher();
	} 

}
