package dominio;

import java.util.ArrayList;

import sensor.Sensor;

public class EstacaoDeColeta extends ComponenteTerritorial {

    private String pontoInstalacao;
    private ArrayList<Sensor> sensores;

    public EstacaoDeColeta(String id, String nome, String regiao, String pontoInstalacao) {
        super(id, nome, regiao);
        this.pontoInstalacao = pontoInstalacao;
        this.sensores = new ArrayList<>();
    }

    public boolean adicionarSensor(Sensor sensor) {
        if (sensor == null) {
            System.out.println("Rejeitado: sensor inválido não pode ser adicionado à estação " + getId() + ".");
            return false;
        }
        sensores.add(sensor);
        return true;
    }

    public ArrayList<Sensor> getSensores() {
        return new ArrayList<>(sensores);
    }

    @Override
    public String verificarStatus() {
        String situacao = isAtivo() ? "ATIVA" : "INATIVA";
        String tipos = "nenhum";
        if (!sensores.isEmpty()) {
            tipos = "";
            for (int i = 0; i < sensores.size(); i++) {
                if (i > 0) {
                    tipos += ", ";
                }
                tipos += sensores.get(i).getTipo();
            }
        }
        return "Estação " + getId() + " - " + getNome()
                + " | Região: " + getRegiao()
                + " | Ponto: " + pontoInstalacao
                + " | Situação: " + situacao
                + " | Sensores (" + sensores.size() + "): " + tipos;
    }

    public String getPontoInstalacao() {
        return pontoInstalacao;
    }
}
