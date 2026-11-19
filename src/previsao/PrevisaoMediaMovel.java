package previsao;

import java.util.ArrayList;

public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    private int tamanhoJanela;

    public PrevisaoMediaMovel(int tamanhoJanela) {
        super("Média Móvel");
        if (tamanhoJanela < 2) {
            throw new IllegalArgumentException("A janela da média móvel deve ter pelo menos 2 leituras.");
        }
        this.tamanhoJanela = tamanhoJanela;
    }

    @Override
    public double prever(ArrayList<Double> leituras) {
        if (!possuiDadosSuficientes(leituras)) {
            return Double.NaN;
        }
        int janela = Math.min(tamanhoJanela, leituras.size());
        int inicio = leituras.size() - janela;
        return arredondar(media(leituras, inicio, leituras.size()));
    }

    public int getTamanhoJanela() {
        return tamanhoJanela;
    }
}
