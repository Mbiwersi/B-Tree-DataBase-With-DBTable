/*Author: Michael Biwersi
 * 
 * B+tree DataBase with DBTable implemented and updated through a
 * RandomAccessFile when given a blockSize as an int.
 * PRE condition: No Duplicates
 * 
 */
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.Stack;

public class BTree {
	private RandomAccessFile f;
	private int order;
	private int blockSize;
	private long root;
	private long free;
	private BTreeNode rootNode;
	
	private class BTreeNode {
		private int count;
		private int keys[];
		private long children[];
		private long address; //the address of the node in the file
		
		/*Inner class for nodes of the tree which hold the address of the node in the file given as a long
		 * the keys to pass to the DBTable
		 */
		private BTreeNode(int count, long nodeAddr) {
			//creating a new node
			this.count = count;
			this.keys = new int[order-1];
			this.children = new long[order];
			this.address = nodeAddr;
		}
		
		//reading a node from the tree into memory
		private BTreeNode(long addr) throws IOException {
			f.seek(addr);
			this.count = f.readInt();
			this.keys = new int[order-1];
			this.children = new long[order];
			this.address = addr;
			for(int i=0; i<order-1;i++) {
				keys[i] = f.readInt();
			}
			for(int i=0; i<order;i++) {
				children[i] = f.readLong();
			}
		}
		
		//Updating the contents of a node in the file after being edited in memory
		private void writeNode(long addr) throws IOException {
			f.seek(addr);
			f.writeInt(this.count);
			for(int i=0; i<this.keys.length;i++) {
				f.writeInt(keys[i]);
			}
			for(int i = 0; i<this.children.length; i++) {
				f.writeLong(children[i]);
			}
		}
		
		//Inserting a value into the bTree after finding the node to which it belongs
		private void nodeInsert(int key, long addr) throws IOException {
			int index = 0;
			int currentKey = this.keys[index];
			if(this.count == 0) {
				this.keys[0] = key;
				this.children[0] = addr;
			}
			else {
				while(index<Math.abs(this.count)) {
					currentKey = this.keys[index];
					if(currentKey>key) {
						//swapping keys
						this.keys[index] = key;
						key = currentKey;
						//swapping addresses
						long tempAddr = this.children[index];
						this.children[index] = addr;
						addr = tempAddr;
						
					}
					index++;
				}
				this.keys[index] = key;
				this.children[index] = addr;
			}
			if(this.count>0) {
				this.count++;
			}
			else {
				this.count--;
			}
		}
		
		private void nonleafNodeInsert(int key, long addr) throws IOException {
			int index = 0;
			int passedKey = key;
			long passedAddr = addr;
			int currentKey = this.keys[index];
			if(this.count == 0) {
				this.keys[0] = key;
				this.children[0] = addr;
			}
			else {
				while(index<Math.abs(this.count)) {
					currentKey = this.keys[index];
					if(currentKey>passedKey) {
						//swapping keys
						this.keys[index] = passedKey;
						passedKey = currentKey;
						
						
					}
					//next key to compare
					index++;					
				}
				
				this.keys[index] = passedKey;
				index=0;
				long tempaddr= 0;
				boolean insertered = false;
				BTreeNode current = new BTreeNode(addr);
				//inserting the addr in the correct spot
				while(index<Math.abs(this.count)+2&&insertered==false) {
					currentKey = this.keys[index];
					if(currentKey == current.keys[0]) {
						tempaddr = this.children[index+1];
						this.children[index+1] = passedAddr;
						insertered=true;
						index++;
					}
					index++;
				}
				passedAddr = tempaddr;
				//moving down the other children
				while(index<Math.abs(this.count)+2) {
					tempaddr = this.children[index];
					this.children[index] = passedAddr;
					passedAddr = tempaddr;
					index++;
				}
					
			}
			if(this.count>0) {
				this.count++;
			}
			else {
				this.count--;
			}
		}
		
		//Checking if a node in the bTree is a leaf
		private boolean isLeaf() {
			if(this.count<0) {
				return true;
			}
			else {
				return false;
			}
		}
		
	}
	public BTree(String filename, int bsize) throws IOException {
	 //bsize is the block size. This value is used to calculate the order
	 //of the B+Tree
	 //all B+Tree nodes will use bsize bytes
	 //makes a new B+tree
		this.f = new RandomAccessFile(filename, "rw");
		this.order = bsize/12;
		this.blockSize = bsize;
		this.root = 20;
		this.free = 0;
		this.rootNode = null;
		f.seek(0);
		f.writeLong(this.root);
		f.writeLong(this.free);
		f.writeInt(this.blockSize);
		f.writeLong(0);
	}
	public BTree(String filename) throws IOException {
	 //open an existing B+Tree
		 this.f = new RandomAccessFile(filename, "rw");
		 f.seek(0);
		 this.root = f.readLong();
		 this.free = f.readLong();
		 this.blockSize = f.readInt();
		 this.order = this.blockSize/12;
		 f.seek(this.root);
		 if(f.readLong()!=0) {
			 this.rootNode = new BTreeNode(this.root);
		 }
		 else {
			 this.rootNode = null;
		 }
	}
	

	
	 private Stack<BTreeNode> searchPath(int key) throws IOException{
		//Search Path
		 Stack<BTreeNode> path = new Stack<BTreeNode>();
		 BTreeNode current = new BTreeNode(this.root);
		 path.add(current);
		 while(current.count>0) {
			 if(current.keys[0]>key) {
				 //if key is less then the first key in node
				 if(!current.isLeaf()) {
					 current = new BTreeNode(current.children[0]);
					 path.add(current);
				 }
			 }
			 else if(current.keys[Math.abs(current.count-1)]<=key) {
				 //if key is greater then the last key in the node
				 if(!current.isLeaf()) {
					 current = new BTreeNode(current.children[Math.abs(current.count)]);
					 path.add(current);
				 }
			 }
			 else {
				 //find the next node between the keys
				 for(int i=0; i<Math.abs(current.count)-1;i++) {
					 //System.out.println("i = "+i);
					 if(current.keys[i]<=key&&current.keys[i+1]>key) {
						 if(!current.isLeaf()) {
							 current = new BTreeNode(current.children[i+1]);
							 path.add(current);
						 }
					 }
				 }
			 }
			 
		 }
		 return path;
	 }
	
	 public boolean insert(int key, long addr) throws IOException {
	 /*
	  * PRE: Key!=0
	 If key is not a duplicate add, key to the B+tree
	 addr (in DBTable) is the address of the row that contains the key
	 return true if the key is added
	 return false if the key is a duplicate
	 */
		 if(this.rootNode==null) {
			 this.rootNode = new BTreeNode(0,this.root);
			 this.rootNode.nodeInsert(key, addr);
			 this.rootNode.writeNode(this.rootNode.address);
			 return true;
		 }
		 else {
			 boolean split = false;
			 Stack<BTreeNode> path = this.searchPath(key);
			 BTreeNode node = path.pop();
			 for(int i =0; i<Math.abs(node.count);i++) {
				 if(node.keys[i]==key) {
					 return false;
				 }
			 }
			 if(Math.abs(node.count)<this.order-1) {
				 //insert key and addr into leaf
				 node.nodeInsert(key, addr);
				 node.writeNode(node.address);
			 }
			 else {
				 //split leaf
				 BTreeNode newNode = new BTreeNode(0,this.nextFree());//new node thats empty
				 newNode.children[newNode.children.length-1] = node.children[node.children.length-1];//makes left node point to the prev next leaf
				 //creating temp arrays to split from
				 int[] splitKeys = new int[this.order];
				 int index=0;
				 long[] splitAddresses = new long[this.order];
				 for(int i = 0; i<splitKeys.length-1;i++) {
					 //filling arrays with the contents of leaf
					 splitKeys[i] = node.keys[i];
					 splitAddresses[i] = node.children[i];
				 }
				 //insert key and address into split keys/addresses
				 index = 0;
				 int currentKey = splitKeys[index];
				 while(currentKey>0||currentKey<0) {
					 if(currentKey>key) {
						//swapping keys
						splitKeys[index] = key;
						key = currentKey;
						//swapping addresses
						long tempAddr = splitAddresses[index];
						splitAddresses[index] = addr;
						addr = tempAddr;
							
					 }
					//next key to compare
					index++;
					currentKey = splitKeys[index];
				}
				splitKeys[index] = key;
				splitAddresses[index] = addr;
				
				node.count = 0;
				for(int i=0; i<(this.order/2);i++) {
					 //inserting the smaller amount of keys into leaf
					 node.nodeInsert(splitKeys[i], splitAddresses[i]);
				}
				 node.children[node.children.length-1] = newNode.address;//connecting old node to the newNode
				 node.writeNode(node.address);//writing node after changes
				 int val = splitKeys[this.order/2];
				 index = 0;
				 for(int i =this.order/2; i<splitKeys.length;i++) {
					//fill the newNode
					newNode.nodeInsert(splitKeys[i], splitAddresses[i]);
				 }
				 newNode.writeNode(newNode.address);
				 long loc = newNode.address;
				 split = true;
				 while(!path.empty()&&split) {
					
					 node = path.pop();
					 if(Math.abs(node.count)<this.order-1) {
						 if(node.isLeaf()) {
							 node.nodeInsert(val, loc);
						 }
						 else {
							 node.nonleafNodeInsert(val, loc);
						 }
						 node.writeNode(node.address);
						 split = false;
					 }
					 else {
		
						 newNode = new BTreeNode(0,this.nextFree());//new node thats empty
						 splitKeys = new int[this.order];
						 index=0;
						 splitAddresses = new long[this.order+1];
						 for(int i = 0; i<splitKeys.length-1;i++) {
							 //filling arrays with the contents of leaf
							 splitKeys[i] = node.keys[i];
							 splitAddresses[i] = node.children[i];
						 }
						 splitAddresses[splitAddresses.length-2] = node.children[Math.abs(node.count)];
						 //insert key and address into split keys/addresses
						 index = 0;
						 currentKey = splitKeys[index];
						 if(val>splitKeys[splitKeys.length-2]) {
							 splitKeys[splitKeys.length-1] = val;
							 splitAddresses[splitAddresses.length-1] = loc;
							 
						 }
						 else {
							 while((currentKey>0||currentKey<0)) {
								 if(currentKey>val) {
									//swapping keys
									splitKeys[index] = val;
									val = currentKey;
									//swapping addresses
									long tempAddr = splitAddresses[index+1];
									splitAddresses[index+1] = loc;
									loc = tempAddr;
										
								 }
								//next key to compare
								index++;
								currentKey = splitKeys[index];
							}
							splitKeys[index] = val;
							splitAddresses[index+1] = loc;
						 }
						node.count = 0;
						 for(int i=0; i<(this.order/2);i++) {
							 //inserting the smaller amount of keys into leaf
							 node.nodeInsert(splitKeys[i], splitAddresses[i]);
						}
						 node.count*=-1;
						 node.writeNode(node.address);//writing node after changes
						 val = splitKeys[this.order/2];
						 index = 0;
						 for(int i =(this.order/2); i<splitKeys.length;i++) {
							//fill the newNode
							newNode.nodeInsert(splitKeys[i], splitAddresses[i]);
						 }
						 newNode.children[Math.abs(newNode.count)] = splitAddresses[splitAddresses.length-1];
						 newNode.count*=-1;
						 newNode.writeNode(newNode.address);
						 loc = newNode.address;
						 split = true;

					 }
				 }
				 
				 if(split) {//the root of was split
					 newNode = new BTreeNode(0,this.nextFree());
					 newNode.nodeInsert(val, this.root);
					 node = new BTreeNode(loc);
					 node.writeNode(node.address);
					 newNode.children[Math.abs(newNode.count)] = loc;
					 this.root = newNode.address;
					 newNode.count = Math.abs(newNode.count);
					 newNode.writeNode(newNode.address);
				 }
				 
			 }
			 
		 }
		 return true;
	 }
	 
	 
	 private long nextFree() throws IOException {
		 if (free==0) {
			 return f.length();
		 }
		 return this.free;
	 }
	 
	 
	 public long search(int k) throws IOException{
	 /*
	 This is an equality search
	 If the key is found return the address of the row with the key
	 otherwise return 0
	 */
		 Stack<BTreeNode> path = this.searchPath(k);
		 BTreeNode node = path.pop();
		 for(int i=0; i<Math.abs(node.count);i++) {
			 if(node.keys[i]==k) {
				 return node.children[i];
			 }
		 }
		 return 0;
		 
	 }
	 
	 public LinkedList<Long> rangeSearch(int low, int high) throws IOException{
	 //PRE: low <= high
	 /*
	
	 return a list of row addresses for all keys in the range low to high inclusive
	 return an empty list when no keys are in the range
	 */
		 Stack<BTreeNode> path = this.searchPath(low);
		 LinkedList<Long> result = new LinkedList<Long>();
		 BTreeNode current = path.pop();
		 boolean isLeaf = true;
		 while(isLeaf) {
			 for(int i =0; i< Math.abs(current.count);i++) {
				 result.addLast(current.children[i]);
			 }
			 if(current.children[current.children.length-1]!=0) {
				 current = new BTreeNode(current.children[current.children.length-1]);
			 }
			 else {
				 isLeaf = false;
			 }
		 }
		 return result;
	 }
	 
	 public void print() throws IOException {
	 //print the B+Tree to standard output
	 //print one node per line
	 //This method can be helpful for debugging
		 System.out.print("\nOrder "+this.order+" B+Tree\n");
		 System.out.print("|Root = "+this.root+"|\n");
		 System.out.print("|Free = "+this.free+"|\n");
		 System.out.print("|Block Size = "+this.blockSize+"|");
		 this.printHelper(this.root);
		 
	 }
	 
	 /* prints out to console the Address of the Node in the file as a long, the number of 
	  * keys in the node (if negative node a leaf), keys, the address of the keys in the 
	  * DBTable and the address of the next Node in the current level of the BTree.
	  */
	 private void printHelper(Long addr) throws IOException {
		 BTreeNode node = new BTreeNode(addr);
		 if(node.count<0) {
			 System.out.print("\n|Addr = "+node.address+", "+node.count);
			 System.out.print(", [");
			 for(int i=0; i<Math.abs(node.count);i++) {
				 System.out.print(node.keys[i]+" ");
			 }
			 System.out.print("]");
			 System.out.print(", [");
			 for(int i=0; i<Math.abs(node.count);i++) {
				 System.out.print(node.children[i]+" ");
			 }
			 System.out.print("]");
			 if(node.count<0) {
				 System.out.print(" next Leaf Addr = "+node.children[node.children.length-1]);
			 }
			 else {
				 System.out.print(" Not leaf");
			 }
			 System.out.print("|");
			 
			 
			 
		 }
		 else {
			 System.out.print("\n|Addr = "+node.address+", "+node.count);
			 System.out.print(", [");
			 for(int i=0; i<Math.abs(node.count);i++) {
				 System.out.print(node.keys[i]+" ");
			 }
			 System.out.print("]");
			 System.out.print(", [");
			 for(int i=0; i<Math.abs(node.count)+1;i++) {
				 System.out.print(node.children[i]+" ");
			 }
			 System.out.print("]");
			 if(node.count<0) {
				 System.out.print(" next Leaf Addr = "+node.children[node.children.length-1]);
			 }
			 else {
				 System.out.print(" Not leaf");
			 }
			 System.out.print("|");
			 for(int i=0; i<Math.abs(node.count)+1;i++) {
				 printHelper(node.children[i]);
			 }
		 }
		 
	 }
	 
	 public void close() throws IOException {
	 //close the B+tree. The tree should not be accessed after close is called
		 f.close();
	 } 
	 
	 /*Returns a list of Addresses as longs from the smallest key 
	  * in the bTree to the largest key
	  */
	 public LinkedList<Long> getLeafAddresses(int smallest) throws IOException {
		 Stack<BTreeNode> path = this.searchPath(smallest);
		 LinkedList<Long> result = new LinkedList<Long>();
		 BTreeNode current = path.pop();
		 boolean isLeaf = true;
		 while(isLeaf) {
			 for(int i =0; i< Math.abs(current.count);i++) {
				 result.addLast(current.children[i]);
			 }
			 if(current.children[current.children.length-1]!=0) {
				 current = new BTreeNode(current.children[current.children.length-1]);
			 }
			 else {
				 isLeaf = false;
			 }
		 }
		 return result;
		 
	 }
	 
	//Returns a list of the keys in the BTree given as Integers
	public LinkedList<Integer> getLeafKeys(int smallest) throws IOException{
		Stack<BTreeNode> path = this.searchPath(smallest);
		 LinkedList<Integer> result = new LinkedList<Integer>();
		 BTreeNode current = path.pop();
		 boolean isLeaf = true;
		 while(isLeaf) {
			 for(int i =0; i< Math.abs(current.count);i++) {
				 result.addLast(current.keys[i]);
			 }
			 if(current.children[current.children.length-1]!=0) {
				 current = new BTreeNode(current.children[current.children.length-1]);
			 }
			 else {
				 isLeaf = false;
			 }
		 }
		 return result;
	}

}
