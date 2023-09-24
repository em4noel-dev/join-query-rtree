package org.obinject.joinqueries;

public class Triple<K, V, X> 
{
    private K first;
    private V second;
    private X third;

    public Triple(K first, V second, X third) 
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public K getFirst() 
    {
        return first;
    }

    public V getSecond() 
    {
        return second;
    }
    
    public X getThird() 
    {
        return third;
    }

    public void setFirst(K first) 
    {
        this.first = first;
    }

    public void setSecond(V second) 
    {
        this.second = second;
    }
    
    public void setThird(X third) 
    {
        this.third = third;
    }
}
