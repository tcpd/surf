package in.edu.ashoka.surf;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import in.edu.ashoka.surf.Dataset;
import in.edu.ashoka.surf.Row;

class Node {
	String name1;
	String name2;
	double cosinesimilarity;
	int index;

	public Node(String name1, String name2, double cosinesimilarity, int index) {
		this.name1 = name1;
		this.name2 = name2;
		this.cosinesimilarity = cosinesimilarity;
		this.index = index;
	}

	public String toString() {
		return index + " " + name1 + " " + name2 + " " + cosinesimilarity;
	}
}

class obj {

	HashMap<Character, Integer> hash;
	Set<Character> char_set;
	double length;
	String word;

	public HashMap<Character, Integer> getHash() {
		return hash;
	}

	public void setHash(HashMap<Character, Integer> hash) {
		this.hash = hash;
	}

	public Set<Character> getChar_set() {
		return char_set;
	}

	public void setChar_set(Set<Character> char_set) {
		this.char_set = char_set;
	}

	public double getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public obj(HashMap<Character, Integer> hash, Set<Character> char_set, double length, String word) {
		this.hash = hash;
		this.char_set = char_set;
		this.length = length;
		this.word = word;
	}

}

public class CosineFunc {

	public static HashMap<Character, Integer> Count(String inputString) {
		HashMap<Character, Integer> charCountMap = new HashMap<Character, Integer>();

		char[] strArray = inputString.toCharArray();

		for (char c : strArray) {
			if (charCountMap.containsKey(c)) {

				charCountMap.put(c, charCountMap.get(c) + 1);
			} else {

				charCountMap.put(c, 1);
			}
		}

		return charCountMap;

	}

	public static obj word2vec(String word) {
		HashMap<Character, Integer> count_characters = Count(word);
//		System.out.println(count_characters);
		Set<Character> set_characters = count_characters.keySet();
//		System.out.println(set_characters);

		double length = 0;
		int key = 0;

		for (Integer in : count_characters.values()) {
			key += (in * in);
		}
		length = Math.sqrt(key);
//		System.out.println(length);

		return new obj(count_characters, set_characters, length, word);

	}

	public static double cosine_similarity(obj vector1, obj vector2) {
		Set<Character> common_characters = new HashSet<Character>(vector1.getChar_set()); // use the copy constructor
		common_characters.retainAll(vector2.getChar_set());
//		System.out.println("Intersection = " + common_characters);

		int product_summation = 0;
		for (Character ch : common_characters) {
			product_summation += vector1.getHash().get(ch) * vector2.getHash().get(ch);
		}
//		System.out.println("product_summation = " + product_summation);

		double length = vector1.length * vector2.length;
//		System.out.println("length = " + length);

		if (length == 0) {
			return 0;
		} else {
			return product_summation / length;
		}

	}

	public List<Set<String>> assign_similarity(Collection<Row> filteredRows, String fieldName,double val) {

//		HashMap<String, List<String>> map = new HashMap<>();
		ArrayList<String> names = new ArrayList<>();
		List<Set<String>> resultx = new ArrayList<Set<String>>();
		filteredRows.forEach(r -> names.add(r.get(fieldName)));
//		filteredRows.forEach(r -> map.put(r.get(fieldName), new ArrayList<>()));
//		ArrayList<Node> similar = new ArrayList<>();
		boolean visited[] = new boolean[names.size()];

//		System.out.println("Map = " + map);

//		for (int i = 0; i < names.size(); i++) {
//			System.out.println(i + " " + names.get(i));
//		}
		ArrayList<obj> aa = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			aa.add(word2vec(names.get(i)));
		}
		
		for (int i = 0; i < names.size(); i++) {
			String one = names.get(i);
			int task = 0;
			Set<String> curr = null;
			if (visited[i] == false) {
				task = 1;
				curr = new LinkedHashSet<String>();
				visited[i] = true;
				curr.add(one);
			}
//			obj v1 = word2vec(one);
			for (int j = i + 1; j < names.size(); j++) {
				String two = names.get(j);
//				obj v2 = word2vec(two);
				double cosine_val = cosine_similarity(aa.get(i),aa.get(j));
//				Node nn = new Node(one, two, cosine_similarity(word2vec(one), word2vec(two)), i);
//				similar.add(nn);

				if (task == 1) {
//					System.out.println("hello");
					if (cosine_val >= val && visited[j] == false) {
//						System.out.println("adi");
						curr.add(two);
						visited[j] = true;
					}
				}
			}
			if (task == 1) {
				resultx.add(curr);
			}
		}
//		int l = 0;
		
		return resultx;
				
//		System.out.println("gggggggggggggggggggggg");
//		for (int i = 0; i < resultx.size(); i++) {
//			l += resultx.get(i).size();
//			System.out.println(resultx.get(i));
//		}
		
//		System.out.println(l);

//		for (String name : map.keySet()) {
//			List<String> list = map.get(name);
//			list.add(name);
//			map.put(name, list);
//		}

//		for (int i = 0; i < similar.size(); i++) {
//			Node node = similar.get(i);
//			if (node.cosinesimilarity > 1.0) {
//				List<String> set1 = map.get(node.name1);
//				set1.add(node.name2);
//				map.put(node.name1, set1);
//
//				List<String> set2 = map.get(node.name2);
//				set2.add(node.name1);
//				map.put(node.name2, set2);
//			}
//		}
//		System.out.println("Map = " + map);
//
//		Collection<List<String>> result = map.values();
//		
//		System.out.println("result = " + result);
//		System.out.println(result.size());
//		
//		int le = 0;
//		for(List<String> aa : result) {
//			System.out.println(aa);
//			le += aa.size();
//		}
//		System.out.println(le);

//		System.out.println(similar);

//		for (int i = 0; i < similar.size(); i++) {
//			System.out.println(similar.get(i));
//		}

	}

//	public static void main(String[] args) throws IOException {
//		// TODO Auto-generated method stub
////		String s1 = "adity a";
////		String s2 = "aditya x";
////
////		System.out.println(cosine_similarity(word2vec(s1), word2vec(s2)));
//
//		Dataset dataset = Dataset.getDataset(path);
//		String fieldName = "Candidate";
//		Collection<Row> filteredRows = dataset.getRows().stream().collect(toList());
//
//		assign_similarity(filteredRows, fieldName);
//
//	}

}