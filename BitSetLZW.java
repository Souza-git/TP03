import java.util.BitSet;

public class BitSetLZW {
    
    BitSet bs;
    int qtdeNumeros;
    int bitSize;
    int ultimoBitUsado;

    public BitSetLZW(int b) {
        bs = new BitSet();
        qtdeNumeros = 0;
        bitSize = b;
        ultimoBitUsado = 0;
    }

    public void add(int n) {
        for(int i=0; i<bitSize; i++) {
            if(n%2 == 0)
                bs.clear(ultimoBitUsado++);
            else
                bs.set(ultimoBitUsado++);
            n = n >> 1;  // divisão inteira por 2
        }
        qtdeNumeros++;
    }

    public int get(int j) {  // retorna o j-ésimo número
        int p = j*bitSize;  // primeiro bit do número
        int n=0;
        for(int i=0; i<bitSize; i++) {
            if(bs.get(p+i))
                n += (int)Math.pow(2,i);
        }
        return n;
    }

    public int getQtdeNumeros() {
        return qtdeNumeros;
    }

    public byte[] getBytes() {
        return bs.toByteArray();
    }

    public void setBytes(int n, byte[] ba) {
        qtdeNumeros = n;
        bs = BitSet.valueOf(ba);
    }

}
