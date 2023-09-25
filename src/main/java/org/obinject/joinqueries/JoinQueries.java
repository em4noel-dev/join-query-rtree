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
    private RTree<R> rtree1;
    private RTree<R> rtree2;
    
    // Ambas devem ter a mesma altura.
    public JoinQueries(RTree<R> rtree1, RTree<R> rtree2) 
    {
        this.rtree1 = rtree1;
        this.rtree2 = rtree2;
    }
    
    public ArrayList<Pair<String, String>> basicJoin()
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
                    if(this.rtree2.geometry.isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2)) // Então nodeRtree1 também é um nó índice.
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            qualifies.add(qualifies.size() - overlap, new Pair<Long, Long>(indexRtree1.readSubPageId(j), indexRtree2.readSubPageId(i)));
                            overlap++;
                        }
                        else // nodeRtree1 e nodeRtree2 são nós folha.
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
        
        return result;
    }
    
    public ArrayList<Pair<String, String>> basicJoinRestringindoEspacoBusca()
    {
        // R-tree 1
        Session se1 = this.rtree1.getWorkspace().openSession();
        long pageIdDescriptor1 = se1.findPageIdDescriptor(this.rtree1.getClassUuid());
        RTreeDescriptor descriptor1 = new RTreeDescriptor(se1.load(pageIdDescriptor1));
        
        // R-tree 2
        Session se2 = this.rtree2.getWorkspace().openSession();
        long pageIdDescriptor2 = se2.findPageIdDescriptor(this.rtree2.getClassUuid());
        RTreeDescriptor descriptor2 = new RTreeDescriptor(se2.load(pageIdDescriptor2));
        
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(descriptor1.readRootPageId(), descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2, intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            Node nodeRtree1 = se1.load(pageId1);
            Node nodeRtree2 = se2.load(pageId2);
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            for (int i = 0; i < entradasRtree2.size(); i++) 
            {
                storedKeyRtree2 = entradasRtree2.get(i).getFirst();
                for (int j = 0; j < entradasRtree1.size(); j++) 
                {
                    storedKeyRtree1 = entradasRtree1.get(j).getFirst();
                    if(this.rtree2.geometry.isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2))
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            intersecao = this.rtree2.geometry.intersection(storedKeyRtree2, storedKeyRtree1);
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
        
        return result;
    }

    public ArrayList<Pair<String, String>> joinPlaneSweep()
    {
        // R-tree 1
        Session se1 = this.rtree1.getWorkspace().openSession();
        long pageIdDescriptor1 = se1.findPageIdDescriptor(this.rtree1.getClassUuid());
        RTreeDescriptor descriptor1 = new RTreeDescriptor(se1.load(pageIdDescriptor1));
        
        // R-tree 2
        Session se2 = this.rtree2.getWorkspace().openSession();
        long pageIdDescriptor2 = se2.findPageIdDescriptor(this.rtree2.getClassUuid());
        RTreeDescriptor descriptor2 = new RTreeDescriptor(se2.load(pageIdDescriptor2));
        
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<Long, Long, R>(descriptor1.readRootPageId(), descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R intersecao;
        
        ArrayList<Pair<String, String>> result = new ArrayList<>();
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            Node nodeRtree1 = se1.load(pageId1);
            Node nodeRtree2 = se2.load(pageId2);
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando o plane sweep order + Ordenação
            ArrayList<Pair<Pair<R, Integer>, Pair<R, Integer>>> paresRetangulos = planeSweep(entradasRtree1, entradasRtree2);
                
            for(int i = 0; i < paresRetangulos.size(); i++)
            {
                if (RTreeIndex.matchNodeType(nodeRtree2))
                {
                    RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                    RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                    intersecao = this.rtree2.geometry.intersection(paresRetangulos.get(i).getSecond().getFirst(), paresRetangulos.get(i).getFirst().getFirst());
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
        
        return result;
    }
    
    public ArrayList<Pair<R, Integer>> restringirEspacoBusca(R intersecao, RTreeNode<R> nodeRtree)
    {
        ArrayList<Pair<R, Integer>> entradasRtree = new ArrayList<>();
        int totalEntriesRtree = nodeRtree.readNumberOfKeys();
        R storedKeyRtree;
       
        for(int i = 0; i < totalEntriesRtree; i++)
        {
            storedKeyRtree = nodeRtree.buildKey(i);
            if(intersecao == null || this.rtree1.geometry.isOverlap(storedKeyRtree, intersecao)) 
                entradasRtree.add(new Pair<R, Integer>(storedKeyRtree, i));
        }
        
        return entradasRtree;
    }
        
    public void loopInterno(Pair<R, Integer> t, int naoMarcado, ArrayList<Pair<R, Integer>> rs, ArrayList<Pair<Pair<R, Integer>, Pair<R, Integer>>> saida, Boolean primeiroLoop)
    {
        int k = naoMarcado;
        int totalEntries = rs.size();
        
        double t_xu = t.getFirst().getOrigin(0) + t.getFirst().getExtension(0);
        
        while(k < totalEntries && rs.get(k).getFirst().getOrigin(0) <= t_xu)
        {
            int dims = t.getFirst().numberOfDimensions();
            Boolean interceptaTodasDimensoes = true;
            
            for (int d = 1; d < dims; d++)
            {
                double t_dl = t.getFirst().getOrigin(d);
                double t_du = t.getFirst().getOrigin(d) + t.getFirst().getExtension(d);
                double rsk_dl = rs.get(k).getFirst().getOrigin(d);
                double rsk_du = rs.get(k).getFirst().getOrigin(d) + rs.get(k).getFirst().getExtension(d);
                
                if((t_dl <= rsk_du && t_du >= rsk_dl) == false)
                {
                    interceptaTodasDimensoes = false;
                    break;
                }   
            }
                
            if(interceptaTodasDimensoes)
                if(primeiroLoop)
                    saida.add(new Pair<>(t, rs.get(k)));
                else
                    saida.add(new Pair<>(rs.get(k), t));
            k++;
        }
    }
    
    public ArrayList<Pair<Pair<R, Integer>, Pair<R, Integer>>> planeSweep(ArrayList<Pair<R, Integer>> mbrsRtree1, ArrayList<Pair<R, Integer>> mbrsRtree2)
    {
        int totalEntries1 = mbrsRtree1.size();
        int totalEntries2 = mbrsRtree2.size();
        int i = 0;
        int j = 0;
        
        // Ordenar entradas pelo x lower dos MBRs
        mbrsRtree1.sort((o1, o2) -> Double.compare(o1.getFirst().getOrigin(0), o2.getFirst().getOrigin(0)));
        mbrsRtree2.sort((o1, o2) -> Double.compare(o1.getFirst().getOrigin(0), o2.getFirst().getOrigin(0)));

        ArrayList<Pair<Pair<R, Integer>, Pair<R, Integer>>> saida = new ArrayList<>();

        while(i < totalEntries1 && j < totalEntries2)
        {
            if(mbrsRtree1.get(i).getFirst().getOrigin(0) <= mbrsRtree2.get(j).getFirst().getOrigin(0))
            {
                loopInterno(mbrsRtree1.get(i), j, mbrsRtree2, saida, true);
                i++;
            }
            else
            {
                loopInterno(mbrsRtree2.get(j), i, mbrsRtree1, saida, false);
                j++;
            }
        }  
        
        return saida;
    }
}
