package org.obinject.joinqueries;

import java.util.ArrayList;

import org.obinject.block.RTreeNode;
import org.obinject.meta.Entity;
import org.obinject.meta.Rectangle;
import org.obinject.storage.EuclideanGeometry;

/**
 * Classe que implementa métodos utilizados pelos algoritmos de junção.
 * 
 * @author Luiz Emanoel Batista Moreira <emanoel@unifei.edu.br>
 * @author Joao Tonet
 * @author Joao Victor
 * @author Luiz Olmes Carvalho <olmes@unifei.edu.br>
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 */
public class JoinQueryUtilities<R extends Rectangle<R> & Entity<? super R>> 
{
    private EuclideanGeometry<R> geometry;
    private long comparisons; // Variável utilizada para contar o número de interseções calculadas
    
    /*
     * Construtor que recebe duas Árvores R de mesma altura que serão utilizadas nas operações de junção.
     * 
     * @param geometry Objeto EuclideanGeometry que realiza cálculos de interseção entre MBRs.
     * */
    public JoinQueryUtilities(EuclideanGeometry<R> geometry)
    {
        this.geometry = geometry;
    }
	
    /*
     * Método que filtra os MBRs de um nó que realizam interseção com um retângulo dado.
     * 
     * @param intersecao Retângulo de interseção.
     * @param nodeRtree  Nó de uma árvore R.
     * @return Lista de pares. Cada par é formado por um MBR e sua posição no seu nó de origem.
     */
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
    
    /*
     * Método que realiza o algoritmo plane-sweep. Primeiramente é realizado uma ordenação 
     * dos MBRs pelo seus valores "x lower". Posteriormente, é feita uma varredura espacial 
     * para encontrar todos os retângulos que se interceptam de forma eficiente. Caso o parâmetro 
     * boolean for verdadeiro, a função calcula o valor unidimensional z-order. Baseado no 
     * algoritmo "SortedIntersectionTest" de Brinkhoff. 
     * 
     * @param mbrsRtree1 Lista de Pares. Cada par é formado por um MBR e sua posição no seu nó de origem (rtree2).
     * @param mbrsRtree2 Lista de Pares. Cada par é formado por um MBR e sua posição no seu nó de origem (rtree1).
     * @param zorder Valor booleano que se verdadeiro, o método calcula o valor unidimensional utilizado pelo z-oder.
     * 
     * @return Lista de trios. Cada trio é constituído por três pares.
     *         O primeiro par é composto por um MBR e sua posição no seu nó de origem (rtree1).
     *         O segundo par é composto por um MBR e sua posição no seu nó de origem (rtree2).
     *         O terceiro par é composto pelo retângulo de interseção entre os dois MBRs e seu
     *         respectivo valor unidimensional utilizado no z-order.
     */
    public ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> planeSweep(ArrayList<Pair<R, Integer>> mbrsRtree1, ArrayList<Pair<R, Integer>> mbrsRtree2, boolean zorder)
    {
        int totalEntries1 = mbrsRtree1.size();
        int totalEntries2 = mbrsRtree2.size();
        int i = 0;
        int j = 0;
        
        // Ordenar entradas pelo x lower dos MBRs
        mbrsRtree1.sort((o1, o2) -> Double.compare(o1.getFirst().getOrigin(0), o2.getFirst().getOrigin(0)));
        mbrsRtree2.sort((o1, o2) -> Double.compare(o1.getFirst().getOrigin(0), o2.getFirst().getOrigin(0)));

        ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> saida = new ArrayList<>();

        while(i < totalEntries1 && j < totalEntries2)
        {
            if(mbrsRtree1.get(i).getFirst().getOrigin(0) <= mbrsRtree2.get(j).getFirst().getOrigin(0))
            {
                loopInterno(mbrsRtree1.get(i), j, mbrsRtree2, saida, true, zorder);
                i++;
            }
            else
            {
                loopInterno(mbrsRtree2.get(j), i, mbrsRtree1, saida, false, zorder);
                j++;
            }
        }  
        
        return saida;
    }
    
    /*
     * Método auxiliar para realização do algoritmo plane sweep. Dado um retângulo de um nó de uma rtree,
     * a função encontra todas as interseções com os retângulos do nó da outra rtree. Baseado no algoritmo 
     * "InternatLoop" de Brinkhoff.
     * 
     * @param t Retângulo de um nó, pode ser da rtee1 ou rtree2.
     * @param naoMarcado Primeira posição ainda não checada da lista de retângulos.
     * @param rs Lista de retângulos da outra rtree.
     * @param saida Lista de trios que será o retorno da função "planeSweep".
     * @param primeiroLoop Valor boolean utilizado para inserir de forma ordenada na lista "saida".
     * @param zorder Valor booleano que se verdadeiro, o método calcula o valor unidimensional utilizado pelo z-oder.
     */
    private void loopInterno(Pair<R, Integer> t, int naoMarcado, ArrayList<Pair<R, Integer>> rs, ArrayList<Triple<Pair<R, Integer>, Pair<R, Integer>, Pair<R, Long>>> saida, boolean primeiroLoop, boolean zorder)
    {
        int k = naoMarcado;
        int totalEntries = rs.size();
        
        double t_xu = t.getFirst().getOrigin(0) + t.getFirst().getExtension(0);
        
        while(k < totalEntries && rs.get(k).getFirst().getOrigin(0) <= t_xu)
        {
            this.comparisons++;
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
                Long zOrderUnidimensional = null;
                R intersecao = this.geometry.intersection(t.getFirst(), rs.get(k).getFirst());
                
                if(zorder)
                    zOrderUnidimensional = zOrder(intersecao, dims);

                if(primeiroLoop)
                    saida.add(new Triple<>(t, rs.get(k), new Pair<>(intersecao, zOrderUnidimensional)));
                else
                    saida.add(new Triple<>(rs.get(k), t, new Pair<>(intersecao, zOrderUnidimensional)));
            }
            k++;
        }
    }
    
    /*
     * Método que recebe um retângulo multidimensional e retorna um valor unidimensional que representa
     * seu ponto central. É com base nesse valor que o z-order é realizado. Esse valor é construído a 
     * partir da alternância dos bits das coordenadas do ponto central.
     * 
     * Observação: O ideal é que o ponto central possua coordenadas positivas e inteiras. Caso não possuir,
     * o valor é truncado e retirado seu sinal.
     * 
     * @param intersecao  Retângulo multidimensional.
     * @param dims Número de dimensões do retângulo.
     * @return Valor unidimensional que representa o ponto central do retângulo multidimensional.
     * */
    private Long zOrder(R intersecao, int dims) 
    {
        Long resultado = 0L;
        int numBits = 64, aux = 0;
        long coordenada;

        for (int bit = 0; bit < numBits; bit++) 
        {
            coordenada = (long)((Math.abs(intersecao.getOrigin(bit % dims)*2) + intersecao.getExtension(bit % dims))/2.0); // Centro do retangulo de intersecao (Truncar parte fracionaria)
            resultado |= (coordenada & (1L << aux)) << (bit - aux);
            if((bit + 1) % dims == 0)
                aux++;
        }

        return resultado;
    }
    
    public EuclideanGeometry<R> getGeometry() 
    {
        return geometry;
    }

    public void setGeometry(EuclideanGeometry<R> geometry) 
    {
        this.geometry = geometry;
    }

    public long getComparisons() 
    {
        return comparisons;
    }

    public void setComparisons(long comparisons) 
    {
        this.comparisons = comparisons;
    }
}
