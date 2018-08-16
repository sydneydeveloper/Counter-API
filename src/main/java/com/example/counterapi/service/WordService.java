package com.example.counterapi.service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.counterapi.model.WordCount;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

@Component
public class WordService {
	public void writeResponseToCsv(PrintWriter writer, List<WordCount>  topOccurrence) {
		try {
			ColumnPositionMappingStrategy mapStrategy = new ColumnPositionMappingStrategy();
			mapStrategy.setType(WordCount.class);


			String[] columns = new String[]{"word","count"};
			mapStrategy.setColumnMapping(columns);

			StatefulBeanToCsv btcsv = new StatefulBeanToCsvBuilder(writer)
					.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
					.withMappingStrategy(mapStrategy)
					.withSeparator('|')
					.build();


			btcsv.write(topOccurrence);
		} catch (CsvDataTypeMismatchException e) {			
			e.printStackTrace();
		} catch (CsvRequiredFieldEmptyException e) {			
			e.printStackTrace();
		}catch(CsvException ex) {
			ex.printStackTrace();
		}
	}
	
	public List<WordCount> findMaxOccurence(Map<String, Integer> map, int max) {
		List<WordComparable> l = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : map.entrySet())
			l.add(new WordComparable(entry.getKey(), entry.getValue()));

		Collections.sort(l);
		List<WordCount> list = new ArrayList<>();
		for (WordComparable w : l.subList(0, max)) {
			WordCount count = new WordCount();
			count.setWord(w.wordFromFile);
			count.setCount(w.numberOfOccurrence);
			list.add(count);
		}
		return list;
	}

	class WordComparable implements Comparable<WordComparable> {
		public String wordFromFile;
		public int numberOfOccurrence;

		public WordComparable(String wordFromFile, int numberOfOccurrence) {
			super();
			this.wordFromFile = wordFromFile;
			this.numberOfOccurrence = numberOfOccurrence;
		}

		@Override
		public int compareTo(WordComparable arg0) {
			int wordCompare = Integer.compare(arg0.numberOfOccurrence, this.numberOfOccurrence);
			return wordCompare != 0 ? wordCompare : wordFromFile.compareTo(arg0.wordFromFile);
		}

		@Override
		public int hashCode() {
			final int uniqueNumber = 19;
			int wordResult = 9;
			wordResult = uniqueNumber * wordResult + numberOfOccurrence;
			wordResult = uniqueNumber * wordResult + ((wordFromFile == null) ? 0 : wordFromFile.hashCode());
			return wordResult;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WordComparable other = (WordComparable) obj;
			if (numberOfOccurrence != other.numberOfOccurrence)
				return false;
			if (wordFromFile == null) {
				if (other.wordFromFile != null)
					return false;
			} else if (!wordFromFile.equals(other.wordFromFile))
				return false;
			return true;
		}
	}

}
