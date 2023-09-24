package org.obinject.joinqueries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.obinject.device.File;
import org.obinject.sbbd2013.geonet.AddFindGeonetRTree;
import org.obinject.sbbd2013.geonet.RectLatLongCoordGeonet;
import org.obinject.storage.RTree;



public class AppTestJoin 
{
    private static final int sizeOfNode = 16384;

    public static void deleteFiles()
    {
        java.io.File arquivo = new java.io.File("rtreeGeonet1-" + sizeOfNode + ".dat");
        if(arquivo.exists())
        {
            arquivo.delete();
            System.out.println("Arquivo rtreGeonet1 deletado.");
        }
        
        arquivo = new java.io.File("rtreeGeonet2-" + sizeOfNode + ".dat");
        if(arquivo.exists())
        {
            arquivo.delete();
            System.out.println("Arquivo rtreGeonet2 deletado.");
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        System.out.println(new Date());
        int count = 0;
        RectLatLongCoordGeonet metric = new RectLatLongCoordGeonet();
        String nomeArq = AddFindGeonetRTree.class.getClassLoader().getResource("geonet_simplificado.txt").getFile();
        String nomeArq2 = AddFindGeonetRTree.class.getClassLoader().getResource("geonet_simplificado.txt").getFile();
        deleteFiles();
        
        // Inserção na R-tree 1
        System.out.println("Inserindo na R-tree 1.");
        File workspace = new File("rtreeGeonet1-" + sizeOfNode + ".dat", sizeOfNode);        
        RTree<RectLatLongCoordGeonet> rtree1 = new RTree<RectLatLongCoordGeonet>(workspace){};
        BufferedReader in = new BufferedReader(new FileReader(nomeArq));
        
        while(in.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(in.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            rtree1.add(metric);
            
            if(count++ % 1000 == 0) 
                System.out.println(count);
        }
        
        in.close();
        in = null;
        System.gc();
        
        // Inserção na R-tree 2
        System.out.println("Inserindo na R-tree 2.");
        count = 0;
        File workspace2 = new File("rtreeGeonet2-" + sizeOfNode + ".dat", sizeOfNode);
        RTree<RectLatLongCoordGeonet> rtree2 = new RTree<RectLatLongCoordGeonet>(workspace2){};
        in = new BufferedReader(new FileReader(nomeArq2));
      
        while (in.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(in.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            rtree2.add(metric);
            
            if(count++ % 1000 == 0) 
                System.out.println(count);
        }
        
        in.close();
        in = null;
        System.gc();
        
        // Conferência da inserção na R-tree 1
        System.out.println("Conferindo se todos os dados foram inseridos corretamente na Rtree-1.");
        BufferedReader check = new BufferedReader(new FileReader(nomeArq));

        int notFound = 0;
        count = 0;

        while (check.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(check.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            if (rtree1.find(metric) == null)
                notFound++;
        }

        check.close();
        
        if(notFound == 0)
            System.out.println("Todos os dados foram encontrados na Árvore R 1.");
        
        
        // Conferência da inserção na R-tree 2
        System.out.println("Conferindo se todos os dados foram inseridos corretamente na Rtree-2.");
        check = new BufferedReader(new FileReader(nomeArq2));

        notFound = 0;
        count = 0;

        while (check.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(check.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            if (rtree2.find(metric) == null)
                notFound++;
        }

        check.close();
        
        if(notFound == 0)
            System.out.println("Todos os dados foram encontrados na Árvore R 2.");
        
        // Testar Basic Join
        System.out.println("Realizando o Join Básico entre as duas árvores.");
        JoinQueries<RectLatLongCoordGeonet> joinQuery = new JoinQueries<>(rtree1, rtree2);
        ArrayList<Pair<String, String>> result = joinQuery.basicJoinSameHeight();
        
        System.out.println("result.size(): " + result.size() + "\n");
        System.out.println("20 primeiras linhas de result: ");
        for(int i = 0; i < 20; i++)
            System.out.println(result.get(i).getFirst() + " " + result.get(i).getSecond());
        
        // Testar Basic Join com Restrição do Espaço de Busca
        System.out.println("\nRealizando o Join Básico com Restrição do Espaço de Busca entre as duas árvores.");
        result = joinQuery.basicJoinRestringindoEspacoBusca();
        
        System.out.println("result.size(): " + result.size() + "\n");
        System.out.println("20 primeiras linhas de result: ");
        for(int i = 0; i < 20; i++)
            System.out.println(result.get(i).getFirst() + " " + result.get(i).getSecond());
        
    }
}
