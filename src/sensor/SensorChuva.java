package sensor;

public class SensorChuva implements Sensor {

    private static final double MINIMO = 0.0;
    private static final double MAXIMO = 120.0;
    private static final double VARIACAO_MAXIMA = 10.0;
    private static final double CHANCE_FALHA = 0.05;

    private double ultimaLeitura;

    public SensorChuva() {
        this.ultimaLeitura = 10.0;
    }

    @Override
    public double lerValor() {
        double variacao = (Math.random() * 2 - 1) * VARIACAO_MAXIMA;
        double novoValor = Math.max(MINIMO, Math.min(MAXIMO, ultimaLeitura + variacao));
        ultimaLeitura = Math.round(novoValor * 10) / 10.0;
        return ultimaLeitura;
    }

    @Override
    public boolean verificarFuncionamento() {
        return Math.random() >= CHANCE_FALHA;
    }

    @Override
    public String getTipo() {
        return "Chuva";
    }

    @Override
    public String getUnidade() {
        return "mm/h";
    }

    @Override
    public boolean excedeuLimite(double valor, double limite) {
        return valor > limite;
    }
}
