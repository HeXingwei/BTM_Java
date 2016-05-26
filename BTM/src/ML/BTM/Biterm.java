package ML.BTM;

import Tool.algorithm;

public class Biterm {
public int w1;
public int w2;
public int z;//topic assignment
public Biterm(int w1,int w2,int z)
{
	this.w1=algorithm.min(w1,w2);
	this.w2=algorithm.max(w1,w2);
	this.z=z;
}
}
