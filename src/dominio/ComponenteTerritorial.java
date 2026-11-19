package dominio;

public abstract class ComponenteTerritorial {

    private String id;
    private String nome;
    private String regiao;
    private boolean status;

    public ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.status = false;
    }

    public void ativar() {
        status = true;
    }

    public void desativar() {
        status = false;
    }

    public abstract String verificarStatus();

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public boolean isAtivo() {
        return status;
    }
}
