package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
    //list of tokens to read
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression(){
        return equality();
    }

    private Expr equality(){
        Expr expr = comparison();
        
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
           Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    
    //see if the current token has any of the given types
    private boolean match(TokenType... types){
        for (TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }

        return false;
    }
    //check if current token is of the given type; does NOT consume token
    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }
    //consumes current token and returns it
    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd(){
        return peek().type == EOF;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

}
