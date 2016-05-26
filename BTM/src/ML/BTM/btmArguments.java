package ML.BTM;

public class btmArguments {
	public int K;//number of topics
	public int M;
	public int V;//the vocabulary size
	public double alpha;//the hyperparameters of p[z]
	public  double beta;//the hyperparameters of  p[w|z]
	public int iter;//number of iterations of Gibbs sampling
	public void setV(int v)
	{
		V=v;
	}
	public void setM(int m)
	{
		M=m;
	}
	public void setK(int k)
	{
		K=k;
	}
	public void setAlpha(double x)
	{
		alpha=x;
	}
	public void setBeta(double x)
	{
		beta=x;
	}
	public void setIter(int iter)
	{
		this.iter=iter;
	}
}
