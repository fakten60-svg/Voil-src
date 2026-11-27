package lol.lwes.voil.util.crypto;

public class IntPermutation {
    private final int[] table = new int[1024];
    private final int[] inverseTable = new int[1024];

    public IntPermutation() {
        int i = 0;

        while (i < 1024) {
            this.table[i] = i++;
        }

        long i1 = -7046029254386353131L;

        for (int j = 1023; j > 0; j--) {
            i1 = mixSeed(i1);
            int k = (int)Long.remainderUnsigned(i1, j + 1);
            int l = this.table[j];
            this.table[j] = this.table[k];
            this.table[k] = l;
        }

        int j1 = 0;

        while (j1 < 1024) {
            this.inverseTable[this.table[j1]] = j1++;
        }
    }

    public int unpermute(int i) {
        return this.inverseTable[i];
    }

    private static long mixSeed(long i) {
        long j = (i ^ i >>> 30) * 0xBF58476D1CE4E5B9L;
        j = (j ^ j >>> 27) * 0x94D049BB133111EBL;
        return j ^ j >>> 31;
    }

    public int permute(int i) {
        return this.table[i];
    }
}
