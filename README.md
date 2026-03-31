# DPP Compiler

### Linguagem "Dhara" - Projeto de Compiladores

---


Esse projeto implementa uma linguagem fictícia "Dhara", tendo suas palavras reservadas baseadas nas músicas favoritas 
da namorada de um dos integrantes do grupo. O principal objetivo é ser capaz de implementar o Front-End de um compilador,
contemplando as fases de análise Léxica, Sintática e Semântica e traduzir para a linguagem-alvo determinada pelo professor
Charles Henrique P. Ferreira.

---

### Expressões Regulares

| Função |       DPP        | Tradução Tradicional |
|:--|:----------------:|:--|
| Início do programa |      style       | — |
| Fim do programa |    borderline    | — |
| Tipo inteiro |      space       | inteiro |
| Tipo decimal |     lithium      | decimal |
| Tipo texto/string |      judas       | texto |
| Começar/terminar texto |        “”        | “” |
| Declaração de variável | usa o tipo acima | — |
| Atribuição |        =         | := |
| Entrada (input) |     pleaser      | leia |
| Saída (print) |     catapult     | escreva |
| Estrutura condicional if |     houdini      | se |
| Estrutura condicional else |       more       | senao |
| Laço while |     problems     | enquanto |
| Laço for |    bloomfield    | para |
| Laço do...while |    not... ok     | faça enquanto |
| Para laço |      loser       | pare |
| Parêntese esquerdo |        (         | ( |
| Parêntese direito |        )         | ) |
| Chave esquerda |        {         | { |
| Chave direita |        }         | } |
| Ponto e vírgula |        ;         | ; |
| Comentário de linha |        ~~        | // |
| Sinal de mais |        +         | + |
| Sinal de menos |        -         | - |
| Sinal de multiplicação |        *         | * |
| Sinal de divisão |        /         | / |

---

### Gramática Livre de Contexto


```ebnf


id → [a-z]+
num → [0-9]+ | num .? num+ 
string → '"'id'"'

operador_comp → '>' | '<' | '==' | '>=' | '<='
operador_arit → '+' | '-' | '*' | '/' 

exp → 
condicao → 





```


### Instruções Para Execução do Código

### Exemplo de Código