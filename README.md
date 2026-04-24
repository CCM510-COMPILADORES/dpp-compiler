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
| Declarar função            | `pleaser`           | function             |
| Retornar                   | `catapult`          | retorna              |
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
expressao  → expressao op_arit expressao | `(` expressao `)` | num | palavra | id
condicao   → condicao  op_comp condicao  | `(` condicao  `)` | num | id

comando → atribuicao | entrada | saida | if | while | do_while | for

parametros_criacao  → parametros_criacao  | parametros_criacao  `,` parametros_criacao  |
                      tipo id
parametros_passagem → parametros_passagem | parametros_passagem `,` parametros_passagem |
                      expressao
declarar_funcao     → tipo `pleaser` id `(` parametros_criacao `)` `{` comando*  `catapult` expressao `}`
chamar_funcao       → id `(` parametros_passagem `)` `;`

if        → `houdini`  `(` condicao `)` `{` comando* `}` |
            `houdini`  `(` condicao `)` `{` comando* `}` `more` `{` comando* `}`
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
space x;

bloomfield(x=0; x<=10; x++) {
    catapult(x);
}

houdini(x == 10) {
    lithium y;

    catapult("Digite um número decimal: ");
    pleaser("%f", y);
    catapult("Você digitou: " + y);
}

houdini(y < 10.5) {
    problems(y < 11) {
        y++;
    }
} more {
    lithium res = x*y;
    catapult("X*Y é igual a: " + res);
}
borderline
```

### Código Traduzido para C
```C
int main(){
  int x;

  for(x=0; x<=10; x++){
    printf("%d", x);
  }

  if(x==10){
    float y;

    printf("Digite um numero decimal: ");
    scanf("%f", &y);
    printf("Voce digitou: %d", y);
  }

  if(y < 10.5){
    while(y < 11){
        y++;
    }
  } else{
    float res = x*y;
    printf("X*Y e' igual a: %f", res);
  }
}
```