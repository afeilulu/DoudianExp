package cn.com.xinli.android.doudian.model;

import java.util.ArrayList;

public class Program {
	private String url;
	private String name;
	private String channel;
	private String category;
	private String showTime;
	private String area;
	private String poster;
	private String description;
	private String director;
	private String actor;
	private String score;
	private String epCount;
	private String epStatus;
	private ArrayList<Source> sources;
//	private ArrayList<Integer> guessRelated;
//	private ArrayList<Integer> actorRelated;
//	private ArrayList<Integer> directorRelated;
	private ArrayList<Integer> related;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getShowTime() {
		return showTime;
	}

	public void setShowTime(String year) {
		this.showTime = year;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public ArrayList<Source> getSources() {
		return sources;
	}

	public void setSources(ArrayList<Source> sources) {
		this.sources = sources;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

//	public ArrayList<Integer> getGuessRelated() {
//		return guessRelated;
//	}
//
//	public void setGuessRelated(ArrayList<Integer> guessRelated) {
//		this.guessRelated = guessRelated;
//	}
//
//	public ArrayList<Integer> getActorRelated() {
//		return actorRelated;
//	}
//
//	public void setActorRelated(ArrayList<Integer> actorRelated) {
//		this.actorRelated = actorRelated;
//	}
//
//	public ArrayList<Integer> getDirectorRelated() {
//		return directorRelated;
//	}
//
//	public void setDirectorRelated(ArrayList<Integer> directorRelated) {
//		this.directorRelated = directorRelated;
//	}

	public String getEpCount() {
		return epCount;
	}

	public void setEpCount(String epCount) {
		this.epCount = epCount;
	}

	public String getEpStatus() {
		return epStatus;
	}

	public void setEpStatus(String epStatus) {
		this.epStatus = epStatus;
	}

	public ArrayList<Integer> getRelated() {
		return related;
	}

	public void setRelated(ArrayList<Integer> related) {
		this.related = related;
	}

	
}
