package org.obinject.joinqueries;

import java.util.ArrayList;

import org.obinject.block.RTreeNode;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.storage.EuclideanGeometry;

public class JoinQueryUtilities<R extends Rectangle<R> & Entity<? super R>> 
{
	private EuclideanGeometry<R> geometry;

	public JoinQueryUtilities(EuclideanGeometry<R> geometry)
	{
	    this.geometry = geometry;
	}
	
    public ArrayList<Pair<R, Integer>> restringirEspacoBusca(R intersecao, RTreeNode<R> nodeRtree)
    {
        ArrayList<Pair<R, Integer>> entradasRtree = new ArrayList<>();
        int totalEntriesRtree = nodeRtree.readNumberOfKeys();
        R storedKeyRtree;
       
        for(int i = 0; i < totalEntriesRtree; i++)
        {
            storedKeyRtree = nodeRtree.buildKey(i);
            if(intersecao == null || this.geometry.isOverlap(storedKeyRtree, intersecao)) 
                entradasRtree.add(new Pair<R, Integer>(storedKeyRtree, i));
        }
        
        return entradasRtree;
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
    
    private void loopInterno(Pair<R, Integer> t, int naoMarcado, ArrayList<Pair<R, Integer>> rs, ArrayList<Pair<Pair<R, Integer>, Pair<R, Integer>>> saida, Boolean primeiroLoop)
    {
        int k = naoMarcado;
        int totalEntries = rs.size();
        
        double t_xu = t.getFirst().getOrigin(0) + t.getFirst().getExtension(0);
        
        while(k < totalEntries && rs.get(k).getFirst().getOrigin(0) <= t_xu)
        {
            int dims = t.getFirst().numberOfDimensions();
            Boolean interceptaTodasDimensoes = true;
            
            for(int d = 1; d < dims; d++)
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
            {
                if(primeiroLoop)
                    saida.add(new Pair<>(t, rs.get(k)));
                else
                    saida.add(new Pair<>(rs.get(k), t));
            }
            k++;
        }
    }
    
    public EuclideanGeometry<R> getGeometry() 
    {
        return geometry;
    }

    public void setGeometry(EuclideanGeometry<R> geometry) 
    {
        this.geometry = geometry;
    }
}
