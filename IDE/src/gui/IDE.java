package gui;

import gals.*;
import simbulo.Simbolo;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IDE extends JFrame implements ActionListener {
    private JTextArea codeTextArea;
    private JTextArea outputTextArea;
    private JTextArea statusArea;
    public List<Simbolo> tabelaSimbolos = new ArrayList<>();
    private Semantico semantico; // Adicionado para manter a referência do objeto semântico

    public IDE() {
        setTitle("GUI.IDE");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Componentes
        codeTextArea = new JTextArea();
        JScrollPane codeScrollPane = new JScrollPane(codeTextArea);
        add(codeScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Área de saída
        outputTextArea = new JTextArea(8, 0); // Definindo 8 linhas de altura para a área de saída
        outputTextArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        bottomPanel.add(outputScrollPane, BorderLayout.CENTER);

        // Botão Compilar
        JButton compileButton = new JButton("Compilar");
        compileButton.addActionListener(this);
        bottomPanel.add(compileButton, BorderLayout.EAST);

        // Área de status
        statusArea = new JTextArea(2, 0); // Definindo 2 linhas de altura para a área de status
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.BOLD, 18));
        bottomPanel.add(statusArea, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // Barra de menu
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Arquivo");
        menuBar.add(fileMenu);

        JMenuItem listaSimbolosMenuItem = new JMenuItem("Lista de Símbolos");
        listaSimbolosMenuItem.addActionListener(this);
        fileMenu.add(listaSimbolosMenuItem);

        JMenuItem geraCodigoMenuItem = new JMenuItem("Gerar Código BIP");
        geraCodigoMenuItem.addActionListener(this);
        fileMenu.add(geraCodigoMenuItem);
    }
    private void showCodePreviewDialog(String code) {
        JTextArea codeTextArea = new JTextArea(code);
        codeTextArea.setLineWrap(true); // Habilitar quebra de linha
        codeTextArea.setWrapStyleWord(true); // Quebrar linha em palavras
        codeTextArea.setCaretPosition(0); // Definir a posição inicial do cursor no início do texto

        JScrollPane scrollPane = new JScrollPane(codeTextArea);
        scrollPane.setPreferredSize(new Dimension(500, 400)); // Definir um tamanho preferido para o JScrollPane

        JButton copyButton = new JButton("Copiar Código");
        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection selection = new StringSelection(code);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
                JOptionPane.showMessageDialog(IDE.this, "Código copiado para a área de transferência.");
            }
        });

        JButton saveButton = new JButton("Salvar Como TXT");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(IDE.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        writer.write(code);
                        JOptionPane.showMessageDialog(IDE.this, "Arquivo salvo com sucesso.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(IDE.this, "Erro ao salvar o arquivo.");
                    }
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(copyButton);
        buttonPanel.add(saveButton);

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        previewPanel.add(buttonPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, previewPanel, "Visualização de Código", JOptionPane.PLAIN_MESSAGE);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Importar")) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    reader.close();
                    codeTextArea.setText(stringBuilder.toString());
                    statusArea.setText("Arquivo importado com sucesso");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    statusArea.setForeground(Color.RED);
                    statusArea.setText("Erro ao importar arquivo");
                }
            }
        } else if (e.getActionCommand().equals("Salvar")) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showSaveDialog(this);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                    writer.write(codeTextArea.getText());
                    writer.close();
                    statusArea.setText("Arquivo salvo com sucesso");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    statusArea.setForeground(Color.RED);
                    statusArea.setText("Erro ao salvar arquivo");
                }
            }
        } else if (e.getActionCommand().equals("Compilar")) {
            try {
                System.out.println("------------------------------------------------------------------------------------------");
                Lexico lexico = new Lexico(codeTextArea.getText());

                Sintatico sintatico = new Sintatico();
                semantico = new Semantico(); // Inicializa a instância do Semantico

                sintatico.parse(lexico, semantico);

                tabelaSimbolos = semantico.tabelaSimbolos;
                semantico.verificarSimbolosNaoUtilizados();
                String alertasConcatenados = "Compilação bem-sucedida\n" + String.join("\n", semantico.alertas_lista);

                if (semantico.alertas_lista.isEmpty()) {
                    outputTextArea.setForeground(Color.BLACK);
                } else {
                    outputTextArea.setForeground(Color.BLUE);
                }

                outputTextArea.setText(alertasConcatenados);
                System.out.println(semantico.com_exe_BIP_lista.toString());
            } catch (LexicalError erroCopilar) {
                outputTextArea.setForeground(Color.RED);
                outputTextArea.setText("Erro Lexico: " + erroCopilar.getMessage());

            } catch (SyntaticError erroCopilar) {
                outputTextArea.setForeground(Color.RED);
                outputTextArea.setText("Erro Sintatico: " + erroCopilar.getMessage());

            } catch (SemanticError erroCopilar) {
                outputTextArea.setForeground(Color.RED);
                outputTextArea.setText("Erro Semantico: " + erroCopilar.getMessage());

            } catch (AnalysisError ex) {
                throw new RuntimeException(ex);
            }

        } else if (e.getActionCommand().equals("Lista de Símbolos")) {
            // Criando e exibindo a nova janela de lista de símbolos
            ListaSimbolosJanela listaSimbolosJanela = new ListaSimbolosJanela(tabelaSimbolos);
            listaSimbolosJanela.setVisible(true);
        } else if (e.getActionCommand().equals("Gerar Código BIP")) {
            try {
                if (semantico != null) {
                    String generatedCode = semantico.getCodigoGerado(); // Obter o código gerado do Semantico
                    showCodePreviewDialog(generatedCode);
                    /*JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showSaveDialog(this);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                            writer.write(generatedCode);
                            statusArea.setText("Código BIP salvo com sucesso");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            statusArea.setForeground(Color.RED);
                            statusArea.setText("Erro ao salvar código BIP");
                        }
                    }*/
                } else {
                    statusArea.setForeground(Color.RED);
                    statusArea.setFont(new Font("Monospaced", Font.BOLD, 18));
                    statusArea.setText("Erro: Compile o código primeiro.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                statusArea.setForeground(Color.RED);
                statusArea.setText("Erro ao gerar código BIP");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            IDE ide = new IDE();
            ide.setVisible(true);
        });
    }
}
