/**
 * DISCLAIMER
 * 
 * The quality of the code is such that you should not copy any of it as best
 * practice how to build Vaadin applications.
 * 
 * @author jouni@vaadin.com
 * 
 */

package com.vaadin.demo.dashboard.data;

import it.unimib.disco.essere.analysis.download.GitDownloadRepository;
import it.unimib.disco.essere.analysis.download.HGDownloadRepository;
import it.unimib.disco.essere.analysis.download.RepositoryUrlNotWellFormed;
import it.unimib.disco.essere.analysis.download.SVNDownloadRepository;
import it.unimib.disco.essere.analysis.maven.SandBox;
import it.unimib.disco.essere.analyzer.build.maven.util.MavenPathRetrieve;
import it.unimib.disco.essere.crawler.type.RepositoryProtocol;
import it.unimib.disco.essere.repofinderUI.tool.dfmc4j.DFMC4JExec;
import it.unimib.disco.essere.repofinderUI.tool.exec.ToolExecutor;
import it.unimib.disco.essere.serial.readindex.RepositoryReadIndex;
import it.unimib.disco.essere.serial.searching.Repository;
import it.unimib.disco.essere.serial.searching.RepositoryDTO;
import it.unimib.disco.essere.serial.searching.SearchEngine;
import it.unimib.disco.essere.serial.type.IndexingConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.util.CurrentInstance;

public class DataProvider implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4408864981761274870L;
	public static Random rand = new Random();
	public static Path _directory = Paths.get(File.separator,"home","r.roveda","workspace","tesi","repoFinderUI","index-searchengine","index");
	public static Path _directoryDownload = Paths.get(File.separator,"home","r.roveda","sandboxdownload");
	public static Path _directorySandobox = Paths.get(File.separator,"home","r.roveda","sandbox");
	/**
	 * Initialize the data for this application.
	 */
	public DataProvider() {
		loadMoviesData();
		loadTheaterData();
		loadProjectData();
		generateTransactionsData();
	}

	public static Directory openDirectoryIndexing() throws IOException{
		return FSDirectory.open(_directory.toFile());
	}

	private ProjectContainer p ;
	private static List<Repository> projects = new ArrayList<Repository>();
	public void loadProjectData() {
		RepositoryReadIndex i = new RepositoryReadIndex(_directory.toString());
		p=getProjectsFromDocument(i);
	}

	public ProjectContainer getProjectsFromDocument(RepositoryReadIndex i){
		p = new ProjectContainer();
		for(Document d:i._l){
			p.addProject(d);
			projects.add(addRepository(d));
		}
		return p;
	}
	public Repository addRepository(Document d){
		Repository r = new Repository();
		r.checkout = d.get(IndexingConstants.CHECKOUT);
		r.checkout_tipo = d.get(IndexingConstants.CHECKOUT_TIPO);
		r.descrizione = d.get(IndexingConstants.DESCRIZIONE);
		r.link = d.get(IndexingConstants.LINK);
		r.nome = d.get(IndexingConstants.NOME);
		r.score = 0.0f;
		r.tag = d.get(IndexingConstants.TAG);
		return r;
	}
	public ProjectContainer getProjects(){
		return p;
	}
	private static final String defaultValues []= {
		IndexingConstants.CHECKOUT,
		IndexingConstants.CHECKOUT_TIPO,
		IndexingConstants.DESCRIZIONE,
		IndexingConstants.LINK,
		IndexingConstants.NOME,
		IndexingConstants.TAG};

	public ProjectContainer getProjectsByQuery(String query){
		SearchEngine se = new SearchEngine(_directory.toString());
		p=new ProjectContainer();
		projects=new ArrayList<Repository>();
		try {
			RepositoryDTO i =se.getResults(query, defaultValues);
			for(Repository r :i.repositorys){
				p.addProject(r);
				projects.add(r);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	void next(int []m){
		m[0]++;
		for(int i=0;i<m.length;i++){
			if(m[i]>1&&i<(m.length-1)){
				m[i+1]++;
				m[i]=0;
			}
		}
	}

	String getQuery(int []m,List<String>query){
		String r="";
		for(int i=0;i<m.length;i++){
			if(m[i]==1){
				r=String.format("%s %s", r,query.get(i));
			}
		}
		return r;
	}

	int countOfOnes(int []m){
		int j=0;
		for(int i:m){
			j+=i;
		}
		return j;
	}

	public ProjectContainer doQueryOR(List<String> query){
		SearchEngine se = new SearchEngine(_directory.toString());
		p=new ProjectContainer();
		projects=new ArrayList<Repository>();
		List<Repository> set = new ArrayList<Repository>();
		
		try {
			int m[] = new int[query.size()];
			for(int j=0;j<query.size();j++){
				m[j]=0;
			}			
			while(countOfOnes(m)<m.length){
				addScoreMiddleSearch(set, m, query, se);
				next(m);
			}
			//ultima query
			addScoreMiddleSearch(set, m, query, se);
//			int n=0;
//			for(Repository r :set){
//				int k=0;
//				for(Repository l :set){
//					if(l.equals(r)){
//						k++;
//					}
//				}
//				System.out.println(String.format("%d\t%d/%d %s",k,n,set.size(),r.nome));
//				n++;
//			}
			for(Repository r :set){
				p.addProject(r);
				
				projects.add(r);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
		
	}
	private void addScoreMiddleSearch(List<Repository> set, int []m, List<String> query, SearchEngine se ) throws ParseException, IOException{
		if(countOfOnes(m)>1){
			RepositoryDTO repo =se.getResults(getQuery(m,query), defaultValues);
			for(Repository r:repo.repositorys){
				if(set.contains(r)){
					for(Repository y:set){	
						if(y.equals(r)){
							y.score=y.score+r.score;
							break;
						}
					}
				}else{
					set.add(r);
				}
			}
		}
	}
	public ProjectContainer doQueryOROld(List<String> query){
		SearchEngine se = new SearchEngine(_directory.toString());
		p=new ProjectContainer();
		projects=new ArrayList<Repository>();
		Set<Repository> set = new HashSet<Repository>();
		try {
			int m[] = new int[query.size()];
			for(int j=0;j<query.size();j++){
				m[j]=0;
			}			
			while(countOfOnes(m)<m.length){
				addScoreMiddleSearch(set, m, query, se);
				next(m);
			}
			//ultima query
			addScoreMiddleSearch(set, m, query, se);

			for(Repository r :set){
				p.addProject(r);
				projects.add(r);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	private void addScoreMiddleSearch(Set<Repository> set, int []m, List<String> query, SearchEngine se ) throws ParseException, IOException{
		Set<Repository> temp = new HashSet<Repository>();
		//aggiungere al set progetti e sommare gli score se countofones è maggiore di 1
		if(countOfOnes(m)>1){
			RepositoryDTO repo =se.getResults(getQuery(m,query), defaultValues);
			for(Repository r:repo.repositorys){
				temp.add(r);
			}
			for(Repository r:temp){
				if(set.contains(r)){
					Iterator<Repository> iterator=set.iterator();
					boolean modificato = false;
					while(iterator.hasNext() && !modificato){
						Repository y = iterator.next();
						if(y.equals(r)){
							y.score=y.score+r.score;
							System.out.println(y.score);
							modificato=true;
						}
					}
				}else{
					set.add(r);
				}
			}
		}
	}

	public static Repository getProjectByName(String name) {
		for (Repository r : projects) {
			if (r.nome.equals(name))
				return r;
		}
		return null;
	}

	static MavenPathRetrieve _m = null;
	public static MavenPathRetrieve getMavenPathRetrieve(){
		return _m;
	}
	public static boolean doDownload(Repository r, String checkout){
		Multimap<String, String> l =r.getRepositories();
		String tipo = "";
		for(String k:l.keySet()){
			if(l.get(k).contains(checkout)){
				tipo=k;
			}
		}
		if(!tipo.equals("")){
			if(tipo.equals(RepositoryProtocol.GIT)||
					tipo.equals(RepositoryProtocol.GIT_HTML)){
				try {
					GitDownloadRepository g =new GitDownloadRepository(_directoryDownload.toString(),checkout);
					g.downloadRepository();
					_m=new MavenPathRetrieve(_directoryDownload.toFile());
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (RepositoryUrlNotWellFormed e) {
					e.printStackTrace();
				}
			}
			if(tipo.equals(RepositoryProtocol.HG)){
				try {
					HGDownloadRepository g =new HGDownloadRepository(_directoryDownload.toString(),checkout);
					g.downloadRepository();
					_m=new MavenPathRetrieve(_directoryDownload.toFile());
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				} catch (RepositoryUrlNotWellFormed e) {
					e.printStackTrace();
				}
			}
			if(tipo.equals(RepositoryProtocol.SVN)||
					tipo.equals(RepositoryProtocol.SVN_HTML)||
					tipo.equals(RepositoryProtocol.SVN_HTML_CO)){
				try {
					SVNDownloadRepository s =new SVNDownloadRepository(_directoryDownload.toString(),checkout);
					if(s.downloadRepository()){
						_m=new MavenPathRetrieve(_directoryDownload.toFile());
						return true;
					}else{
						return false;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (RepositoryUrlNotWellFormed e) {
					e.printStackTrace();
				} 
			}
		}
		return false;
	}

	public static boolean doMavenBuild(final String artifact){
		if(artifact!=null){
			SandBox s = new SandBox(_directoryDownload.toFile());
			s.runMaven(s._m.getArtifact(artifact));
			if(s.isMavenBuildSuccess()){
				try {
					s.copyAllDependencies();
					s.copyAllSources();
					return true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}else{
				return false;
			}
		}else{
			return false;
		}

	}

	public static boolean doRunTool() {
		ToolExecutor t = new DFMC4JExec();
		return t.exec(_directorySandobox, null);
	}

	/**
	 * =========================================================================
	 * Movies in theaters
	 * =========================================================================
	 */
	/** Simple Movie class */
	public static class Movie {
		public final String title;
		public final String synopsis;
		public final String thumbUrl;
		public final String posterUrl;
		/** In minutes */
		public final int duration;
		public Date releaseDate = null;

		public int score;
		public double sortScore = 0;

		Movie(String title, String synopsis, String thumbUrl, String posterUrl,
				JsonObject releaseDates, JsonObject critics) {
			this.title = title;
			this.synopsis = synopsis;
			this.thumbUrl = thumbUrl;
			this.posterUrl = posterUrl;
			this.duration = (int) ((1 + Math.round(Math.random())) * 60 + 45 + (Math
					.random() * 30));
			try {
				String datestr = releaseDates.get("theater").getAsString();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				releaseDate = df.parse(datestr);
				score = critics.get("critics_score").getAsInt();
				sortScore = 0.6 / (0.01 + (System.currentTimeMillis() - releaseDate
						.getTime()) / (1000 * 60 * 60 * 24 * 5));
				sortScore += 10.0 / (101 - score);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		public String titleSlug() {
			return title.toLowerCase().replace(' ', '-').replace(":", "")
					.replace("'", "").replace(",", "").replace(".", "");
		}

		public void reCalculateSortScore(Calendar cal) {
			if (cal.before(releaseDate)) {
				sortScore = 0;
				return;
			}
			sortScore = 0.6 / (0.01 + (cal.getTimeInMillis() - releaseDate
					.getTime()) / (1000 * 60 * 60 * 24 * 5));
			sortScore += 10.0 / (101 - score);
		}
	}

	/*
	 * List of movies playing currently in theaters
	 */
	private static ArrayList<Movie> movies = new ArrayList<Movie>();

	/**
	 * Get a list of movies currently playing in theaters.
	 * 
	 * @return a list of Movie objects
	 */
	public static ArrayList<Movie> getMovies() {
		return movies;
	}

	/**
	 * Initialize the list of movies playing in theaters currently. Uses the
	 * Rotten Tomatoes API to get the list. The result is cached to a local file
	 * for 24h (daily limit of API calls is 10,000).
	 */
	private static void loadMoviesData() {

		File cache;

		// TODO why does this sometimes return null?
		VaadinRequest vaadinRequest = CurrentInstance.get(VaadinRequest.class);
		if (vaadinRequest == null) {
			// PANIC!!!
			cache = new File("movies.txt");
		} else {
			File baseDirectory = vaadinRequest.getService().getBaseDirectory();
			cache = new File(baseDirectory + "/movies.txt");
		}

		JsonObject json = null;
		try {
			// TODO check for internet connection also, and use the cache anyway
			// if no connection is available
			if (cache.exists()
					&& System.currentTimeMillis() < cache.lastModified() + 1000
					* 60 * 60 * 24) {
				json = readJsonFromFile(cache);
			} else {
				// Get an API key from http://developer.rottentomatoes.com
				String apiKey = "6psypq3q5u3wf9f2be38t5fd";
				json = readJsonFromUrl("http://api.rottentomatoes.com/api/public/v1.0/lists/movies/in_theaters.json?page_limit=30&apikey=" + apiKey);
				// Store in cache
				FileWriter fileWriter = new FileWriter(cache);
				fileWriter.write(json.toString());
				fileWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (json == null) {
			return;
		}

		JsonArray moviesJson;
		movies.clear();
		moviesJson = json.getAsJsonArray("movies");
		for (int i = 0; i < moviesJson.size(); i++) {
			JsonObject movieJson = moviesJson.get(i).getAsJsonObject();
			JsonObject posters = movieJson.get("posters").getAsJsonObject();
			if (!posters.get("profile").getAsString()
					.contains("poster_default")) {
				Movie movie = new Movie(movieJson.get("title").getAsString(),
						movieJson.get("synopsis").getAsString(), posters.get(
								"profile").getAsString(), posters.get(
										"detailed").getAsString(), movieJson.get(
												"release_dates").getAsJsonObject(), movieJson
												.get("ratings").getAsJsonObject());
				movies.add(movie);
			}
		}
	}

	/* JSON utility method */
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/* JSON utility method */
	private static JsonObject readJsonFromUrl(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JsonElement jelement = new JsonParser().parse(jsonText);
			JsonObject jobject = jelement.getAsJsonObject();
			return jobject;
		} finally {
			is.close();
		}
	}

	/* JSON utility method */
	private static JsonObject readJsonFromFile(File path) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(path));
		String jsonText = readAll(rd);
		JsonElement jelement = new JsonParser().parse(jsonText);
		JsonObject jobject = jelement.getAsJsonObject();
		return jobject;
	}

	/**
	 * =========================================================================
	 * Countries, cities, theaters and rooms
	 * =========================================================================
	 */

	/* List of countries and cities for them */
	static HashMap<String, ArrayList<String>> countryToCities = new HashMap<String, ArrayList<String>>();

	static List<String> theaters = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("Threater 1");
			add("Threater 2");
			add("Threater 3");
			add("Threater 4");
			add("Threater 5");
			add("Threater 6");
		}
	};

	static List<String> rooms = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("Room 1");
			add("Room 2");
			add("Room 3");
			add("Room 4");
			add("Room 5");
			add("Room 6");
		}
	};

	/**
	 * Parse the list of countries and cities
	 */
	private static HashMap<String, ArrayList<String>> loadTheaterData() {

		/* First, read the text file into a string */
		StringBuffer fileData = new StringBuffer(2000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				DataProvider.class.getResourceAsStream("cities.txt")));

		char[] buf = new char[1024];
		int numRead = 0;
		try {
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String list = fileData.toString();

		/*
		 * The list has rows with tab delimited values. We want the second (city
		 * name) and last (country name) values, and build a Map from that.
		 */
		countryToCities = new HashMap<String, ArrayList<String>>();
		for (String line : list.split("\n")) {
			String[] tabs = line.split("\t");
			String city = tabs[1];
			String country = tabs[tabs.length - 2];

			if (!countryToCities.containsKey(country)) {
				countryToCities.put(country, new ArrayList<String>());
			}
			countryToCities.get(country).add(city);
		}

		return countryToCities;

	}

	/**
	 * =========================================================================
	 * Transactions data, used in tables and graphs
	 * =========================================================================
	 */

	/** Container with all the transactions */
	private TransactionsContainer transactions;

	public TransactionsContainer getTransactions() {
		return transactions;
	}

	/** Create a list of dummy transactions */
	private void generateTransactionsData() {
		GregorianCalendar today = new GregorianCalendar();
		/*
		 * Data items: timestamp, country, city, theater, room, movie title,
		 * number of seats, price
		 */
		transactions = new TransactionsContainer();

		/* Amount of items to create initially */
		for (int i = 1000; i > 0; i--) {
			// Start from 1st of current month
			GregorianCalendar c = new GregorianCalendar();

			// we will go at most 4 months back
			int newMonthSubstractor = (int) (5.0 * rand.nextDouble());
			c.add(Calendar.MONTH, -newMonthSubstractor);

			int newDay = (int) (1 + (int) (30.0 * rand.nextDouble()));
			c.set(Calendar.DAY_OF_MONTH, newDay);

			if (today.before(c)) {
				newDay = (int) (1 + (int) (today.get(Calendar.DAY_OF_MONTH) * rand
						.nextDouble()));
				c.set(Calendar.DAY_OF_MONTH, newDay);
			}

			// Randomize time of day
			c.set(Calendar.HOUR, (int) (rand.nextDouble() * 24.0));
			c.set(Calendar.MINUTE, (int) (rand.nextDouble() * 60.0));
			c.set(Calendar.SECOND, (int) (rand.nextDouble() * 60.0));
			createTransaction(c);
			// System.out.println(df.format(c.getTime()));
		}
		transactions.sort(new String[] { "timestamp" }, new boolean[] { true });
		updateTotalSum();

	}

	private static double totalSum = 0;

	private void updateTotalSum() {
		totalSum = 0;
		for (Object id : transactions.getItemIds()) {
			Item item = transactions.getItem(id);
			Object value = item.getItemProperty("Price").getValue();
			totalSum += Double.parseDouble(value.toString());
		}
		/*
		 * try { Number amount = NumberFormat.getCurrencyInstance().parse( "$" +
		 * totalSum); totalSum = amount.doubleValue(); } catch (ParseException
		 * e) { e.printStackTrace(); }
		 */
	}

	public static double getTotalSum() {
		return totalSum;
	}

	private void createTransaction(Calendar cal) {
		// Country
		Object[] array = countryToCities.keySet().toArray();
		int i = (int) (Math.random() * (array.length - 1));
		String country = array[i].toString();

		for (Movie m : movies) {
			m.reCalculateSortScore(cal);
		}

		Collections.sort(movies, new Comparator<Movie>() {
			@Override
			public int compare(Movie o1, Movie o2) {
				return (int) (100.0 * (o2.sortScore - o1.sortScore));
			}
		});

		// City
		ArrayList<String> cities = countryToCities.get(country);
		String city = cities.get(0);

		// Theater
		String theater = theaters.get((int) (rand.nextDouble() * (theaters
				.size() - 1)));

		// Room
		String room = rooms.get((int) (rand.nextDouble() * (rooms.size() - 1)));

		// Title
		int randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies.size() / 2.0 - 1));
		while (randomIndex >= movies.size()) {
			randomIndex = (int) (Math.abs(rand.nextGaussian()) * (movies.size() / 2.0 - 1));
		}
		if (movies.get(randomIndex).releaseDate.compareTo(cal.getTime()) >= 0) {
			// System.out.println("skipped " + movies.get(randomIndex).title);
			// System.out.println(df.format(movies.get(randomIndex).releaseDate));
			// System.out.println(df.format(cal.getTime()));
			// System.out.println();
			// ++skippedCount;
			// System.out.println(skippedCount);
			return;
		}
		String title = movies.get(randomIndex).title;

		// Seats
		int seats = (int) (1 + rand.nextDouble() * 3);

		// Price (approx. USD)
		double price = (double) (seats * (6 + (rand.nextDouble() * 3)));

		transactions.addTransaction(cal, country, city, theater, room, title,
				seats, price);

		// revenue.add(cal.getTime(), title, price);

	}

	public IndexedContainer getRevenueForTitle(String title) {
		// System.out.println(title);
		IndexedContainer revenue = new IndexedContainer();
		revenue.addContainerProperty("timestamp", Date.class, new Date());
		revenue.addContainerProperty("revenue", Double.class, 0.0);
		revenue.addContainerProperty("date", String.class, "");
		int index = 0;
		for (Object id : transactions.getItemIds()) {
			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("MM/dd/yyyy");

			Item item = transactions.getItem(id);

			if (title.equals(item.getItemProperty("Title").getValue())) {
				Date d = (Date) item.getItemProperty("timestamp").getValue();

				Item i = revenue.getItem(df.format(d));
				if (i == null) {
					i = revenue.addItem(df.format(d));
					i.getItemProperty("timestamp").setValue(d);
					i.getItemProperty("date").setValue(df.format(d));
				}
				double current = (Double) i.getItemProperty("revenue")
						.getValue();
				current += (Double) item.getItemProperty("Price").getValue();

				i.getItemProperty("revenue").setValue(current);
			}
		}

		revenue.sort(new Object[] { "timestamp" }, new boolean[] { true });
		return revenue;
	}

	public IndexedContainer getRevenueByTitle() {
		IndexedContainer revenue = new IndexedContainer();
		revenue.addContainerProperty("Title", String.class, "");
		revenue.addContainerProperty("Revenue", Double.class, 0.0);

		for (Object id : transactions.getItemIds()) {

			Item item = transactions.getItem(id);

			String title = item.getItemProperty("Title").getValue().toString();

			if (title == null || "".equals(title))
				continue;

			Item i = revenue.getItem(title);
			if (i == null) {
				i = revenue.addItem(title);
				i.getItemProperty("Title").setValue(title);
			}
			double current = (Double) i.getItemProperty("Revenue").getValue();
			current += (Double) item.getItemProperty("Price").getValue();
			i.getItemProperty("Revenue").setValue(current);
		}

		revenue.sort(new Object[] { "Revenue" }, new boolean[] { false });

		// TODO sometimes causes and IndexOutOfBoundsException
		if (revenue.getItemIds().size() > 10) {
			// Truncate to top 10 items
			List<Object> remove = new ArrayList<Object>();
			for (Object id : revenue
					.getItemIds(10, revenue.getItemIds().size())) {
				remove.add(id);
			}
			for (Object id : remove) {
				revenue.removeItem(id);
			}
		}

		return revenue;
	}

	public static Movie getMovieForTitle(String title) {
		for (Movie movie : movies) {
			if (movie.title.equals(title))
				return movie;
		}
		return null;
	}



}
