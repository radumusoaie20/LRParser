package parser;

import grammar.Production;

import java.util.Objects;

public class Article {

    public Production production;

    public int dot;

    public Article(Production production){
        this.production = production;
        this.dot = 0; //0 = Before first character
    }

    @Override
    public int hashCode() {
        return production.hashCode() * 13 + dot * 11;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return dot == article.dot && Objects.equals(production, article.production);
    }

    @Override
    public String toString(){
        String result = production.start + " -> ";
        int i = 0, l =production.result.length();
        for(;i < dot && i < l; i++)
            result += production.result.charAt(i);
        result += '.';
        for(; i < l; i++)
            result += production.result.charAt(i);
        return result;
    }
}
