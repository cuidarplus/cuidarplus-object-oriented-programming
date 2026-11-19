package sensor;

public interface Sensor {

    double lerValor();

    boolean verificarFuncionamento();

    String getTipo();

    String getUnidade();

    boolean excedeuLimite(double valor, double limite);
}
