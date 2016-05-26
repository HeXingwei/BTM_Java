package ML.BTM;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import Tool.algorithm;

/**
 * 
 * @author hxw
 *
 */
public class BTM {
	private int K;//number of topics
	private int V;//the vocabulary size
	private int M;//the number of documents
	private int Nwz[][];//K*V ( topic z,word w ) the number of times that each word w assigned to topic z
	private int Nb_z[];//K the number of Biterms assigned to topic z
	private double alpha;//the hyperparameters of p[z]
	private double beta;//the hyperparameters of  p[w|z]
	private int iter;//number of iterations of Gibbs sampling
	private List<Biterm>corpus=new ArrayList<>();
	private int [][]documents;
	private int corpusLength=0;
	private int window;
	private double []theta;
	private double [][]phi;
	private double [][]Pz_d;//the topic distribution of each document
	Map<String,Integer>vocabularyStrToInt=new HashMap<>();// the vocabulary
	Map<Integer,String>vocabularyIntToStr=new HashMap<>();// the vocabulary
	public BTM()
	{
		setDefault();
	}
	public void setDefault()
	{
		alpha=0.1;
		beta=0.01;
		K=20;
		iter=500;
		window=15;
	}
	public boolean init(btmArguments option)
	{
		if(option == null)
			return false;
		alpha = option.alpha;
		beta = option.beta;
		iter = option.iter;
		K = option.K;
		V = option.V;
		M=option.M;
		Nwz=new int[K][V];
		Nb_z=new int[K];
		documents=new int[M][];
		theta = new double[M];
		phi = new double[K][V];
		Pz_d=new double[M][K];
		return true;
	}
	/**
	 * load data from file
	 * @param filename
	 */
	public void LoadCorpus(String filename){
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			int count = 0;
			try {
				br.readLine();br.readLine();
				while((line = br.readLine()) != null){

					String[] data = line.split(" ");
					documents[count]=new int[data.length];
					for(int i=0;i<data.length;i++)					
						documents[count][i] = Integer.valueOf(data[i])-1;//store input data into corpus				
					count ++;
				}
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Random rand = new Random();
		int tmpz = rand.nextInt(K);//random initialize topic
		for(int docu=0;docu<M;docu++)
		{
			int []document=documents[docu];
			for(int i=0;i<document.length;i++)
				for(int j=i+1;j<algorithm.min(i + window,document.length);j++)
				{
					corpus.add(new Biterm(document[i],document[j],rand.nextInt(K)));
				}
		}
		//init Nb_z ,Nwz
		corpusLength=corpus.size();
		for(int i=0;i<corpusLength;i++)
		{
			Biterm bt=corpus.get(i);
			int w1=bt.w1;
			int w2=bt.w2;
			int k=bt.z;
			Nb_z[k]++;
			Nwz[k][w1]++;
			Nwz[k][w2]++;
		}
			
	}
	
	/**
	 * load dictionary from the dicFile
	 * @param dic
	 * @throws IOException
	 */
	public void loadDic(String dic) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(dic));
		br.readLine();
		String line=null;
		while((line=br.readLine())!=null)
		{
			String []data=line.split(" ");
			vocabularyStrToInt.put(data[0],Integer.valueOf(data[1]));
			vocabularyIntToStr.put(Integer.valueOf(data[1]),data[0]);
		}
	}
	public void Estimation()
	{
		
		for(int iteration=0;iteration<iter;iteration++)
		{
			System.out.println("iteration"+"	"+(iteration+1));
			for(int i=0;i<corpusLength;i++)
			{	
				Biterm bt=corpus.get(i);
				int w1=bt.w1;
				int w2=bt.w2;
				int k=bt.z;
				//System.out.println(k);
				Nb_z[k]--;
				Nwz[k][w1]--;
				Nwz[k][w2]--;
				k=gibbsSampling(w1,w2);
				bt.z=k;
				Nb_z[k]++;
				Nwz[k][w1]++;
				Nwz[k][w2]++;
				//System.out.println(corpus.get(i).z+"---");
				
			}
			
		}
	}
	public int  gibbsSampling(int w1,int w2)
	{
		double []proba=new double[K];
		double p1 = 0,p2 = 0;
		int k;
		for(k=0;k<K;k++)
		{
			p1=(Nwz[k][w1]+beta)/(2*Nb_z[k]+V*beta);
			p2=(Nwz[k][w2]+beta)/(2*Nb_z[k]+1+V*beta);
			proba[k]=(Nb_z[k]+alpha)*p1*p2;
			
		}
		for(int i=1;i<K;i++)
			proba[i] +=proba[i-1];
		if(proba[K-1]==0)
			{
			return (int) (Math.random()*K);
			}
		double u = Math.random() * proba[K-1]; //random u
		
		for(k=0;k<K;k++)
			if(u<proba[k])
				break;
		return k;
	}
	/**
	 * compute theta
	 */
	public void computeTheta()
	{
		for(int k=0;k<K;k++)
			theta[k]=(Nb_z[k]+alpha)/(corpusLength+K*alpha);
	}
	/**
	 * compute phi
	 */
	public void computePhi()
	{
		for(int k=0;k<K;k++)
			for(int i=0;i<V;i++)			
				phi[k][i]=(Nwz[k][i]+beta)/(2*Nb_z[k]+V*beta);	
	}
	/**
	 * compute the topic distribution of each document
	 * p(z|d) = \sum_b{ p(z|b)p(b|d)
	 */
	public void computePzd()
	{
		computeTheta();
		computePhi();
		for(int docu=0;docu<M;docu++)
		{
			int []document=documents[docu];
			if(document.length==1)
			{
				int w=document[0];
				for(int k=0;k<K;k++)
					Pz_d[docu][k]=theta[k]*phi[k][w];
				
			}
			else//more than one words
			{
				
				List<Biterm>BS=new ArrayList<>();
				for(int i=0;i<document.length;i++)
					for(int j=i+1;j<algorithm.min(i + window,document.length);j++)
					{
						BS.add(new Biterm(document[i],document[j],0));
					}
				double []Pz_b=new double[K];
				double sum=0;
				for(int i=0;i<BS.size();i++)
				{
					int w1=BS.get(i).w1;
					int w2=BS.get(i).w2;
					
					for(int k=0;k<K;k++)				
						Pz_b[k]=theta[k]*phi[k][w1]*phi[k][w2];
						
					algorithm.normalize(Pz_b);
					for(int k=0;k<K;k++)
						Pz_d[docu][k]+=Pz_b[k];	
				}
			}//else
			algorithm.normalize(Pz_d[docu]);
		}
		
	}
	/**
	 * compute the corpus loglikelihood
	 * @return
	 */
	public double computeLogLikelihood()
	{
		double Loglikeli=0;
		for(int i=0;i<corpusLength;i++)
		{
			Biterm b=corpus.get(i);
			int w1=b.w1;
			int w2=b.w2;
			int z=b.z;
			double sum=0;
			for(int k=0;k<K;k++)
				sum+=theta[k]*phi[k][w1]*phi[k][w2];
			Loglikeli+=Math.log(sum);
		}
		System.out.println("the corpus loglikelihood is :"+Loglikeli);
		return Loglikeli;
	}
	/**
	 * save the top 20 words of each topic  
	 * @param directory
	 */
	public void saveTopicWord(String directory)
	{
		
		String s1=directory+"/topic_word.txt";
		try {
			BufferedWriter bw1=new BufferedWriter(new FileWriter(s1));
			
			for(int i=0;i<this.K;i++)
			{
				int []visited=new int[V];
				bw1.write("Topic "+(i+1)+"\n");
				for(int j=0;j<20;j++)
				{
					double max=-1;
					int max_id=-1;
					for(int k=0;k<this.V;k++)
					{
						if(visited[k]==0&&max<phi[i][k])
						{
							max_id=k;
							max=phi[i][k];
						}
					}
					bw1.write(vocabularyIntToStr.get(max_id+1)+":"+max+"\n");
					visited[max_id]=1;
				}
				
			}

			bw1.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void saveDocTopic(String directory)
	{
		computePzd();
		String s1=directory+"/doc_topic.txt";
		try {
			BufferedWriter bw1=new BufferedWriter(new FileWriter(s1));
			int minTopic=5>this.K?this.K:5;
			for(int doc=0;doc<M;doc++)
			{
				int []visited=new int[K];
				bw1.write("document:"+(doc+1)+"\n");
				//select the top 5 topics
				for(int j=0;j<minTopic;j++)
				{
					double max=-1;
					int max_id=-1;
					for(int k=0;k<this.K;k++)
					{
						if(visited[k]==0&&Pz_d[doc][k]>max)
						{
							max=Pz_d[doc][k];
							max_id=k;
						}
					}
					
					bw1.write(" "+max_id+" "+ max);
					visited[max_id]=1;	
				}
				bw1.write("\n");
			}
			
			bw1.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String []args) throws IOException
	{
		btmArguments arg = new btmArguments();
		arg.setAlpha(0.1);arg.setBeta(0.01);
		arg.setIter(200);arg.setK(10);
		arg.setV(8212);arg.setM(10768);
		BTM btm=new BTM();
		btm.init(arg);
		btm.LoadCorpus("data/data_translate.txt");
		long startTime=System.currentTimeMillis();
		btm.Estimation();
		long endTime=System.currentTimeMillis();
		System.out.println("training uing time:"+(endTime-startTime));		
		btm.loadDic("data/data_dic.txt");	
		btm.computeTheta();
		btm.computePhi();
		btm.computeLogLikelihood();
		btm.saveTopicWord("data");
		btm.saveDocTopic("data");
	}
}
