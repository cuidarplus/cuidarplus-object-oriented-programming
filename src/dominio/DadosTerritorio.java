package dominio;

public class DadosTerritorio {

    private static final double CHUVA_MIN = 5.0;
    private static final double CHUVA_MAX = 150.0;
    private static final double RIO_MIN = 0.5;
    private static final double RIO_MAX = 10.0;
    private static final double UMIDADE_MIN = 30.0;
    private static final double UMIDADE_MAX = 100.0;

    private String regiao;
    private int populacao;
    private String codigoAcesso;
    private double limiteChuva;
    private double limiteNivelRio;
    private double limiteUmidade;

    public DadosTerritorio(String regiao, int populacao, String codigoAcesso) {
        if (regiao == null || regiao.isBlank()) {
            throw new IllegalArgumentException("Região não pode ser vazia.");
        }
        if (populacao <= 0) {
            throw new IllegalArgumentException("População deve ser maior que zero.");
        }
        if (codigoAcesso == null || codigoAcesso.isBlank()) {
            throw new IllegalArgumentException("Código de acesso não pode ser vazio.");
        }
        this.regiao = regiao;
        this.populacao = populacao;
        this.codigoAcesso = codigoAcesso;
        this.limiteChuva = 50.0;
        this.limiteNivelRio = 3.0;
        this.limiteUmidade = 90.0;
    }

    public String getRegiao() {
        return regiao;
    }

    public boolean setRegiao(String novaRegiao) {
        if (novaRegiao == null || novaRegiao.isBlank()) {
            System.out.println("Rejeitado: a região não pode ser vazia. Valor mantido: " + regiao);
            return false;
        }
        regiao = novaRegiao;
        return true;
    }

    public int getPopulacao() {
        return populacao;
    }

    public boolean setPopulacao(int novaPopulacao) {
        if (novaPopulacao <= 0) {
            System.out.println("Rejeitado: a população deve ser maior que zero. Valor mantido: " + populacao);
            return false;
        }
        populacao = novaPopulacao;
        return true;
    }

    public double getLimiteChuva() {
        return limiteChuva;
    }

    public boolean setLimiteChuva(double novoLimite, String codigo) {
        if (!autorizar(codigo, limiteChuva, " mm/h")) {
            return false;
        }
        if (!dentroDaFaixa(novoLimite, CHUVA_MIN, CHUVA_MAX, "chuva", " mm/h", limiteChuva)) {
            return false;
        }
        limiteChuva = novoLimite;
        return true;
    }

    public double getLimiteNivelRio() {
        return limiteNivelRio;
    }

    public boolean setLimiteNivelRio(double novoLimite, String codigo) {
        if (!autorizar(codigo, limiteNivelRio, " m")) {
            return false;
        }
        if (!dentroDaFaixa(novoLimite, RIO_MIN, RIO_MAX, "nível do rio", " m", limiteNivelRio)) {
            return false;
        }
        limiteNivelRio = novoLimite;
        return true;
    }

    public double getLimiteUmidade() {
        return limiteUmidade;
    }

    public boolean setLimiteUmidade(double novoLimite, String codigo) {
        if (!autorizar(codigo, limiteUmidade, " %")) {
            return false;
        }
        if (!dentroDaFaixa(novoLimite, UMIDADE_MIN, UMIDADE_MAX, "umidade", " %", limiteUmidade)) {
            return false;
        }
        limiteUmidade = novoLimite;
        return true;
    }

    private boolean autorizar(String codigo, double valorAtual, String unidade) {
        if (codigo == null || !codigo.equals(codigoAcesso)) {
            System.out.println("Rejeitado: código de acesso incorreto. Valor mantido: " + valorAtual + unidade);
            return false;
        }
        return true;
    }

    private boolean dentroDaFaixa(double valor, double minimo, double maximo,
                                  String nomeLimite, String unidade, double valorAtual) {
        if (Double.isNaN(valor) || valor < minimo || valor > maximo) {
            System.out.println("Rejeitado: limite de " + nomeLimite + " deve estar entre "
                    + minimo + " e " + maximo + unidade + ". Valor mantido: " + valorAtual + unidade);
            return false;
        }
        return true;
    }
}
