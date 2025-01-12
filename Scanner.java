package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*

private final List<Token> tokens = new ArrayList<>();
private int start = 0; //points to first character in lexeme being scanned
private int current = 0; //tracks what source line current is on
private int line = 1;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    Scanner(String source){
        this.source = source;
    }
}

List<Token> scanTokens(){
    while (!isAtEnd()){
        //current lexeme
        start = current;
        scanToken();
    }

    tokens.add(newToken(EOF, "", null, line));
    return tokens;
}

private boolean isAtEnd(){
    return current >= source.length();
}

