package doefacil;
public final class Roupa extends ItemDoacao {
    private final String tamanho, condicao;
    public Roupa(long id,String descricao,int qtd,int saiu,String tamanho,String condicao) {
        super(id,descricao,qtd,saiu); this.tamanho=texto(tamanho,30);this.condicao=texto(condicao,60);
    }
    public String tamanho(){return tamanho;}
    public String condicao(){return condicao;}
    @Override public String descreverDetalhes(){return "Roupa | tamanho: "+tamanho+" | condição: "+condicao;}
}
