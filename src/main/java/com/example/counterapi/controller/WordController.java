package com.example.counterapi.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.counterapi.model.WordCount;
import com.example.counterapi.service.WordService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping(value="/counter-api")
public class WordController {

	@Autowired
	WordService wordService;
	
	@RequestMapping(value = "/getFile", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE, method=GET)
	public @ResponseBody byte[] getFile() throws IOException {
		InputStream in = getClass().getResourceAsStream("/sample.txt");
		return IOUtils.toByteArray(in);
	}

	@RequestMapping(value="/search", consumes = "application/json",method=POST)
	public @ResponseBody String search(@RequestBody String requestBody) {	

		Map<String, Integer> wordCounts = new HashMap<>();		
		JSONObject jsonObj = new JSONObject(requestBody);
		JSONArray temp = jsonObj.getJSONArray("searchText");
		int length = temp.length();
		if (length > 0) { 
			for (int i = 0; i < length; i++) {
				wordCounts.put(temp.getString(i).toLowerCase(), 0);
			}
		}
		
		BufferedReader bufferedReader = convertToLocalFile();

		String inputLine = null;
		
		try {
			while ((inputLine = bufferedReader.readLine()) != null) {
				String[] words = inputLine.split("[ \n\t\r.,;:!?(){}]");
				for (int counter = 0; counter < words.length; counter++) {
					String key = words[counter].toLowerCase();
					if (key.length() > 0) {
						if (wordCounts.containsKey(key)) {
							wordCounts.put(key, wordCounts.get(key) + 1);						
						} 
					}
				}
			}

		} catch (IOException e) {			
			e.printStackTrace();
		}finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
		Set<Map.Entry<String, Integer>> entrySet = wordCounts.entrySet();	
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Map.Entry<String, Integer> entry : entrySet) {
			JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
			objectBuilder.add(WordUtils.capitalize(entry.getKey()), entry.getValue());
			arrayBuilder.add(objectBuilder);			
		}

		javax.json.JsonObject jo = Json.createObjectBuilder().add("counts", arrayBuilder).build();
		return jo.toString();	
	}

	
	@RequestMapping(value="/topCount/{count}",method=GET,produces = "text/csv")
	public void topCount(@PathVariable int count,HttpServletResponse response) {		

		BufferedReader bufferedReader = convertToLocalFile();

		String inputLine = null;	
		Map<String, Integer> wordCounts = new HashMap<>();
		try {
			while ((inputLine = bufferedReader.readLine()) != null) {
				String[] words = inputLine.split("[ \n\t\r.,;:!?(){}]");
				for (int counter = 0; counter < words.length; counter++) {

					String key = words[counter].toLowerCase();
					if (key.length() > 0) {
						if (!wordCounts.containsKey(key)) {
							wordCounts.put(key, 1);
						} else {
							wordCounts.put(key, wordCounts.get(key) + 1);
						}
					}
				}
			}
			List<WordCount> myTopOccurrence = wordService.findMaxOccurence(wordCounts, count);						
			wordService.writeResponseToCsv(response.getWriter(), myTopOccurrence);

		} catch (IOException e) {			
			e.printStackTrace();
		}finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
	} 
	
	private BufferedReader convertToLocalFile() {
		byte[] result =null;
		FileOutputStream fileOuputStream = null;
		File file = null;
		try {
			result = getFile();		
			file = new File("newFile.txt");
			fileOuputStream = new FileOutputStream(file); 
			fileOuputStream.write(result);
		} catch (IOException e) {			
			e.printStackTrace();
		} finally {
			try {
				fileOuputStream.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}

		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		return bufferedReader;
	}

	
	

}
