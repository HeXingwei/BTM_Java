package Tool;

public class algorithm {
	public static int min(int x,int y)
	{
		if(x<y)
			return x;
		else
			return y;
	}
	public static int max(int x,int y)
	{
		if(x>y)
			return x;
		else
			return y;
	}
	public static void  normalize(double []s)
	{
		double sum=0;
		for(int i=0;i<s.length;i++)
			sum+=s[i];
		for(int i=0;i<s.length;i++)
			s[i]/=sum;
	}
}
