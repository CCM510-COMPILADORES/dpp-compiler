# DPP Compiler

## Linguagem "Dhara" - Projeto de Compiladores

Esse projeto implementa uma linguagem fictícia "Dhara", tendo suas palavras reservadas baseadas nas músicas favoritas da namorada de um dos integrantes do grupo. O principal objetivo é ser capaz de implementar o Front-End de um compilador, contemplando as fases de análise Léxica, Sintática e Semântica e traduzir para a linguagem-alvo determinada pelo Prof. Dr. Charles Henrique P. Ferreira.

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

```
palavra → [a-zA-Z]+[a-zA-Z]*
num     → [0-9]+

id      → palavra+num*
int     → num+
float   → num+ '.' num+
string  → `“`palavra*`”`

op_log  → `&&` | `||` | `!`
op_comp → `==` | `>=` | `<=` | `>` | `<` 
op_arit → `+`  | `-`  | `*`  | `/` | `%`

tipo       → `space` | `lithium` | `judas`
atribuicao → tipo id `=` expressao `;`| id `=` expressao `;`
declaracao → tipo id `;`
expressao  → expressao op_arit expressao | `(` expressao `)` | num | id
condicao   → id op_comp num | id op_comp id | num op_comp num | `(` condicao `)`

comando → atribuicao | entrada | saida | if | while | do_while | for

entrada → `pleaser` `(` “%i” id `)` `;` | `pleaser` `(` “%f” id `)` `;` | `pleaser` `(` “%s” id `)` `;`
saida   → `catapult` expressao `;`

if        → `houdini` `(` condicao `)` `{` comando* `}` |
            `houdini` `(` condicao `)` `{` comando* `}` `more` `{` comando* `}`
while     → `problems` `(` condicao `)` `{` comando* `}`
do_while  → `not...ok` `{` comando* `}` `while` `(` condicao `)` `;`
for       → `bloomfield` `(` atribuicao condicao atribuicao `)` `{` comando* `}`

main    → `style` codigo `borderline`
codigo  → comando* codigo*
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
int main(){
  int x;
}
```