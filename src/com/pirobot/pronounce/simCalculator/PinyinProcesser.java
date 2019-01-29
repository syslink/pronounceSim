package com.pirobot.pronounce.simCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PinyinProcesser {
	private static PinyinProcesser pinyinProcesser = null;
	private PinyinProcesser(){}
	public static PinyinProcesser getInstance()
	{
		if(pinyinProcesser == null)
			pinyinProcesser = new PinyinProcesser();
		return pinyinProcesser;
	}
	
	// 获取两句话的发音相似度
	public double getSimiliarity(String firstSentence, String secondSentence) throws Exception
	{
		double similiarity = 0.0f;
		List<Pinyin> firstPinyinList = PronounceSimTool.convertToPinyinList(firstSentence);
		List<Pinyin> secondPinyinList = PronounceSimTool.convertToPinyinList(secondSentence);
		
		int firstLen = firstPinyinList.size();
		int secondLen = secondPinyinList.size();

		double integration = calculateTwoPinyinList(firstPinyinList, secondPinyinList);
		similiarity = (1.0f * integration / (firstLen > secondLen ? firstLen : secondLen));
		return similiarity;
	}
	public List<Double> getSimiliarityList(String firstSentence, List<String> sentenceList) throws Exception
	{
		List<Double> similiarityList = new ArrayList<Double>();
		for(String sentence : sentenceList)
		{
			double similiarity = getSimiliarity(firstSentence, sentence);
			similiarityList.add(similiarity);
		}
		return similiarityList;
	}
	private double calculateTwoPinyinList(List<Pinyin> firstNormalizedPinyinList, List<Pinyin> secondNormalizedPinyinList)
	{
		double totalIntegration = 0.0f;
		for (Pinyin firstPiniyin : firstNormalizedPinyinList) {
			double maxIntegration = 0.0f;
			Pinyin matchedPinyin = null;
			for (Pinyin secondPinyin : secondNormalizedPinyinList) {
				double integration = calculateTwoPinyin(firstPiniyin, secondPinyin);
				if(integration > maxIntegration)
				{
					maxIntegration = integration;
					matchedPinyin = secondPinyin;
				}
			}
			totalIntegration += maxIntegration;
			if(maxIntegration > 0.7)
				secondNormalizedPinyinList.remove(matchedPinyin);
		}
		return totalIntegration;
	}
	private double calculateTwoPinyin(Pinyin firstPinyin, Pinyin secondPinyin)
	{
		double shengmuValue = firstPinyin.getShengmu().equals(secondPinyin.getShengmu()) ? 0.5 : 0.0f;
		double yunmuValue = firstPinyin.getYunmu().equals(secondPinyin.getYunmu()) ? 0.5f : 0.0f;
		return shengmuValue + yunmuValue;
	}
	public static void main(String[] args)throws Exception
	{
		System.out.println(PinyinProcesser.getInstance().getSimiliarity("二", "三"));
	}
}
