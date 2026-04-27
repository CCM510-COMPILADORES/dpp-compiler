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
| Atribuição                 | =                   | :=                   |
| Declarar função            | `PREY`              | function             |
| Retornar                   | `HOMETOWN`          | retorna              |
| Print (output)             | `pleaser`           | input                |
| Scan  (input)              | `catapult`          | output               |
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
palavra    → [a-zA-Z]+[a-zA-Z]*
num        → [0-9]+

id         → palavra+num*
int        → num+
float      → num+ '.' num+
string     → `“`palavra*`”`

op_logic   → `&&` | `||`
op_comp    → `==` | `!=` | `>=` | `<=` | `>` | `<` 
op_arit    → `+`  | `-`  | `*`  | `/` | `%`

tipo       → `space` | `lithium` | `judas`
atribuicao → tipo id `=` expressao `;`| id `=` expressao `;`
declaracao → tipo id `;`

comentario → `~~` palavra* `\n`

expressao  → termo expressao'
expressao' → op_arit termo expressao' | ε
termo      → `(` expressao `)` | int | float | string | id | chamada_funcao

condicao   → termo condicao'
condicao'  → (op_logic | op_comp) termo condicao' | ε
termo      → `(` condicao `)`  | int | float | id

saida      → `catapult` `[` expressao `]` `;`

entrada           → `pleaser`  `[` entrada' `]` `;`
entrada'          → `“` ponteiros `”` `,` identificadores
ponteiros         → formatadores ponteiros'
ponteiros'        → `,` ponteiros | ε
identificadores   → id identificadores'
identificadores'  → `,` identificadores | ε
formatadores      → `%d` | `%f` | `%s`


declaracao_funcao       → tipo `PREY` id `[` parametros_declaracao `]` `{` comando*  `HOMETOWN` expressao? `}`
parametros_declaracao   → tipo id parametros_declaracao' | ε
parametros_declaracao'  → `,` tipo id parametros_declaracao' | ε

chamada_funcao          → id `[` parametros_chamada `]` `;`
parametros_chamada      → expressao parametros_chamada'
parametros_chamada'     → `,` expressao parametros_chamada' | ε

if        → `houdini`  `(` condicao `)` `{` comando* `}` else
else      → `more` `{` comando* `}` | ε
while     → `problems` `(` condicao `)` `{` comando* `}`
do_while  → `not...ok` `{` comando* `}` `while` `(` condicao `)` `;`
for       → `bloomfield` `(` atribuicao condicao atribuicao `)` `{` comando* `}`

comando   → comentario | atribuicao | entrada | saida | if | while | do_while | for | chamada_funcao
main      → declaracao_funcao* `style` codigo `borderline`
codigo    → comando* codigo* | ε
```

## Instruções Para Execução do Código

## Exemplo de Código
### Código em DPP
```
lithium PREY multiplicar[lithium a, lithium b] {
    ~~ multiplica dois numeros
    HOMETOWN a*b;
}

style
space x;
space q;
lithium y;

bloomfield(x=0; x<=10; x++) {
    catapult[x];
}

houdini((x == 10) && (x != 0)) {
    catapult["Digite um número inteiro e um decimal: "];
    pleaser["%d,%f", q, y];
    catapult["Você digitou: " + q + " e " + y];
}

houdini(y < 10.5) {
    problems(y < 11) {
        y++;
    }
} more {
    lithium res = multiplicar[q, y];
    catapult["Q*Y é igual a: " + res];
}
borderline
```

### Código Traduzido para C
```C
float multiplicar(float a, float b) {
    // multiplica dois numeros
    return a*b;
}

int main(){
  int x;
  int q;
  float y;

  for(x=0; x<=10; x++){
    printf("%d", x);
  }

  if((x==10) && (x!=0)){
    printf("Digite um numero decimal e um inteiro: ");
    scanf("%d %f", &q, &y);
    printf("Voce digitou: %d e %f", q, y);
  }

  if(y < 10.5){
    while(y < 11){
        y++;
    }
  } else{
    float res = multiplicar(q, y);
    printf("Q*Y e' igual a: %f", res);
  }
}
```