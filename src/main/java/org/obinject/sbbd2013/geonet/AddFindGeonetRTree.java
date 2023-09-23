package org.obinject.sbbd2013.geonet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;
import org.obinject.block.RTreeIndex;
import org.obinject.device.File;
import org.obinject.meta.Uuid;
import org.obinject.storage.RTree;

public class AddFindGeonetRTree
{

    private static final int sizeOfNode = 16384;

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        System.out.println(new Date());
        int count=0;
        RectLatLongCoordGeonet metric = new RectLatLongCoordGeonet();
//		Memory workspace = new Memory(sizeOfNode);
        File workspace = new File("rtreeGeonet-" + sizeOfNode + ".dat", sizeOfNode);
        RTree<RectLatLongCoordGeonet> rtree = new RTree<RectLatLongCoordGeonet>(workspace)
        {
        };
        
        String nomeArq = AddFindGeonetRTree.class.getClassLoader().getResource("geonet.txt").getFile();
        BufferedReader in = new BufferedReader(new FileReader(nomeArq));
        int i = 0;
        while (in.ready() == true)// && i++ < 20)
        {
            StringTokenizer tok = new StringTokenizer(in.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
//            metric.setExtension(0, metric.getOrigin(0)+0.01);
//            metric.setExtension(1, metric.getOrigin(0)+0.01);
            
            rtree.add(metric);
            //seq.add(metric);
			if(count++ % 1000 == 0) System.out.println(count);
        }

        //rtree.bfs();

        in.close();
        in = null;
        System.gc();

        System.out.println("size of page: " + workspace.sizeOfArray());
        System.out.println("time to insert: " + rtree.getAverageForAdd().measuredTime());
        System.out.println("calculos de distancia: " + rtree.getAverageForAdd().measuredVerifications());
        System.out.println("acesso a blocos: " + rtree.getAverageForAdd().measuredDiskAccess());

        BufferedReader check = new BufferedReader(new FileReader(nomeArq));

        int notFound = 0;
        count = 0;
// 2714 - 29172
        while (check.ready() == true)
        {
            StringTokenizer tok = new StringTokenizer(check.readLine(), "\t");
            tok.nextToken();
            metric.setOrigin(0, Double.parseDouble(tok.nextToken()));
            metric.setOrigin(1, Double.parseDouble(tok.nextToken()));
            if (rtree.find(metric) == null)
            {
                notFound++;
            }
//          if(count++ % 1000 == 0) System.out.println(count);
        }

        check.close();
        System.out.println("not found: " + notFound + " objects");
        System.out.println("time to insert: " + rtree.getAverageForFind().measuredTime());
        System.out.println("calculos de distancia: " + rtree.getAverageForFind().measuredVerifications());
        System.out.println("acesso a blocos: " + rtree.getAverageForFind().measuredDiskAccess());
        
        
        
    }
}

/*
 run:
Fri May 18 16:31:23 BRT 2012
time to insert: 55693
num de mbr: 19208
acesso a blocos: 1819911
size of page: 8192
altura: 3
time to find: 54270
not found: 519 objects
acesso a blocos: 2415426
CONSTRUÍDO COM SUCESSO (tempo total: 1 minuto 50 segundos)
 */