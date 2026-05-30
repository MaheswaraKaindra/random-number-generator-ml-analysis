import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PRNGDatasetGenerator {

    // Interface
    interface PRNG {
        long next();
    }

    // LCG
    static class LCG implements PRNG {
        private long state;
        private final long a = 1664525;
        private final long c = 1013904223;
        private final long m = (1L << 32);

        public LCG(long seed) {
            this.state = seed % m;
        }

        @Override
        public long next() {
            state = (a * state + c) % m;
            return state;
        }
    }

    // Xorshift32
    static class Xorshift32 implements PRNG {
        private int state;

        public Xorshift32(long seed) {
            this.state = (seed == 0) ? 1 : (int) seed;
        }

        @Override
        public long next() {
            state ^= (state << 13);
            state ^= (state >>> 17);
            state ^= (state << 5);
            return state & 0xFFFFFFFFL;
        }
    }

    // A5/1
    static class A51 implements PRNG {
        private int r1, r2, r3;

        public A51(long seed) {
            this.r1 = (int) (seed & 0x7FFFF) | 1;
            this.r2 = (int) ((seed >> 19) & 0x3FFFFF) | 1;
            this.r3 = (int) ((seed >> 41) & 0x7FFFFF) | 1;
        }

        // Fungsi majority yang sebelumnya hilang ditambahkan di sini
        private int majority(int a, int b, int c) {
            return (a & b) | (a & c) | (b & c);
        }

        @Override
        public long next() {
            int output = 0;

            for (int i = 0; i < 32; i++) {
                int majority = majority((r1 >> 8) & 1, (r2 >> 10) & 1, (r3 >> 10) & 1);

                if (((r1 >> 8) & 1) == majority) {
                    int newR1 = ((r1 >> 18) ^ (r1 >> 17) ^ (r1 >> 16) ^ (r1 >> 13)) & 1;
                    // Diperbaiki menggunakan mask & 0x7FFFF
                    r1 = ((r1 << 1) | newR1) & 0x7FFFF; 
                }

                if (((r2 >> 10) & 1) == majority) {
                    int newR2 = ((r2 >> 21) ^ (r2 >> 20) ^ (r2 >> 16) ^ (r2 >> 12)) & 1;
                    r2 = ((r2 << 1) | newR2) & 0x3FFFFF;
                }

                if (((r3 >> 10) & 1) == majority) {
                    int newR3 = ((r3 >> 22) ^ (r3 >> 21) ^ (r3 >> 20) ^ (r3 >> 7)) & 1;
                    r3 = ((r3 << 1) | newR3) & 0x7FFFFF;
                }

                int outBit = ((r1 >> 18) ^ (r2 >> 21) ^ (r3 >> 22)) & 1;
                output = (output << 1) | outBit;
            }
            return output & 0xFFFFFFFFL;
        }
    }

    public static void generateDataset(PRNG algorithm, String filename, int numberOfSamples) {
        System.out.println("Generating Dataset: " + filename);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            
            for (int i = 0; i < numberOfSamples; i++) {
                writer.write(algorithm.next() + "\n");
            }
            
            System.out.println("Success! " + numberOfSamples + " samples saved.\n");
        
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        
        if (args.length < 3) {
            System.out.println("Usage: java PRNGDatasetGenerator <Algorithm> <Samples> <Seed>");
            return;
        }

        String algoName = args[0].toUpperCase();
        int numSamples = Integer.parseInt(args[1]);
        long seed = Long.parseLong(args[2]);

        PRNG prng;
        switch (algoName) {
            case "LCG":
                prng = new LCG(seed);
                break;
            case "XORSHIFT":
                // Diperbaiki menjadi Xorshift32
                prng = new Xorshift32(seed); 
                break;
            case "A51":
                prng = new A51(seed);
                break;
            default:
                System.out.println("Algorithm not recognized: " + algoName);
                return;
        }

        String filename = "data/raw/" + algoName.toLowerCase() + "_data.txt";
        generateDataset(prng, filename, numSamples);
    }
}