package main_package;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.json.JSONException;
import org.json.JSONObject;

public class TwitchBot extends PircBot implements Runnable {

	//////////
	// PARAM
	//////////

	public boolean CHIFFRERANDOM = false;
	public boolean checkModo = false;
	public int chiffreCHIFFRERANDOM = 0;
	public String channelToJoin;
	public ArrayList<String> listModo;
	public Configuration config;
	public String bufferMessage;
	public ArrayList<String> dictionnaireMotsInterdits;
	private boolean PLANT;
	private String plantValue;
	private String plantSender;
	public final String PATH_TO_TXT = "ressources/liste_mots_moderation.txt";
	private AntiSpamThread antiSpamThread;
	private Thread tSpam;

	////////////////
	// CONSTRUCTEUR
	////////////////

	public TwitchBot(Configuration c) {
		config = c;
	}

	////////////
	// METHODES
	////////////

	@Override
	public void run() {
		System.out.println("THREAD TWITCH BOT: " + Thread.currentThread().getId());
		this.channelToJoin = new String(config.getChannelToJoin());
		CHIFFRERANDOM = false;
		PLANT = false;
		checkModo = true;
		plantValue = "";
		plantSender = "";
		listModo = new ArrayList<String>();
		dictionnaireMotsInterdits = new ArrayList<String>();

		// Lancement d'un thread daemon pour contrer les spams de message
		// intempestifs
		this.setAntiSpamThread(new AntiSpamThread(this));
		tSpam = new Thread(this.getAntiSpamThread());
		tSpam.setDaemon(true);
		tSpam.start();
		System.out.println("THREAD ID SPAM: " + tSpam.getId());

		// Remplissage du dictionnaires de mots � censurer
		this.setDictionnaireMotsInterdits(this.remplirListeDico(PATH_TO_TXT));
		this.setBufferMessage("");

		try {
			this.setEncoding(Charset.forName("UTF-8").toString());
			System.out.println("CHARSET : " + Charset.forName("UTF-8").toString());
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.setName(config.getBotName());
		this.isConnected();
		this.setVerbose(true);

		try {
			this.connect(config.getUrl(), config.getPort(), config.getoAuth());
		} catch (NickAlreadyInUseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IrcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.joinChannel("#" + config.getChannelToJoin());
		this.sendAction("#" + config.getChannelToJoin(), " vient de se connecter !");
	}

	@Override
	protected void onConnect() {

		this.sendRawLine("CAP REQ :twitch.tv/membership");
		this.sendRawLine("CAP REQ :twitch.tv/commands");
		this.sendMessage("#" + channelToJoin, ".mods");
		// Commande pour afficher la liste des
		// mod�rateur et r�cup�r� par onNotice
	}

	public void onMessage(String channel, String sender, String login, String hostname, String message) {

		// reset du bufferString
		this.setBufferMessage("");

		this.getAntiSpamThread().add(sender);

		// Cr�ation d'un thread d�di� � la mod�ration
		ModerationThread mt = new ModerationThread(this, channel, sender, message, this.getDictionnaireMotsInterdits());
		Thread t = new Thread(mt);
		t.start();

		// On v�rifie dans la liste si le message envoy� correspond ou pas � un
		// message dans le JSON.
		for (Commande c : config.getListCommandes()) {
			String returnCheckCommand;
			if (!c.isModOnly()) {
				returnCheckCommand = c.checkCommand(channel, sender, login, hostname, message, false);
			} else {
				returnCheckCommand = c.checkCommand(channel, sender, login, hostname, message, this.isMod(sender));
			}
			if (!returnCheckCommand.equals("")) {
				sendMessage(channel, bufferMessage = returnCheckCommand);
				return;
			}
		}

		// Question au bot
		Pattern pQuestion = Pattern.compile("\\s*" + this.getConfig().getBotName() + ".*\\?\\s*",
				Pattern.CASE_INSENSITIVE);
		Matcher mQuestion = pQuestion.matcher(message);
		if (mQuestion.matches()) {
			Random rn = new Random();
			int random = rn.nextInt(5 - 0 + 1) + 0;
			switch (random) {
			case 0:
				this.sendMessage(channel, "@" + sender + " Evidemment !");
				break;
			case 1:
				this.sendMessage(channel, "@" + sender + " J'en suis s�r.");
				break;
			case 2:
				this.sendMessage(channel, "@" + sender + " C'est pas faux.");
				break;
			case 3:
				this.sendMessage(channel, "@" + sender + " Dans tes r�ves.");
				break;
			case 4:
				this.sendMessage(channel, "@" + sender + " Je ne crois pas, non.");
				break;
			case 5:
				this.sendMessage(channel, "@" + sender + " Totalement faux !");
				break;
			default:
				System.err.println("ERREUR : Fail random lors de la question au bot !");
				break;
			}
		}

		// INFO
		if (message.equalsIgnoreCase("info")) {
			sendMessage(channel, bufferMessage = listModo.toString());
			System.out.println(bufferMessage);
			return;
		}

		// POLL
		Pattern pattern = Pattern.compile("!poll *//.*//.*//.*", Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(message);
		if (m.matches()) {
			try {
				if (isMod(sender)) {
					requestAPIStrawpoll(channel, sender, login, hostname, message);
					return;
				} else {
					sendMessage(channel,
							bufferMessage = "NotLikeThis Vous n'avez pas les droits pour cr�er un strawpoll NotLikeThis");
					return;
				}

			} catch (JSONException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// !COMMANDS
		if (message.equalsIgnoreCase("!commandes") || message.equalsIgnoreCase("!commands")) {
			StringBuilder sb = new StringBuilder();
			sb.append("!poll // <titre> // <option1> // <option2> // <optionN> ; ");
			sb.append("!suicide ; ");
			sb.append("!random ; ");
			sb.append("!plant ; ");
			sb.append("!creator ; ");
			sb.append("!run ; ");
			int length = this.getConfig().getListCommandes().size();
			int inc = 0;
			for (Commande c : this.getConfig().getListCommandes()) {
				inc++;
				if(!c.isRegExp()) {
					sb.append(c.getNomCommande());
					if(inc != length)
						sb.append(" ; ");
				}
			}
			this.sendMessage(channel, sb.toString());
		}

		// DISCONNECT
		if (message.equalsIgnoreCase("disconnect")
				&& (sender.equalsIgnoreCase(channelToJoin) || sender.equalsIgnoreCase("thronghar"))) {
			deconnexion();
		}

		// SUICIDE
		if (message.equalsIgnoreCase("!suicide")) {
			sendMessage(channel, bufferMessage = "rip " + sender);
			sendMessage(channel, bufferMessage = ".timeout " + sender + " 10");
			return;
		}

		// CHIFFRE RANDOM debut
		if (message.equalsIgnoreCase("!random") && CHIFFRERANDOM == false) {
			sendMessage(channel, "C'est l'heure du random les petits fruits ! Choisissez un nombre entre 1 et 10.");
			CHIFFRERANDOM = true;
			Random rn = new Random();
			chiffreCHIFFRERANDOM = rn.nextInt(10 - 0 + 1) + 0;
			return;
		}

		// CHIFFRE RANDOM fin
		if (NumberUtils.isNumber(message) && CHIFFRERANDOM == true) {
			if (Integer.parseInt(message) == chiffreCHIFFRERANDOM) {
				sendMessage(channel, sender + " a trouv� le bon chiffre ! (" + chiffreCHIFFRERANDOM + ")");
				CHIFFRERANDOM = false;
			}
			return;
		}

		// PLANT COMMANDE d�but
		if (message.equalsIgnoreCase("!plant") && !PLANT) {
			Random rn = new Random();
			int outputRandom = rn.nextInt(3 - 0 + 0) + 1;
			if (outputRandom == 1) {
				plantValue = "!cut bleu";
			} else if (outputRandom == 2) {
				plantValue = "!cut rouge";
			} else if (outputRandom == 3) {
				plantValue = "!cut vert";
			}
			this.sendMessage(channel, "@" + sender + " vient de poser la bombe ! !cut rouge/bleu/vert pour defuse !");
			PLANT = true;
			plantSender = sender;
		}

		// PLANT COMMANDE fin
		if (message.startsWith("!cut ") && PLANT && !sender.equals(plantSender)) {
			if (message.equalsIgnoreCase(plantValue)) {
				this.sendMessage(channel, "@" + sender + " a r�ussi � defuse la bombe !");
				PLANT = false;
				plantSender = "";
			} else if (message.equalsIgnoreCase("!cut bleu") || message.equalsIgnoreCase("!cut vert")
					|| message.equalsIgnoreCase("!cut rouge")) {
				this.sendMessage(channel, "@" + sender + " s'est tromp� ! Il est maintenant mort ! RIP");
				PLANT = false;
				plantSender = "";
			}
		}

		// CREATOR
		if (message.equalsIgnoreCase("!creator") || message.equalsIgnoreCase("!" + this.getName())
				|| message.equalsIgnoreCase("!bot")) {
			this.sendMessage(channel, "@" + sender
					+ ": J'ai �t� r�alis� par Barnini Nicholas https://github.com/Barnini-Nicholas/TheSuperUselessBot");
		}
	}

	public void deconnexion() {
		sendMessage("#" + this.getChannelToJoin(), bufferMessage = "bye bb");
		sendAction("#" + channelToJoin, " vient de se d�connecter ! ");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.disconnect();
		System.exit(0);
	}

	// M�thode temporaire cr��e car on ne peut d�finir directement sur la
	// m�thode onMessage qu'elle peut "throws" des exceptions
	public void requestAPIStrawpoll(String channel, String sender, String login, String hostname, String message)
			throws JSONException, MalformedURLException, IOException {

		// On d�finit les arguments JSON
		List<String> options = new ArrayList<>();
		List<String> tempo = new ArrayList<>();
		tempo = Arrays.asList((message.split("//")));

		// On ajoute les options � part le !poll et le titre
		int i = 0;
		for (String s : tempo) {
			if (i > 1)
				options.add(s);
			i++;
		}

		// On configure le json
		JSONObject json = new JSONObject().put("title", tempo.get(1)).put("options", options);

		// On ouvre la connection avec l'API strawpoll
		HttpURLConnection con;
		con = (HttpURLConnection) new URL("http://strawpoll.me/api/v2/polls").openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", "Mozilla");
		con.setRequestProperty("Content-Type", "application/json");
		con.setDoOutput(true);

		// On met dans un buffer
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()))) {
			writer.write(json.toString());
		}

		// On v�rifie le code de retour
		int code = con.getResponseCode();
		if (code != 201 && code != 200) {
			System.out.println("La cr�ation du poll a retourn� un code (" + code + ") diff�rent de 200 ou 201");
			return;
		}
		StringBuilder response = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			String input;
			while ((input = reader.readLine()) != null) {
				response.append(input).append("\n");
			}
		}
		sendMessage(channel, bufferMessage = "www.strawpoll.me/" + new JSONObject(response.toString()).getInt("id"));
	}

	public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {

		if (checkModo) {
			Pattern pattern = Pattern.compile(".*The moderators of this room are.*", Pattern.CASE_INSENSITIVE);
			Matcher m = pattern.matcher(notice);
			if (m.matches()) {
				this.listModo = getListMods(notice);
				checkModo = false;
			}
		}
	}

	public ArrayList<String> getListMods(String sample) {
		String sampleAfterReplace = sample.replaceFirst(".*The moderators of this room are: ", "");
		return new ArrayList<String>(Arrays.asList(sampleAfterReplace.split(",")));
	}

	public boolean isMod(String userToTest) {
		for (String s : this.listModo) {
			// On enleve les espaces restant grace aux replaceAll et
			// on compare en ignorant la casse
			if (s.replaceAll(" ", "").equalsIgnoreCase(userToTest.replaceAll(" ", "")))
				return true;
		}
		return false;
	}

	public ArrayList<String> remplirListeDico(String path) {
		ArrayList<String> dico = new ArrayList<String>();

		File f = new File(path);
		Scanner source;
		try {
			source = new Scanner(f);
			while (source.hasNextLine()) {
				String mot = source.nextLine().replaceAll("^\\s+", "").replaceAll("\\s+$", "");
				if (!mot.startsWith("#") && !mot.equals(""))
					dico.add(mot);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("Probl�me lors de l'initialisation du scanner // Fichier non trouv� : " + f.toString());
			e.printStackTrace();
		}
		return dico;
	}

	public String getBufferMessage() {
		return bufferMessage;
	}

	public void setBufferMessage(String bufferMessage) {
		this.bufferMessage = bufferMessage;
	}

	public boolean isCHIFFRERANDOM() {
		return CHIFFRERANDOM;
	}

	public void setCHIFFRERANDOM(boolean cHIFFRERANDOM) {
		CHIFFRERANDOM = cHIFFRERANDOM;
	}

	public boolean isCheckModo() {
		return checkModo;
	}

	public void setCheckModo(boolean checkModo) {
		this.checkModo = checkModo;
	}

	public int getChiffreCHIFFRERANDOM() {
		return chiffreCHIFFRERANDOM;
	}

	public void setChiffreCHIFFRERANDOM(int chiffreCHIFFRERANDOM) {
		this.chiffreCHIFFRERANDOM = chiffreCHIFFRERANDOM;
	}

	public String getChannelToJoin() {
		return channelToJoin;
	}

	public void setChannelToJoin(String channelToJoin) {
		this.channelToJoin = channelToJoin;
	}

	public ArrayList<String> getListModo() {
		return listModo;
	}

	public void setListModo(ArrayList<String> listModo) {
		this.listModo = listModo;
	}

	public Configuration getConfig() {
		return config;
	}

	public void setConfig(Configuration config) {
		this.config = config;
	}

	public ArrayList<String> getDictionnaireMotsInterdits() {
		return dictionnaireMotsInterdits;
	}

	public void setDictionnaireMotsInterdits(ArrayList<String> dictionnaireMotsInterdits) {
		this.dictionnaireMotsInterdits = dictionnaireMotsInterdits;
	}

	public boolean isPLANT() {
		return PLANT;
	}

	public void setPLANT(boolean pLANT) {
		PLANT = pLANT;
	}

	public String getPlantValue() {
		return plantValue;
	}

	public void setPlantValue(String plantValue) {
		this.plantValue = plantValue;
	}

	public AntiSpamThread getAntiSpamThread() {
		return antiSpamThread;
	}

	public void setAntiSpamThread(AntiSpamThread antiSpamThread) {
		this.antiSpamThread = antiSpamThread;
	}

	public String getPATH_TO_TXT() {
		return PATH_TO_TXT;
	}

	public Thread gettSpam() {
		return tSpam;
	}

	public void settSpam(Thread tSpam) {
		this.tSpam = tSpam;
	}

}
