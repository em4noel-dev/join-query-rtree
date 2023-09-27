package org.obinject.joinqueries;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.obinject.block.Node;

// Reference: https://stackoverflow.com/questions/23772102/lru-cache-in-java-with-generics-and-o1-operations
public class LRUCache 
{
    private int capacity;
    private LinkedHashMap<String, Node> map;

    public LRUCache(int capacity) 
    {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true);
    }

    public Node get(String key) 
    {
        Node value = this.map.get(key);
        return value;
    }

    public void put(String key, Node value) 
    {
        if(!this.map.containsKey(key) && this.map.size() == this.capacity) 
        {
            Iterator<String> it = this.map.keySet().iterator();
            it.next();
            it.remove();
        }
        this.map.put(key, value);
    }
}