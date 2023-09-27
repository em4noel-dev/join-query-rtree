package org.obinject.joinqueries;

import java.util.ArrayList;
import java.util.Stack;

import org.obinject.block.Node;
import org.obinject.block.RTreeDescriptor;
import org.obinject.block.RTreeIndex;
import org.obinject.block.RTreeLeaf;
import org.obinject.block.RTreeNode;
import org.obinject.device.Session;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.meta.Uuid;
import org.obinject.storage.RTree;

public class JoinQueries<R extends Rectangle<R> & Entity<? super R>> 
{
    public static int sizeOfBuffer = 128; // Em quantidade de páginas 
    
    private RTree<R> rtree1;
    private RTree<R> rtree2;
    
    private Session se1, se2;
    private RTreeDescriptor descriptor1, descriptor2;
    private JoinQueryUtilities<R> joinUtilities;
    
    public JoinQueries(RTree<R> rtree1, RTree<R> rtree2) throws Exception 
    {
        if(rtree1.height() != rtree2.height())
            throw new Exception("The trees should have the same height.");
            
        this.rtree1 = rtree1;
        this.rtree2 = rtree2;
        setup();
    }
    
    private void setup()
    {
    	// R-tree 1
        this.se1 = this.rtree1.getWorkspace().openSession();
        long pageIdDescriptor1 = se1.findPageIdDescriptor(this.rtree1.getClassUuid());
        this.descriptor1 = new RTreeDescriptor(se1.load(pageIdDescriptor1));
        
        // R-tree 2
        this.se2 = this.rtree2.getWorkspace().openSession();
        long pageIdDescriptor2 = se2.findPageIdDescriptor(this.rtree2.getClassUuid());
        this.descriptor2 = new RTreeDescriptor(se2.load(pageIdDescriptor2));
        
        joinUtilities = new JoinQueryUtilities<R>(this.rtree1.geometry);
    }
    
    public ArrayList<Pair<String, String>> basicJoin()
    {
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        Stack<Pair<Long, Long>> qualifies = new Stack<>();
        qualifies.push(new Pair<Long, Long>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId()));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        // Metricas
        long totalDiskAccess = 0;
        
        do
        {
            Pair<Long, Long> pair = qualifies.pop();
            pageId1 = pair.getFirst();
            pageId2 = pair.getSecond();
            
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            int totalEntriesRtree1 = gerericNodeRtree1.readNumberOfKeys();
            int totalEntriesRtree2 = gerericNodeRtree2.readNumberOfKeys();
            overlap = 0;
            
            for (int i = 0; i < totalEntriesRtree2; i++) 
            {
                storedKeyRtree2 = gerericNodeRtree2.buildKey(i);
                for (int j = 0; j < totalEntriesRtree1; j++) 
                {
                    storedKeyRtree1 = gerericNodeRtree1.buildKey(j);
                    if(this.joinUtilities.getGeometry().isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2)) // Entao nodeRtree1 tambem e um no indice.
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            qualifies.add(qualifies.size() - overlap, new Pair<Long, Long>(indexRtree1.readSubPageId(j), indexRtree2.readSubPageId(i)));
                            overlap++;
                        }
                        else // nodeRtree1 e nodeRtree2 sao nos folha
                        {
                            RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                            Uuid uuidRtree1 = leafRtree1.readEntityUuid(j);
                            Uuid uuidRtree2 = leafRtree2.readEntityUuid(i);
                            result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                        } 
                    }
                }            
            }
        }
        while(!qualifies.isEmpty());
        
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
    
    public ArrayList<Pair<String, String>> basicJoinRestringindoEspacoBusca()
    {      
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2, intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        // Metricas
        long totalDiskAccess = 0;
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo espaco de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            for (int i = 0; i < entradasRtree2.size(); i++) 
            {
                storedKeyRtree2 = entradasRtree2.get(i).getFirst();
                for (int j = 0; j < entradasRtree1.size(); j++) 
                {
                    storedKeyRtree1 = entradasRtree1.get(j).getFirst();
                    if(this.joinUtilities.getGeometry().isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2))
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            intersecao = this.joinUtilities.getGeometry().intersection(storedKeyRtree2, storedKeyRtree1);
                            qualifies.add(qualifies.size() - overlap, new Triple<Long, Long, R>(indexRtree1.readSubPageId(entradasRtree1.get(j).getSecond()), indexRtree2.readSubPageId(entradasRtree2.get(i).getSecond()), intersecao));
                            overlap++; 
                        }
                        else
                        {
                            RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                            Uuid uuidRtree1 = leafRtree1.readEntityUuid(entradasRtree1.get(j).getSecond());
                            Uuid uuidRtree2 = leafRtree2.readEntityUuid(entradasRtree2.get(i).getSecond());
                            result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                        }
                    }
                }            
            }    
        }
        while(!qualifies.isEmpty());
        
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }

    public ArrayList<Pair<String, String>> joinPlaneSweep()
    {        
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        // Metricas
        long totalDiskAccess = 0;
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
                                    
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo espaco de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando o plane sweep order + Ordenacao
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Long>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, false);
            
            for(int i = 0; i < paresRetangulos.size(); i++)
            {
                if (RTreeIndex.matchNodeType(nodeRtree2))
                {
                    RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                    RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                    intersecao = this.joinUtilities.getGeometry().intersection(paresRetangulos.get(i).getSecond().getFirst(), paresRetangulos.get(i).getFirst().getFirst());
                    qualifies.add(qualifies.size() - overlap, new Triple<Long, Long, R>(indexRtree1.readSubPageId(paresRetangulos.get(i).getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(i).getSecond().getSecond()), intersecao));
                    overlap++;
                }
                else
                {
                    RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                    RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                    Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
                    Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
                    result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                }
            }
        }
        while(!qualifies.isEmpty());
        
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
    
    public ArrayList<Pair<String, String>> joinPlaneSweepFixacao()
    {
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));
        
        long pageId1, pageId2;
        R intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        // Metricas
        long totalDiskAccess = 0;
   
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());  
            
            // Restringindo espa�o de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando o plane sweep order + Ordena��o
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Long>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, false);
            int totalPares = paresRetangulos.size();
            boolean[] visitado = new boolean[totalPares];
                        
            for(int i = 0; i < totalPares; i++)
            {
                if(!visitado[i])
            	{
                    Triple<Pair<R, Integer>, Pair<R, Integer>, Long> par = paresRetangulos.get(i);
            	    if (RTreeIndex.matchNodeType(nodeRtree2))
            	    {
            	        RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                        intersecao = this.joinUtilities.getGeometry().intersection(par.getSecond().getFirst(), par.getFirst().getFirst());
                        qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                        visitado[i] = true;
                        
                        // Calcular grau das entradas
                        int grauEntrada1 = 0, grauEntrada2 = 0;
                        for(int j = i + 1; j < paresRetangulos.size(); j++)
                        {
                            if(par.getFirst() == paresRetangulos.get(j).getFirst())
                                grauEntrada1++;

                            if(par.getSecond() == paresRetangulos.get(j).getSecond())
                                grauEntrada2++;            	
                        }                        

                        if(grauEntrada1 >= grauEntrada2)
                        {
                            for(int j = i + 1; j < paresRetangulos.size(); j++)
                            {
                                if(!visitado[j] && par.getFirst() == paresRetangulos.get(j).getFirst())
                                {
                                    intersecao = this.joinUtilities.getGeometry().intersection(par.getFirst().getFirst(), paresRetangulos.get(j).getSecond().getFirst());
                                    qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(j).getSecond().getSecond()), intersecao));
                                    visitado[j] = true;     
                                }
                            }
                        }
                        else
                        {
                            for(int k = i + 1; k < paresRetangulos.size(); k++)
                            {
                                if(!visitado[k] && par.getSecond() == paresRetangulos.get(k).getSecond())
                                {                        			
                                    intersecao = this.joinUtilities.getGeometry().intersection(par.getSecond().getFirst(), paresRetangulos.get(k).getFirst().getFirst());
                                    qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(paresRetangulos.get(k).getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                                    visitado[k] = true;               
                                }    
                            }
                        }
            	    }
            	    else
            	    {
            	        RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
            	        RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
            	        Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
            	        Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
            	        result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                    }
            	}
            }
        }
        while(!qualifies.isEmpty());
        
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }

    // Obsevacao em relacao a numeros decimais e negativos.
    public ArrayList<Pair<String, String>> joinZorder()
    {
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        long pageId1, pageId2;
        R intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        // Metricas
        long totalDiskAccess = 0;
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            
            // Restringindo espaco de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando o plane sweep order + Ordenacao
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Long>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, true);
            
            // Z-ordering
            paresRetangulos.sort((o1, o2) -> Long.compare(o1.getThird(), o2.getThird()));
            
            int totalPares = paresRetangulos.size();
            boolean[] visitado = new boolean[totalPares];
            
            for(int i = 0; i < totalPares; i++)
            {
                if(!visitado[i])
                {
                    Triple<Pair<R, Integer>, Pair<R, Integer>, Long> par = paresRetangulos.get(i);
                    if (RTreeIndex.matchNodeType(nodeRtree2))
                    {
                        RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                        intersecao = this.joinUtilities.getGeometry().intersection(par.getSecond().getFirst(), par.getFirst().getFirst());
                        qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                        visitado[i] = true;
                        
                        // Calcular grau das entradas
                        int grauEntrada1 = 0, grauEntrada2 = 0;
                        for(int j = i + 1; j < paresRetangulos.size(); j++)
                        {
                            if(par.getFirst() == paresRetangulos.get(j).getFirst())
                                grauEntrada1++;

                            if(par.getSecond() == paresRetangulos.get(j).getSecond())
                                grauEntrada2++;             
                        }

                        
                        if(grauEntrada1 >= grauEntrada2)
                        {
                            for(int j = i + 1; j < paresRetangulos.size(); j++)
                            {
                                if(!visitado[j] && par.getFirst() == paresRetangulos.get(j).getFirst())
                                {
                                    intersecao = this.joinUtilities.getGeometry().intersection(par.getFirst().getFirst(), paresRetangulos.get(j).getSecond().getFirst());
                                    qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(j).getSecond().getSecond()), intersecao));
                                    visitado[j] = true;               
                                }
                            }
                        }
                        else
                        {
                            for(int k = i + 1; k < paresRetangulos.size(); k++)
                            {
                                if(!visitado[k] && par.getSecond() == paresRetangulos.get(k).getSecond())
                                {                                   
                                    intersecao = this.joinUtilities.getGeometry().intersection(par.getSecond().getFirst(), paresRetangulos.get(k).getFirst().getFirst());
                                    qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(paresRetangulos.get(k).getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                                    visitado[k] = true;               
                                }    
                            }
                        }
                    }
                    else
                    {
                        RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                        Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
                        Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
                        result.add(new Pair<String, String>(uuidRtree1.toString(), uuidRtree2.toString()));
                    }
                }
            }
        }
        while(!qualifies.isEmpty());
        
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
}
