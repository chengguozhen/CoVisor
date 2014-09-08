package edu.princeton.cs.expr;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SwitchTime {
	private List<Float> switchTime;
	
	public SwitchTime(String fileName) {
		this.switchTime = new ArrayList<Float>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				this.switchTime.add(Float.parseFloat(line) / 300);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public float getTime() {
		return switchTime.get(OFFlowModHelper.getRandomNumber(0, switchTime.size()));
	}
}
