package gals;

public class SemanticTable {

    public static final String ERR = "ERR";
    public static final String OK_ = "OK_";
    public static final String WAR = "WAR";


    public static final String INT = "int";
    public static final String FLO = "float";
    public static final String CHA = "char";
    public static final String STR = "string";
    public static final String BOO = "bool";

    public static final String SUM = "SUM";
    public static final String SUB = "SUB";
    public static final String MUL = "MUL";
    public static final String DIV = "DIV";
    public static final String REL = "REL"; // qualquer operador relacional
    public static final String BIT = "BIT";

    static String listTipo[]= {INT,FLO,CHA,STR,BOO};
    static String listOpe[]= {SUM,SUB,MUL,DIV,REL,BIT};


    // TIPO DE RETORNO DAS EXPRESSOES ENTRE TIPOS
    // 5 x 5 X 5  = TIPO X TIPO X OPER
    static String expTable [][][] =
                               {/*       INT       */ /*       FLOAT     */ /*      CHAR       */ /*      STRING     */ /*     BOOL        */
                    /*   INT*/ {{INT,INT,INT,FLO,BOO,INT},{FLO,FLO,FLO,FLO,BOO,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR}},
                    /* FLOAT*/ {{FLO,FLO,FLO,FLO,BOO,FLO},{FLO,FLO,FLO,FLO,BOO,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR}},
                    /*  CHAR*/ {{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{CHA,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR}},
                    /*STRING*/ {{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{STR,ERR,ERR,STR,BOO,ERR},{ERR,ERR,ERR,ERR,ERR,ERR}},
                    /*  BOOL*/ {{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR},{ERR,ERR,ERR,BOO,ERR,ERR},{ERR,ERR,ERR,ERR,ERR,ERR}}
            };

    // atribuicoes compativeis
    // 5 x 5 = TIPO X TIPO
    static String atribTable [][]={/* INT FLO CHA STR BOO  */
                            /*INT*/ {OK_,WAR,ERR,ERR,ERR},
                            /*FLO*/ {OK_,OK_,ERR,ERR,ERR},
                            /*CHA*/ {ERR,ERR,ERR,ERR,ERR},
                            /*STR*/ {ERR,ERR,ERR,OK_,ERR},
                            /*BOO*/ {ERR,ERR,ERR,ERR,OK_}
    };

    static String resultType (String TP1, String TP2, String OP) throws IllegalArgumentException {
        // Obtém as posições dos valores TP1, TP2 e OP nos vetores listTipo e listOpe
        int indexTP1 = getIndex(listTipo, TP1);
        int indexTP2 = getIndex(listTipo, TP2);
        int indexOP = getIndex(listOpe, OP);

        return expTable[indexTP1][indexTP2][indexOP];

    }



    static String atribType (String TP1, String TP2)throws IllegalArgumentException {
        int indexTP1 = getIndex(listTipo, TP1);
        int indexTP2 = getIndex(listTipo, TP2);

        return (atribTable[indexTP1][indexTP2]);
    }

    // Método para obter a posição de um valor em um vetor
    static int getIndex(String[] array, String value) throws IllegalArgumentException{
        for(int i = 0; i < array.length; i++) {
            if(array[i].equals(value)) {
                return i; // Retorna a posição do valor no vetor
            }
        }
        throw new IllegalArgumentException("Posição inválida");
    }
}
