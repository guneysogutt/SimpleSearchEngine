package SimpleSearchEngine;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

public class HashedDictionary<K, V> implements DictionaryInterface<K, V> {

	private TableEntry<K, V, Object>[] hashTable;
	private int numberOfEntries;
	private double locationsUsed;
	private static final int DEFAULT_SIZE = 2477;
	private static final double MAX_LOAD_FACTOR = 0.5;

	protected int collisionCount = 0;

	public HashedDictionary() throws FileNotFoundException {
		this(DEFAULT_SIZE);
	}

	@SuppressWarnings("unchecked")
	public HashedDictionary(int tableSize) throws FileNotFoundException {
		int primeSize = getNextPrime(tableSize);
		hashTable = new TableEntry[primeSize];
		numberOfEntries = 0;
		locationsUsed = 0;
	}

	public boolean isPrime(int num) {
		boolean prime = true;
		for (int i = 2; i <= num / 2; i++) {
			if ((num % i) == 0) {
				prime = false;
				break;
			}
		}
		return prime;
	}

	public int getNextPrime(int num) {
		if (num <= 1)
			return 2;
		else if(isPrime(num))
			return num;
		boolean found = false;
		while (!found)
		{
			num++;
			if (isPrime(num))
				found = true;
		}
		return num;
	}

	public V add(K key, V value, Object[] newArray, boolean check) {

		Object[] oldArray;

		if (isHashTableTooFull())
			rehash();

		int index = hashPAF(key);

		if (index >= hashTable.length) // if index > table length take the mod
			index = index % hashTable.length;

		index = doubleHash(index,key); // call hashing


		if ((hashTable[index] == null) || hashTable[index].isRemoved()) { // if key not found

			if (!check){ // check for detect whether the function called in rehash or not
				newArray = increaseFileCount(newArray,value);// increase the count of word
			}
			hashTable[index] = new TableEntry<K, V,Object>(key, value,newArray); // new entry to the table
			//increase the values
			numberOfEntries++;
			locationsUsed++;
		}
		else {// if key found

			if (!check) {
				oldArray = hashTable[index].getObjects();
				oldArray = increaseFileCount(oldArray, value);
				hashTable[index].setObjects(oldArray);
			}
			else
				hashTable[index].setObjects(newArray);
		}
		return value;
	}

	private Object[] increaseFileCount (Object[] objectArray, Object searchedObject){
		 int index = Integer.parseInt(searchedObject.toString().substring(0,3));// get file number to detect index
		 String count = objectArray[index - 1].toString().substring(8); // hold the old word count
		 int fileNameCount = Integer.parseInt(count);
		 fileNameCount += 1; // increase the count
		 objectArray[index - 1] = searchedObject + " " + fileNameCount; // change the old entry with new one
		 return objectArray;
	 }

	public boolean isHashTableTooFull() {
		double load_factor = locationsUsed / hashTable.length;
		if (load_factor >= MAX_LOAD_FACTOR)
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public void rehash() {
		TableEntry<K, V,Object>[] oldTable = hashTable;
		int oldSize = hashTable.length;
		int newSize = getNextPrime(2 * oldSize);
		this.hashTable = new TableEntry[newSize];
		numberOfEntries = 0;
		locationsUsed = 0;
		for (int index = 0; index < oldSize; index++) {
			if(!(oldTable[index] == null || oldTable[index].isRemoved())){
				add(oldTable[index].getKey(), oldTable[index].getValue(),oldTable[index].getObjects(),true);
			}
		}
	}

	private int linearProbe(int index, K key) {
		boolean found = false;
		int removedStateIndex = -1;
		int count = 0;
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn()) {
				if (key.equals(hashTable[index].getKey()))
					found = true;
				else {
					index = (index + 1) % hashTable.length;
				}
			}
			else {
				if (removedStateIndex == -1)
					removedStateIndex = index;
				index = (index + 1) % hashTable.length;
			}
		}
		return index;
	}

	private int doubleHash(int index,K key){
		boolean found = false;
		int tempIndex = index, countOpTimes=1;
		while(!found && hashTable[index] != null){
			if (hashTable[index].isIn()){
				if (key.equals(hashTable[index].getKey()))
					found = true;
				else{
					index = (tempIndex%hashTable.length + countOpTimes*(31 - tempIndex%31)) % hashTable.length;
					countOpTimes++;

				}
			}
		}
		return index;
	}

	private int locate(int index, K key) {
		boolean found = false;
		int counter = 0;
		int tempIndex = index, countOpTimes=1;
		while (!found && (hashTable[index] != null)) {
			if (hashTable[index].isIn() && key.equals(hashTable[index].getKey()))
				found = true;
			else{
				index = (index + 1) % hashTable.length; // linear probing
				// double hash
				index = (tempIndex%hashTable.length + countOpTimes*(31 - tempIndex%31)) % hashTable.length;
				countOpTimes++;
				counter++;
			}
		}
		int result = -1;
		if (found) {
			result = index;
		}
		collisionCount+= counter;
		return result;
	}

	public V getValue(K key) {
		V result = null;
		int index = hashPAF(key);
		index = locate(index, key);
		if (index != -1)
			result = hashTable[index].getValue();
		return result;
	}

	public boolean contains(K key) {
		int index = hashPAF(key);
		if (index >= hashTable.length)
			index = index % hashTable.length;
		index = locate(index,key);
		if (index != -1)
			return true;
		return false;
	}

	private int hashSSF(K key){
		int sum = 0;
		String stringKey = key.toString().toLowerCase(Locale.ENGLISH);
		for (int i = 0; i < stringKey.length(); i++) {
			sum += (stringKey.charAt(i) - 96);
		}
		return sum;
	}

	private int hashPAF(K key){
		int sum = 0;
		int z = 31;
		String stringKey = key.toString().toLowerCase(Locale.ENGLISH); // store the key as string
		for (int i = 0; i < stringKey.length(); i++) {
			//sum += (((stringKey.charAt(i) - 96)* Math.pow(z,charArr.length - 1 - i)) % hashTable.length);
			sum = (((stringKey.charAt(i) - 96)+ z*sum) % hashTable.length); // horner's rule
		}
		return sum;
	}

	public boolean isEmpty() {
		return numberOfEntries == 0;
	}

	public int getSize() {
		return numberOfEntries;
	}

	public Iterator<K> getKeyIterator() {
		return new KeyIterator();
	}

	public Iterator<V> getValueIterator() {
		return new ValueIterator();
	}

	public Iterator<Object> getObjectIterator() {
		return new ObjectIterator();
	}

	private class TableEntry<S, T, O> {
		private S key;
		private T value;
		private O[] objects;
		private boolean inTable;

		private TableEntry(S key, T value, O[] objects) {
			this.key = key;
			this.value = value;
			this.objects = objects;
			inTable = true;
		}

		private S getKey() {
			return key;
		}

		private T getValue() {
			return value;
		}

		private void setValue(T value) {
			this.value = value;
		}

		public O[] getObjects() {
			return objects;
		}

		public void setObjects(O[] objects) {
			this.objects = objects;
		}

		private boolean isRemoved() {
			return inTable == false;
		}

		private void setToRemoved() {
			inTable = false;
		}

		private void setToIn() {
			inTable = true;
		}

		private boolean isIn() {
			return inTable == true;
		}
	}

	private class KeyIterator implements Iterator<K> {
		private int currentIndex;
		private int numberLeft;

		private KeyIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		}

		public boolean hasNext() {
			return numberLeft > 0;
		}

		public K next() {
			K result = null;
			if (hasNext()) {
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved()) {
					currentIndex++;
				}
				result = hashTable[currentIndex].getKey();
				numberLeft--;
				currentIndex++;
			} else
				throw new NoSuchElementException();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class ValueIterator implements Iterator<V> {
		private int currentIndex;
		private int numberLeft;

		private ValueIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		}

		public boolean hasNext() {
			return numberLeft > 0;
		}

		public V next() {
			V result = null;
			if (hasNext()) {
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved()) {
					currentIndex++;
				}
				result = hashTable[currentIndex].getValue();
				numberLeft--;
				currentIndex++;
			} else
				throw new NoSuchElementException();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private class ObjectIterator implements Iterator<Object> {
		private int currentIndex;
		private int numberLeft;

		private ObjectIterator() {
			currentIndex = 0;
			numberLeft = numberOfEntries;
		}

		public boolean hasNext() {
			return numberLeft > 0;
		}

		public Object[] next() {
			Object[] result = null;
			if (hasNext()) {
				while ((hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved()) {
					currentIndex++;
				}
				result = hashTable[currentIndex].getObjects();
				numberLeft--;
				currentIndex++;
			} else
				throw new NoSuchElementException();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
