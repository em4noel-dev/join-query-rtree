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

/**
 * Classe que implementa cinco diferentes métodos de junção entre
 * duas Árvores R propostos no trabalho de Brinkhoff. <p/>
 * 
 * @author Luiz Emanoel Batista Moreira <emanoel@unifei.edu.br>
 * @author Joao Tonet
 * @author Joao Victor
 * @author Luiz Olmes Carvalho <olmes@unifei.edu.br>
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 */
public class JoinQueries<R extends Rectangle<R> & Entity<? super R>> 
{
    public static int sizeOfBuffer = 32; // Em quantidade de páginas
    
    private RTree<R> rtree1;
    private RTree<R> rtree2;
    
    private Session se1, se2;
    private RTreeDescriptor descriptor1, descriptor2;
    private JoinQueryUtilities<R> joinUtilities;
    
    /*
     * Construtor que recebe duas Árvores R de mesma altura que serão utilizadas nas operações de junção.
     * 
     * @param rtree1 Primeira Árvore R.
     * @param rtree2 Segunda Árvore R.
     * @throws Exception Se as árvores tiverem alturas diferentes.
     * */
    public JoinQueries(RTree<R> rtree1, RTree<R> rtree2) throws Exception 
    {
        if(rtree1.height() != rtree2.height())
            throw new Exception("The trees should have the same height.");
            
        this.rtree1 = rtree1;
        this.rtree2 = rtree2;
        setup();
    }
    
    /*
     * Método que inicializa Session e RTreeDescriptor para cada árvore.
     * Também inicializa o atributo joinUtilities.
     * */
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
    
    /*
     * Método que implementa a abordagem mais básica de join de forma iterativa. 
     * A ideia é comparar cada entrada de um nó da rtree2 com todas as entradas de
     * um nó da rtree1. Baseado no algoritmo "SpatiaLJoin1" de Brinkhoff.
     * 
     * @return Lista contendo todos os pares de objetos espaciais (Uuid) que se interceptam.
     */
    public ArrayList<Pair<Uuid, Uuid>> basicJoin()
    {
        // Buffer LRU utilizado para diminuir a leitura de páginas de disco.
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        
        // Pilha contendo pares de ID das páginas de discos qualificadas a serem examinadas.
        // A pilha é necessária devido a implementação iterativa do algoritmo.
        // É ela que define a ordem dos pares de nós examinados.
        Stack<Pair<Long, Long>> qualifies = new Stack<>();
        qualifies.push(new Pair<>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId()));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2;
        ArrayList<Pair<Uuid, Uuid>> result = new ArrayList<>();
        
        // Métricas
        long totalDiskAccess = 0;
        long totalComparisons = 0; // Quantidade de interseções e overlaps calculados.
        long timeStart = System.nanoTime();
        
        do
        {
            Pair<Long, Long> pair = qualifies.pop();
            pageId1 = pair.getFirst();
            pageId2 = pair.getSecond();
            
            // Leitura de páginas de disco e utilização do buffer LRU
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
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
                    totalComparisons++;
                    if(this.joinUtilities.getGeometry().isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2)) // nodeRtree1 e nodeRtree2 são nós índices.
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            qualifies.add(qualifies.size() - overlap, new Pair<>(indexRtree1.readSubPageId(j), indexRtree2.readSubPageId(i)));
                            overlap++;
                        }
                        else // nodeRtree1 e nodeRtree2 são nós folhas.
                        {
                            RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                            Uuid uuidRtree1 = leafRtree1.readEntityUuid(j);
                            Uuid uuidRtree2 = leafRtree2.readEntityUuid(i);
                            result.add(new Pair<>(uuidRtree1, uuidRtree2));
                        } 
                    }
                }            
            }
        }
        while(!qualifies.isEmpty());
        
        // Impressão das métricas
        System.out.println("Time spent (seconds): " + (System.nanoTime() - timeStart) / 1000000000.0);
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
    
    /*
     * Método que utiliza uma restrição no espaco de busca na abordagem básica de join.
     * A ideia é comparar somente as entradas dos nós que interceptam a interseção dos seus 
     * MBRs "pais". Baseado no algoritmo "SpatiaLJoin2" de Brinkhoff.
     * 
     * @return Lista contendo todos os pares de objetos espaciais (Uuid) que se interceptam.
     */
    public ArrayList<Pair<Uuid, Uuid>> basicJoinRestringindoEspacoBusca()
    {
        // Buffer LRU utilizado para diminuir a leitura de páginas de disco.
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        
        // Pilha contendo pares de ID das páginas de discos qualificadas a serem examinadas.
        // A pilha é necessária devido a implementação iterativa do algoritmo.
        // É ela que define a ordem dos pares de nós examinados.
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R storedKeyRtree1, storedKeyRtree2, intersecao;
        ArrayList<Pair<Uuid, Uuid>> result = new ArrayList<>();
        
        // Métricas
        long totalDiskAccess = 0;
        long totalComparisons = 0;
        long timeStart = System.nanoTime();
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            // Leitura de páginas de disco e utilização do buffer LRU
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo o espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            for (int i = 0; i < entradasRtree2.size(); i++) 
            {
                storedKeyRtree2 = entradasRtree2.get(i).getFirst();
                for (int j = 0; j < entradasRtree1.size(); j++) 
                {
                    storedKeyRtree1 = entradasRtree1.get(j).getFirst();
                    totalComparisons++;
                    if(this.joinUtilities.getGeometry().isOverlap(storedKeyRtree2, storedKeyRtree1)) 
                    {
                        if (RTreeIndex.matchNodeType(nodeRtree2)) // nodeRtree1 e nodeRtree2 são nós índices.
                        {
                            RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                            intersecao = this.joinUtilities.getGeometry().intersection(storedKeyRtree2, storedKeyRtree1);
                            qualifies.add(qualifies.size() - overlap, new Triple<>(indexRtree1.readSubPageId(entradasRtree1.get(j).getSecond()), indexRtree2.readSubPageId(entradasRtree2.get(i).getSecond()), intersecao));
                            overlap++; 
                        }
                        else // nodeRtree1 e nodeRtree2 são nós folhas.
                        {
                            RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                            RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                            Uuid uuidRtree1 = leafRtree1.readEntityUuid(entradasRtree1.get(j).getSecond());
                            Uuid uuidRtree2 = leafRtree2.readEntityUuid(entradasRtree2.get(i).getSecond());
                            result.add(new Pair<>(uuidRtree1, uuidRtree2));
                        }
                    }
                }            
            }    
        }
        while(!qualifies.isEmpty());
        
        // Impressão das métricas
        System.out.println("Time spent (seconds): " + (System.nanoTime() - timeStart) / 1000000000.0);
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }

    /*
     * Método que utiliza o algoritmo de plane-sweep e ordenação espacial na junção.
     * A ideia é ordenar todos os MBRs pelo "x lower" para encontrar todas as interseções
     * de forma eficiente. Também utiliza a restrição de espaço de busca. Baseado no algoritmo 
     * "SpatiaLJoin3" de Brinkhoff.
     * 
     * @return Lista contendo todos os pares de objetos espaciais (Uuid) que se interceptam.
     */
    public ArrayList<Pair<Uuid, Uuid>> joinPlaneSweep()
    {        
        // Buffer LRU utilizado para diminuir a leitura de páginas de disco.
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        
        // Pilha contendo pares de ID das páginas de discos qualificadas a serem examinadas.
        // A pilha é necessária devido a implementação iterativa do algoritmo.
        // É ela que define a ordem dos pares de nós examinados.
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        int overlap;
        long pageId1, pageId2;
        R intersecao;
        ArrayList<Pair<Uuid, Uuid>> result = new ArrayList<>();
        
        // Métricas
        long totalDiskAccess = 0;
        long totalComparisons = 0;
        long timeStart = System.nanoTime();
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            // Leitura de páginas de disco e utilização do buffer LRU
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
                                    
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            overlap = 0;
            
            // Restringindo o espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando a ordenação e o algoritmo plane-sweep
            joinUtilities.setComparisons(0);
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, false);
            totalComparisons += joinUtilities.getComparisons();
            
            for(int i = 0; i < paresRetangulos.size(); i++)
            {
                if (RTreeIndex.matchNodeType(nodeRtree2)) // nodeRtree1 e nodeRtree2 são nós índices.
                {
                    RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                    RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                    intersecao = paresRetangulos.get(i).getThird().getFirst();
                    qualifies.add(qualifies.size() - overlap, new Triple<>(indexRtree1.readSubPageId(paresRetangulos.get(i).getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(i).getSecond().getSecond()), intersecao));
                    overlap++;
                }
                else // nodeRtree1 e nodeRtree2 são nós folhas.
                {
                    RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                    RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                    Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
                    Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
                    result.add(new Pair<>(uuidRtree1, uuidRtree2));
                }
            }
        }
        while(!qualifies.isEmpty());
        
        // Impressão das métricas
        System.out.println("Time spent (seconds): " + (System.nanoTime() - timeStart) / 1000000000.0);
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
    
    /*
     * Método que aprimora a junção baseada no plane-sweep utilizando uma técnica de fixação. 
     * A ideia é fazer as junções de um MBR com todos os outros que se interceptam com ele, buscando
     * reduzir a quantidade de leitura de páginas de disco necessárias. Baseado no algoritmo 
     * "SpatiaLJoin4" de Brinkhoff.
     * 
     * @return Lista contendo todos os pares de objetos espaciais (Uuid) que se interceptam.
     */
    public ArrayList<Pair<Uuid, Uuid>> joinPlaneSweepFixacao()
    {
        // Buffer LRU utilizado para diminuir a leitura de páginas de disco.
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        
        // Pilha contendo pares de ID das páginas de discos qualificadas a serem examinadas.
        // A pilha é necessária devido a implementação iterativa do algoritmo.
        // É ela que define a ordem dos pares de nós examinados.
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));
        
        long pageId1, pageId2;
        R intersecao;       
        ArrayList<Pair<Uuid, Uuid>> result = new ArrayList<>();
        
        // Métricas
        long totalDiskAccess = 0;
        long totalComparisons = 0;
        long timeStart = System.nanoTime();
   
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            // Leitura de páginas de disco e utilização do buffer LRU
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());  
            
            // Restringindo o espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando a ordenação e o algoritmo plane-sweep
            joinUtilities.setComparisons(0);
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, false);
            totalComparisons += joinUtilities.getComparisons();
            
            // Array para checar se um determinado par de retângulos já foi examinado ou não.
            int totalPares = paresRetangulos.size();
            boolean[] visitado = new boolean[totalPares]; 
                        
            for(int i = 0; i < totalPares; i++)
            {
                if(!visitado[i])
                {
                    Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>> par = paresRetangulos.get(i);
                    if (RTreeIndex.matchNodeType(nodeRtree2)) // nodeRtree1 e nodeRtree2 são nós índices.
                    {
                        RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());
                        
                        intersecao = par.getThird().getFirst();
                        qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                        visitado[i] = true;
                        
                        // Calculando o graus dos dois MBRs
                        int grauEntrada1 = 0, grauEntrada2 = 0;
                        for(int j = i + 1; j < paresRetangulos.size(); j++)
                        {
                            if(par.getFirst() == paresRetangulos.get(j).getFirst())
                                grauEntrada1++;

                            if(par.getSecond() == paresRetangulos.get(j).getSecond())
                                grauEntrada2++;             
                        }                        
                        
                        // Definir como pares qualificados aqueles que se interceptam com o MBR de maior grau
                        if(grauEntrada1 >= grauEntrada2)
                        {
                            for(int j = i + 1; j < paresRetangulos.size(); j++)
                            {
                                if(!visitado[j] && par.getFirst() == paresRetangulos.get(j).getFirst())
                                {
                                    intersecao = paresRetangulos.get(j).getThird().getFirst();
                                    qualifies.add(0, new Triple<>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(j).getSecond().getSecond()), intersecao));
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
                                    intersecao = paresRetangulos.get(k).getThird().getFirst();
                                    qualifies.add(0, new Triple<>(indexRtree1.readSubPageId(paresRetangulos.get(k).getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                                    visitado[k] = true;               
                                }    
                            }
                        }
                    }
                    else // nodeRtree1 e nodeRtree2 são nós folhas.
                    {
                        RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                        Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
                        Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
                        result.add(new Pair<>(uuidRtree1, uuidRtree2));
                    }
                }
            }
        }
        while(!qualifies.isEmpty());
        
        // Impressão das métricas
        System.out.println("Time spent (seconds): " + (System.nanoTime() - timeStart) / 1000000000.0);
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }

    /*
     * Método que implementa a junção utilizando uma ideia de ordenação espacial 
     * de pontos conhecida como z-order. Utiliza a restrição de espaço e o algoritmo
     * de plane-sweep para encontrar as interseções. Baseado no algoritmo "SpatiaLJoin5" 
     * de Brinkhoff.
     * 
     * @return Lista contendo todos os pares de objetos espaciais (Uuid) que se interceptam.
     */
    public ArrayList<Pair<Uuid, Uuid>> joinZorder()
    {
        // Buffer LRU utilizado para diminuir a leitura de páginas de disco.
        LRUCache bufferLRU = new LRUCache(sizeOfBuffer);
        
        // Pilha contendo pares de ID das páginas de discos qualificadas a serem examinadas.
        // A pilha é necessária devido a implementação iterativa do algoritmo.
        // É ela que define a ordem dos pares de nós examinados.
        Stack<Triple<Long, Long, R>> qualifies = new Stack<>();
        qualifies.push(new Triple<>(this.descriptor1.readRootPageId(), this.descriptor2.readRootPageId(), null));

        long pageId1, pageId2;
        R intersecao;    
        ArrayList<Pair<Uuid, Uuid>> result = new ArrayList<>();
        
        // Métricas
        long totalDiskAccess = 0;
        long totalComparisons = 0;
        long timeStart = System.nanoTime();
        
        do
        {
            Triple<Long, Long, R> trio = qualifies.pop();
            pageId1 = trio.getFirst();
            pageId2 = trio.getSecond();
            intersecao = trio.getThird();
            
            // Leitura de páginas de disco e utilização do buffer LRU
            Node nodeRtree1 = bufferLRU.get(pageId1 + "-1");
            if(nodeRtree1 == null)
            {
                nodeRtree1 = se1.load(pageId1);
                bufferLRU.put(pageId1 + "-1", nodeRtree1);
                totalDiskAccess++;
            }
            
            Node nodeRtree2 = bufferLRU.get(pageId2 + "-2");
            if(nodeRtree2 == null)
            {
                nodeRtree2 = se2.load(pageId2);
                bufferLRU.put(pageId2 + "-2", nodeRtree2);
                totalDiskAccess++;
            }
            
            RTreeNode<R> gerericNodeRtree1 = new RTreeNode<>(nodeRtree1, this.rtree1.getObjectClass());
            RTreeNode<R> gerericNodeRtree2 = new RTreeNode<>(nodeRtree2, this.rtree2.getObjectClass());
            
            // Restringindo espaço de busca
            ArrayList<Pair<R, Integer>> entradasRtree1 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree1);
            ArrayList<Pair<R, Integer>> entradasRtree2 = joinUtilities.restringirEspacoBusca(intersecao, gerericNodeRtree2);
            
            // Aplicando o algoritmo plane-sweep
            joinUtilities.setComparisons(0);
            ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> paresRetangulos = joinUtilities.planeSweep(entradasRtree1, entradasRtree2, true);
            totalComparisons += joinUtilities.getComparisons();
            
            // Aplicando a ordenação Z
            paresRetangulos.sort((o1, o2) -> Long.compare(o1.getThird().getSecond(), o2.getThird().getSecond()));
            
            int totalPares = paresRetangulos.size();
            boolean[] visitado = new boolean[totalPares];
            
            for(int i = 0; i < totalPares; i++)
            {
                if(!visitado[i])
                {
                    Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>> par = paresRetangulos.get(i);
                    if (RTreeIndex.matchNodeType(nodeRtree2)) // nodeRtree1 e nodeRtree2 são nós índices.
                    {
                        RTreeIndex<R> indexRtree1 = new RTreeIndex<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeIndex<R> indexRtree2 = new RTreeIndex<>(nodeRtree2, this.rtree2.getObjectClass());

                        intersecao = par.getThird().getFirst();
                        qualifies.add(0, new Triple<Long, Long, R>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                        visitado[i] = true;
                        
                        // Calculando o graus dos dois MBRs
                        int grauEntrada1 = 0, grauEntrada2 = 0;
                        for(int j = i + 1; j < paresRetangulos.size(); j++)
                        {
                            if(par.getFirst() == paresRetangulos.get(j).getFirst())
                                grauEntrada1++;

                            if(par.getSecond() == paresRetangulos.get(j).getSecond())
                                grauEntrada2++;             
                        }
                        
                        // Definir como pares qualificados aqueles que se interceptam com o MBR de maior grau
                        if(grauEntrada1 >= grauEntrada2)
                        {
                            for(int j = i + 1; j < paresRetangulos.size(); j++)
                            {
                                if(!visitado[j] && par.getFirst() == paresRetangulos.get(j).getFirst())
                                {
                                    intersecao = paresRetangulos.get(j).getThird().getFirst();
                                    qualifies.add(0, new Triple<>(indexRtree1.readSubPageId(par.getFirst().getSecond()), indexRtree2.readSubPageId(paresRetangulos.get(j).getSecond().getSecond()), intersecao));
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
                                    intersecao = paresRetangulos.get(k).getThird().getFirst();
                                    qualifies.add(0, new Triple<>(indexRtree1.readSubPageId(paresRetangulos.get(k).getFirst().getSecond()), indexRtree2.readSubPageId(par.getSecond().getSecond()), intersecao));
                                    visitado[k] = true;               
                                }    
                            }
                        }
                    }
                    else // nodeRtree1 e nodeRtree2 são nós folhas.
                    {
                        RTreeLeaf<R> leafRtree1 = new RTreeLeaf<>(nodeRtree1, this.rtree1.getObjectClass());
                        RTreeLeaf<R> leafRtree2 = new RTreeLeaf<>(nodeRtree2, this.rtree2.getObjectClass());
                        Uuid uuidRtree1 = leafRtree1.readEntityUuid(paresRetangulos.get(i).getFirst().getSecond());
                        Uuid uuidRtree2 = leafRtree2.readEntityUuid(paresRetangulos.get(i).getSecond().getSecond());
                        result.add(new Pair<>(uuidRtree1, uuidRtree2));
                    }
                }
            }
        }
        while(!qualifies.isEmpty());
        
        // Impressão das métricas
        System.out.println("Time spent (seconds): " + (System.nanoTime() - timeStart) / 1000000000.0);
        System.out.println("Total Comparisons: " + totalComparisons);
        System.out.println("Total Disk Access: " + totalDiskAccess);
        return result;
    }
}