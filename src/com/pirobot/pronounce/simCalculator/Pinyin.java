package com.pirobot.pronounce.simCalculator;

public class Pinyin {
	private String shengmu;
	private String yunmu;
	private String fullPinyin;
	public Pinyin(String shengmu, String yunmu)
	{		
		this.shengmu = shengmu;
		this.yunmu = yunmu;
		this.fullPinyin = shengmu + yunmu;
	}
	public String getShengmu() {
		return shengmu;
	}
	public void setShengmu(String shengmu) {
		this.shengmu = shengmu;
	}
	public String getYunmu() {
		return yunmu;
	}
	public void setYunmu(String yunmu) {
		this.yunmu = yunmu;
	}
	public String getFullPinyin() {
		return fullPinyin;
	}
}
