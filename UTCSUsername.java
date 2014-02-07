import java.io.*;
import java.util.*;

public class UTCSUsername
{
	public static final String RESET = "\u001B[0m";
	public static final String BLACK = "\u001B[30m";
	public static final String RED = "\u001B[31m";
	public static final String GREEN = "\u001B[32m";
	public static final String YELLOW = "\u001B[33m";
	public static final String BLUE = "\u001B[34m";
	public static final String PURPLE = "\u001B[35m";
	public static final String CYAN = "\u001B[36m";
	public static final String WHITE = "\u001B[37m";

	private static final TreeMap<String, Account> cache = new TreeMap<String, Account>();
	private static final Random RAND = new Random();

	private static class Account
	{
		String username, name, group, shell;

		public Account()
		{
			username = "";
			name = "";
			group = "";
			shell = "";
		}

		public String toString()
		{
			return username + "\n" + name + "\n" + group /*+ "\n" + shell*/;
		}
	}

	public static void main(String[] args) throws Exception
	{
		// System.out.println(Arrays.toString(args));
		boolean enhance = false;
		boolean test = false;
		for (int k = 0; k < args.length; k++) {
			test = test || args[k].equals("--test");
			enhance = enhance || args[k].equals("--enhance");
		}
		boolean none = true;
		if (test) {
			System.out.println(YELLOW + "\nTEST MODE" + RESET);
			buildCache();
			testMode();
			none = false;
		}
		if (enhance) {
			System.out.println(YELLOW + "\nENHANCING CACHE" + RESET);
			enhanceCache();
			none = false;
		}
		if (none) {
			buildCache();
			String[] usernames = getUsers();
			printUsers(usernames);
		}
	}

	private static void testMode() throws FileNotFoundException
	{
		/*
		int num = 0;
		for (String username : cache.keySet()) {
			System.out.printf("%5d : ", (++num));
			Account acct = cache.get(username);
			int lineNum = 0;
			for (String line : acct.toString().split("\n")) {
				if (lineNum == 0)
					System.out.print(CYAN);
				else
					System.out.print(RESET);
				String prefix = ++lineNum == 1 ? "" : "\t";
				System.out.println(prefix + line);
			}
			System.out.println();
		}
		*/
		printUsernames(getAllUsers());
	}

	private static void buildCache() throws FileNotFoundException
	{
		File folder = new File("./cache/");
		if (folder.exists()) {
			File[] listOfFiles = folder.listFiles();
			for (File file : listOfFiles) {
				String name = file.getName();
				if (name.endsWith(".cache")) {
					String username = name.split("\\.")[0];
					Scanner reader = new Scanner(file);
					Account acct = new Account();
					try {
						acct.username = reader.nextLine();
						acct.name = reader.nextLine();
						acct.group = reader.nextLine();
						acct.shell = reader.nextLine();
						cache.put(username, acct);
					} catch (Exception ex) {}
				}
			}
		} else {
			exec("mkdir cache");
		}
	}

	private static void enhanceCache() throws FileNotFoundException
	{
		enhanceCache(5);
	}

	private static void enhanceCache(int numEntries) throws FileNotFoundException
	{
		int l = Integer.toString(numEntries).length();
		java.text.DecimalFormat f = new java.text.DecimalFormat("0.0");
		buildCache();
		int numAdded = 0;
		LinkedList<String> allUsers = getAllUsers();
		allUsers = shuffle(allUsers);
		for (String name : allUsers) {
			if (!cache.keySet().contains(name)) {
				numAdded++;
				String msg = addNewUser(name);
				System.out.printf(" %" + l + "d / %" + l + "d : " + CYAN + "%-8s" + RESET, numAdded, numEntries, name);
				System.out.println(PURPLE + " " + msg + RESET);
			}
			if (numAdded >= numEntries)
				break;
		}
		int cached = cache.keySet().size();
		int total = allUsers.size();
		int remaining = total - cached;
		if (remaining >= 0) {
			System.out.print(YELLOW);
			System.out.println("Cached " + cached + " / " + total + " users with " + remaining + " remaining");
			System.out.println("Cached " + f.format((double)(cached * 100) / total) + "% of users.");
			System.out.println(RESET);
		} else {
			System.out.println(YELLOW + "Cached " + cached + " users." + RESET);
			System.out.println(PURPLE + "Cached users not found in UTCS username list:" + RESET);
			LinkedList<String> loners = new LinkedList<String>();
			for (String username : cache.keySet()) {
				if (!allUsers.contains(username)) {
					loners.addLast(username);
				}
			}
			printUsernames(loners);
		}
	}

	private static String[] getUsers()
	{
		String[] usernames = getConnectedUsers();
		return usernames;
	}

	private static LinkedList<String> getAllUsers()
	{
		LinkedList<String> allUsers = exec("ls /u/");
		allUsers.remove("restore_symboltable");
		allUsers.remove("www");
		allUsers.remove("www-body");
		allUsers.remove("www-data");
		return allUsers;
	}

	public static String[] getSelectUsers()
	{
		return new String[] {"bismith", "gheith", "klivans"};
	}

	public static String[] getConnectedUsers()
	{
		LinkedList<String> users = exec("users");
		TreeSet<String> set = new TreeSet<String>();
		for (String line : users) {
			String[] tokens = line.split(" ");
			for (String token : tokens) {
				set.add(token);
			}
		}
		return set.toArray(new String[] {});
	}

	public static String[] getCS429Users()
	{
		LinkedList<String> usernameslist = exec("ls ~gheith/dropbox/");
		String[] usernames = new String[usernameslist.size()];
		int i = 0;
		for (String username : usernameslist)
			usernames[i++] = username.split("\\.", 2)[0];
		return usernames;
	}

	private static void printUsers(String[] usernames)
	{
		for (String username : usernames) {
			if (!cache.keySet().contains(username)) {
				addNewUser(username);
				System.out.println(RED + "ADDING NEW USER" + RESET);
			}
			// System.out.println(cache.get(username) + "\n");
			for (String line : cache.get(username).toString().split("\n")) {
				if (line.equals(username)) {
					System.out.println(YELLOW + line + RESET);
				} else if (line.startsWith("Name:")) {
					String[] split = line.split(" ", 2);
					System.out.println(split[0] + " " + CYAN + split[1] + RESET);
				} else {
					System.out.println(line);
				}
			}
			System.out.println();
		}
	}

	private static void printUsernames(Collection<String> usernames)
	{
		int columns = 80;
		int spacer = 3;
		int maxLength = 0;

		System.out.print("Key:     ");
		System.out.print(GREEN + "current     " + RESET);
		System.out.print(CYAN + "CS429     " + RESET);
		System.out.print(YELLOW + "uncached     " + RESET);
		System.out.println();
		for (int k = 0; k < columns; k++)
			System.out.print('-');
		System.out.println();

		LinkedList<String> allUsers = getAllUsers();
		LinkedList<String> cs429users = new LinkedList<String>();
		for (String name : getCS429Users()) {
			cs429users.addLast(name);
		}
		for (String username : usernames) {
			maxLength = Math.max(username.length(), maxLength);
		}
		int x = 0;
		for (String username : usernames) {
			while (username.length() != (maxLength + spacer)) {
				username += " ";
			}
			x += username.length();
			if (x >= (columns + spacer)) {
				System.out.println();
				x = username.length();
			}
			String un = username.trim();
			if (un.equals(System.getProperty("user.name"))) {
				System.out.print(GREEN + username + RESET);
			} else if (cs429users.contains(un)) {
				System.out.print(CYAN + username + RESET);
			} else if (!cache.keySet().contains(un)) {
				System.out.print(YELLOW + username + RESET);
			} else {
				System.out.print(username);
			}
		}
		System.out.println();
	}

	private static String addNewUser(String username)
	{
		try {
			String tempfilename = (RAND.nextInt() + System.currentTimeMillis()) + ".temp";
			exec("finger " + username + "@cs.utexas.edu > " + tempfilename);
			Account acct = new Account();
			acct.username = username;
			acct.name = "Name:         " + exec("cat " + tempfilename + " | grep Name:").getFirst().split(" ", 2)[1].trim();
			acct.group = exec("cat " + tempfilename + " | grep Group:").getFirst();
			acct.shell = exec("cat " + tempfilename + " | grep Shell:").getFirst();
			cache.put(username, acct);
			// System.out.println(acct + "\n");
			PrintWriter writer = new PrintWriter(new File("cache/" + username + ".cache"));
			writer.println(acct + "\n");
			writer.close();
			exec("rm -f " + tempfilename);
			// (new File(tempfilename)).delete();
			return "";
		} catch (Exception ex) {
			return "record not found";
			// System.out.println(username + ": record could not be found\n");
			// ex.printStackTrace();
		}
	}

	private static LinkedList<String> shuffle(LinkedList<String> list)
	{
		String[] array = new String[list.size()];
		int index = 0;
		for (String item : list)
			array[index++] = item;
		for (int k = 0; k < array.length; k++) {
			String temp = array[k];
			index = RAND.nextInt(array.length);
			array[k] = array[index];
			array[index] = temp;
		}
		LinkedList<String> result = new LinkedList<String>();
		for (String item : array)
			result.addLast(item);
		return result;
	}

	private static LinkedList<String> exec(String cmd)
	{
		return exec(new String[] {cmd});
	}
	private static LinkedList<String> exec(String[] cmds)
	{
		LinkedList<String> output = new LinkedList<String>();
		try {
			String filename = (RAND.nextInt() + System.currentTimeMillis()) + ".sh";
			File file = new File(filename);
			PrintWriter pw = new PrintWriter(file);
			for (String cmd : cmds)
				pw.println(cmd);
			pw.println("rm -f " + filename);
			pw.close();
			if (!file.canExecute())
				file.setExecutable(true, true);
			Process proc = Runtime.getRuntime().exec("./" + filename);
			// (new File(filename)).delete();

			String s;
			BufferedReader stdInput = new BufferedReader(new 
				InputStreamReader(proc.getInputStream()));
			// read the output from the command
			while (true && (s = stdInput.readLine()) != null) {
				output.add(s);
			}
		} catch (Exception ex) {
			System.err.println(ex);
		}
		return output;
	}
}

