package org.obinject.joinqueries;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import org.obinject.device.File;
import org.obinject.meta.Uuid;
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
            System.out.println("File " + "rtreeGeonet1-" + sizeOfNode + ".dat" + " deleted.");
        }
        
        arquivo = new java.io.File("rtreeGeonet2-" + sizeOfNode + ".dat");
        if(arquivo.exists())
        {
            arquivo.delete();
            System.out.println("File " + "rtreeGeonet2-" + sizeOfNode + ".dat" + " deleted.");
        }
    }
    
    public static RTree<RectLatLongCoordGeonet> lerArvore(String preixoArquivoArvore)
    {
        File workspace = new File(preixoArquivoArvore + "-" + sizeOfNode + ".dat", sizeOfNode);
        RTree<RectLatLongCoordGeonet> rtree = new RTree<RectLatLongCoordGeonet>(workspace){};
        return rtree;
    }
    
    public static RTree<RectLatLongCoordGeonet> inserirGeoNetRtree(String nomeArquivo, String preixoArquivoArvore) throws FileNotFoundException, IOException
    {
        String file = AddFindGeonetRTree.class.getClassLoader().getResource(nomeArquivo).getFile();
        RectLatLongCoordGeonet metric = new RectLatLongCoordGeonet();
        File workspace = new File(preixoArquivoArvore + "-" + sizeOfNode + ".dat", sizeOfNode);
        RTree<RectLatLongCoordGeonet> rtree1 = new RTree<RectLatLongCoordGeonet>(workspace){};
        BufferedReader in = new BufferedReader(new FileReader(file));
        int count = 0;
        
        while(in.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(in.readLine(), " ");
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            metric.setExtension(0, Double.parseDouble(tok.nextToken()));
            metric.setExtension(1, Double.parseDouble(tok.nextToken()));
            rtree1.add(metric);
            
            if(count++ % 50000 == 0) 
                System.out.print(count + " ");
        }
        
        in.close();
        in = null;
        System.gc();
        return rtree1;
    }
    
    public static boolean conferirInsercaoGeoNetRtree(String nomeArquivo, RTree<RectLatLongCoordGeonet> rtree) throws FileNotFoundException, IOException
    {
        String file = AddFindGeonetRTree.class.getClassLoader().getResource(nomeArquivo).getFile();
        RectLatLongCoordGeonet metric = new RectLatLongCoordGeonet();
        BufferedReader check = new BufferedReader(new FileReader(file));

        int notFound = 0;
        while (check.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(check.readLine(), " ");
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            metric.setExtension(0, Double.parseDouble(tok.nextToken()));
            metric.setExtension(1, Double.parseDouble(tok.nextToken()));
            if (rtree.find(metric) == null)
                notFound++;
        }

        check.close();      
        return notFound == 0;
    }
    
    public static void main(String[] args) throws Exception
    {
        System.out.println(new Date());
//        String nomeArquivoDados1 = "pontos_aleatorios1.txt";
//        String nomeArquivoDados2 = "pontos_aleatorios2.txt";
//        
//        deleteFiles();
//        
//        // Insert in R-tree 1
//        System.out.println("\nInserting data into the first r-tree.");
//        RTree<RectLatLongCoordGeonet> rtree1 = inserirGeoNetRtree(nomeArquivoDados1, "rtreeGeonet1");
//        
//        // Insert in R-tree 1
//        System.out.println("\n\nInserting data into the second r-tree.");
//        RTree<RectLatLongCoordGeonet> rtree2 = inserirGeoNetRtree(nomeArquivoDados2, "rtreeGeonet2");
//        
//        // Check if all the data has been stored correctly. 34051
//        System.out.println("\n\nChecking if all the data has been stored correctly.");
//        if(conferirInsercaoGeoNetRtree(nomeArquivoDados1, rtree1))
//            System.out.println("All data was found in rtree1.");
//        else
//            System.err.println("There are missing data in rtree1.");
//        
//        if(conferirInsercaoGeoNetRtree(nomeArquivoDados2, rtree2))
//            System.out.println("All data was found in rtree2.");
//        else
//            System.err.println("There are missing data in rtree2.");
        
        RTree<RectLatLongCoordGeonet> rtree1 = lerArvore("rtreeGeonet1");
        RTree<RectLatLongCoordGeonet> rtree2 = lerArvore("rtreeGeonet2");
        
        // Print tree heights (they should be equal).
        System.out.println("\nRtree1 height: " + rtree1.height());
        System.out.println("Rtree2 height: " + rtree2.height() + "\n");
        
        // Initialization for the join
        JoinQueries<RectLatLongCoordGeonet> joinQuery = new JoinQueries<>(rtree1, rtree2);
        ArrayList<Pair<Uuid, Uuid>> result;
        
        System.out.println("Size of buffer LRU: " + JoinQueries.sizeOfBuffer + " disk pages.\n");
        
        // Test basic Join
        System.out.println("Basic join: ");
        result = joinQuery.basicJoin();
        System.out.println("result.size(): " + result.size() + "\n");
        
        // Test basic join restricting the search space
        System.out.println("Basic join restricting the search space: ");
        result = joinQuery.basicJoinRestringindoEspacoBusca();
        System.out.println("result.size(): " + result.size() + "\n");
        
        // Test local plane-sweep order join
        System.out.println("Local plane-sweep order join: ");
        result = joinQuery.joinPlaneSweep();
        System.out.println("result.size(): " + result.size() + "\n");
        
        // Test local plane-sweep order join with pinning
        System.out.println("Local plane-sweep order join with pinning: ");
        result = joinQuery.joinPlaneSweepFixacao();
        System.out.println("result.size(): " + result.size() + "\n");
                
        // Test local z-order join
        System.out.println("Local z-order join: ");
        result = joinQuery.joinZorder();
        System.out.println("result.size(): " + result.size() + "\n");
   
        // System.out.println("20 primeiras linhas de result: ");
        // for(int i = 0; i < 20; i++)
        //     System.out.println(result.get(i).getFirst() + " " + result.get(i).getSecond());         
    }
}
