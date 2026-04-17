# Funcionamento do Analisador Léxico

## Visão Geral

O analisador léxico (lexer) é a primeira fase de um compilador. Sua função é ler o código-fonte como uma sequência de caracteres e transformá-la em uma sequência de **tokens** — unidades significativas como palavras-chave, identificadores, números e operadores.

Este analisador é organizado em camadas bem definidas:

```
Código-fonte (String)
        │
        ▼
     [Lexer]          ← coordena a análise
        │
        ├── [MathOperator]   ← reconhece operadores e símbolos
        ├── [Number]         ← reconhece literais numéricos
        └── [Identifier]     ← reconhece identificadores e palavras-chave
                │
                └── [KeyWords]  ← classifica palavras reservadas
        │
        ▼
  Lista de Tokens
        │
        └── cada Token aponta para → [LexTable] → [InputTable]
```

---

## Classes

### `Lexer.java` — Orquestrador Principal

O `Lexer` é o ponto de entrada da análise léxica. Ele recebe o código-fonte como `String`, cria um iterador de caracteres e coordena todos os autômatos (AFDs) para reconhecer os tokens.

**Responsabilidades:**
- Instanciar e manter a lista de AFDs (`MathOperator`, `Number`, `Identifier`)
- Iterar sobre o código-fonte caractere a caractere
- Ignorar espaços em branco e controlar a contagem de linhas
- Chamar cada AFD na ordem até que um token seja reconhecido
- Lançar erro se nenhum AFD reconhecer o caractere atual

**Fluxo do método `getTokens()`:**

```
1. Pula espaços em branco (skipWhiteSpaces)
2. Tenta cada AFD em ordem (searchNextToken)
3. Se algum retornar um Token → registra a linha e adiciona à lista
4. Se nenhum reconhecer → lança RuntimeException ("token not recognized")
5. Repete até encontrar o token EOF
```

**Detalhe importante — backtracking:** Se um AFD consumir caracteres mas não retornar um token (retornar `null`), o iterador é resetado para a posição original (`it.setIndex(pos)`), permitindo que o próximo AFD tente a partir do mesmo ponto.

**Contagem de linhas:** A variável estática `line` é incrementada sempre que um `\n` é encontrado durante o `skipWhiteSpaces`. O número de linha é atribuído a cada token via `t.setLine(line)`.

---

### `AFD.java` — Classe Base Abstrata

Todos os reconhecedores herdam de `AFD` (*Autômato Finito Determinístico*). Ela define o contrato comum e oferece um utilitário compartilhado.

**Atributo:** `lexTable` — referência à tabela de símbolos compartilhada entre todos os AFDs.

**Método abstrato:** `evaluate(CharacterIterator code)` — cada subclasse implementa sua própria lógica de reconhecimento. Retorna um `Token` se reconheceu algo, ou `null` caso contrário.

**Método utilitário — `isTokenSeparator`:** Verifica se o caractere atual é um separador válido de token (espaço, operadores, parênteses, quebras de linha, ou fim de string). Usado pelo `Number` para garantir que um número foi completamente lido e não faz parte de outro lexema.

---

### `MathOperator.java` — Reconhecedor de Operadores e Símbolos

Reconhece todos os operadores e símbolos especiais da linguagem por meio de um `switch` sobre o caractere atual.

**Tokens reconhecidos:**

| Caractere(s) | Tipo do Token | Descrição |
|---|---|---|
| `+` | `PLUS` | Adição |
| `-` | `SUB` | Subtração |
| `*` | `MULT` | Multiplicação |
| `/` | `DIV` | Divisão |
| `(` | `AP` | Abre parêntese |
| `)` | `FP` | Fecha parêntese |
| `{` | `AC` | Abre chave |
| `}` | `FC` | Fecha chave |
| `\n` | `NEW_LINE` | Quebra de linha |
| `=` | `OP_ATRI` | Atribuição |
| `==` | `EQ` | Igualdade |
| `>` | `GT` | Maior que |
| `>=` | `GE` | Maior ou igual |
| `<` | `LT` | Menor que |
| `<=` | `LE` | Menor ou igual |
| `EOF` | `EOF` | Fim do arquivo |

**Lookahead de 1 caractere:** Para `=`, `>` e `<`, o autômato consome o primeiro caractere e inspeciona o próximo para decidir entre operador simples ou composto (ex: `=` vs `==`).

---

### `Number.java` — Reconhecedor de Literais Numéricos

Reconhece inteiros e números de ponto flutuante.

**Lógica do AFD:**

```
Estado inicial: caractere atual é dígito?
  ├── NÃO → retorna null
  └── SIM → lê sequência de dígitos (readNumber)
              └── próximo char é '.'?
                    ├── NÃO → verifica separador → retorna NUM_INT
                    └── SIM → consome '.', lê mais dígitos
                                └── verifica separador → retorna NUM_FLOAT
```

**Validação com `isTokenSeparator`:** Após ler os dígitos, o autômato verifica se o caractere imediatamente seguinte é um separador válido. Isso impede que uma sequência como `123abc` seja reconhecida como o número `123`.

**Erro futuro:** Se após o `.` não houver dígito (ex: `3.`), o método atualmente retorna `null`. Está marcado no código como um ponto para tratamento de erro futuro.

**Tipos de token produzidos:** `NUM_INT` para inteiros, `NUM_FLOAT` para decimais.

---

### `Identifier.java` — Reconhecedor de Identificadores e Palavras-chave

Reconhece sequências que começam com letra ou `_`, seguidas de letras, dígitos ou `_`.

**Lógica do AFD:**

```
Estado inicial: caractere atual é letra ou '_'?
  ├── NÃO → retorna null
  └── SIM → acumula caracteres (letras, dígitos, '_') em StringBuilder
              └── consulta KeyWords.isKeyWord(lexema)
                    ├── É palavra-chave → tipo = KeyWords.getTokenType(lexema)
                    └── NÃO é palavra-chave → tipo = "ID"
              └── retorna Token com tipo determinado
```

**Integração com KeyWords:** Após construir o lexema completo, o `Identifier` delega a classificação para a classe `KeyWords`. Se o lexema for uma palavra reservada da linguagem, recebe o tipo correspondente (ex: `"houdini"` → `"IF"`). Caso contrário, é tratado como identificador genérico (`"ID"`).

---

### `KeyWords.java` — Tabela de Palavras Reservadas

Define as palavras-chave da linguagem por meio de um `HashMap` estático, mapeando cada lexema ao seu tipo de token.

**Palavras-chave definidas:**

| Lexema | Token | Categoria |
|---|---|---|
| `style` | `START` | Início de programa |
| `borderline` | `END` | Fim de programa |
| `space` | `INT` | Tipo inteiro |
| `lithium` | `FLOAT` | Tipo float |
| `judas` | `STRING` | Tipo string |
| `pleaser` | `INPUT` | Leitura (scanf) |
| `catapult` | `PRINT` | Impressão (printf) |
| `houdini` | `IF` | Condicional se |
| `more` | `ELSE` | Condicional senão |
| `problems` | `WHILE` | Laço while |
| `bloomfield` | `FOR` | Laço for |
| `loser` | `BREAK` | Parada de laço |

O bloco `static { ... }` garante que o mapa é populado uma única vez quando a classe é carregada pela JVM.

---

### `Token.java` — Representação de um Token

Cada token reconhecido é encapsulado nesta classe.

**Atributos:**
- `tipo` — string com o tipo do token (ex: `"IF"`, `"ID"`, `"NUM_INT"`)
- `ref` — referência à entrada na `LexTable` (evita duplicação de strings)
- `line` — linha do código-fonte onde o token aparece

**Formato de saída (`toString`):**

```
<TIPO,LINHA,lexema(linha N)>
```

Exemplo: `<IF,3,houdini(linha 0)>`

---

### `LexTable.java` — Tabela de Símbolos

Implementa a tabela de símbolos usando um `HashMap<String, InputTable>`. Funciona como um dicionário centralizado de todos os lexemas encontrados durante a análise.

**Comportamento do método `add`:** Verifica se o lexema já existe na tabela. Se sim, retorna a entrada existente. Se não, cria uma nova `InputTable` e armazena. Isso garante que lexemas idênticos compartilham a mesma referência em memória, evitando duplicação.

**Compartilhamento:** Uma única instância de `LexTable` é criada no `Lexer` e repassada a todos os AFDs, garantindo que toda a análise use a mesma tabela de símbolos.

---

### `InputTable.java` — Entrada na Tabela de Símbolos

Representa uma entrada individual na tabela de símbolos.

**Atributos:**
- `lexema` — o texto original do token (ex: `"houdini"`, `"42"`, `"x"`)
- `linha` — linha onde o lexema foi declarado pela primeira vez

**Observação:** Atualmente, a linha é sempre passada como `0` no momento da inserção. O número de linha real é armazenado no `Token` via `setLine`, não na `InputTable`.

---

## Fluxo Completo — Exemplo Prático

Para o trecho de código:

```
space x = 10
```

O `Lexer` produziria os seguintes tokens:

```
1. "space"  → Identifier → KeyWords → <INT, 1, space(linha 0)>
2. "x"      → Identifier → não é keyword → <ID, 1, x(linha 0)>
3. "="      → MathOperator → <OP_ATRI, 1, =(linha 0)>
4. "10"     → Number → <NUM_INT, 1, 10(linha 0)>
5. EOF      → MathOperator → <EOF, 1, $(linha 0)>
```

Cada token carrega seu tipo semântico, a linha onde aparece e uma referência à tabela de símbolos — informações que serão consumidas pelas fases seguintes do compilador (análise sintática e semântica).