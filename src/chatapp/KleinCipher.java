package chatapp;

import java.util.Arrays;

public class KleinCipher {
	
	public static int[] encryption(int in[], int key[], int Sbox[]){
		
		int rounds = 12;
		int out[] = new int[16];
				
		System.out.println("Key: 0");
		for(int i=0; i<16; i++){
			System.out.print(key[i] + ", ");
		}
		System.out.println();
		
		for(int i=1; i<=12; i++){
			
			out = roundProcessing(Sbox, in, key);
			key = nextRoundKey(key, i, Sbox);
			
			System.out.println("Cipher: " + (i-1));
			for(int j=0; j<16; j++){
				System.out.print(out[j] + ", ");
			}
			System.out.println();
			
			System.out.println("Key: " + i);
			for(int j=0; j<16; j++){
				System.out.print(key[j] + ", ");
			}
			System.out.println();
			
			for(int j=0; j<16; j++)
				in[j] = out[j];
		}
		
		out = addRoundKey(out, key);
		
		return out;
	}
	
	public static int[] decryption(int cipher[], int key[], int Sbox[]) {
		
		int plain[] = new int[16];
		int rounds = 12;
		int inverseRoundKeys[][] = new int[13][16];
		
		for(int i=0; i<16; i++)
			inverseRoundKeys[0][i] = key[i];
		
		for(int i=1; i<=12; i++){
			int prev[] = new int[16];
			for(int j=0; j<16; j++)
				prev[j] = inverseRoundKeys[i-1][j];
			
			inverseRoundKeys[i] = nextRoundKey(prev, i, Sbox);
		}
		
		System.out.println("Key: 12");
		for(int j=0; j<16; j++){
			System.out.print(inverseRoundKeys[12][j] + ", ");
		}
		System.out.println();
		
		cipher = addRoundKey(cipher, inverseRoundKeys[12]);
		
		for(int i=11; i>=0; i--) {
			
			System.out.println("Key: " + (i));
			for(int j=0; j<16; j++){
				System.out.print(inverseRoundKeys[i][j] + ", ");
			}
			System.out.println();
			
			cipher = reverseRoundProcessing(Sbox, cipher, inverseRoundKeys[i]);
		}
		
		return cipher;
	}
	
	public static int[] roundProcessing(int Sbox[], int in[], int key[]){
		
		int out[] = new int[16];
		int mc[][] = {{2,3,1,1}, {1,2,3,1},{1,1,2,3},{3,1,1,2}};
		
		//Add round Key
		in = addRoundKey(in, key);
		
		//Sub Nibbles
		in = subNibbles(in, Sbox);
		
		//Rotate Nibbles
		in = leftRotate(in, 4);
		
		//Mix Nibbles
		out = mixNibbles(in, mc);
			
		return out;
	}
	
	public static int[] reverseRoundProcessing(int Sbox[], int cipher[], int key[]) {
		
		int in[] = new int[16];
		int mc[][] = {{14,11,13,9}, {9,14,11,13},{13,9,14,11},{11,13,9,14}};
		
		in = mixNibbles(cipher, mc);
		
		in = rightRotate(in, 4);
		
		in = subNibbles(in, Sbox);
		
		in = addRoundKey(in, key);
		
		return in;
	}
	
	public static int[] addRoundKey(int in[], int key[]){
		
		int out[] = new int[16];
		for(int i=0; i<16; i++){
			out[i] = in[i] ^ key[i];
		}
		
		return out;
	}
	
	public static int[] subNibbles(int in[], int Sbox[]){
		
		int out[] = new int[16];
		for(int i=0; i<16; i++){
			out[i] = Sbox[in[i]];
		}
		
		return out;
	}
	
	public static int[] mixNibbles(int in[], int mc[][]){
		
		int out[] = new int[16];
		
		int input[][][] = new int[2][4][1];
		int x = 0;
		
		for(int i=0; i<input.length; i++){
			for(int j=0; j<input[0].length; j++){
				String s = concat(decimalToBinary(in[x++], 4)) + concat(decimalToBinary(in[x++], 4));
				input[i][j][0] = Integer.parseInt(s, 2);
			}
		}
		
		//int mc[][] = {{2,3,1,1}, {1,2,3,1},{1,1,2,3},{3,1,1,2}};
		int c[] = new int[4];
		x = 0;
		
		for(int l=0; l<2; l++) {
			for(int i=0; i<4; i++) {
				for(int j=0; j<1; j++) {
					for(int k=0; k<4; k++) {
					
						int m1[] = multiply(decimalToBinary(mc[i][k], 4), decimalToBinary(input[l][k][j], 8), 4, 8);
						int x1 = Integer.parseInt(concat(m1), 2);
						x1 = x1 > 255 ? reduce(x1) : x1;
						c[i] = c[i] ^ x1;
					}
				}
				
				int bits[] = decimalToBinary(c[i], 8);
				int nibble[] = Arrays.copyOfRange(bits, 0, 4);
				out[x++] = Integer.parseInt(concat(nibble), 2);
				nibble = Arrays.copyOfRange(bits, 4, 8);
				out[x++] = Integer.parseInt(concat(nibble), 2);
			}
		}
		
		return out;
	}
	
	public static int[] nextRoundKey(int key[], int r, int Sbox[]) {
		
		int newKey[] = new int[16];
		
		int a[] = new int[4];
		int b[] = new int[4];
		int x = 0;
		int y = 8;
		
		for(int i=0; i<4; i++){
			String s = concat(decimalToBinary(key[x++], 4)) + concat(decimalToBinary(key[x++], 4));
			a[i] = Integer.parseInt(s, 2);
			
			s = concat(decimalToBinary(key[y++], 4)) + concat(decimalToBinary(key[y++], 4));
			b[i] = Integer.parseInt(s, 2);
		}
		
		leftRotate(a, 1);
		leftRotate(b, 1);
		
		for(int i=0; i<4; i++){
			a[i] = a[i] ^ b[i];
		}
		
		b[2] = b[2] ^ r;
		
		x = 0;
		y = 8;
		
		for(int i=0; i<4; i++){
			int bits[] = decimalToBinary(b[i], 8);
			int nibble[] = Arrays.copyOfRange(bits, 0, 4);
			newKey[x++] = Integer.parseInt(concat(nibble), 2);
			nibble = Arrays.copyOfRange(bits, 4, 8);
			newKey[x++] = Integer.parseInt(concat(nibble), 2);
			
			
			bits = decimalToBinary(a[i], 8);
			nibble = Arrays.copyOfRange(bits, 0, 4);
			newKey[y++] = Integer.parseInt(concat(nibble), 2);
			nibble = Arrays.copyOfRange(bits, 4, 8);
			newKey[y++] = Integer.parseInt(concat(nibble), 2);
			
		}
		
		for(int i=10; i<14; i++){
			newKey[i] = Sbox[newKey[i]];
		}
		
		return newKey;
		
	}
	
	public static int reduce(int v){
		
		int[] data = new int[20];
        int id = 0;
 
        while (v > 0) {
            data[id++] = v % 2;
            v = v / 2;
        }
        int binary[] = new int[id];
        System.arraycopy(data, 0, binary, 0, id);
        
        binary = reverse(binary, binary.length);
        
		int poly[] = {1, 0, 0, 0, 1, 1, 0, 1, 1};
		
		for(int i=0; i<(binary.length-poly.length) + 1; i++){
			if(binary[i] == 1){
				int index = i;
				for(int j=0; j<poly.length; j++){
					binary[index] = binary[index] ^ poly[j];
					index++;
				}
			}
		}
		binary = reverse(binary, binary.length);
		int a[] = new int[8];
		System.arraycopy(binary, 0, a, 0, 8);
		a = reverse(a, a.length);
		int x = Integer.parseInt(concat(a), 2);
		
		return x;
	}
	
	public static int[] decimalToBinary(int num, int bits)
    {
		int[] binary = new int[bits];
        int id = 0;
 
        while (num > 0) {
            binary[id++] = num % 2;
            num = num / 2;
        }
        
        return reverse(binary, binary.length);
    }
	
	public static int[] multiply(int A[], int B[], int m, int n) {
		
		int[] prod = new int[m + n - 1];
		
		for (int i = 0; i < m + n - 1; i++) {
			prod[i] = 0;
		}
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) 
			{
				prod[i + j] += A[i] * B[j];
			}
		}
		
		for(int i=0; i<prod.length; i++)
			prod[i] = prod[i] % 2;
		return prod;
	}
	
	public static String concat(int a[]){
		
		String s = "";
		for(int i=0; i<a.length; i++){
			s += a[i];
		}
		StringBuilder input1 = new StringBuilder();          
        input1.append(s); 
        input1.reverse(); 
		return s;
	}
	
	public static int[] reverse(int a[], int n) 
    { 
        int[] b = new int[n]; 
        int j = n; 
        for (int i = 0; i < n; i++) { 
            b[j - 1] = a[i]; 
            j = j - 1; 
        } 
        return b;
    }
	
	public static int[] leftRotate(int arr[], int n){
		
		for(int i = 0; i < n; i++){    
            int j;
            int first;
            first = arr[0];
            
            for(j = 1; j<arr.length; j++){
            	arr[j-1] = arr[j];
            }
            arr[arr.length-1] = first;
        }
		
		return arr;
	}
	
	public static int[] rightRotate(int arr[], int n){
		
		for(int i = 0; i < n; i++){    
            int j;
            int last;
            last = arr[arr.length-1];
            
            for(j = arr.length-1; j>0; j--){
            	arr[j] = arr[j-1];
            }
            arr[0] = last;
        }
		
		return arr;
	}

}