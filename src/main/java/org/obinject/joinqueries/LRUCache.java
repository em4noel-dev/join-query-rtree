package org.obinject.joinqueries;

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.obinject.block.Node;

/*
 * Classe que implementa as funcionaldades de um buffer que
 * utiliza a estratégia Least-Recently-Used (LRU). Ao adicionar
 * um novo elemento no buffer lotado, o elemento menos usado 
 * recentemente é substituído. Essa estrutura usa o esquema
 * de chave, valor. A chave é o id da página de disco e o valor
 * é o Nó lido da página de disco.
 * 
 * Reference: https://stackoverflow.com/questions/23772102/lru-cache-in-java-with-generics-and-o1-operations
 * 
 * @author Luiz Emanoel Batista Moreira <emanoel@unifei.edu.br>
 * @author Joao Tonet
 * @author Joao Victor
 * @author Luiz Olmes Carvalho <olmes@unifei.edu.br>
 * @author Enzo Seraphim <seraphim@unifei.edu.br>
 */
public class LRUCache 
{
    private int capacity;
    private LinkedHashMap<String, Node> map;

    /*
     * Construtor de um buffer LRU.
     * 
     * @param capacity Capacidade em páginas de disco do buffer LRU
     */
    public LRUCache(int capacity) 
    {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(capacity, 0.75f, true);
    }
    
    /*
     * Recupera um novo elemento do buffer a partir de um id de página de disco. 
     * Se o elemento existir, ele é atualizado como mais recentemente utilizado.
     * Se o elemento não existir, é retornado null.
     * 
     * @param key String que representa o id da página de disco. Formato: "pageId-RtreeNumber"
     * @return Caso o elemento esteja presente no buffer é retornado o nó representado por key.
     *         Caso contrário retorna null.
     * */
    public Node get(String key) 
    {
        Node value = this.map.get(key);
        return value;
    }
    
    /*
     * Insere um novo elemento no buffer, que se torna o dado mais recentemente utilizado.
     * 
     * @param key String que representa o id da página de disco. Formato: "pageId-RtreeNumber"
     * @param value Nó da árvore que foi lida de uma página de disco.
     */
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