package it.gadg.contagiapp;

public class User {
    public String nome;
    public String cognome;
    public String email;
    public int rischio;
    public int etichetta;

    public User(String nome,String cognome,String email){
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.rischio = 0;
        this.etichetta=1;


    }
}
