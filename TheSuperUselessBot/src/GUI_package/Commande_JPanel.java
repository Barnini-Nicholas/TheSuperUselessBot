package GUI_package;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import main_package.Commande;

public class Commande_JPanel extends JPanel {
	
	private JTextArea nomCommande;
	private JTextArea resultatCommande;
	private JCheckBox activated;
	private JCheckBox modOnly;
	private JCheckBox isRegExp;
	private Commande commande;
	
	

	public Commande_JPanel() {
		super();
		nomCommande = new JTextArea(1, 15);
		resultatCommande = new JTextArea(1, 50);
		activated = new JCheckBox();
		modOnly = new JCheckBox();
		isRegExp = new JCheckBox();
		this.add(nomCommande);
		this.add(resultatCommande);
		this.add(activated);
		this.add(modOnly);
		this.add(isRegExp);
		
	}



	public JTextArea getNomCommande() {
		return nomCommande;
	}



	public void setNomCommande(JTextArea nomCommande) {
		this.nomCommande = nomCommande;
	}



	public JTextArea getResultatCommande() {
		return resultatCommande;
	}



	public void setResultatCommande(JTextArea resultatCommande) {
		this.resultatCommande = resultatCommande;
	}



	public JCheckBox getActivated() {
		return activated;
	}



	public void setActivated(JCheckBox activated) {
		this.activated = activated;
	}



	public JCheckBox getModOnly() {
		return modOnly;
	}



	public void setModOnly(JCheckBox modOnly) {
		this.modOnly = modOnly;
	}



	public JCheckBox getIsRegExp() {
		return isRegExp;
	}



	public void setIsRegExp(JCheckBox isRegExp) {
		this.isRegExp = isRegExp;
	}



	public Commande getCommande() {
		return commande;
	}



	public void setCommande(Commande commande) {
		this.commande = commande;
	}

	
	
}
