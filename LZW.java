import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class LZW {

    public static final int BITS_POR_INDICE = 12;

    public static void main(String[] args) {

        String mensagem = "O RATO ROEU A ROUPA DO REI DE ROMA";
        System.out.println(mensagem);
        byte[] msgBytes = mensagem.getBytes();

        // A mensagem codificada é um vetor de bytes que contém:
        // 1 - Um int que representa a quantidade de índices (de 12 bits) codificados
        // 2 - Um vetor de bytes que contém o vetor de bits
        byte[] msgCodificada = codifica(msgBytes);
        System.out.println(msgCodificada.length);

        byte[] msgDecodificada = decodifica(msgCodificada);
        System.out.println(new String(msgDecodificada));


    }

    public static byte[] codifica(byte[] msgBytes) {

        // Declara um dicionário como um vetor de vetores de bytes
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();

        // Inicializa o dicionário
        ArrayList<Byte> vetorBytes;
        byte b;
        for(int j=-128; j<=127; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        // Inicializa a saída
        ArrayList<Integer> saida = new ArrayList<>();

        // Inicia a codificação
        int i = 0;
        int indice = -1;
        int ultimoIndice;
        
        // Para a repetição quando chega ao fim do arquivo.
        // Se o indice for diferente de -1, quer dizer que os bytes acabaram
        while(indice==-1 && i<msgBytes.length) {

            // Cria um vetor de bytes com um único byte
            vetorBytes = new ArrayList<>();
            b = msgBytes[i];
            vetorBytes.add(b);
            indice = dicionario.indexOf(vetorBytes);
            ultimoIndice = indice;

            // Testa um vetor de bytes maior
            while(indice!=-1 && i<msgBytes.length-1) {
                ultimoIndice = indice;
                i++;
                b = msgBytes[i];
                vetorBytes.add(b);
                indice = dicionario.indexOf(vetorBytes);
            }

            // Adiciona o último índice à saída
            saida.add(ultimoIndice);

            // Acrescenta o novo vetor de bytes ao fim do dicionário
            if(dicionario.size() < (int)Math.pow(2,BITS_POR_INDICE))
                dicionario.add(vetorBytes);
        }

        System.out.println(saida);
        System.out.println(saida.size());

        // Cria o vetor de bits
        BitSetLZW bs = new BitSetLZW(BITS_POR_INDICE);
        for(i=0; i<saida.size(); i++)
            bs.add(saida.get(i));

        // Retornar um vetor de bytes com os dados do bitset
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(bs.getQtdeNumeros());
            dos.write(bs.getBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static byte[] decodifica(byte[] msgCodificada) {
        ByteArrayInputStream bais = new ByteArrayInputStream(msgCodificada);
        DataInputStream dis = new DataInputStream(bais);
        int n;
        byte[] ba;
        try {
            n = dis.readInt();
            ba = new byte[msgCodificada.length-4];
            dis.read(ba);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        // Recria o vetor de bits
        BitSetLZW bs = new BitSetLZW(BITS_POR_INDICE);
        bs.setBytes(n, ba);

        // Transforma o vetor de bits em um array de números
        ArrayList<Integer> entrada = new ArrayList<>();
        for(int i=0; i<n; i++) 
            entrada.add(bs.get(i));

        // Declara um dicionário como um vetor de vetores de bytes
        ArrayList<ArrayList<Byte>> dicionario = new ArrayList<>();

        // Inicializa o dicionário
        ArrayList<Byte> vetorBytes;
        byte b;
        for(int j=-128; j<=127; j++) {
            b = (byte)j;
            vetorBytes = new ArrayList<>();
            vetorBytes.add(b);
            dicionario.add(vetorBytes);
        }

        ArrayList<Byte> msgDecodificada = new ArrayList<>();
        ArrayList<Byte> proxVetorBytes;
        int i=0;
        while(i<entrada.size()) {
            vetorBytes = (ArrayList<Byte>)(dicionario.get(entrada.get(i)).clone());

            // acrescenta cada byte à resposta
            for(int j=0; j<vetorBytes.size(); j++)
                msgDecodificada.add(vetorBytes.get(j));

            // recupera o primeiro byte do próximo número
            i++;
            if(i<entrada.size()) {
                proxVetorBytes = dicionario.get(entrada.get(i));
                vetorBytes.add(proxVetorBytes.get(0));
            }

            // acrescenta o novo vetor de bytes ao dicionário
            if(dicionario.size() < (int)Math.pow(2, BITS_POR_INDICE))
                dicionario.add(vetorBytes);

        }

        byte[] msgBytes = new byte[msgDecodificada.size()];
        for(int j=0; j<msgDecodificada.size(); j++)
            msgBytes[j] = msgDecodificada.get(j);
        
        return msgBytes;
    }

}