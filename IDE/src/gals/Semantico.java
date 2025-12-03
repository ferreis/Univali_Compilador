package gals;

import gals.gerenciadorvartemp.GerenciadorVarTemp;
import simbulo.Simbolo;

import java.util.*;
import java.util.stream.IntStream;

public class Semantico implements Constants {
    private StringBuilder codigoGerado = new StringBuilder();
    private boolean dataHeaderAdded = false;

    private Stack<List<String>> valores_lista = new Stack<>();
    private Stack<List<String>> ope_lista = new Stack<>();
    private Stack<String> flag_lista = new Stack<>();

    public List<String> com_exe_BIP_lista = new ArrayList<>();
    public List<Simbolo> tabelaSimbolos = new ArrayList<>();
    public List<Integer> escopo_lista = new ArrayList<>();
    public List<String> alertas_lista = new ArrayList<>();
    public List<String> funcao_lista = new ArrayList<>();
    public Map<String, Integer> parametrosFuncoes = new HashMap<>();
    public GerenciadorVarTemp gerenciadorVarTemp = new GerenciadorVarTemp();
    private Deque<String> operacao_pilha= new ArrayDeque<>();
    public int proxEscopo = 1;
    String nomeFuncao;
    String nomeFuncaoAtual;
    String tipo;
    String complementoVariavelFuncao = "";
    //FLAGS
    boolean atri_vetor = false;
    boolean atri_vari = false;
    boolean bloq_sti = false;
    int numFlag = 0;
    int posParametro;
    int qtdParametros;
    //Temp
    String ultimaVariavel;
    String ultimaRel;
    String ultimaVarAtri;
    String ultimaFuncao;

    //Método de tratamento de ações semânticas, esse método vai ter um switch case para resolver
    //tudo o que está declarado no gals
    public void executeAction(int action, Token token) throws SemanticError {

        //System.out.println("Ação #"+action+", Token: "+token);

        switch (action) {

            case 1: // <type>
                this.tipo = token.getLexeme();
                break;
            case 2: // id | variável
                addVariavel(token);
                break;
            case 3: // id | vetor
                bloq_sti = true;
                addVariavelVetor(token);
                break;
            case 4: // id | parâmetro
                addVariavelParametro(token);
                break;
            case 5: // id | função
                addDeclaracaoFuncao(token);

                //Gera o rótulo de início da rotina
                nomeFuncao = token.getLexeme();
                com_exe_BIP_lista.add("_" + nomeFuncao + ":");
                break;
            case 6: // id | atri variável
                this.atri_vari = true;
                ultimaVarAtri = token.getLexeme();
                break;
            case 7: // id | atri vetor
                this.atri_vetor = true;
                ultimaVarAtri = token.getLexeme();
                break;
            case 8: // inicializa a ultima variavel acessada
                ultimaVarAtri = ultimaVariavel;

                break;
            case 9: // seta o tamanho do ultimo vetor acessado
                confeTipo("int");
                if (this.atri_vari){
                    setPossicaoVetor(token);
                } else{
                    setTamDeclaVetor(token);
                }
                this.atri_vetor= false;
                this.atri_vari = false;
                bloq_sti = false;
                break;

            case 10: // RESETA POSICAO PARAMETRO
                this.posParametro = 0;
                break;
            case 11: // desativa parametro de atribuicao
                this.atri_vetor = false;
                this.atri_vari = false;


                // atribVariavel(); nao apagar nao sei o que desativei
                break;
            case 12: // adiciona quantidade de parametros na função
                addQuantidadeParametrosFuncoes();
                complementoVariavelFuncao = "_" + nomeFuncao;
                break;
            case 13: // pega o nome da chamada da função
                this.nomeFuncaoAtual =  token.getLexeme();
                break;
            case 14: // adiciona na flag, quantos parametros tem na chamada de função
                this.qtdParametros++;
                String ultimoElemento = com_exe_BIP_lista.get(com_exe_BIP_lista.size() - 1);
                if (ultimoElemento.startsWith("STO")) {
                    com_exe_BIP_lista.remove(com_exe_BIP_lista.size() - 1);
                }
                com_exe_BIP_lista.add("STO " + new Simbolo().getIdParametro(nomeFuncaoAtual,qtdParametros,tabelaSimbolos));
                break;
            case 15: // verifica quantidade de parametros na chamada da função
                verificaQuantidadeParametros();
                this.qtdParametros = 0;
                com_exe_BIP_lista.add("CALL _" + nomeFuncaoAtual);
                break;
            case 16:
                gerarComando();
                break;
            case 17:
                valores_lista.push(new ArrayList<>());
                ope_lista.push(new ArrayList<>());
                break;
            case 18: // seta o tamanho do ultimo vetor acessado
                confeTipo("int");
                if (this.atri_vetor){
                    setTamanhoVetor(token);
                    //Salva a posicao em um temp
                    com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                } else{
                    //Se nao for atribuicao ja salva no $indr
                    com_exe_BIP_lista.add("STO $indr");
                    setPossicaoVetor(token);
                }
                this.atri_vetor= false;
                this.atri_vari = false;
                break;
            case 20: // Valor decimal.
            case 21: // Valor inteiro binário.
            case 22: // Valor inteiro hexadecimal.
                addOpeValor("int");
                this.valores_lista.peek().add(token.getLexeme());
                break;
            case 23: // Valor float.
                addOpeValor("float");
                this.valores_lista.peek().add(token.getLexeme());
                break;
            case 24: // Identificador.
            case 25: // Identificador com declaração de vetor.\
                indeVariEAddOpe(token);
                this.valores_lista.peek().add(token.getLexeme());
                break;
            case 26: // String.
                addOpeValor("string");
                this.valores_lista.peek().add(token.getLexeme());
                break;
            case 27: // Char.
                addOpeValor("char");
                this.valores_lista.peek().add(token.getLexeme());
                break;
            case 28: // Função.
                indeFuncEAddOpe();
                break;
            case 29: // bool.
                addOpeValor("bool");
                this.valores_lista.peek().add(token.getLexeme());
                break;


            case 80: // BIT_SHIFT_RIGHT.
                this.ope_lista.peek().add("SRL");

                resultadoTipo("BIT");
                break;
            case 81: // BIT_SHIFT_LEFT.
                this.ope_lista.peek().add("SLL");
                resultadoTipo("BIT");
                break;
            case 34: // Operador Bitwise AND.
            case 33: // Operador Bitwise XOR.
            case 32: // Operador Bitwise OR.
                resultadoTipo("BIT");
                break;
            case 70: // RelacionaL MAIOR.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "MAIOR";
                break;
            case 74: // RelacionaL IGUAL_RELACIONAL.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "IGUAL_RELACIONAL";
                break;
            case 73: // RelacionaL MENOR_IGUAL.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "MENOR_IGUAL";
                break;
            case 71: // RelacionaL MENOR.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "MENOR";
                break;
            case 72: // RelacionaL MAIOR_IGUAL.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "MAIOR_IGUAL";
                break;
            case 75: // RelacionaL DIFERENTE.
                resultadoTipo("REL");
                this.ope_lista.peek().add("REL");
                ultimaRel = "DIFERENTE";
                break;

            case 31: // Operador Lógico AND.
                resultadoTipo("REL");
                this.ope_lista.peek().add("AND");
                break;
            case 30: // Operador Lógico OR.
                resultadoTipo("REL");
                this.ope_lista.peek().add("OR");
                break;
            case 37: // Operador Adição.
                resultadoTipo("SUM");
                this.ope_lista.peek().add("SUM");

                break;
            case 38: // Operador Subtração.
                resultadoTipo("SUB");
                this.ope_lista.peek().add("SUB");
                break;
            case 39: // Operador Multiplicação
                resultadoTipo("MUL");
                this.ope_lista.peek().add("MUL");
                break;
            case 40: // Operador de Divisão.
                resultadoTipo("DIV");
                this.ope_lista.peek().add("DIV");
                break;
            case 41: // Operador MOD.
                resultadoTipo("MOD");
                this.ope_lista.peek().add("MOD");
                break;
            case 42: // Operador de Negação.
                confeTipo("bool");
                addOpeValor("bool");

                break;
            case 43: // loop e condicionais.
                confeTipo("bool");
                break;
            case 44: // retorno
                confeRetornoFunc();
                break;
            case 45: // atribuição a variavel.
                confereAtribuicao();
                atribVariavel();

                break;
            case 50: // iniciar escopo
                initEscopo(token);
                break;
            case 51: // finalisar escopo
                finaEscopo(token);
                break;
            case 52: // finaliza escopo procedimento
                com_exe_BIP_lista.add("RETURN 0");
                complementoVariavelFuncao = "";

                if (com_exe_BIP_lista.contains("INICIO:")) {
                    com_exe_BIP_lista.remove("INICIO:");
                }

                com_exe_BIP_lista.add("INICIO:");
                break;
            case 100: // iniciar escopo
                com_exe_BIP_lista.add("LD      $in_port");
                break;
            case 101: // finalisar escopo
                com_exe_BIP_lista.add("STO     $out_port");
                break;
            case 110: // flag apos o () do if
                String flag;
                flag = "if" + numFlag++;
                flag_lista.add(flag);
                if (Objects.equals(ultimaRel, "MAIOR")){

                    com_exe_BIP_lista.add("BLE " + flag);

                }else if (Objects.equals(ultimaRel, "IGUAL_RELACIONAL")){
                    com_exe_BIP_lista.add("BNE " + flag);
                }else if (Objects.equals(ultimaRel, "MENOR_IGUAL")){
                    com_exe_BIP_lista.add("BGT " + flag);
                }else if (Objects.equals(ultimaRel, "MAIOR_IGUAL")){
                    com_exe_BIP_lista.add("BLT " + flag);
                }else if (Objects.equals(ultimaRel, "MENOR")){
                    com_exe_BIP_lista.add("BGE " + flag);
                }else if (Objects.equals(ultimaRel, "DIFERENTE")){
                    com_exe_BIP_lista.add("BEQ " + flag);
                }
                else {
                    com_exe_BIP_lista.add("Erro ao relacionar");
                }
                ultimaRel = "";
                break;
            case 111:
                String flag3;
                flag3 = "if" + numFlag++;
                com_exe_BIP_lista.add("JMP " + flag3);
                com_exe_BIP_lista.add(flag_lista.pop() + ":");
                flag_lista.add(flag3);
                if (Objects.equals(ultimaRel, "MAIOR")){

                    com_exe_BIP_lista.add("BLE " + flag3);

                }else if (Objects.equals(ultimaRel, "IGUAL_RELACIONAL")){
                    com_exe_BIP_lista.add("BNE " + flag3);
                }else if (Objects.equals(ultimaRel, "MENOR_IGUAL")){
                    com_exe_BIP_lista.add("BGT " + flag3);
                }else if (Objects.equals(ultimaRel, "MAIOR_IGUAL")){
                    com_exe_BIP_lista.add("BLT " + flag3);
                }else if (Objects.equals(ultimaRel, "MENOR")){
                    com_exe_BIP_lista.add("BGE " + flag3);
                }else if (Objects.equals(ultimaRel, "DIFERENTE")){
                    com_exe_BIP_lista.add("BEQ " + flag3);
                }
                else {
                    com_exe_BIP_lista.add("Erro ao relacionar");
                }
                ultimaRel = "";
                break;
            case 112:
                String flag2;
                flag2 = "if" + numFlag++;
                com_exe_BIP_lista.add("JMP " + flag2);
                com_exe_BIP_lista.add(flag_lista.pop() + ":");
                flag_lista.add(flag2);
                break;
            case 113:
                com_exe_BIP_lista.add(flag_lista.pop() + ":");
                break;
            case 114:
                String flag5;
                flag5 = "wh" + numFlag++;
                com_exe_BIP_lista.add(flag5 + ":");
                flag_lista.add(flag5);
                break;
            case 115: // flag apos o () do if
                String flag4;
                flag4 = "wh" + numFlag++;
                flag_lista.add(flag4);
                if (Objects.equals(ultimaRel, "MAIOR")){

                    com_exe_BIP_lista.add("BLE " + flag4);

                }else if (Objects.equals(ultimaRel, "IGUAL_RELACIONAL")){
                    com_exe_BIP_lista.add("BNE " + flag4);
                }else if (Objects.equals(ultimaRel, "MENOR_IGUAL")){
                    com_exe_BIP_lista.add("BGT " + flag4);
                }else if (Objects.equals(ultimaRel, "MAIOR_IGUAL")){
                    com_exe_BIP_lista.add("BLT " + flag4);
                }else if (Objects.equals(ultimaRel, "MENOR")){
                    com_exe_BIP_lista.add("BGE " + flag4);
                }else if (Objects.equals(ultimaRel, "DIFERENTE")){
                    com_exe_BIP_lista.add("BEQ " + flag4);
                }
                else {
                    com_exe_BIP_lista.add("Erro ao relacionar");
                }
                ultimaRel = "";
                break;
            case 116:
                String flag6;
                flag6 = flag_lista.pop();
                com_exe_BIP_lista.add("JMP " + flag_lista.pop());
                com_exe_BIP_lista.add(flag6 + ":");
                break;
            case 117:
                String flag7;
                flag7 = "dw" + numFlag++;
                com_exe_BIP_lista.add(flag7 + ":");
                flag_lista.add(flag7);
                break;
            case 118: // flag apos o () do if
                String flag8;
                flag8 = flag_lista.pop();
                if (Objects.equals(ultimaRel, "MAIOR")){
                    com_exe_BIP_lista.add("BGT  " + flag8);
                }else if (Objects.equals(ultimaRel, "IGUAL_RELACIONAL")){
                    com_exe_BIP_lista.add("BEQ  " + flag8);
                }else if (Objects.equals(ultimaRel, "MENOR_IGUAL")){
                    com_exe_BIP_lista.add("BLE  " + flag8);
                }else if (Objects.equals(ultimaRel, "MAIOR_IGUAL")){
                    com_exe_BIP_lista.add("BGE  " + flag8);
                }else if (Objects.equals(ultimaRel, "MENOR")){
                    com_exe_BIP_lista.add("BLT  " + flag8);
                }else if (Objects.equals(ultimaRel, "DIFERENTE")){
                    com_exe_BIP_lista.add("BNE " + flag8);
                }
                else {
                    com_exe_BIP_lista.add("Erro ao relacionar");
                }
                ultimaRel = "";
                break;
            case 119:
                String flag9;
                flag9 = "fo" + numFlag++;
                com_exe_BIP_lista.add(flag9 + ":");
                flag_lista.add(flag9);
                break;
            case 120: // flag apos o () do if
                String flag10;
                flag10 = "fo" + numFlag++;
                flag_lista.add(flag10);
                if (Objects.equals(ultimaRel, "MAIOR")){

                    com_exe_BIP_lista.add("BLE " + flag10);

                }else if (Objects.equals(ultimaRel, "IGUAL_RELACIONAL")){
                    com_exe_BIP_lista.add("BNE " + flag10);
                }else if (Objects.equals(ultimaRel, "MENOR_IGUAL")){
                    com_exe_BIP_lista.add("BGT " + flag10);
                }else if (Objects.equals(ultimaRel, "MAIOR_IGUAL")){
                    com_exe_BIP_lista.add("BLT " + flag10);
                }else if (Objects.equals(ultimaRel, "MENOR")){
                    com_exe_BIP_lista.add("BGE " + flag10);
                }else if (Objects.equals(ultimaRel, "DIFERENTE")){
                    com_exe_BIP_lista.add("BEQ " + flag10);
                }
                else {
                    com_exe_BIP_lista.add("Erro ao relacionar");
                }
                ultimaRel = "";
                break;
            case 121:
                String flag11;
                flag11 = flag_lista.pop();
                com_exe_BIP_lista.add("JMP " + flag_lista.pop());
                com_exe_BIP_lista.add(flag11 + ":");
                break;
        }
    }

    private  void gerarComando() {
        List<String> valores_temp = valores_lista.pop();

        if (valores_temp.isEmpty()){
            return;
        }

        List<String> ope_temp = ope_lista.pop();
        String primeiroElemento = valores_temp.get(0);
        valores_temp.remove(0);
        String segundoElemento = "";
        boolean primeiroEhVetor = false;
        boolean segundoEhVetor = false;
        //verifica se o primeiro elemento é vetor
        if (new Simbolo().ehVetor(primeiroElemento, this.tabelaSimbolos)) {
            primeiroEhVetor = true;
        }
        //verifica se o segundo elemento existe
        if (!valores_temp.isEmpty() ) {
            segundoElemento = valores_temp.get(0);
            valores_temp.remove(0);
            //verifica se o segundo elemento é vetor
            if (new Simbolo().ehVetor(segundoElemento, this.tabelaSimbolos)) {
                segundoEhVetor = true;
                //Se somente o segundo elemento é vetor é carregado antes do primeiro e salvo em temp
                if (!primeiroEhVetor) {
                    com_exe_BIP_lista.add("LDV " + segundoElemento + complementoVariavelFuncao);
                    com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                }
                else {
                    //Se os dois são vetores
                    //pega a posicao aonde ja foi incerido o calculo da posicao do vetor para o LDV E STO ficar antes desse calculo
                    //Salvando os dois valores em temp
                    //E no final colocar o primeiro elemento
                    int ultimaPosicaoLD = IntStream.range(0, com_exe_BIP_lista.size())
                            .filter(i -> com_exe_BIP_lista.get(i).startsWith("LD"))
                            .reduce((first, second) -> second)
                            .orElse(-1);
                    com_exe_BIP_lista.add(ultimaPosicaoLD,"LDV " + primeiroElemento + complementoVariavelFuncao);
                    com_exe_BIP_lista.add(ultimaPosicaoLD,"STO " + gerenciadorVarTemp.obterTemp());
                    com_exe_BIP_lista.add("LDV " + segundoElemento + complementoVariavelFuncao);
                    com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                    com_exe_BIP_lista.add("LD " + gerenciadorVarTemp.usarPenUltimaTemp());
                }
            }
        }
        if (!bloq_sti) {
            //Carrega o primeiro simbulo se ambos não são vetores
            if (new Simbolo().ehSimbulo(primeiroElemento, this.tabelaSimbolos)) {
                if (primeiroEhVetor & !segundoEhVetor) {
                    com_exe_BIP_lista.add("LDV " + primeiroElemento + complementoVariavelFuncao);
                }
                else if (!segundoEhVetor){
                    com_exe_BIP_lista.add("LD " + primeiroElemento + complementoVariavelFuncao);
                }
            } else {

                com_exe_BIP_lista.add("LDI " + primeiroElemento);

            }
        }
        Collections.reverse(ope_temp);
        for (String ope : ope_temp) {
            String comando = "";
            boolean aceitaAddI = false;
            boolean relacional = false;
            if (Objects.equals(ope, "SUM")){
                comando = "ADD";
                aceitaAddI = true;
            } else if (Objects.equals(ope, "SUB")) {
                comando = "SUB";
                aceitaAddI = true;
            } else if (Objects.equals(ope, "SLL")) {
                comando = ope;
            }else if (Objects.equals(ope, "SRL")) {
                comando = ope;
            }else if (Objects.equals(ope, "AND")) {
                comando = ope;
                aceitaAddI = true;
            }else if (Objects.equals(ope, "OR")) {
                comando = ope;
                aceitaAddI = true;
            }else  if (Objects.equals(ope, "REL")) {
                relacional = true;

            }else{
                comando = " OPERACAO INCOPATIVEL " + ope + " ";
            }


            if (!Objects.equals(segundoElemento, "")) {
                if (!bloq_sti){
                    //Quando o ultimo é vetor faz a operação com o que esta na memoria e o ultimo temp e salva em temp
                    if (segundoEhVetor){
                        com_exe_BIP_lista.add(comando + " " + gerenciadorVarTemp.usarUltimaTemp());
                        com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                    }
                    //Quando o segundo é variavel não coloca o I, se a ope não aceita o I não coloca
                    else if (relacional) {
                        com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                        if (new Simbolo().ehSimbulo(segundoElemento, this.tabelaSimbolos)) {
                            com_exe_BIP_lista.add("LD " + segundoElemento + complementoVariavelFuncao);
                        } else {
                            com_exe_BIP_lista.add("LDI " + segundoElemento);
                        }com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                        com_exe_BIP_lista.add("LD " + gerenciadorVarTemp.usarTemp());
                        com_exe_BIP_lista.add("SUB  " + gerenciadorVarTemp.usarTemp());
                    } else if (new Simbolo().ehSimbulo(segundoElemento, this.tabelaSimbolos)) {
                        com_exe_BIP_lista.add(comando + " " + segundoElemento + complementoVariavelFuncao);
                    } else if (aceitaAddI) {
                        com_exe_BIP_lista.add(comando + "I " + segundoElemento);
                    } else {
                        com_exe_BIP_lista.add(comando + " " + segundoElemento);
                    }
                }


            } else {
                System.out.println("A pilha ou a lista dentro da pilha está vazia.");
            }

        }

    }




    private void confeRetornoFunc() throws SemanticError {
        String tipo = getTipoId(funcao_lista.get(escopo_lista.size() - 1));
        confeTipo(tipo);
        System.out.println("confeRetornoFunc: " + funcao_lista.get(escopo_lista.size() - 1) + " pilha: " + operacao_pilha.toString() + "lista funcao : " + funcao_lista.toString() + " escopo "+ escopo_lista.get(escopo_lista.size() - 1));
    }

    private void confereAtribuicao() throws SemanticError {
        confeTipo(tipo);
    }

    private void resultadoTipo(String op) throws SemanticError,IllegalArgumentException {
        String tp1 = this.operacao_pilha.pop();
        String tp2 = this.operacao_pilha.pop();
        String res = SemanticTable.resultType(tp1, tp2, op);
        if (Objects.equals(res, SemanticTable.ERR))
        {
            throw new SemanticError("Tipos Incopatíveis:  " + tp1 + " " + op + " " + tp2);
        }
        System.out.println("resultadoTipo: " + tp1 + " " + op + " " + tp2 + " = " + res);
        addOpeValor(res);
    }

    private void confeTipo(String tp1) throws SemanticError,IllegalArgumentException  {
        if (Objects.equals(tp1, "")){
            tp1 = this.operacao_pilha.pop();
        }


        String tp2 = this.operacao_pilha.pop();
        String res = SemanticTable.atribType(tp1, tp2);
        if (Objects.equals(res, SemanticTable.ERR))
        {
            throw new SemanticError("Tipos Incopatíveis:  " + tp1 + " = " + tp2);
        } else if (Objects.equals(res, SemanticTable.WAR) ){
            this.alertas_lista.add("Podem ocorrer perdas de dados ao atribuir ("+this.ultimaVariavel+")" + tp2 + "  para " + tp1);
        }

        System.out.println("resultadoTipo: " + tp1 + " == " + tp2 + " == " + res);
    }

    private void indeFuncEAddOpe() throws SemanticError {
        confSeAtriguidaESetUsada(ultimaFuncao);
        String tipo = getTipoId(ultimaFuncao);
        addOpeValor(tipo);
    }

    private void indeVariEAddOpe(Token token) throws SemanticError {
        //this.atri_vari = true;
        usarVariavel(token);
        String id;
        String tipo;
        id = token.getLexeme();
        tipo = getTipoId(id);
        addOpeValor(tipo);
    }

    private void addOpeValor( String tipo) {
        operacao_pilha.push(tipo);
        System.out.println("Inserindo pilha: " + tipo + " pilha: " + operacao_pilha.toString());
    }


    private void addVariavel(Token token) throws SemanticError {
        String id;
        int escopo;
        id = token.getLexeme();
        ultimaVariavel = id;
        if (escopo_lista.isEmpty()) {
            escopo = 0;
        } else {
            escopo = escopo_lista.get(escopo_lista.size() - 1);
        }
        adicionarSimboloTabela(id, this.tipo, false, false, escopo, false, 0, false, false, false,0);
        System.out.println("Inserir símbolo: " + id + " com o tipo: " + tipo);
        addOpeValor(tipo);

        // Geração de código para declaração de variável
        if (!dataHeaderAdded) {
            codigoGerado.append(".data\n");
            dataHeaderAdded = true;
        }
        codigoGerado.append("    ").append(id).append(" : ").append("0").append("\n"); // Inicializa variável com 0 ou valor padrão
    }

    private void addVariavelVetor(Token token) throws SemanticError {
        String id;
        int escopo;
        id = token.getLexeme();
        ultimaVariavel = id;
        if (escopo_lista.isEmpty()) {
            escopo = 0;
        } else {
            escopo = escopo_lista.get(escopo_lista.size() - 1);
        }
        adicionarSimboloTabela(id, this.tipo, false, false, escopo, false, 0, true, false, false,0);
        System.out.println("Inserir vetor: " + id + " com o tipo: " + tipo);
        addOpeValor(tipo);
    }

    private void addVariavelParametro(Token token) throws SemanticError {
        String id;

        int escopo;
        this.posParametro++;
        id = token.getLexeme();
        ultimaVariavel = id;
        if (escopo_lista.isEmpty()) {
            escopo = 1;
        } else {
            escopo = escopo_lista.get(escopo_lista.size() - 1)+1;
        }
        adicionarSimboloTabela(id, this.tipo, true, false, escopo, true, this.posParametro, false, false, false,0);
        System.out.println("Inserir parametro: " + id + " com o tipo: " + tipo);

        // Geração de código para declaração de variável
        if (!dataHeaderAdded) {
            codigoGerado.append(".data\n");
            dataHeaderAdded = true;
        }
        codigoGerado.append("    ").append(id).append("_").append(nomeFuncao).append(" : ").append("0").append("\n"); // Inicializa variável com 0 ou valor padrão
    }

    private void addDeclaracaoFuncao(Token token) throws SemanticError {
        String id;
        int escopo;
        id = token.getLexeme();
        this.ultimaFuncao = id;
        funcao_lista.add(id);
        if (escopo_lista.isEmpty()) {
            escopo = 0;
        } else {
            escopo = escopo_lista.get(escopo_lista.size() - 1);
        }
        adicionarSimboloTabela(id, this.tipo, true, false, escopo, true, 0, false, false, true,0);
        System.out.println("Inserir funcao: " + id + " com o tipo: " + tipo);
    }
    private void addQuantidadeParametrosFuncoes() throws SemanticError {

        String nomeFuncao = this.ultimaFuncao;

        parametrosFuncoes.put(nomeFuncao, this.posParametro);

        this.posParametro = 0;
    }

    private void verificaQuantidadeParametros() throws SemanticError {

        String nomeFuncao = this.nomeFuncaoAtual;

        // Verifica se o nome da função está presente no mapa
        if (parametrosFuncoes.containsKey(nomeFuncao)) {
            int qtdParametrosFuncao = parametrosFuncoes.get(nomeFuncao);

            // Compara a quantidade de parâmetros registrada com a quantidade atual
            if (qtdParametrosFuncao != this.qtdParametros) {
                throw new SemanticError("Quantidade incorreta de parâmetros para a função '" + nomeFuncao + "'");
            }
        }
    }
    private void atribVariavel() throws SemanticError {

        ultimaVariavel = ultimaVarAtri;
        if (com_exe_BIP_lista.isEmpty() || !com_exe_BIP_lista.get(com_exe_BIP_lista.size() - 1).equals("STO " + this.ultimaVarAtri)) {
            if (new Simbolo().ehVetor(ultimaVariavel,tabelaSimbolos)) {
                String ultimoElemento = com_exe_BIP_lista.get(com_exe_BIP_lista.size() - 1);
                //Contorna a duplicidadede desso comando
                if (!ultimoElemento.startsWith("STO")) {
                    com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                }
                //Pega a posição salva em temp salva no $indr, pega o valor, salvo em temp, para ser atribuido no vetor e salva no vetor
                com_exe_BIP_lista.add("LD " + gerenciadorVarTemp.usarTemp());
                com_exe_BIP_lista.add("STO $indr");
                com_exe_BIP_lista.add("LD " + gerenciadorVarTemp.usarTemp());
                com_exe_BIP_lista.add("STOV " + this.ultimaVarAtri + complementoVariavelFuncao);
            }
            else {
                com_exe_BIP_lista.add("STO " + this.ultimaVarAtri + complementoVariavelFuncao);
            }
        }
        inicializarSimboloTabela(ultimaVarAtri);
        System.out.println("inicializa símbolo: " + ultimaVarAtri + " com o tipo: " + tipo);
        addOpeValor(tipo);

    }

    private void usarVariavel(Token token) throws SemanticError {
        String id;
        id = token.getLexeme();
        ultimaVariavel = id;

        confSeAtriguidaESetUsada(id);
        System.out.println("usando símbolo: " + id + " com o tipo: " + tipo);
    }

    public String getCodigoGerado() {
        codigoGerado.append(".text\n");
        codigoGerado.append("JMP INICIO\n");
        com_exe_BIP_lista.add("HLT 0");
        for (String item : com_exe_BIP_lista) {
            codigoGerado.append(item).append("\n");
        }
        com_exe_BIP_lista.clear();
        return codigoGerado.toString();
    }

    private void setVarDeclInit() throws SemanticError {
        inicializarSimboloTabela(ultimaVariavel);
        System.out.println("setVarDeclInit: " + ultimaVariavel);
    }

    private int getTamanhoVetor(String id) throws SemanticError {
        for (Simbolo s : this.tabelaSimbolos) {

            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                System.out.println("tabela " + this.tabelaSimbolos.toString());
                return s.tamVetor;
            }
        }
        return 0;
    }
    private void setTamDeclaVetor(Token token) throws SemanticError {

        int tamVetor;

        try {
            tamVetor =Integer.parseInt(token.getLexeme());

        } catch (NumberFormatException e) {
            tamVetor = 0;
        }
        setTamVetor(ultimaVariavel,tamVetor);
        System.out.println("seta tamanho vetor : " + ultimaVariavel + " tam: " + tamVetor);

        // Obtém o tamanho do vetor diretamente da variável local
        int tamanhoVetor = getTamanhoVetor(ultimaVariavel);

        // Geração de código para declaração de vetor com tamanho específico
        if (!dataHeaderAdded) {
            codigoGerado.append(".data\n");
            dataHeaderAdded = true;
        }
        // Inicializa o vetor com o tamanho especificado
        codigoGerado.append("    ").append(ultimaVariavel).append(" : ");
        for (int i = 0; i < tamanhoVetor; i++) {
            // Define o valor inicial de cada elemento do vetor como 0
            codigoGerado.append("0");
            if (i < tamanhoVetor - 1) {
                codigoGerado.append(", ");
            }
        }
        codigoGerado.append("\n");
    }
    private void setTamanhoVetor(Token token) throws SemanticError {

        int tamVetor;

        try {
            tamVetor =Integer.parseInt(token.getLexeme());

        } catch (NumberFormatException e) {
            tamVetor = 0;
        }
        setTamVetor(ultimaVariavel,tamVetor);
        System.out.println("seta tamanho vetor : " + ultimaVariavel + " tam: " + tamVetor);

    }

    private void setPossicaoVetor(Token token) throws SemanticError {

        int posVetor;

        try {
            posVetor =Integer.parseInt(token.getLexeme());

        } catch (NumberFormatException e) {
            posVetor = -1;
        }
        setPosVetor(ultimaVariavel,posVetor);
        System.out.println("seta possicao vetor : " + ultimaVariavel + " tam: " + posVetor);
    }
    private void initEscopo(Token token) {
        escopo_lista.add(proxEscopo);
        proxEscopo++;
        System.out.println("50 #" + escopo_lista.toString() + ", Token: " + token + ", id: " + ultimaVariavel);
        escopo_lista.get(escopo_lista.size() - 1);
    }

    private void finaEscopo(Token token) {
        escopo_lista.remove(escopo_lista.size() - 1);
        System.out.println("51 #" + escopo_lista.toString() + ", Token: " + token);
    }


    private void adicionarSimboloTabela(String id, String tipo, Boolean iniciada, Boolean usada, int escopo, Boolean parametro, int posicao, Boolean vetor, Boolean matriz, Boolean funcao, int tamVetor) throws SemanticError {

        Simbolo simbolo = new Simbolo();
        simbolo.id = id;
        simbolo.tipo = tipo;
        simbolo.iniciada = iniciada;
        simbolo.usada = usada;
        simbolo.escopo = escopo;
        simbolo.parametro = parametro;
        simbolo.posicao = posicao;
        simbolo.vetor = vetor;
        simbolo.matriz = matriz;
        simbolo.funcao = funcao;
        simbolo.tamVetor =tamVetor;

        if (posicao>0){
            simbolo.nomeFuncao=ultimaFuncao;
        }
        else {
            simbolo.nomeFuncao = "";
        }

        if (!simbolo.simboloJaDeclarado(simbolo.id, escopo_lista, this.tabelaSimbolos)) {
            this.tabelaSimbolos.add(simbolo);
            System.out.println("tabela " + this.tabelaSimbolos.toString());
        } else {
            throw new SemanticError("Você não pode declarar outra variável com o nome:" + id);
        }
    }


    private String getTipoId(String id) throws SemanticError {

        for (Simbolo s : this.tabelaSimbolos) {
            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                return s.tipo;
            }
        }

        throw new SemanticError("Variavel não declarada " + id);
    }

    private void inicializarSimboloTabela(String id) throws SemanticError {

        for (Simbolo s : this.tabelaSimbolos) {
            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                s.iniciada = true;
                return;
            }
        }

        throw new SemanticError("Variavel não declarada " + id);
    }

    private void confSeAtriguidaESetUsada(String id) throws SemanticError {

        for (Simbolo s : this.tabelaSimbolos) {
            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                if (s.iniciada)
                {
                    s.usada=true;
                } else{
                    alertas_lista.add("O símbolo '" + id + "' foi usado sem ser inicializado.");
                }
                return;


            }
        }

        throw new SemanticError("Variavel não declarada " + id);
    }

    private void setTamVetor(String id,int tamVetor) throws SemanticError {

        for (Simbolo s : this.tabelaSimbolos) {
            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                s.tamVetor = tamVetor;
                System.out.println("tabela " + this.tabelaSimbolos.toString());
                return;
            }
        }

        throw new SemanticError("Variavel não declarada " + id);
    }
    private void setPosVetor(String id,int posVetor) throws SemanticError {

        for (Simbolo s : this.tabelaSimbolos) {
            if (new Simbolo().equalsID(s.id,id) && (escopo_lista.contains(s.escopo)|| 0==s.escopo)) {
                s.posVetor = posVetor;
                //Se for atri salva a posicao em temp para depois resgatar ao atribuir o vetor
                if(atri_vetor){
                    com_exe_BIP_lista.add("STO " + gerenciadorVarTemp.obterTemp());
                }
                System.out.println("tabela " + this.tabelaSimbolos.toString());
                return;
            }
        }

        throw new SemanticError("Variavel não declarada " + id);
    }



    public void verificarSimbolosNaoUtilizados() {

        for (Simbolo s : tabelaSimbolos) {
            if (s.iniciada && !s.usada) {
                alertas_lista.add("Variável inicializada, mas não utilizada: " + (s.id + "_" + s.escopo));
            }
        }
        for (Simbolo s : tabelaSimbolos) {
            if (!s.iniciada && !s.usada) {
                alertas_lista.add("Variável não inicializada e não utilizada: " + (s.id + "_" + s.escopo));
            }
        }
    }

}
