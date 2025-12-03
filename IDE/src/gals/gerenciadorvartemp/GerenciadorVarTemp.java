package gals.gerenciadorvartemp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedList;

public class GerenciadorVarTemp {
    private Map<String, Integer> variaveisTemporarias = new LinkedHashMap<>();
    private LinkedList<String> tempAtivas = new LinkedList<>();
    private int proximoIndice = 1000;

    public String obterTemp() {
        // Itera sobre o mapa para encontrar a primeira chave livre
        for (Map.Entry<String, Integer> entrada : variaveisTemporarias.entrySet()) {
            if (entrada.getValue() == 0) {
                variaveisTemporarias.put(entrada.getKey(), 1);  // Marca como ocupada
                String novaVar = entrada.getKey();
                tempAtivas.add(novaVar);
                return novaVar;
            }
        }
        // Se não encontrar nenhuma chave livre, cria uma nova
        String novaVar = String.valueOf(proximoIndice++);
        tempAtivas.add(novaVar);
        variaveisTemporarias.put(novaVar, 1);  // Marca como ocupada
        return novaVar;
    }

    public String usarTemp() {
        String var = tempAtivas.poll(); // Remove and return the first element
        marcarComoLivre(var);
        return var;
    }

    public String usarUltimaTemp() {
        String var = tempAtivas.pollLast(); // Remove and return the last element
        marcarComoLivre(var);
        return var;
    }

    public String usarPenUltimaTemp() {
        String var = tempAtivas.get(tempAtivas.size() - 2);
        tempAtivas.remove(tempAtivas.size() - 2);
        marcarComoLivre(var);
        return var;
    }

    public void marcarComoLivre(String chave) {
        if (variaveisTemporarias.containsKey(chave)) {
            variaveisTemporarias.put(chave, 0);  // Marca como livre
        }
    }
}
