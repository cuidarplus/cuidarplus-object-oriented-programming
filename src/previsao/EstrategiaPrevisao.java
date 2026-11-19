package previsao;

import java.util.ArrayList;

public abstract class EstrategiaPrevisao {

    private static final double TOLERANCIA_ESTABILIDADE = 0.05;

    private String nome;
    private int minimoLeituras;

    public EstrategiaPrevisao(String nome) {
        this.nome = nome;
        this.minimoLeituras = 5;
    }

    public abstract double prever(ArrayList<Double> leituras);

    public boolean possuiDadosSuficientes(ArrayList<Double> leituras) {
        return leituras != null && leituras.size() >= minimoLeituras;
    }

    public String interpretar(ArrayList<Double> leituras, double valorPrevisto, String unidade) {
        if (!possuiDadosSuficientes(leituras) || Double.isNaN(valorPrevisto)) {
            int disponiveis = leituras == null ? 0 : leituras.size();
            return "[" + nome + "] Dados insuficientes: " + disponiveis + " de " + minimoLeituras
                    + " leituras necessárias. Previsão não gerada.";
        }

        double ultimaLeitura = leituras.get(leituras.size() - 1);
        double diferenca = valorPrevisto - ultimaLeitura;
        double tolerancia = Math.abs(ultimaLeitura) * TOLERANCIA_ESTABILIDADE;

        String tendencia;
        if (diferenca > tolerancia) {
            tendencia = "tendência de ALTA";
        } else if (diferenca < -tolerancia) {
            tendencia = "tendência de QUEDA";
        } else {
            tendencia = "tendência ESTÁVEL";
        }

        return "[" + nome + "] Previsto: " + String.format("%.2f", valorPrevisto) + " " + unidade
                + " (última leitura: " + String.format("%.2f", ultimaLeitura) + " " + unidade
                + ") - " + tendencia + ".";
    }

    protected double media(ArrayList<Double> leituras, int inicio, int fim) {
        double soma = 0;
        for (int i = inicio; i < fim; i++) {
            soma += leituras.get(i);
        }
        return soma / (fim - inicio);
    }

    protected double arredondar(double valor) {
        return Math.round(valor * 100) / 100.0;
    }

    public String getNome() {
        return nome;
    }

    public int getMinimoLeituras() {
        return minimoLeituras;
    }
}
