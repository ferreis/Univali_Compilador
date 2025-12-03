package simbulo;

import java.util.List;

public class Simbolo {

    public String id;
    public String tipo;
    public Boolean iniciada;
    public Boolean usada;
    public int escopo = 0;
    public Boolean parametro;
    public int posicao;
    public int posVetor;

    public int tamVetor;
    public Boolean vetor;
    public Boolean matriz;
    public Boolean funcao;
    public String nomeFuncao;

    public boolean simboloJaDeclarado(String id, List<Integer> escopoLista,List<Simbolo> tabelaSimbolos) {
        for (Simbolo s : tabelaSimbolos) {
            if ((s.id).equals(id) && (escopoLista.contains(s.escopo)|| 0==s.escopo)) {
                return true;
            }
        }
        return false;
    }

    public boolean ehSimbulo(String id,List<Simbolo> tabelaSimbolos) {
        for (Simbolo s : tabelaSimbolos) {
            if ((s.id).equals(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean ehVetor(String id,List<Simbolo> tabelaSimbolos) {
        for (Simbolo s : tabelaSimbolos) {
            if ((s.id).equals(id) & s.vetor) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Simbolo{" +
                "id='" + id + '\'' +
                ", tipo='" + tipo + '\'' +
                ", iniciada=" + iniciada +
                ", usada=" + usada +
                ", escopo=" + escopo +
                ", parametro=" + parametro +
                ", posicao=" + posicao +
                ", posVetor=" + posVetor +
                ", tamVetor=" + tamVetor +
                ", vetor=" + vetor +
                ", matriz=" + matriz +
                ", funcao=" + funcao +
                ", nomeFuncao='" + nomeFuncao + '\'' +
                '}';
    }

    public  String getIdParametro(String funcao, int pos,List<Simbolo> tabelaSimbolos) {

        for (Simbolo s : tabelaSimbolos) {
            if (s.nomeFuncao.equals(funcao) & s.posicao==pos & s.parametro) {
                return s.id + "_" + s.nomeFuncao;
            }
        }
        return null;
    }

    private  String remSublinhado(String entrada) {

        if (entrada == null) {
            return null; // Retorna null se a string de entrada for null
        }

        int indiceSublinhado = entrada.indexOf('_');
        if (indiceSublinhado != -1) {
            return entrada.substring(0, indiceSublinhado);
        }

        return entrada;
    }

    public  boolean equalsID(String id1, String id2) {
        id1= remSublinhado(id1);
        id2= remSublinhado(id2);
        return id1.equals(id2);
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Simbolo)) {
            return false;
        }

        Simbolo s = (Simbolo) obj;

        if (this.id.contains("_") & !s.id.contains("_")) {
            return this.id.equals(s.id + "_" + s.escopo) && this.escopo == s.escopo;
        } else if (!this.id.contains("_") & s.id.contains("_")) {
            return (this.id + "_" + this.escopo).equals(s.id) && this.escopo == s.escopo;
        }
        return this.id.equals(s.id) && this.escopo == s.escopo;
    }
}
