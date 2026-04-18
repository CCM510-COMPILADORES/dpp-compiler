# DPP Compiler

## Linguagem "Dhara" - Projeto de Compiladores

Esse projeto implementa uma linguagem fictícia "Dhara", tendo suas palavras reservadas baseadas nas músicas favoritas 
da namorada de um dos integrantes do grupo. O principal objetivo é ser capaz de implementar o Front-End de um compilador,
contemplando as fases de análise Léxica, Sintática e Semântica e traduzir para a linguagem-alvo determinada pelo professor
Charles Henrique P. Ferreira.

---

## Expressões Regulares

| Função                     | Palavra-chave (DPP) | Tradução Tradicional |
|:---------------------------|:-------------------:|:---------------------|
| Início do programa         | `style`             | —                    |
| Fim do programa            | `borderline`        | —                    |
| Tipo inteiro               | `space`             | inteiro              |
| Tipo decimal               | `lithium`           | decimal              |
| Tipo texto/string          | `judas`             | texto                |
| Começar/terminar texto     | `“”`                | `“”`                 |
| Declaração de variável     | `tipos acima`       | —                    |
| Atribuição                 | =                   | :=                   |
| Entrada (input)            | `pleaser`           | leia                 |
| Saída (print)              | `catapult`          | escreva              |
| Estrutura condicional if   | `houdini`           | se                   |
| Estrutura condicional else | `more`              | senao                |
| Laço while                 | `problems`          | enquanto             |
| Laço for                   | `bloomfield`        | para                 |
| Laço do...while            | `not...ok`          | faça enquanto        |
| Para laço                  | `loser`             | pare                 |
| Parêntese esquerdo         | (                   | (                    |
| Parêntese direito          | )                   | )                    |
| Chave esquerda             | {                   | {                    |
| Chave direita              | }                   | }                    |
| Ponto e vírgula            | ;                   | ;                    |
| Comentário de linha        | ~~                  | //                   |
| Sinal de mais              | +                   | +                    |
| Sinal de menos             | -                   | -                    |
| Sinal de multiplicação     | *                   | *                    |
| Sinal de divisão           | /                   | /                    |

---

## Gramática Livre de Contexto (GLC)


```ebnf
palavra → [a-zA-Z]+[a-zA-Z]*
numero → [0-9]+

id      → palavra+numero*
inteiro → num+
decimal → num+ '.' num+
string  → `“`palavra`”`

operador_comp → `>` | `<` | `==` | `>=` | `<=`
operador_arit → `+` | `-` | `*` | `/` 

exp      → exp operador_arit exp | `(` exp `)` | num | id
condicao → (` id operador_comp num `)`
```

## Instruções Para Execução do Código

## Exemplo de Código
### Código em DPP
```
style

lithium x;

borderline
```

### Código Traduzido para X
```C
printf("Hello, World!");
```