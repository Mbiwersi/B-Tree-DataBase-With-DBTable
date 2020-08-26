import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

public class DBTable {
	
	 private RandomAccessFile rows; //the file that stores the rows in the table
	 private long free; //head of the free list space for rows
	 private int numOtherFields;
	 private int otherFieldLengths[];
	 private BTree tree;
	 private int smallestKey;
	 private class Row {
	 private int keyField;
	 private char otherFields[][];
	 /*
	 Each row consists of unique key and one or more character array fields.
	 Each character array field is a fixed length field (for example 10
	 characters).
	 Each field can have a different length.
	 Fields are padded with null characters so a field with a length of
	 of x characters always uses space for x characters.
	 */
	 	private Row(int key, char[][] fields) {
	 		this.keyField = key;
	 		this.otherFields = fields;
	 	}
	 	
	 	private Row(long addr) throws IOException {
	 		//constructor for picking up a node.
	 		rows.seek(addr);
	 		this.keyField = rows.readInt();
	 		char[][] fields = new char[numOtherFields][];
	 		for(int i=0; i<numOtherFields;i++) {
	 			String result = "";
	 			for(int j=0; j<otherFieldLengths[i];j++) {
	 				result+= rows.readChar();
	 			}
	 			fields[i] = result.toCharArray();
	 		}
	 		this.otherFields = fields;
	 	}
	 	
	 	private void writeRow(long addr) throws IOException {
	 		rows.seek(addr);
	 		rows.writeInt(this.keyField);
	 		for(int i=0; i<this.otherFields.length;i++) {
	 			for(int j=0; j<this.otherFields[i].length;j++) {
	 				rows.writeChar(this.otherFields[i][j]);
	 			}
	 		}
	 	}
	 }
	 public DBTable(String filename, int fL[], int bsize ) throws IOException {
	 /*
	 Use this constructor to create a new DBTable.
	 filename is the name of the file used to store the table
	 fL is the lengths of the otherFields
	 fL.length indicates how many other fields are part of the row
	 bsize is the block size. It is used to calculate the order of the B+Tree
	 A B+Tree must be created for the key field in the table

	 If a file with name filename exists, the file should be deleted before the
	 new file is created.
	 */
		 this.rows = new RandomAccessFile(filename, "rw");
		 this.free = 0;
		 this.numOtherFields = fL.length;
		 this.otherFieldLengths = fL;
		 this.tree = new BTree("B+Tree",bsize);
		 this.smallestKey = 999999999;
		 rows.seek(0);
		 rows.writeInt(this.smallestKey);
		 rows.writeInt(this.numOtherFields);
		 for(int i=0; i<this.numOtherFields;i++) {
			 rows.writeInt(this.otherFieldLengths[i]);
		 }
		 rows.writeLong(this.free);
		 
	 }
	 public DBTable(String filename) throws IOException {
	 //Use this constructor to open an existing DBTable
		 this.rows = new RandomAccessFile(filename, "rw");
		 this.rows.seek(0);
		 this.smallestKey = rows.readInt();
		 this.numOtherFields = rows.readInt();
		 int[] fl  = new int[this.numOtherFields];
		 this.otherFieldLengths = fl;
		 this.tree = new BTree("B+Tree");
		 for(int i =0; i<fl.length;i++) {
			 fl[i] = rows.readInt();
		 }
		 this.free = rows.readLong();
	 }
	 
	 private long nextFree() throws IOException {
		 if(this.free==0) {
			 return rows.length();
		 }
		 else {
			 return this.free;
		 }
	 }
	 
	 public BTree getTree() {
		 return this.tree;
	 }
	 
	 public boolean insert(int key, char fields[][]) throws IOException {
	 //PRE: the length of each row is fields matches the expected length
	 //PRE: key!=0
	 /*
	 If a row with the key is not in the table, the row is added and the method
	 returns true otherwise the row is not added and the method returns false.
	 The method must use the B+tree to determine if a row with the key exists.
	 If the row is added the key is also added into the B+tree.
	 */
		 if(!tree.insert(key, this.nextFree())) {
			 return false;
		 }
		 Row row = new Row(key,fields);
		 row.writeRow(this.nextFree());
		 if(key<this.smallestKey) {
			 this.smallestKey= key;
			 rows.seek(0);
			 rows.writeInt(this.smallestKey);
		 }
		 return true;
		 
		 
	 } 
	
	 public LinkedList<String> search(int key) throws IOException {
	 /*
	 If a row with the key is found in the table return a list of the other fields in
	 the row.
	 The string values in the list should not include the null characters.
	 If a row with the key is not found return an empty list
	 The method must use the equality search in B+Tree
	 */
		 LinkedList<String> result = new LinkedList<String>();
		 long addr  = this.tree.search(key);
		 if(addr!=0) {
			 Row current = new Row(addr);
			 //System.out.println("current.key = "+current.keyField);
			 for(int i=0; i<current.otherFields.length;i++) {
				 String field = "";
				 for(int j =0; j<current.otherFields[i].length;j++) {
					 if(current.otherFields[i][j]!=0) {
						 field+=current.otherFields[i][j];
					 }
				 }
				 result.addLast(field);
	
			 }
			 return result;
		 }
		 else {
			 return result;
		 }
	 }
	 public LinkedList<LinkedList<String>> rangeSearch(int low, int high) throws IOException {
	 //PRE: low <= high
	 /*
	 For each row with a key that is in the range low to high inclusive a list
	 of the fields (including the key) in the row is added to the list
	returned by the call.
	 If there are no rows with a key in the range return an empty list
	 The method must use the range search in B+Tree
	 */
		 LinkedList<LinkedList<String>> result = new LinkedList<LinkedList<String>>();
		 LinkedList<Integer> leafKeys = this.tree.getLeafKeys(low);
		 //System.out.println("leafKeys.size()  ="+leafKeys.size());
		 LinkedList<Long> leafAddresses = this.tree.rangeSearch(low, high);
		 //System.out.println("leafAddresses.size() = "+leafAddresses.size());
		 for(int i=0; i<leafKeys.size();i++) {
			 //System.out.println("Key = "+leafKeys.get(i)+", Addresses = "+leafAddresses.get(i));
		 }
		 int index  = 0;
		 int currentKey = leafKeys.get(index);
		 while(index<leafKeys.size()) {
			 if(currentKey<=high&&currentKey>=low) {
				 Row currentRow = new Row(leafAddresses.get(index));
				 LinkedList<String> fields = new LinkedList<String>();
				 for(int i=0; i<currentRow.otherFields.length;i++) {
					 String field = "";
					 for(int j=0; j<currentRow.otherFields[i].length;j++) {
						 if(currentRow.otherFields[i][j]!=0) {
							 field+=currentRow.otherFields[i][j];
						 }
					 }
					 fields.addLast(field);
				 }
				 fields.addFirst(Integer.toString(currentKey));
				 result.add(fields);
			 }
			 index++;
			 if(index<leafKeys.size()) {
				 currentKey = leafKeys.get(index);
			 }
		 }
		 return result;
	 }
	 public void print() throws IOException {
	 //Print the rows to standard output is ascending order (based on the keys)
	 //One row per line
		 this.rows.seek(0);
		 System.out.println("|SmallestKey = "+this.rows.readInt()+"|");
		 System.out.println("|numOtherFields = "+this.rows.readInt()+"|");
		 for(int i=0; i<this.numOtherFields;i++) {
			 System.out.println("|Length "+(i+1)+" = "+this.rows.readInt()+"|");
		 }
		 System.out.println("|Free = "+rows.readLong()+"|");
		 LinkedList<Long> leafs = tree.getLeafAddresses(smallestKey);
		 for(int i=0; i<leafs.size();i++) {
			 Row current = new Row(leafs.get(i));
			 System.out.print("|Addr = "+leafs.get(i)+", Key = "+current.keyField+", ");
			 for(int j =0; j<current.otherFields.length;j++) {
				 for(int k =0; k<current.otherFields[j].length;k++) {
					 if(current.otherFields[j][k]!=0) {
						 System.out.print(current.otherFields[j][k]);
					 }
				 }
				 System.out.print(" ");
			 }
			 System.out.print("|");
			 System.out.println();
		 }
	 }
	 public void close() throws IOException {
	 //close the DBTable. The table should not be used after it is closed
		 rows.close();
		 this.tree.close();
	 } 

}
