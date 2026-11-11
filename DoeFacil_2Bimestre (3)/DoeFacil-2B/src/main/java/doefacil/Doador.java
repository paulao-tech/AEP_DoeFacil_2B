package doefacil;
public record Doador(long id,String nome,String telefone) {
    public Doador {nome=ItemDoacao.texto(nome,120);telefone=ItemDoacao.texto(telefone,30);}
}
