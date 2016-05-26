package preprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;



/**
 * preprocess the file ,change the to word to number
 * @author hxw
 *
 */
public class FileProcess {
	/**
	 * change the file to dicFile and file with numbers
	 * @param file
	 * @throws IOException 
	 */
	public static void preProcess(String file) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(file));
		
		String s=file.split("\\.")[0];
		BufferedWriter bw1=new BufferedWriter(new FileWriter(s+"_dic.txt"));
		BufferedWriter bw2=new BufferedWriter(new FileWriter(s+"_translate.txt"));
		int M=0;
		Map<String,Integer>vocabulary=new HashMap<>();// the vocabulary
		M=Integer.valueOf(br.readLine());
		String line;
		int value;
		int dicLength=0;
		while((line=br.readLine())!=null)
		{
			String[] data = line.split(" ");
			for(int i=0;i<data.length;i++){
				if(vocabulary.get(data[i])!=null)
					value=vocabulary.get(data[i]);
				else
					{					
					dicLength++;
					vocabulary.put(data[i], dicLength);
					value=dicLength;
					}
			}
			
		}
		br.close();
		bw1.write(dicLength+"\n");
		for(String e:vocabulary.keySet())
			bw1.write(e+" "+vocabulary.get(e)+"\n");
		bw1.close();
		bw2.write(M+"\n");
		bw2.write(dicLength+"\n");
		br=new BufferedReader(new FileReader(file));
		br.readLine();
		int document_index=0;
		while((line=br.readLine())!=null)
		{
			document_index++;
			String[] data = line.split(" ");
			
			for(int i=0;i<data.length-1;i++){				
					value=vocabulary.get(data[i]);
					bw2.write(value+" ");							
			}
			bw2.write(vocabulary.get(data[data.length-1])+"\n");		
		}
		
		bw2.close();	
		System.out.println("preprocess finish");
	}//method
	public static void main(String []args)
	{

		try {
			preProcess("data/data.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
