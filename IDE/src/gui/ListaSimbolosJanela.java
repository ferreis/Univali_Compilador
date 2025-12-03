package gui;

import simbulo.Simbolo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ListaSimbolosJanela extends JFrame {

    public ListaSimbolosJanela(List<Simbolo> tabelaSimbolos) {
        setTitle("Lista de Símbolos");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Fecha apenas esta janela
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Criando a tabela com um modelo de tabela padrão
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new String[]{"ID", "Tipo", "Iniciada", "Usada", "Escopo", "Parâmetro", "Posição", "Posição Vetor", "Tamanho Vetor", "Vetor", "Matriz", "Função"});

        // Preenchendo a tabela com os dados dos símbolos
        for (Simbolo simbolo : tabelaSimbolos) {
            model.addRow(new Object[]{
                    simbolo.id,
                    simbolo.tipo,
                    simbolo.iniciada,
                    simbolo.usada,
                    simbolo.escopo,
                    simbolo.parametro,
                    simbolo.posicao,
                    simbolo.posVetor,
                    simbolo.tamVetor,
                    simbolo.vetor,
                    simbolo.matriz,
                    simbolo.funcao
            });
        }

        // Criando a tabela com o modelo criado
        JTable table = new JTable(model);

        // Adicionando a tabela a um JScrollPane e adicionando ao conteúdo da janela
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }
}
