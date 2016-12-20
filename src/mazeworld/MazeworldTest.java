package mazeworld;

import java.util.Comparator;

import datastructures.*;

public class MazeworldTest {
	private int failedTests = 0;
	
	private class IntegerKey implements KeyableObject {
		public int value;
		
		private IntegerKey(int v) {
			value = v;
		}
		
		public int intValue() {
			return this.value;
		}
		
		@Override
		public String toString() {
			return "" + this.value;
		}

		@Override
		public int calculateKey() {
			return this.value;
		}
	}
	
	public class IntegersComparator implements Comparator<IntegerKey> {
		public int compare(IntegerKey x, IntegerKey y)
		{	
	    	if (x.intValue() > y.intValue()) { return 1; }
	    	if (x.intValue() < y.intValue()) { return -1; }
	    	
	        return 0;
	    }
	}
	
	public void runTests() {
		String result = "";
		
		System.out.println("Test 1: Check if after inserting integers 0 to 49\n"
				+ "in reverse order to the heap, all of them can be extracted\n"
				+ "until the heap is empty");
		
		result = IntegerTest1() ? "PASSED" : "FAILED";
		
		System.out.println(result + "\n");
		result = "";
		
		System.out.println("Test 2: Check if after inserting integers 0 to 49\n"
				+ "in order to the heap, all of them can be extracted\n"
				+ "at different times until the heap is empty");
		result = IntegerTest2() ? "PASSED" : "FAILED";
		
		System.out.println(result + "\n");
		result = "";
		
		result = failedTests == 0 ?  "ALL TESTS PASSED" : failedTests 
				+ " TESTS FAILED";
		System.out.println(result);
	}
	
	private boolean IntegerTest1() {
		PriorityFibonacciHeap<IntegerKey> fibHeap = 
				new PriorityFibonacciHeap<IntegerKey>(new IntegersComparator());
		for (int i = 0; i < 50; i++) {
			fibHeap.insert(new 
					FibonacciHeapNode<IntegerKey>(new IntegerKey(49 - i)));
		}
		
		for (int i = 0; i < 50; i++) {
			if (fibHeap.poll().getValue().intValue() != i) {
				failedTests++;
				return false; 
			}
		}
		
		if (!fibHeap.isEmpty()) { 
			failedTests++;
			return false; 
		}
		
		return true;
	}
	
	private boolean IntegerTest2() {
		PriorityFibonacciHeap<IntegerKey> fibHeap = 
				new PriorityFibonacciHeap<IntegerKey>(new IntegersComparator());
		int expectedInt = 0;
		
		for (int i = 0; i < 50; i++) {
			fibHeap.insert(new 
					FibonacciHeapNode<IntegerKey>(new IntegerKey(i)));
			
			if (i % 5 == 0) {
				if (fibHeap.poll().getValue().intValue() != expectedInt) {
					failedTests++; 
					return false; 
				}
				
				expectedInt++;
			}
		}
		
		while (!fibHeap.isEmpty()) { 
			if (fibHeap.poll().getValue().intValue() != expectedInt) {
				failedTests++; 
				return false; 
			}
			
			expectedInt++;
		}
		
		return true;
	}
}
