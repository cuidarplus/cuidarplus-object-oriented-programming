package sensor;

public class SensorNivelRio implements Sensor {

    private static final double MINIMO = 0.3;
    private static final double MAXIMO = 8.0;
    private static final double VARIACAO_MAXIMA = 0.3;
    private static final double CHANCE_FALHA = 0.05;

    private double ultimaLeitura;

    public SensorNivelRio() {
        this.ultimaLeitura = 1.5;
    }

    @Override
    public double lerValor() {
        double variacao = (Math.random() * 2 - 1) * VARIACAO_MAXIMA;
        double novoValor = Math.max(MINIMO, Math.min(MAXIMO, ultimaLeitura + variacao));
        ultimaLeitura = Math.round(novoValor * 100) / 100.0;
        return ultimaLeitura;
    }

    @Override
    public boolean verificarFuncionamento() {
        return Math.random() >= CHANCE_FALHA;
    }

    @Override
    public String getTipo() {
        return "Nível do Rio";
    }

    @Override
    public String getUnidade() {
        return "m";
    }

    @Override
    public boolean excedeuLimite(double valor, double limite) {
        return valor > limite;
    }
}
