package org.obinject.joinqueries;

import java.util.ArrayList;
import java.util.Stack;

import org.obinject.block.Node;
import org.obinject.block.RTreeDescriptor;
import org.obinject.block.RTreeIndex;
import org.obinject.block.RTreeLeaf;
import org.obinject.device.Session;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.meta.Uuid;
import org.obinject.storage.RTree;

public class JoinQueries<R extends Rectangle<R> & Entity<? super R>> 
{
	private RTree<R> rtree1;
	private RTree<R> rtree2;
	
	public JoinQueries(RTree<R> rtree1, RTree<R> rtree2) 
	{
        this.rtree1 = rtree1;
        this.rtree2 = rtree2;
    }
		
	public ArrayList<Pair<String, String>> basicJoinSameHeight()
    {
		// R-tree 1
    	Session se1 = this.rtree1.getWorkspace().openSession();
    	long pageIdDescriptor1 = se1.findPageIdDescriptor(this.rtree1.getClassUuid());
    	RTreeDescriptor descriptor1 = new RTreeDescriptor(se1.load(pageIdDescriptor1));
    	
    	// R-tree 2
    	Session se2 = this.rtree2.getWorkspace().openSession();
    	long pageIdDescriptor2 = se2.findPageIdDescriptor(this.rtree2.getClassUuid());
    	RTreeDescriptor descriptor2 = new RTreeDescriptor(se2.load(pageIdDescriptor2));
    	
    	Stack<Pair<Long, Long>> qualifies = new Stack<>();
    	qualifies.push(new Pair<Long, Long>(descriptor1.readRootPageId(), descriptor2.readRootPageId()));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        do
        {
        	Pair<Long, Long> pair = qualifies.pop();
        	pageId1 = pair.getFirst();
        	pageId2 = pair.getSecond();
        	Node nodeRtree1 = se1.load(pageId1);
        	Node nodeRtree2 = se2.load(pageId2);
       	
        	if (RTreeIndex.matchNodeType(nodeRtree2)) // Então nodeRtree1 também é um nó índice.
        	{
        		RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
            	RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                int totalEntriesRtree2 = indexRtree2.readNumberOfKeys();
                overlap = 0;
                
                for (int i = 0; i < totalEntriesRtree2; i++) 
                {
                	storedKeyRtree2 = indexRtree2.buildKey(i);
                    int totalEntriesRtree1 = indexRtree1.readNumberOfKeys();
                	
                    for (int j = 0; j < totalEntriesRtree1; j++) 
                    {
                    	storedKeyRtree1 = indexRtree1.buildKey(j);
                        if(this.rtree2.geometry.isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                        {
                            qualifies.add(qualifies.size() - overlap, new Pair<Long, Long>(indexRtree1.readSubPageId(j), indexRtree2.readSubPageId(i)));
                            overlap++;
                        }
                    }            
                }
        	}
        	else // nodeRtree1 e nodeRtree2 são nós folha.
        	{
        		RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
        		RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                int totalEntriesRtree2 = leafRtree2.readNumberOfKeys();
                
                for (int i = 0; i < totalEntriesRtree2; i++) 
                {
                	storedKeyRtree2 = leafRtree2.buildKey(i);
                    int totalEntriesRtree1 = leafRtree1.readNumberOfKeys();
                	
                    for (int j = 0; j < totalEntriesRtree1; j++) 
                    {
                    	storedKeyRtree1 = leafRtree1.buildKey(j);
                        if(this.rtree2.geometry.isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                        {
                        	Uuid uuidRtree1 = leafRtree1.readEntityUuid(j);
                        	Uuid uuidRtree2 = leafRtree2.readEntityUuid(i);
                            result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                        }
                    }
                }
        	}
        }
        while(!qualifies.isEmpty());
        
        return result;
    }
}
