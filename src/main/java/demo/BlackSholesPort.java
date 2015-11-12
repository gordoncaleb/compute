package demo;

import java.util.Random;

public class BlackSholesPort {

    private Random rand = new Random(123L);
    // number of options
    final int N = 200000000;

    // black-scholes parameters
    final float risk_free_rate = 0.02f;
    final float volatility = 0.30f;

    public static void main(String[] args) {
        new BlackSholesPort().run();
    }

    private float rand_float(float min, float max) {
        float val = rand.nextFloat();
        return val * (max - min) + min;
    }

    private void run() {

        System.out.println("Starting Demo of OpenCL Black Scholes implementation");

        System.out.println("Building random input data...");
        float[] stock_price_data = new float[N];
        float[] option_strike_data = new float[N];
        float[] option_years_data = new float[N];

        for (int i = 0; i < N; i++) {
            stock_price_data[i] = rand_float(5.0f, 30.0f);
            option_strike_data[i] = rand_float(1.0f, 100.0f);
            option_years_data[i] = rand_float(0.25f, 10.0f);
        }

        double[] call_result = new double[N];
        double[] put_result = new double[N];

        System.out.println("Running Java version of Black Scholes alg");
        long t = System.currentTimeMillis();
        black_scholes(
                call_result,
                put_result,
                stock_price_data,
                option_strike_data,
                option_years_data,
                risk_free_rate,
                volatility
        );

        System.out.println("option 0 call price: " + call_result[0]);
        System.out.println("option 0 put price: " + put_result[0]);
        System.out.println("Time: " + (System.currentTimeMillis() - t) + "ms");
    }

    private double cnd(double d) {
        final float A1 = 0.319381530f;
        final float A2 = -0.356563782f;
        final float A3 = 1.781477937f;
        final float A4 = -1.821255978f;
        final float A5 = 1.330274429f;
        final float RSQRT2PI = 0.39894228040143267793994605993438f;

        double K = 1.0f / (1.0f + 0.2316419f * Math.abs(d));
        double cnd = RSQRT2PI * Math.exp(-0.5f * d * d) *
                (K * (A1 + K * (A2 + K * (A3 + K * (A4 + K * A5)))));

        if (d > 0) {
            cnd = 1.0f - cnd;
        }

        return cnd;
    }

    // black-scholes option pricing kernel
    private void black_scholes(double[] call_result,
                               double[] put_result,
                               float[] stock_price,
                               float[] option_strike,
                               float[] option_years,
                               float risk_free_rate,
                               float volatility) {


        for (int opt = 0; opt < stock_price.length; opt++) {
            float S = stock_price[opt];
            float X = option_strike[opt];
            float T = option_years[opt];
            float R = risk_free_rate;
            float V = volatility;

            double sqrtT = Math.sqrt(T);
            double d1 = (Math.log(S / X) + (R + 0.5f * V * V) * T) / (V * sqrtT);
            double d2 = d1 - V * sqrtT;
            double CNDD1 = cnd(d1);
            double CNDD2 = cnd(d2);

            double expRT = Math.exp(-R * T);
            call_result[opt] = S * CNDD1 - X * expRT * CNDD2;
            put_result[opt] = X * expRT * (1.0f - CNDD2) - S * (1.0f - CNDD1);
        }
    }
}