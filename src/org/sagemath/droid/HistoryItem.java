package org.sagemath.droid;

import java.util.Date;
import java.util.UUID;

public class HistoryItem {
	private UUID itemId;
	private String input;
	//private String output;
	private Date itemDate;
	private boolean favorite;
	//private String type;
	
	public HistoryItem(String sageInput) {
		itemId = UUID.randomUUID();
		itemDate = new Date();
		input = sageInput;
	}
	
	public void setInput(String sageInput) {
		input = sageInput;
	}
	
	public String getInput() {
		return input;
	}
	
	public UUID getId() {
		return itemId;
	}
	
	public boolean isFavorite() {
		return favorite;
	}
	
	public void setFavorite(boolean fav) {
		favorite = fav;
	}
	
	public void setDate(Date date) {
		itemDate = date;
	}

}
