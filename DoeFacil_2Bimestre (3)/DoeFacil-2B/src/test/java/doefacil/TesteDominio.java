package doefacil;
import java.time.LocalDate;
import java.util.*;
public final class TesteDominio {
    static void exige(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    interface Acao {void rodar() throws Exception;}
    static void recusa(Acao a)throws Exception {
        try{a.rodar();}catch(IllegalArgumentException esperado){return;}throw new AssertionError("Era esperado bloqueio pela regra de negócio.");
    }
    public static void main(String[] args)throws Exception {
        var dia=LocalDate.of(2026,11,11);
        var r=new Roupa(1,"Camiseta",5,2,"M","boa");var a=new Alimento(2,"Arroz",4,0,dia);
        var itens=new ArrayList<ItemDoacao>(List.of(r,a));var d=new Doacao(1,1,dia,itens);itens.clear();
        exige(d.itens().size()==2,"Composição não preservada");exige(r.calcularSaldo()==3,"Saldo incorreto");
        exige(d.itens().get(0).descreverDetalhes().contains("M") && d.itens().get(1).descreverDetalhes().contains(dia.toString()),"Detalhes do subtipo");
        recusa(()->r.validarSaida(4,dia));recusa(()->r.validarSaida(0,dia));recusa(()->a.validarSaida(1,dia.plusDays(1)));
        a.validarSaida(4,dia);recusa(()->new Doacao(1,1,dia,List.of()));recusa(()->new Roupa(0," ",1,0,"P","boa"));
        System.out.println("OK: composição, polimorfismo, saldo, validade e entradas inválidas.");
    }
}
