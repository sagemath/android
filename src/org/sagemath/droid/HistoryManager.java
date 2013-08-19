package org.sagemath.droid;

import java.util.ArrayList;
import java.util.UUID;

public class HistoryManager {
	
	private ArrayList<HistoryItem> HistoryItems;
	
	public HistoryItem getItem(UUID itemId) {
		for (HistoryItem h: HistoryItems) {
			if (h.getId().equals(itemId)){
				return h;
			}
		}
		return null;
	}
	
	public void addHistory(HistoryItem history) {
		HistoryItems.add(history);
	}
	
	public void deleteHistory(HistoryItem history) {
		HistoryItems.remove(history);
	}
	
	public ArrayList<HistoryItem> getHistory(){
		return HistoryItems;
	}

}
