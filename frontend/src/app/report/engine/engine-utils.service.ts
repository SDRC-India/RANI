import { Injectable } from '@angular/core';
import { Token } from './Token';
import { TokenChar } from './TokenChar';

@Injectable({
  providedIn: 'root'
})
export class EngineUtilsProvider {


  result: Token[] = []
  letterBuffer: String[] = []
  numberBuffer: String[] = []
  outputQueue: Token[] = []
  opStack: Token[] = []
  wereValuesStack : Token[] = [];
	argStack : number[] = [];

  OPERATORS: Map < String, number[] > = new Map();
  LEFT_ASSOC_ARRAY: number[] = []
  RIGHT_ASSOC_ARRAY: number[] = []
  LEFT_ASSOC: number = 0;
  RIGHT_ASSOC: number = 1;

  constructor() {
    this.LEFT_ASSOC_ARRAY.push(0)
    this.LEFT_ASSOC_ARRAY.push(this.LEFT_ASSOC)
    this.OPERATORS.set("&", this.LEFT_ASSOC_ARRAY);
    this.OPERATORS.set("|", this.LEFT_ASSOC_ARRAY);
  }

  // parseAndResolveScore(expression,questionMap){
  //   let expressions[] = expression.split("+");



  // }


  resolveExpression(expression, questionMap): any {
    // if(expression == "lessThan(${F1Q35},sum(${F1Q36},${F1Q37}))"){
    //   // expression = "lessThan(sum(${F1Q36},${F1Q37}),${F1Q35})"
    //   // expression = "(${F1Q36} + 3) <= ${F1Q35}"
    //   expression = "  ${F1Q35} >= (${F1Q36} + 3)"
    // }
    this.result = []
    this.letterBuffer = []
    this.numberBuffer = []

    this.outputQueue = []
    this.opStack = []
    this.wereValuesStack = []
    this.argStack = []
    
    let tokens: Token[] = this.tokenizer(expression, questionMap);
    let reversedTokens = this.InfixToRpnTransformer(tokens)
    let resultingTokens:Token =  this.expressionResolver(reversedTokens);

    console.log("LogginResult After Parsing Exp::::::",expression,JSON.stringify(resultingTokens))
    return resultingTokens.value.toString()
    
  }


  tokenizer(s: String, questionMap: {}): Token[] {

    let  ss = s.replace(" ", "");
    let str: String[] = ss.split("");

    for (let i = 0; i < str.length; i++) {

      let ch: string = str[i] as string;
      if (ch == '$') {
        let qName = "";
        let values: String[] = []
        for (let j = i + 2; j < str.length; j++) {
          if (str[j] == "}") { 
            i = j;
            break;
          }
          qName = qName + (str[j]);
        }
        if (questionMap[qName].value) {
          // for (let ss of ((questionMap[qName].value) as string).split("")) {
          //   values.push(ss);
          // }
          values.push(questionMap[qName].value);
          this.pushIntoNumberBuffer(values);
        } else {
          values.push('null');
          // values.push('0');
          this.pushIntoNumberBuffer(values);
        }

      } else if (new RegExp("\\d").test(ch)) {
        this.numberBuffer.push(ch);
      } else if (new RegExp("[.]").test(ch)) {
        this.numberBuffer.push(ch);
      } else if (new RegExp("[a-zA-Z]").test(ch)) {
        if (this.numberBuffer.length > 0) {
          this.clearNumberBuffer();
          this.result.push(new Token(TokenChar.OPERATOR, "*"));
        }
        this.letterBuffer.push(ch);
      } else if (new RegExp("[-+*/%=<><=>=]").test(ch)) {

        this.clearNumberBuffer();
        this.clearLetterBuffer();
        let ch1: string = str[i+1] as string;
        if(ch1 == "="){
          this.result.push(new Token(TokenChar.OPERATOR, ch+ch1));
          i = i+1;
        }else{
          this.result.push(new Token(TokenChar.OPERATOR, ch));
        }
       
      } else if (new RegExp("[(]").test(ch)) {
        if (this.letterBuffer.length > 0) {
          this.result.push(new Token(TokenChar.FUNCTION,
            this.letterBuffer.join("")));
          this.letterBuffer = []
        } else if (this.numberBuffer.length > 0) {
          this.clearNumberBuffer();
          this.result.push(new Token(TokenChar.OPERATOR, "*"));
        }
        this.result.push(new Token(TokenChar.LEFT_PARENTHESIS, ch));
      } else if (new RegExp("[)]").test(ch)) {
        this.clearLetterBuffer();
        this.clearNumberBuffer();
        this.result.push(new Token(TokenChar.RIGHT_PARENTHESIS, ch));
      } else if (new RegExp("[,]").test(ch)) {
        this.clearNumberBuffer();
        this.clearLetterBuffer();
        this.result.push(new Token(TokenChar.FUNCTION_ARGS_SEPARATOR, ch));
      }
    };

    if (this.numberBuffer.length > 0) {
      this.clearNumberBuffer();
    }
    if (this.letterBuffer.length > 0) {
      this.clearLetterBuffer();
    }

    return this.result;
  }

  clearLetterBuffer() {
    let l = this.letterBuffer.length;
    for (let i = 0; i < l; i++) {
      this.result.push(new Token(TokenChar.VARIABLE, this.letterBuffer[i]));
      if (i < l - 1) {
        this.result.push(new Token(TokenChar.OPERATOR, "*"));
      }
    }
    this.letterBuffer = []
  }

  clearNumberBuffer() {
    if (this.numberBuffer.length > 0) {
      this.result.push(
        new Token(TokenChar.LITERAL, this.numberBuffer.join("")));
      this.numberBuffer = []
    }
  }

  pushIntoNumberBuffer(numbers: String[]) {
    if (numbers.length > 0) {
      numbers.forEach(num => {
        this.numberBuffer.push(num);
      });
    }
  }
//   while there are tokens to be read:
//   read a token.
//   if the token is a number, then:
//       push it to the output queue.
//   if the token is a function then:
//       push it onto the operator stack 
//   if the token is an operator, then:
//       while ((there is a function at the top of the operator stack)
//              or (there is an operator at the top of the operator stack with greater precedence)
//              or (the operator at the top of the operator stack has equal precedence and is left associative))
//             and (the operator at the top of the operator stack is not a left bracket):
//           pop operators from the operator stack onto the output queue.
//       push it onto the operator stack.
//   if the token is a left bracket (i.e. "("), then:
//       push it onto the operator stack.
//   if the token is a right bracket (i.e. ")"), then:
//       while the operator at the top of the operator stack is not a left bracket:
//           pop the operator from the operator stack onto the output queue.
//       pop the left bracket from the stack.
//       /* if the stack runs out without finding a left bracket, then there are mismatched parentheses. */
// if there are no more tokens to read:
//   while there are still operator tokens on the stack:
//       /* if the operator token on the top of the stack is a bracket, then there are mismatched parentheses. */
//       pop the operator from the operator stack onto the output queue.
// exit.

  InfixToRpnTransformer(tokens: Token[]): Token[] {

    tokens.forEach(v => {
      // if the token is a number, then: push it to the output queue.
      if (v.type == TokenChar.LITERAL) {
            this.outputQueue.push(v);
            if(this.wereValuesStack.length != 0 ) {
              this.wereValuesStack.pop();
              this.wereValuesStack.push(new Token(TokenChar.TRUE, "true"));
            }
          }
          else if (v.type == TokenChar.FUNCTION) {
                this.opStack.push(v);
                this.argStack.push(0);
                let temwereValuesStack:Token[] = []
                if(this.wereValuesStack.length !=0) {
                  this.wereValuesStack.pop();
                  temwereValuesStack.push(new Token(TokenChar.TRUE, "true"));
                }
                this.wereValuesStack = (temwereValuesStack)
                this.wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
          } 
          else if(v.type == TokenChar.LEFT_PARENTHESIS){
             this.opStack.push(v);
          } else if (v.type == TokenChar.RIGHT_PARENTHESIS) {
           
                while (this.opStack.length != 0  && this.opStack[this.opStack.length - 1].type==TokenChar.OPERATOR  && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
                  this.outputQueue.push(this.opStack.pop());
                }
                if(this.opStack.length == 0){ console.error("Invalid expressions !");throw Error("Invalid Expression") }

                this.opStack.pop();
                // if (this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.FUNCTION) {
                //     this.outputQueue.push(this.opStack.pop());
                // }
                if (this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.FUNCTION) {
                  // outputQueue.push(opStack.pop());
                  let f : Token  = this.opStack.pop();
                  let argsCount = this.argStack.pop();
                  let  hasValues : Token= this.wereValuesStack.pop();
                  if (hasValues.type == TokenChar.TRUE) {
                    argsCount++;
                  }
                  f.arguments = argsCount;
                  this.outputQueue.push(f);
                }
          }else if (v.type == TokenChar.OPERATOR) {
                while (this.opStack.length != 0 && (this.opStack[this.opStack.length - 1].type == TokenChar.OPERATOR)
                  &&
                  ((v.associativity() == "left" && (v.precedence() <= this.opStack[this.opStack.length - 1].precedence()))
                    ||
                    (v.associativity() == "right" &&
                      v.precedence() < this.opStack[this.opStack.length - 1].precedence()))) {
                  this.outputQueue.push(this.opStack.pop());
                }
                this.opStack.push(v);
           }else if(v.type == TokenChar.FUNCTION_ARGS_SEPARATOR){
                while(this.opStack.length != 0 && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
                  this.outputQueue.push(this.opStack.pop());
                }
                if(this.wereValuesStack.pop().type == TokenChar.TRUE) {
                  let argsc = this.argStack.pop();
                  argsc++;
                  this.argStack.push(argsc);
               }
               this.wereValuesStack.push(new Token(TokenChar.FALSE, "false"));
              }
            })
        if(this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS){
          console.error("Invalid expressions !");throw Error("Invalid Expression") 
        }
        this.opStack = this.opStack.reverse();
          for (let op of this.opStack) {
            this.outputQueue.push(op);
          }
          return this.outputQueue;
  }


  expressionResolver(tokens: Token[]): Token {
    let rstack: Token[] = []

    // For each token
    for (let token of tokens) {
      // If the token is a value push it onto the stack
      console.log("Token::" + token);

      switch (token.type) {
        case TokenChar.LITERAL:
          rstack.push(token);
          break;
        case TokenChar.FUNCTION:
          switch (token.value.toString()) {
            case "clone":
            let val: Token = rstack.pop();
            rstack.push(new Token(TokenChar.RESULT, val.value));
            break;
            case "sum":
              {
                let sum = NaN
                let farguments = token.arguments;
                do{
                  farguments--;
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(sum))
                    sum = +0
                  if (val.value != null && val.value != "null") {
                    sum = +sum + +val.value
                  }
                }while(farguments >= 1)
                // while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                //   let val: Token = rstack.pop();
                //   if (val.value != null && val.value != "null" && isNaN(sum))
                //     sum = +0
                //   if (val.value != null && val.value != "null") {
                //     sum = +sum + +val.value
                //   }
                // }
                rstack.push(new Token(TokenChar.RESULT, sum));
              }
              break;
            case "minus":
              {
                // BigDecimal minus = BigDecimal.ZERO;
                let minus = NaN
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null && val.value != "null" && isNaN(minus))
                    minus = +0
                  if (val.value != null && val.value != "null") {
                    minus = +val.value - +minus
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, minus));
              }
              break;
            case "mul":
              {
                // BigDecimal mul = BigDecimal.ONE;
                let mul: any = 1
                while (!(rstack.filter(f => (f.type == TokenChar.LITERAL)).length == 0)) {
                  let val: Token = rstack.pop();
                  if (val.value != null) {
                    mul = +mul * +val.value
                  }
                }
                rstack.push(new Token(TokenChar.RESULT, mul));

              }
              break;
            case "div":
              {
                
                if(token.arguments == 2){
                  let t1: Token = rstack.pop();
                  let t2: Token = rstack.pop();
                  if (t2.value == null) {
                    // throw new Error("Division not possible, denominator can't be null");
                    rstack.push(new Token(TokenChar.RESULT,null));
                  }
                  if (t1.value != null || t2.value != null) {
                    let i1 = (t1.value.toString());
                    let i2 = (t2.value.toString());
                    rstack.push(new Token(TokenChar.RESULT, +i2 / +i1));
                  }
                }else if(token.arguments == 3){
                  // ;
                  let precision : Token = rstack.pop();
                  let denominator: Token = rstack.pop();
                  let numerator: Token = rstack.pop();
                 
                  if (denominator.value == null) {
                    // throw new Error("Division not possible, denominator can't be null");
                    rstack.push(new Token(TokenChar.RESULT,null));
                  }
                  if (denominator.value != null || numerator.value != null) {
                    let i1 = (numerator.value.toString());
                    let i2 = (denominator.value.toString());
                    rstack.push(new Token(TokenChar.RESULT, (Math.round( numerator.value / denominator.value * 100 * 10 ) / 10).toFixed(precision.value)));
                  }
                }else{
                  throw new Error("Invalid number of arguments for division!")
                }
               
              }
              break;
            case "round":
              {
                let rounder: Token = rstack.pop();
                let t1 = rstack.pop();
                // BigDecimal d = t1.getValue() == null ? BigDecimal.ZERO :  new BigDecimal(t1.getValue().toString());
                // d = d.setScale(Integer.valueOf(rounder.getValue().toString()),RoundingMode.HALF_UP);
                rstack.push(new Token(TokenChar.LITERAL, t1.value.round(rounder.value as any)));
              }
              break;
            case "lessThan":
              {
                let less1 = rstack.pop()
                let less2 = rstack.pop()
                if(less1.value != null && less1.value != "null" && less1.value != "" &&  typeof less1.value == 'string'){
                  less1.value = Number(less1.value)
                }
                if(less2.value != null && less2.value != "null" && less2.value != ""  &&  typeof less2.value == 'string'){
                  less2.value = Number(less2.value)
                }
                if (less1.value != null && less1.value != "null" && less1.value != "" && less2.value != null && less2.value != "null" && less2.value != ""  &&  (less1.value < less2.value))
                {
                  rstack.push(new Token(TokenChar.RESULT, 1));
                }else{
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
              case "if":
              {
              
             
                let scoreFailure = rstack.pop()
                let scoreSucess = rstack.pop()
                let valueOfControlAfterConditionResolved = rstack.pop()

          
                if(valueOfControlAfterConditionResolved.value == 1){
                  rstack.push(new Token(TokenChar.RESULT, parseInt(scoreSucess.value)));
                }else{
                  rstack.push(new Token(TokenChar.RESULT, parseInt(scoreFailure.value)));
                }
              }
              break;
              case "selected":
              {
                let valueOfControl = rstack.pop()
                let valueToMatch = rstack.pop()
                if(valueOfControl.value == valueToMatch.value){
                  rstack.push(new Token(TokenChar.RESULT, 1));
                }else{
                  rstack.push(new Token(TokenChar.RESULT, 0));
                }
              }
              break;
          }
          break;
          case TokenChar.OPERATOR:{
            switch (token.value) {
              case "+":
                    let sum = NaN
                    while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                      let val: Token = rstack.pop();
                      if (val.value != null && val.value != "null" && isNaN(sum))
                        sum = +0
                      if (val.value != null && val.value != "null") {
                        sum = +sum + +val.value
                      }
                    }
                    rstack.push(new Token(TokenChar.RESULT, sum));
                break;
                case "-":
                {
                  // BigDecimal minus = BigDecimal.ZERO;
                  let minus = NaN
                  while (!(rstack.filter(f => (f.type == TokenChar.LITERAL || f.type == TokenChar.RESULT)).length == 0)) {
                    let val: Token = rstack.pop();
                    if (val.value != null && val.value != "null" && isNaN(minus))
                      minus = +0
                    if (val.value != null && val.value != "null") {
                      minus = +val.value - +minus
                    }
                  }
                  rstack.push(new Token(TokenChar.RESULT, minus));
                }
                break;
                case "*":
                
                break;
                case "/":
                
                break;
                case "<":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                  else if(value2.value < value1.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case ">":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }


                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, null));
                  }
                  else if(value1.value > value2.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case "<=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, null));
                  }
                  else if(value2.value <= value1.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case ">=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value!="null" && typeof value1.value == "string"){
                    value1.value = parseInt(value1.value)
                  }
                  if(value2.value!="null" && typeof value2.value == "string"){
                    value2.value = parseInt(value2.value)
                  }

                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, null));
                  }
                  else if(value2.value >= value1.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
                case "=":
                {
                  let value1 = rstack.pop()
                  let value2 = rstack.pop()
                  if(value1.value=="null" || value2.value=="null"){
                    rstack.push(new Token(TokenChar.RESULT, null));
                  }
                  else if(value1.value == value2.value){
                    rstack.push(new Token(TokenChar.RESULT, 1));
                  }else{
                    rstack.push(new Token(TokenChar.RESULT, 0));
                  }
                }
                break;
            }
          }
        default:
          break;
      }
    }
    return rstack.pop();
  }



  isOperator(token: String): boolean {
    return this.OPERATORS.has(token);
  }

  isAssociative(token: String, type: number): boolean {
    if (!this.isOperator(token)) {
      throw new Error("Token is invalid: " + token);
    }
    if (this.OPERATORS.get(token)[1] == type) {
      return true;
    }
    return false;
  }

  // Compare precedence of operators.
  compareOperatorPrecedence(token1: String, token2: String): number {
    if (!this.isOperator(token1) || !this.isOperator(token2)) {
      throw new Error("Tokens are invalid: " + token1 + " " + token2);
    }
    return this.OPERATORS.get(token1)[0] - this.OPERATORS.get(token2)[0];
  }

  // Convert infix expression format into reverse Polish notation
  transformInfixToReversePolishNotationForRelevance(inputTokens: String[]): String[] {
    let outputArray: String[] = []
    let stack: String[] = []

    for (let token of inputTokens) {
      if (this.isOperator(token)) {
        while (!(stack.length == 0) && this.isOperator(stack[stack.length - 1])) {
          if ((this.isAssociative(token, this.LEFT_ASSOC) && this.compareOperatorPrecedence(token, stack[stack.length - 1]) <= 0) 
          ||
          (this.isAssociative(token, this.RIGHT_ASSOC) && this.compareOperatorPrecedence(token, stack[stack.length - 1]) < 0)) {
           
            outputArray.push(stack.pop());
            continue;
          }
          break;
        }
        stack.push(token);
      } else if (token == "(") {
        stack.push(token);
      } else if (token == ")") {
        while (!(stack.length == 0) && !(stack[stack.length - 1] == "(")) {
          outputArray.push(stack.pop());
        }
        stack.pop();
      } else {
        outputArray.push(token);
      }
    }
    while (!(stack.length == 0)) {
      outputArray.push(stack.pop());
    }
    return outputArray;
  }

  arithmeticExpressionResolverForRelevance(tokens: String[]): boolean {

    let stack: String[] = []
    // For each token
    for (let token of tokens) {
      // If the token is a value push it onto the stack
      if (token == "")
        continue;
      if (!this.isOperator(token)) {
        stack.push(token);
      } else {
        // Token is an operator: pop top two entries
        let d2: Number = new Number(stack.pop());
        let d1: Number = new Number(stack.pop());

        // Get the result
        switch (token) {
          case "&":
            if (d1 == 1 && d2 == 1) {
              stack.push(new String(1));
            } else
              stack.push(new String(0));
            break;
          case "|":
            if (d1 == 1 || d2 == 1) {
              stack.push(new String(1));
            } else {
              stack.push(new String(0));
            }
            break;
          
        }
      }
    }
    return new Number(stack.pop()) == 1 ? true : false;
  }


  expressionToArithmeticExpressionTransfomerForRelevance(expression: String, questionMap): String {

    let expressionTransformed = "";
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = (expressions[i]);
        switch (exp) {
          case "@AND":
            expressionTransformed = expressionTransformed + " & ";
            break;
          case "@OR":
            expressionTransformed = expressionTransformed + " | ";
            break;
          case "{":
            expressionTransformed = expressionTransformed + "";
            break;
          case "}":
            expressionTransformed = expressionTransformed + "";
            break;
          case "":
            break;
          default:
            if (exp.includes("(")) {
              expressionTransformed = expressionTransformed + " ( ";
            } else if (exp.includes(")")) {
              expressionTransformed = expressionTransformed + " ) ";
            }
            break;
          case "optionEquals":
            {
              // find the type detail Id
              // if null or value in expression doesn't match match value selected in UI push
              // 0
              // else push 1
              {
                let qName = (expressions[i - 1]);
                if (questionMap[qName as any].controlType == 'dropdown' &&  questionMap[qName as any].type == 'checkbox') {
                  let typeDetailsId = parseInt(expressions[i + 1] as string);
                  if (questionMap[qName as any].value) {
                   for (let v of questionMap[qName as any].value) {
                      if (v == typeDetailsId) {
                        expressionTransformed = expressionTransformed + " 1 ";
                        break;
                      } else {
                        expressionTransformed = expressionTransformed + " 0 ";
                      }
                    }
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;
                } else {
                  //handling dropdodwn
                  let typeDetailsId = parseInt(expressions[i + 1] as string);

                  if (questionMap[qName as any].value == typeDetailsId) {
                    expressionTransformed = expressionTransformed + " 1 ";
                  } else {
                    expressionTransformed = expressionTransformed + " 0 ";
                  }
                  i = i + 1;
                }

              }
            }
            break;
          case "optionEqualsMultiple":
            // find the type detail Id
            // if null or value in expression doesn't match match value selected in UI push
            // 0
            // else push 1
            {
              let typeDetailsIds: String[] = expressions[i + 1].split(",");
              let qName = expressions[i - 1];
              let matches = 0;
              for (let typeDetailsId of typeDetailsIds) {
                if (questionMap[qName as any].value != null &&
                  parseInt(questionMap[qName as any].value as string) == parseInt(typeDetailsId as string)) {
                  matches = 1;
                  break;
                } else {
                  matches = 0;
                }
              }
              if (matches == 1) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "textEquals":
            {
              let num = (expressions[i + 1]);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null && (questionMap[qName as any].value as string) == num) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "numEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) == (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "greaterThan":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) > (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "greaterThanEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) >= (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "lessThan":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) < (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
          case "lessThanEquals":
            {
              let num = parseInt(expressions[i + 1] as string);
              let qName = expressions[i - 1];
              if (questionMap[qName as any].value != null &&
                parseInt(questionMap[qName as any].value as string) <= (num)) {
                expressionTransformed = expressionTransformed + " 1 ";
              } else {
                expressionTransformed = expressionTransformed + " 0 ";
              }
              i = i + 1;
            }
            break;
        }
      }
    }
    return expressionTransformed;
  }


  // InfixToRpnTransformer(tokens: Token[]): Token[] {
  //   tokens.forEach(v => {
  //     // If the token is a number, then push it to the output queue
  //     if (v.type == TokenChar.LITERAL || v.type == TokenChar.VARIABLE) {
  //       this.outputQueue.push(v);
  //     }
  //     // If the token is a function token, then push it onto the stack.
  //     else if (v.type == TokenChar.FUNCTION) {
  //       this.opStack.push(v);
  //     } // If the token is a function argument separator
  //     else if (v.type == TokenChar.FUNCTION_ARGS_SEPARATOR) {
  //       // Until the token at the top of the stack is a left parenthesis
  //       // pop operators off the stack onto the output queue.
  //       while ((this.opStack.length != 0 && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS))) {
  //         this.outputQueue.push(this.opStack.pop());
  //       }
  //       /*
  //        * if(opStack.length == 0){ console.log("Mismatched parentheses"); return; }
  //        */
  //     }
  //     // If the token is an operator, o1, then:
  //     else if (v.type == TokenChar.OPERATOR) {
  //       // while there is an operator token o2, at the top of the operator stack and
  //       // either
  //       while (this.opStack.length != 0 && (this.opStack[this.opStack.length - 1].type == TokenChar.OPERATOR)
  //         // o1 is left-associative and its precedence is less than or equal to that of
  //         // o2, or
  //         &&
  //         ((v.associativity() == "left" && (v.precedence() <= this.opStack[this.opStack.length - 1].precedence()))
  //           // o1 is right associative, and has precedence less than that of o2,
  //           ||
  //           (v.associativity() == "right" &&
  //             v.precedence() < this.opStack[this.opStack.length - 1].precedence()))) {
  //         this.outputQueue.push(this.opStack.pop());
  //       }
  //       // at the end of iteration push o1 onto the operator stack
  //       this.opStack.push(v);
  //     }

  //     // If the token is a left parenthesis (i.e. "("), then push it onto the stack.
  //     else if (v.type == TokenChar.LEFT_PARENTHESIS) {
  //       this.opStack.push(v);
  //     }
  //     // If the token is a right parenthesis (i.e. ")"):
  //     else if (v.type == TokenChar.RIGHT_PARENTHESIS) {
  //       // Until the token at the top of the stack is a left parenthesis, pop operators
  //       // off the stack onto the output queue.
  //       while (this.opStack.length != 0 && !(this.opStack[this.opStack.length - 1].type == TokenChar.LEFT_PARENTHESIS)) {
  //         this.outputQueue.push(this.opStack.pop());
  //       }
  //       /*
  //        * if(opStack.length == 0){ console.log("Unmatched parentheses"); return; }
  //        */
  //       // Pop the left parenthesis from the stack, but not onto the output queue.
  //       this.opStack.pop();

  //       // If the token at the top of the stack is a function token, pop it onto the
  //       // output queue.
  //       if (this.opStack.length != 0 && this.opStack[this.opStack.length - 1].type == TokenChar.FUNCTION) {
  //         this.outputQueue.push(this.opStack.pop());
  //       }
  //     }
  //   });
  //   this.opStack = this.opStack.reverse();
  //   for (let op of this.opStack) {
  //     this.outputQueue.push(op);
  //   }
  //   return this.outputQueue;
  // }
}
