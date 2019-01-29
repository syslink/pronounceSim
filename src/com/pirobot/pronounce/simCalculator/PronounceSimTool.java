package com.pirobot.pronounce.simCalculator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;

public class PronounceSimTool {
	// 具有相似发音的声母、韵母对应表
	private static Map<String, String> similiarToneMap = new HashMap<String, String>(){
	{
		put("zh", "z"); put("ch", "c"); put("sh", "s"); 
		put("ang", "an");put("eng", "en");put("ing", "in");
		put("l", "n");put("h", "f");put("p", "b");
	}};
	
	// 汉字unicode编码到拼音的映射
	private static Map<String, String> unicodePinyinMap = new HashMap<String, String>();
	
	// 做缓存，加快运算速度
	private static Map<String, List<String>> pinyinListMap = new HashMap<String, List<String>>();
	
	// 此函数用于初始化汉字unicode到拼音的映射，必须在一开始便调用
	public static void init() throws Exception
	{
		InputStream input = PronounceSimTool.class.getClassLoader().getResourceAsStream("resource/unicode_to_hanyu_pinyin.txt");
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(input));
		String line = "";
		while((line = bufReader.readLine()) != null)
		{
			String[] lineInfo = line.split(" ");
			if(lineInfo.length == 2)
			{
				unicodePinyinMap.put(lineInfo[0], lineInfo[1]);
			}
		}
		bufReader.close();
	}
	
	// 判断oriStr的发音中是否包含containedStr的发音，如包含，则返回起始为止，否则返回-1
	public static int contain(String oriStr, String containedStr) throws Exception
	{
		int matchedPos = -1;
		if(containedStr.length() > oriStr.length())
			return matchedPos;
		if(containedStr.length() == oriStr.length() && oriStr.equals(containedStr))
			return 0;
		
		List<String> oriPinyinList = getPinyinList(oriStr);
		List<String> containedPinyinList = getPinyinList(containedStr);
		
		int len = oriPinyinList.size();
		int comparedLen = containedPinyinList.size();
		for(int i = 0; i < len; i++)
		{
			int curPos = i;
			int j = 0;
			for(; j < comparedLen; j++)
			{
				String oriPinyin = oriPinyinList.get(curPos);
				if(!oriPinyin.equals(containedPinyinList.get(j)))
					break;
				curPos++;				
			}
			if(j == comparedLen)
			{
				matchedPos = i;
				break;
			}
		}
		return matchedPos;
	}
	
	// 判断oriStr的发音中是否包含containedStr的发音（发音相似度大于threshold即可），如包含，则返回起始为止，否则返回-1
	public static int robustContain(String oriStr, String containedStr, double threshold) throws Exception
	{
		oriStr = clearNotChinese(oriStr);
		containedStr = clearNotChinese(containedStr);
		int matchedPos = -1;
		if(containedStr.length() > oriStr.length())
			return matchedPos;
		if(containedStr.length() == oriStr.length() && oriStr.equals(containedStr))
			return 0;
				
		int len = oriStr.length();
		int comparedLen = containedStr.length();
		for(int i = 0; i < len; i++)
		{
			if(i + comparedLen <= len)
			{
				String subStr = oriStr.substring(i, i + comparedLen);
				double similiarity = PinyinProcesser.getInstance().getSimiliarity(subStr, containedStr);
				if(Math.abs(similiarity - threshold) < 0.000001)
				{
					matchedPos = i;
					break;
				}
			}
		}
		return matchedPos;
	}
	// 获取结构化的拼音列表
	public static List<Pinyin> convertToPinyinList(String chSentence) throws Exception
	{
		List<Pinyin> pinyins = new ArrayList<Pinyin>();
		List<String> pinyinList = getPinyinList(chSentence);
		for(String pinyin : pinyinList)
		{
			pinyins.add(new Pinyin(pinyin.substring(0, 1), pinyin.substring(1)));
		}
		return pinyins;
	}
	// 获取中文语句对应的拼音列表（结果中已将相似的发音按similiarToneMap中的设置进行归一化处理）
	private static List<String> getPinyinList(String chSentence) throws Exception
	{
		if(pinyinListMap.containsKey(chSentence))
			return pinyinListMap.get(chSentence);
		List<String> pinyinList = new ArrayList<String>();  
        char[] nameChar = chSentence.toCharArray();  
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();  
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
        for (int i = 0; i < nameChar.length; i++) {  
            if (nameChar[i] > 128) { 
            	String unicode = Integer.toHexString(nameChar[i]).toUpperCase();
            	if(!unicodePinyinMap.containsKey(unicode))
            		continue;
            	String[] pinyinArr = unicodePinyinMap.get(unicode).split(",");
            	if(pinyinArr == null)
            		continue;
                String pinyin = pinyinArr[0].substring(0, pinyinArr[0].length() - 1); 
                Iterator<Entry<String, String>> iter = similiarToneMap.entrySet().iterator();
        		while(iter.hasNext())
        		{
        			Map.Entry<String, String> entry = iter.next();
        			if(pinyin.contains(entry.getKey()))
        			{
        				pinyin = pinyin.replace(entry.getKey(), entry.getValue());
        			}
        		}
        		pinyinList.add(pinyin);
            }else{  
            	pinyinList.add(nameChar[i] + "");  
            }  
        }  
        pinyinListMap.put(chSentence, pinyinList);
        return pinyinList; 
	}

	private static String clearNotChinese(String buff){
		String tmpString =buff.replaceAll("(?i)[^a-zA-Z0-9\u4E00-\u9FA5]", "");//去掉所有中英文符号
		return tmpString;
	}
	
	public static void main(String[] args) throws Exception
	{
		PronounceSimTool.init();
		System.out.println(PronounceSimTool.contain("我是小白泥壕", "你好"));
	}
}

