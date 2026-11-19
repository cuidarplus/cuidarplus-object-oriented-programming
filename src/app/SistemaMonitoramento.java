package app;

import java.util.ArrayList;
import java.util.Scanner;

import dominio.DadosTerritorio;
import dominio.EstacaoDeColeta;
import previsao.EstrategiaPrevisao;
import previsao.PrevisaoComparacao;
import previsao.PrevisaoMediaMovel;
import sensor.Sensor;
import sensor.SensorChuva;
import sensor.SensorNivelRio;
import sensor.SensorUmidade;

public class SistemaMonitoramento {

    private static final String CODIGO_ACESSO_INICIAL = "1234";
    private static final double MAXIMO_CHUVA = 120.0;
    private static final double MAXIMO_RIO = 8.0;
    private static final double MAXIMO_UMIDADE = 100.0;
    private static final double FATOR_ATENCAO = 0.8;
    private static final double FATOR_CRITICO = 1.3;

    private static final double ACRESCIMO_MAXIMO_CHUVA = 75.0;
    private static final double ACRESCIMO_MAXIMO_RIO = 2.8;
    private static final double ACRESCIMO_MAXIMO_UMIDADE = 25.0;
    private static final int ATRASO_RESPOSTA_RIO = 2;

    private static final double[] INTENSIDADE_EVENTO = {
        0.00, 0.00, 0.00, 0.00, 0.05, 0.10,
        0.20, 0.35, 0.50, 0.70, 0.90, 1.00,
        1.00, 0.90, 0.75, 0.60, 0.45, 0.30,
        0.20, 0.10, 0.05, 0.00, 0.00, 0.00
    };

    private final Scanner scanner;
    private final DadosTerritorio territorio;
    private final EstacaoDeColeta estacao;
    private final Sensor sensorChuva;
    private final Sensor sensorNivelRio;
    private final Sensor sensorUmidade;
    private final ArrayList<Double> historicoChuva;
    private final ArrayList<Double> historicoNivelRio;
    private final ArrayList<Double> historicoUmidade;
    private final EstrategiaPrevisao[] estrategias;
    private final ArrayList<String> ultimasPrevisoes;
    private final ArrayList<String> ultimosAlertas;
    private String origemUltimaAnalise;
    private boolean entradaEncerrada;

    public SistemaMonitoramento() {
        scanner = new Scanner(System.in);
        territorio = new DadosTerritorio("Baixada Norte - Bacia do Córrego Central", 18000, CODIGO_ACESSO_INICIAL);
        estacao = new EstacaoDeColeta("EST-01", "Estação Baixada Norte", territorio.getRegiao(),
                "Ponte sobre o córrego principal");

        sensorChuva = new SensorChuva();
        sensorNivelRio = new SensorNivelRio();
        sensorUmidade = new SensorUmidade();
        estacao.adicionarSensor(sensorChuva);
        estacao.adicionarSensor(sensorNivelRio);
        estacao.adicionarSensor(sensorUmidade);
        estacao.ativar();

        historicoChuva = new ArrayList<>();
        historicoNivelRio = new ArrayList<>();
        historicoUmidade = new ArrayList<>();

        estrategias = new EstrategiaPrevisao[] {
            new PrevisaoMediaMovel(5),
            new PrevisaoComparacao(3)
        };

        ultimasPrevisoes = new ArrayList<>();
        ultimosAlertas = new ArrayList<>();
        origemUltimaAnalise = "nenhuma";
    }

    public static void main(String[] args) {
        new SistemaMonitoramento().executar();
    }

    public void executar() {
        System.out.println("==========================================================");
        System.out.println(" TERRITORY PREDICTIVE INSIGHTS");
        System.out.println(" Monitoramento de chuva intensa e risco de alagamento");
        System.out.println("==========================================================");

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            int opcao = lerInteiro("Escolha uma opção: ");
            switch (opcao) {
                case 1 -> verificarSensores();
                case 2 -> coletarRodada();
                case 3 -> rodarPrevisoesEAlertas();
                case 4 -> gerenciarTerritorio();
                case 5 -> exibirStatusCompleto();
                case 6 -> simularVinteQuatroHoras();
                case 0 -> rodando = false;
                default -> System.out.println("Opção inválida. Escolha um número entre 0 e 6.");
            }
            if (entradaEncerrada) {
                rodando = false;
            }
        }
        System.out.println("Sistema encerrado.");
        scanner.close();
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== MENU PRINCIPAL ===");
        System.out.println("1 - Verificar sensores e status da estação");
        System.out.println("2 - Simular novas leituras");
        System.out.println("3 - Rodar previsões e alertas");
        System.out.println("4 - Gerenciar dados do território");
        System.out.println("5 - Exibir status completo");
        System.out.println("6 - Bônus: simulação de 24 horas");
        System.out.println("0 - Sair");
    }

    private void verificarSensores() {
        System.out.println();
        System.out.println("=== VERIFICAÇÃO DA ESTAÇÃO ===");
        System.out.println(estacao.verificarStatus());
        System.out.println("--- Autodiagnóstico dos sensores ---");
        for (Sensor sensor : estacao.getSensores()) {
            String resultado = sensor.verificarFuncionamento() ? "OK" : "FALHA";
            System.out.println("  " + sensor.getTipo() + " (" + sensor.getUnidade() + "): " + resultado);
        }
    }

    private void coletarRodada() {
        System.out.println();
        System.out.println("=== COLETA DE LEITURAS ===");
        if (!estacao.isAtivo()) {
            System.out.println("Estação inativa. Nenhuma leitura coletada.");
            return;
        }
        coletarLeitura(sensorChuva, historicoChuva, 0, MAXIMO_CHUVA);
        coletarLeitura(sensorNivelRio, historicoNivelRio, 0, MAXIMO_RIO);
        coletarLeitura(sensorUmidade, historicoUmidade, 0, MAXIMO_UMIDADE);
    }

    private boolean coletarLeitura(Sensor sensor, ArrayList<Double> historico, double acrescimo, double maximo) {
        if (!sensor.verificarFuncionamento()) {
            System.out.println("  " + sensor.getTipo() + ": FALHA no autodiagnóstico. Leitura descartada.");
            return false;
        }
        double valor = Math.min(maximo, sensor.lerValor() + acrescimo);
        valor = Math.round(valor * 100) / 100.0;
        historico.add(valor);
        System.out.println("  " + sensor.getTipo() + ": " + formatar(valor) + " " + sensor.getUnidade()
                + " (" + historico.size() + " leituras no histórico)");
        return true;
    }

    private void rodarPrevisoesEAlertas() {
        System.out.println();
        System.out.println("=== PREVISÕES E ALERTAS ===");
        int alertas = rodarAnalise("menu principal", historicoChuva, historicoNivelRio, historicoUmidade, true);

        boolean algumaPrevisao = estrategias[0].possuiDadosSuficientes(historicoChuva)
                || estrategias[0].possuiDadosSuficientes(historicoNivelRio)
                || estrategias[0].possuiDadosSuficientes(historicoUmidade);

        System.out.println("---");
        if (!algumaPrevisao) {
            System.out.println("Nenhuma previsão gerada: o sistema não emite alertas sem dados suficientes.");
            System.out.println("Use a opção 2 para coletar pelo menos " + estrategias[0].getMinimoLeituras()
                    + " leituras por sensor.");
        } else if (alertas == 0) {
            System.out.println("Nenhum alerta: todas as previsões estão abaixo de "
                    + Math.round(FATOR_ATENCAO * 100) + "% dos limites.");
        } else {
            System.out.println(alertas + " alerta(s) emitido(s). A decisão de agir cabe à equipe responsável.");
        }
    }

    private int rodarAnalise(String origem, ArrayList<Double> chuva, ArrayList<Double> nivelRio,
                             ArrayList<Double> umidade, boolean exibirPrevisoes) {
        ultimasPrevisoes.clear();
        ultimosAlertas.clear();
        origemUltimaAnalise = origem;
        analisarSensor(sensorChuva, chuva, territorio.getLimiteChuva(), exibirPrevisoes);
        analisarSensor(sensorNivelRio, nivelRio, territorio.getLimiteNivelRio(), exibirPrevisoes);
        analisarSensor(sensorUmidade, umidade, territorio.getLimiteUmidade(), exibirPrevisoes);
        return ultimosAlertas.size();
    }

    private void analisarSensor(Sensor sensor, ArrayList<Double> historico, double limite, boolean exibirPrevisoes) {
        if (!exibirPrevisoes && !estrategias[0].possuiDadosSuficientes(historico)) {
            System.out.println("  " + sensor.getTipo() + ": previsão suspensa (" + historico.size() + " de "
                    + estrategias[0].getMinimoLeituras() + " leituras necessárias).");
        }

        for (EstrategiaPrevisao estrategia : estrategias) {
            double previsto = estrategia.prever(historico);
            if (sensor == sensorUmidade && previsto > MAXIMO_UMIDADE) {
                previsto = MAXIMO_UMIDADE;
            }
            String interpretacao = sensor.getTipo() + " " + estrategia.interpretar(historico, previsto, sensor.getUnidade());
            ultimasPrevisoes.add(interpretacao);
            if (exibirPrevisoes) {
                System.out.println("  " + interpretacao);
            }

            if (Double.isNaN(previsto)) {
                continue;
            }

            String alerta = classificarAlerta(sensor, estrategia, previsto, limite);
            if (alerta != null) {
                ultimosAlertas.add(alerta);
                System.out.println("  " + alerta);
            }
        }
    }

    private String classificarAlerta(Sensor sensor, EstrategiaPrevisao estrategia, double previsto, double limite) {
        String nivel;
        String motivo;
        if (previsto >= limite * FATOR_CRITICO) {
            nivel = "CRÍTICO";
            motivo = "a previsão supera o limite em " + Math.round((FATOR_CRITICO - 1) * 100)
                    + "% ou mais; risco elevado de alagamento.";
        } else if (sensor.excedeuLimite(previsto, limite)) {
            nivel = "ALERTA";
            motivo = "a previsão ultrapassa o limite configurado para a região.";
        } else if (previsto >= limite * FATOR_ATENCAO) {
            nivel = "ATENÇÃO";
            motivo = "a previsão atingiu " + Math.round(FATOR_ATENCAO * 100)
                    + "% do limite; a situação se aproxima do ponto de alerta.";
        } else {
            return null;
        }

        long percentual = Math.round(previsto / limite * 100);
        return "[" + nivel + "] Sensor: " + sensor.getTipo()
                + " | Estratégia: " + estrategia.getNome()
                + " | Previsto: " + formatar(previsto) + " " + sensor.getUnidade()
                + " | Limite: " + formatar(limite) + " " + sensor.getUnidade()
                + " (" + percentual + "% do limite)"
                + System.lineSeparator() + "      Motivo: " + motivo;
    }

    private void gerenciarTerritorio() {
        boolean noSubmenu = true;
        while (noSubmenu && !entradaEncerrada) {
            System.out.println();
            System.out.println("=== DADOS DO TERRITÓRIO ===");
            System.out.println("1 - Alterar região (atual: " + territorio.getRegiao() + ")");
            System.out.println("2 - Alterar população (atual: " + territorio.getPopulacao() + ")");
            System.out.println("3 - Alterar limite de chuva (atual: " + formatar(territorio.getLimiteChuva()) + " mm/h)");
            System.out.println("4 - Alterar limite do nível do rio (atual: " + formatar(territorio.getLimiteNivelRio()) + " m)");
            System.out.println("5 - Alterar limite de umidade (atual: " + formatar(territorio.getLimiteUmidade()) + " %)");
            System.out.println("0 - Voltar");

            int opcao = lerInteiro("Escolha uma opção: ");
            boolean alterado = false;
            switch (opcao) {
                case 1 -> alterado = territorio.setRegiao(lerTexto("Nova região: "));
                case 2 -> alterado = territorio.setPopulacao(lerInteiro("Nova população: "));
                case 3 -> alterado = territorio.setLimiteChuva(lerDecimal("Novo limite de chuva (mm/h): "),
                        lerTexto("Código de acesso: "));
                case 4 -> alterado = territorio.setLimiteNivelRio(lerDecimal("Novo limite do nível do rio (m): "),
                        lerTexto("Código de acesso: "));
                case 5 -> alterado = territorio.setLimiteUmidade(lerDecimal("Novo limite de umidade (%): "),
                        lerTexto("Código de acesso: "));
                case 0 -> noSubmenu = false;
                default -> System.out.println("Opção inválida. Escolha um número entre 0 e 5.");
            }
            if (alterado) {
                System.out.println("Alteração registrada com sucesso.");
            }
        }
    }

    private void exibirStatusCompleto() {
        System.out.println();
        System.out.println("=== STATUS COMPLETO ===");
        System.out.println("--- Território ---");
        System.out.println("  Região: " + territorio.getRegiao());
        System.out.println("  População estimada: " + territorio.getPopulacao());
        System.out.println("  Limite de chuva: " + formatar(territorio.getLimiteChuva()) + " mm/h");
        System.out.println("  Limite do nível do rio: " + formatar(territorio.getLimiteNivelRio()) + " m");
        System.out.println("  Limite de umidade: " + formatar(territorio.getLimiteUmidade()) + " %");

        System.out.println("--- Estação ---");
        System.out.println("  " + estacao.verificarStatus());

        System.out.println("--- Histórico de leituras ---");
        System.out.println("  " + resumirHistorico(sensorChuva, historicoChuva));
        System.out.println("  " + resumirHistorico(sensorNivelRio, historicoNivelRio));
        System.out.println("  " + resumirHistorico(sensorUmidade, historicoUmidade));

        System.out.println("--- Últimas previsões (origem: " + origemUltimaAnalise + ") ---");
        if (ultimasPrevisoes.isEmpty()) {
            System.out.println("  Nenhuma análise executada ainda.");
        }
        for (String previsao : ultimasPrevisoes) {
            System.out.println("  " + previsao);
        }

        System.out.println("--- Últimos alertas ---");
        if (ultimosAlertas.isEmpty()) {
            System.out.println("  Nenhum alerta registrado na última análise.");
        }
        for (String alerta : ultimosAlertas) {
            System.out.println("  " + alerta);
        }
    }

    private String resumirHistorico(Sensor sensor, ArrayList<Double> historico) {
        if (historico.isEmpty()) {
            return sensor.getTipo() + ": nenhuma leitura registrada.";
        }
        double minimo = historico.get(0);
        double maximo = historico.get(0);
        for (double valor : historico) {
            minimo = Math.min(minimo, valor);
            maximo = Math.max(maximo, valor);
        }

        int inicio = Math.max(0, historico.size() - 5);
        String ultimas = "";
        for (int i = inicio; i < historico.size(); i++) {
            if (i > inicio) {
                ultimas += "; ";
            }
            ultimas += formatar(historico.get(i));
        }

        return sensor.getTipo() + " (" + sensor.getUnidade() + "): " + historico.size() + " leituras"
                + " | últimas: " + ultimas
                + " | mín: " + formatar(minimo)
                + " | máx: " + formatar(maximo);
    }

    private void simularVinteQuatroHoras() {
        System.out.println();
        System.out.println("=== BÔNUS: SIMULAÇÃO DE 24 HORAS ===");
        System.out.println("Cenário: evento de chuva intensa que começa de madrugada, atinge o pico");
        System.out.println("no fim da manhã e perde força à noite. O rio responde com "
                + ATRASO_RESPOSTA_RIO + " horas de atraso.");
        System.out.println("O histórico da simulação é separado do histórico do menu principal.");

        ArrayList<Double> chuva = new ArrayList<>();
        ArrayList<Double> nivelRio = new ArrayList<>();
        ArrayList<Double> umidade = new ArrayList<>();
        int descartadas = 0;
        int totalCritico = 0;
        int totalAlerta = 0;
        int totalAtencao = 0;

        for (int hora = 0; hora < INTENSIDADE_EVENTO.length; hora++) {
            double intensidade = INTENSIDADE_EVENTO[hora];
            double intensidadeRio = hora >= ATRASO_RESPOSTA_RIO ? INTENSIDADE_EVENTO[hora - ATRASO_RESPOSTA_RIO] : 0;

            System.out.println();
            System.out.println(String.format("--- Hora %02d:00 | intensidade do evento: %d%% ---",
                    hora, Math.round(intensidade * 100)));

            if (!coletarLeitura(sensorChuva, chuva, intensidade * ACRESCIMO_MAXIMO_CHUVA, MAXIMO_CHUVA)) {
                descartadas++;
            }
            if (!coletarLeitura(sensorNivelRio, nivelRio, intensidadeRio * ACRESCIMO_MAXIMO_RIO, MAXIMO_RIO)) {
                descartadas++;
            }
            if (!coletarLeitura(sensorUmidade, umidade, intensidade * ACRESCIMO_MAXIMO_UMIDADE, MAXIMO_UMIDADE)) {
                descartadas++;
            }

            int alertasNaHora = rodarAnalise(String.format("simulação 24h, hora %02d:00", hora),
                    chuva, nivelRio, umidade, false);
            if (alertasNaHora == 0) {
                System.out.println("  Nenhum alerta nesta hora.");
            }

            for (String alerta : ultimosAlertas) {
                if (alerta.startsWith("[CRÍTICO]")) {
                    totalCritico++;
                } else if (alerta.startsWith("[ALERTA]")) {
                    totalAlerta++;
                } else {
                    totalAtencao++;
                }
            }
        }

        System.out.println();
        System.out.println("=== RESUMO DA SIMULAÇÃO ===");
        System.out.println("  Alertas CRÍTICO: " + totalCritico);
        System.out.println("  Alertas ALERTA: " + totalAlerta);
        System.out.println("  Alertas ATENÇÃO: " + totalAtencao);
        System.out.println("  Leituras descartadas por falha no autodiagnóstico: " + descartadas);
        System.out.println("  " + resumirHistorico(sensorChuva, chuva));
        System.out.println("  " + resumirHistorico(sensorNivelRio, nivelRio));
        System.out.println("  " + resumirHistorico(sensorUmidade, umidade));
    }

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        if (!scanner.hasNextLine()) {
            entradaEncerrada = true;
            System.out.println();
            return null;
        }
        return scanner.nextLine().trim();
    }

    private int lerInteiro(String mensagem) {
        while (true) {
            String linha = lerLinha(mensagem);
            if (linha == null) {
                return 0;
            }
            try {
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: digite um número inteiro.");
            }
        }
    }

    private double lerDecimal(String mensagem) {
        while (true) {
            String linha = lerLinha(mensagem);
            if (linha == null) {
                return Double.NaN;
            }
            try {
                return Double.parseDouble(linha.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida: digite um número (ex.: 45.5).");
            }
        }
    }

    private String lerTexto(String mensagem) {
        String linha = lerLinha(mensagem);
        return linha == null ? "" : linha;
    }

    private String formatar(double valor) {
        return String.format("%.2f", valor);
    }
}
