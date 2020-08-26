/*Testfile used to test both the DBTable.java
 * Implementation and BTree.java Implementation.
 */
import java.io.*;
import java.util.*;
public class BTreeTest {
    
    public BTreeTest() {
        
    }
    
    public void test1() throws IOException {
        //this test creates an DBTable with a height 1 order 5 BTree
        //and only does inserts and print
        System.out.println("Start test 1");
        int i;
        int sFieldLens[] = {10, 15};
        int nums[] = {9, 5, 1, 13, 17, 2, 6, 7, 8, 3, 4, 10, 18, 11, 12, 14, 19, 15, 16, 20};
        int len = nums.length;
        DBTable t1 = new DBTable("t1", sFieldLens, 60);
        char sFields[][] = new char[2][];
        for ( i = 0; i < len; i++) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);

            t1.insert(nums[i], sFields);
        }
        
        System.out.println("***************************\nPast inserts in test 1");
        t1.print();
        t1.close();
    }
    
    public void test1a() throws IOException {
         //this test creates an DBTable with a height 2 order 5 BTree
         //and only does inserts and print
         System.out.println("Start test 1a");
         int i;
         int sFieldLens[] = {10, 15};
         int nums[] = {10, 2000,1000, 3000, 500, 50, 250, 750, 1500, 2500, 20, 30, 300, 600, 1200, 1700, 1900,
                        2200, 2700, 40, 80, 550, 1600, 300, 400, 17, 507, 999, 4000};
         int len = nums.length;
         DBTable t1a = new DBTable("t1a", sFieldLens, 60);
         char sFields[][] = new char[2][];
         for ( i = 0; i < len; i++) {
             sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
             sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);

             t1a.insert(nums[i], sFields);
         }
         
         System.out.println("***************************\nPast inserts in test 1a");
         t1a.print();
         t1a.close();
     }
    
    public void test2() throws IOException {
        //this test creates an DBTable with a height 1 order 5 BTree
        //and does inserts, print, search and range search
        System.out.println("***************************\nStart test 2");
        int i;
        int sFieldLens[] = {10, 15};
        int nums[] = {9, 5, 1, 13, 17, 2, 6, 7, 8, 3, 4, 10, 18, 11, 12, 14, 19, 15, 16, 20};
        int len = nums.length;
        DBTable t2 = new DBTable("t2", sFieldLens, 60);
        char sFields[][] = new char[2][];
        for ( i = 0; i < len; i++) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);

            t2.insert(nums[i], sFields);
        }
        
        System.out.println("***************************\nPast inserts in test 2\n");
        t2.print();
        System.out.println("***************************\nPast print in test 2\n");
        System.out.println("Search for 1, 6, 11, 16, 21 in test 2\n***************************");
        LinkedList<String> s2;
        for (i = 1; i <= 21; i = i+5) {
            s2 = t2.search(i);
            if (s2.size() == 0) System.out.println(i+" not found");
            else System.out.println(i+" "+s2.get(0)+" "+s2.get(1));
        }
         System.out.println("***************************\nRange search 7 to 18 in test 2");
        LinkedList<LinkedList<String>> s2a = t2.rangeSearch(7,18);
        if  (s2a.size() == 0) System.out.println("No items found in range 7 to 18");
        else {
            for (int j = 0; j < s2a.size(); j++) {
                s2 = s2a.get(j);
                System.out.println(s2.get(0)+" "+s2.get(1)+" "+s2.get(2));
            }
        }
        
        t2.close();
        System.out.println("***************************\n");
    }
    
    public void test2a() throws IOException {
        //this test creates an DBTable with a height 2 order 5 BTree
        //and does inserts, print, search and range search
        System.out.println("***************************\nStart test 2a");
        int i;
        int sFieldLens[] = {10, 15};
        int nums[] = {10, 2000,1000, 3000, 500, 50, 250, 750, 1500, 2500, 20, 30, 300, 600, 1200, 1700, 1900,
                         2200, 2700, 40, 80, 550, 1600, 300, 400, 17, 507, 999, 4000};
        int len = nums.length;
        DBTable t2a = new DBTable("t2a", sFieldLens, 60);
        char sFields[][] = new char[2][];
        for ( i = 0; i < len; i++) {
            sFields[0] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 10);
            sFields[1] = Arrays.copyOf((new Integer(nums[i])).toString().toCharArray(), 15);

            t2a.insert(nums[i], sFields);
        }
        
        System.out.println("Past inserts in test 2a\n***************************\n");
        t2a.print();
        System.out.println("Past print in test 2a\n***************************\n");
        System.out.println("Search for 0, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000, 4500  in test 2a\n***************************\n");
        LinkedList<String> s2a;
        for (i = 0; i < 5000; i = i+500) {
            s2a = t2a.search(i);
            if (s2a.size() == 0) System.out.println(i+" not found");
            else System.out.println(i+" "+s2a.get(0)+" "+s2a.get(1));
        }
         System.out.println("***************************\nRange search 1000 to 2000 in test 2a\n***************************\n");
        LinkedList<LinkedList<String>> s2ar = t2a.rangeSearch(1000,2000);
        if  (s2ar.size() == 0) System.out.println("No items found in range 1000 to 2000");
        else {
            for (int j = 0; j < s2ar.size(); j++) {
                s2a = s2ar.get(j);
                System.out.println(s2a.get(0)+" "+s2a.get(1)+" "+s2a.get(2));
            }
        }
        
        

        t2a.close();
        System.out.println("***************************\n");
    }
 
    public static void main(String args[]) throws IOException {
        BTreeTest test = new BTreeTest();
        test.test1();
        test.test2();        
        test.test1a();
        test.test2a();
    }

}


