package org.easyaccess;

import java.util.ArrayList;

import android.app.Application;
import android.speech.tts.TextToSpeech;

public class TTS extends Application{
	/**
	* TTS class consists of methods used to give a text to speech feedback to the user.
	*/
	private static TextToSpeech tts = null;
	
    public static TextToSpeech getObject() {
    	return tts;
	}
	  
	public static void setObject(TextToSpeech obj) {
	    tts = obj;
	}
	  
	/** 
	* Reads out the string passed as parameter using the tts object.
	* @param message The string to be read out.
	*/
	public static void speak(String message) {
		if(TTS.tts != null) {
			try {
				TTS.tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 
	* Stops the TTS playback
	*/
	public static void stop() {
		if(TTS.tts != null) {
			TTS.tts.stop();
		}
	}
	
	/** 
	* Determines whether the TTS object is currently reading a text.
	* @return Returns true if the TTS object is currently reading a text, else, returns false.
	*/
	public static boolean isSpeaking() {
		if(tts != null) {
			return TTS.tts.isSpeaking();
		}
		return false;
	}
	  
	/**
	* Reads a number from left to right, one digit at a time.
	* @param number The string that is broken down into characters and read out, one
	* character at a time.
	*/
	 public static String readNumber(String number) {
		//call a function to split the number into digits and return an ArrayList
		ArrayList<String> digits = splitNumber(number);
		String listDigits = "";
		for (String digit : digits)
		{
		    listDigits += " " + digit;
		}
		return listDigits;
	}
		
	/**
	* Splits a number into digits.
	* @param number The string that is to be split into characters.
	* @return an ArrayList consisting of all the characters that form the 
	* string, as its elements.
	*/
	public static ArrayList<String> splitNumber(String number) {
		ArrayList<String> digits = new ArrayList<String>();
		for(int i=0; i<number.length(); i++) {
			digits.add(number.substring(i, i+1));
		}
		return digits;
	}
}
