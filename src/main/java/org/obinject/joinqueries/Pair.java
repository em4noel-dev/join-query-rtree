package org.obinject.joinqueries;

/*
 * Classe utilizada para armazenar dois objetos genéricos em forma de par.
 * 
 * @author Luiz Emanoel Batista Moreira <emanoel@unifei.edu.br>
 * @author Joao Tonet
 * @author Joao Victor
 * @author Luiz Olmes Carvalho <olmes@unifei.edu.br>
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 * */
public class Pair<K, V> 
{
    private K first;
    private V second;

    public Pair(K first, V second) 
    {
        this.first = first;
        this.second = second;
    }

    public K getFirst() 
    {
        return first;
    }

    public V getSecond() 
    {
        return second;
    }

    public void setFirst(K first) 
    {
        this.first = first;
    }

    public void setSecond(V second) 
    {
        this.second = second;
    }
}
