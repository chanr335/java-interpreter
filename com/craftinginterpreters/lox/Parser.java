package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}


    //list of tokens to read
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    //Expr parse(){
    //    try{
    //    return expression();
    //    } catch(ParseError error){
    //        return null;
    //    }
    //}
    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    //expression -> equality;.....
    //expression -> assignment ;
    private Expr expression(){
        return assignment();
    }
    
    //delcaration -> varDecl | statement;
    private Stmt declaration(){
        try{
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error){
        synchronize();
        return null;
        }
    }

    //statement -> exprStmt | printStmt
    private Stmt statement(){
        if(match(PRINT)) return printStatement();
        return expressionStatement();
    }

    //printStmt -> "print" expression ";" ;
    private Stmt printStatement(){
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    //varDecl -> "var" IDENTIFIER ("=" expression)? ";" ;
    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    //exprStmt -> expression ";" ;
    private Stmt expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    //assignment -> IDENTIFIER "=" assignment | equality ;
    private Expr assignment(){
        Expr expr = equality();

        if (match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable){
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    //equality -> comparison(( "!=" | "==") comparison)*;
    private Expr equality(){
        Expr expr = comparison();
        
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
           Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //comparison -> term (( ">" | ">=" | "<" | "<=" ) term)* ;
    private Expr comparison(){
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //term -> factor (( "-" | "+" ) factor ) * ;
    private Expr term(){
        Expr expr = factor();
        
        while (match(MINUS, PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //factor -> unary(( "/" | "*" ) unary ) * ;
    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    //unary -> ( "!" | "-" ) unary
    private Expr unary(){
        if (match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    //primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")";
    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL)) return new Expr.Literal(null);

        if(match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect Expression.");
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

    private Token consume(TokenType type, String message){
        if(check(type)) return advance();

        throw error(peek(), message);
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

    private ParseError error(Token token, String message){
        Lox.error(token, message);
        return new ParseError();
    }

    //after catching a ParseError, call this to get back into sync
    private void synchronize(){
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
            advance();
        }
    }

}
