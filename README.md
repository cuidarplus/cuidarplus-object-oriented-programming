# Territory Predictive Insights

Sistema de console em Java puro que monitora **chuva intensa e risco de alagamento** em um ponto vulnerável do território. Uma estação de coleta reúne sensores de chuva, nível do rio e umidade; o histórico de leituras alimenta duas estratégias de previsão, e um motor de alertas compara as previsões com os limiares configurados para a região.

## Como compilar e executar

Requisito: JDK 17 ou superior.

Na raiz do projeto:

```bash
javac -d out src/dominio/*.java src/sensor/*.java src/previsao/*.java src/app/*.java
java -cp out app.SistemaMonitoramento
```

Não há dependências externas, Maven ou Gradle. As classes compiladas vão para a pasta `out/`, que não é versionada.

### Estrutura de diretórios

```
src/
├── dominio/    ComponenteTerritorial, EstacaoDeColeta, DadosTerritorio
├── sensor/     Sensor, SensorChuva, SensorNivelRio, SensorUmidade
├── previsao/   EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao
└── app/        SistemaMonitoramento (classe com main)
```

Cada pasta corresponde a um pacote Java (`package dominio;`, `package sensor;` etc.). As dependências seguem um único sentido: `app` usa os outros três pacotes, `dominio` usa `sensor`, e `sensor` e `previsao` não dependem de nenhum outro pacote do projeto.

### Roteiro rápido de uso

1. Opção **3** logo no início: o sistema recusa a previsão porque ainda não há leituras suficientes.
2. Opção **2** pelo menos cinco vezes para formar o histórico mínimo.
3. Opção **3** novamente: previsões geradas e alertas avaliados.
4. Opção **4** para alterar dados do território. Alterações de limiares exigem o código de acesso de demonstração `1234`, definido em `SistemaMonitoramento.java`.
5. Opção **6** para a simulação de 24 horas com um evento de chuva intensa, que exercita os três níveis de alerta.

## Público beneficiado e decisão apoiada

O sistema foi pensado para quem convive com alagamentos recorrentes em áreas baixas próximas a córregos: moradores de ruas que inundam, comerciantes da região, escolas e unidades de saúde do entorno. O usuário direto é a **equipe de Defesa Civil ou de gestão local** responsável pelo território.

A decisão apoiada é **quando acionar medidas preventivas** (comunicar a população, interditar vias de risco, mobilizar equipes) com base na tendência prevista das leituras, e não apenas quando a água já subiu.

O território é descrito somente por região e equipamento. O código e esta documentação não contêm nenhum dado pessoal real.

## Arquitetura e decisões técnicas

| Arquivo | Papel |
|---|---|
| `ComponenteTerritorial` | Classe abstrata com `id`, `nome`, `regiao`, `status`; métodos concretos `ativar()`/`desativar()` e abstrato `verificarStatus()`. |
| `EstacaoDeColeta` | Herda de `ComponenteTerritorial` (`extends` + `super(...)`) e agrupa os sensores em `ArrayList<Sensor>`. |
| `DadosTerritorio` | Encapsulamento total: atributos `private`, setters validados que mantêm o valor anterior em caso de erro e limiares protegidos por código de acesso. |
| `Sensor` | Interface: `lerValor()`, `verificarFuncionamento()`, `getTipo()`, `getUnidade()`, `excedeuLimite()`. |
| `SensorChuva`, `SensorNivelRio`, `SensorUmidade` | Implementam `Sensor`; simulam leituras com `Math.random()` em forma de passeio aleatório limitado a faixas físicas. |
| `EstrategiaPrevisao` | Classe abstrata com o mínimo de 5 leituras, a interpretação textual (alta, queda, estável) e utilitários comuns. |
| `PrevisaoMediaMovel`, `PrevisaoComparacao` | Herdam de `EstrategiaPrevisao` com atributos próprios (`tamanhoJanela`, `tamanhoPeriodo`) e sobrescrevem `prever()`. |
| `SistemaMonitoramento` | `main`, menu interativo, coleta, motor de alertas e simulação de 24 horas. |

Decisões principais:

- **Polimorfismo por interface e por herança.** A estação conhece apenas `Sensor`, e o motor de alertas percorre um vetor de `EstrategiaPrevisao`. Para adicionar um novo sensor ou uma nova estratégia, basta criar uma classe, sem alterar o motor.
- **Leituras com continuidade.** Cada sensor parte da última leitura e varia dentro de um passo máximo. A série resultante é plausível, sem saltos aleatórios, e é isso que dá sentido às previsões.
- **Falha de autodiagnóstico descarta a leitura.** Antes de cada coleta o sistema chama `verificarFuncionamento()`. Se o sensor falhar, a leitura não entra no histórico, para não contaminar a previsão.
- **Previsão inválida representada por `Double.NaN`.** Com menos de 5 leituras, `prever()` não inventa um número. O motor sempre testa `Double.isNaN()` antes de avaliar alertas.
- **Motor de alertas em três níveis**, relativo ao limiar de cada grandeza:
  - `ATENÇÃO`: previsão a partir de 80% do limite.
  - `ALERTA`: previsão acima do limite, verificada por `excedeuLimite()` do sensor.
  - `CRÍTICO`: previsão a partir de 130% do limite.

  Cada alerta informa o sensor, a estratégia, o valor previsto, o limite, o percentual e o motivo.
- **Simulação de 24 horas sem quebrar o encapsulamento.** O evento de chuva é um perfil de intensidade hora a hora somado à leitura pública de cada sensor. O nível do rio responde com 2 horas de atraso em relação à chuva. O histórico da simulação é separado do histórico do menu.
- **Entrada robusta.** Toda leitura do `Scanner` é feita linha a linha, com tratamento de `NumberFormatException` e de fim de entrada. O menu nunca é interrompido por exceção.

## Divisão de tarefas da equipe

| Integrante | RM | Responsabilidades |
|---|---|---|
| Danilo Roberto Anterio dos Santos | 566966 | Camada de domínio (`ComponenteTerritorial`, `EstacaoDeColeta`, `DadosTerritorio`), sensores (`Sensor` e implementações) e testes manuais do fluxo |


## Reflexão crítica

**Quando o sistema deve se calar?** Sempre que não houver base estatística mínima. As duas estratégias exigem pelo menos 5 leituras no histórico. Abaixo disso, `prever()` devolve `Double.NaN`, a interpretação informa "dados insuficientes" e o motor de alertas não avalia nada. Leituras de sensores que falham no autodiagnóstico também são descartadas em vez de preenchidas com valores estimados. Um sistema que emite previsões com duas leituras transmite uma confiança que não tem. Num contexto de risco, um falso alarme desgasta a credibilidade dos avisos, e uma falsa tranquilidade pode atrasar uma evacuação. Calar-se e dizer explicitamente por quê é parte da responsabilidade do sistema.

**O que os dados não representam?** Uma única estação com um sensor de cada tipo descreve um ponto do território, não a região inteira. A chuva pode ser muito mais intensa a poucos quilômetros dali, um bueiro entupido pode alagar uma rua sem que o córrego transborde, e o nível do rio depende do que acontece rio acima, fora da área monitorada. Os dados também não contêm informação sobre quem mora em cada rua, quem tem mobilidade reduzida ou quais imóveis já alagaram antes. As leituras são simuladas e as estratégias são simples (média móvel e comparação de períodos). Elas indicam tendência, não substituem modelos hidrológicos nem o conhecimento de quem vive no território.

**Por que o alerta apoia a decisão humana em vez de decidir por ela?** Porque as consequências das ações (interditar uma via, acionar sirenes, remover famílias) envolvem contexto que o sistema não enxerga: recursos disponíveis, eventos em andamento, informações de campo e o histórico local. Por isso o alerta é construído para ser explicável: informa o sensor, a estratégia, o valor previsto, o limite e o motivo, e as duas estratégias podem discordar. Os limiares são ajustáveis apenas por quem possui o código de acesso, o que mantém a calibragem sob responsabilidade humana. O sistema antecipa e organiza a informação; a decisão e a responsabilidade por ela permanecem com a equipe.

## Uso de IA

A inteligência artificial foi utilizada apenas como apoio na estruturação e revisão textual deste `README.md`. Todo o conteúdo foi revisado e ajustado manualmente pela equipe para garantir fidelidade ao projeto implementado e aos requisitos da disciplina.
