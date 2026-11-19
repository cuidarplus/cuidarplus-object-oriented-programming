package previsao;

import java.util.ArrayList;

public class PrevisaoComparacao extends EstrategiaPrevisao {

    private int tamanhoPeriodo;

    public PrevisaoComparacao(int tamanhoPeriodo) {
        super("Comparação de Períodos");
        if (tamanhoPeriodo < 2) {
            throw new IllegalArgumentException("O período de comparação deve ter pelo menos 2 leituras.");
        }
        this.tamanhoPeriodo = tamanhoPeriodo;
    }

    @Override
    public double prever(ArrayList<Double> leituras) {
        if (!possuiDadosSuficientes(leituras)) {
            return Double.NaN;
        }
        int periodo = Math.min(tamanhoPeriodo, leituras.size() / 2);
        int fim = leituras.size();

        double mediaRecente = media(leituras, fim - periodo, fim);
        double mediaAnterior = media(leituras, fim - 2 * periodo, fim - periodo);
        double variacao = mediaRecente - mediaAnterior;

        return arredondar(Math.max(0, mediaRecente + variacao));
    }

    public int getTamanhoPeriodo() {
        return tamanhoPeriodo;
    }
}
