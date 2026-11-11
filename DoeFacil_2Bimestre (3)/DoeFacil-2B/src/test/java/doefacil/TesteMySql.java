package doefacil;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import static doefacil.TesteDominio.*;

/** Cria somente dados fictícios, em uma base cujo nome precisa terminar em _teste. */
public final class TesteMySql {
    public static void main(String[] args)throws Exception {
        try(var c=Banco.abrir()){
            if(!c.getCatalog().endsWith("_teste")) throw new IllegalStateException("Use uma base dedicada terminada em _teste.");
        }
        var s=new DoacaoService();var hoje=LocalDate.now();
        long doador=s.cadastrarDoador(new Doador(0,"Doador de teste","0000"));
        var roupa=new Roupa(0,"Casaco",10,0,"G","boa");var alimento=new Alimento(0,"Feijão",3,0,hoje.minusDays(1));
        long id=s.receber(new Doacao(0,doador,hoje.minusDays(2),List.of(roupa,alimento)));
        var relida=new DoacaoService().buscar(id);exige(relida.itens().size()==2,"Doação não persistida");
        long item=relida.itens().get(0).id(), vencido=relida.itens().get(1).id();
        s.editarCabecalho(id,doador,hoje.minusDays(1));exige(s.buscar(id).dataRecebimento().equals(hoje.minusDays(1)),"Data não editada");
        s.corrigirItem(id,item,new Roupa(0,"Casaco azul",9,0,"G","boa"));
        s.distribuir(id,new Distribuicao(item,hoje,4));
        recusa(()->s.distribuir(id,new Distribuicao(item,hoje,6)));
        recusa(()->s.distribuir(id,new Distribuicao(vencido,hoje,1)));
        recusa(()->s.corrigirItem(id,item,new Roupa(0,"Casaco azul",3,0,"G","boa")));
        recusa(()->s.corrigirItem(id,item,new Alimento(0,"Feijão",9,0,hoje)));
        recusa(()->s.excluir(id));exige(s.buscar(id).itens().get(0).calcularSaldo()==5,"Saldo após recusas");
        // Duas sessões disputam as mesmas cinco unidades: exatamente uma saída deve ser aceita.
        var sinal=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(2);
        Callable<Boolean> tentativa=()->{sinal.await();try{s.distribuir(id,new Distribuicao(item,hoje,5));return true;}
            catch(IllegalArgumentException e){return false;}};
        try{var x=pool.submit(tentativa);var y=pool.submit(tentativa);sinal.countDown();
            exige(x.get(30,TimeUnit.SECONDS)^y.get(30,TimeUnit.SECONDS),"Falha na concorrência");}
        finally{pool.shutdownNow();}
        exige(s.buscar(id).itens().get(0).calcularSaldo()==0,"Saldo final");
        long livre=s.receber(new Doacao(0,doador,hoje,List.of(roupa)));s.excluir(livre);
        recusa(()->s.buscar(livre));exige(s.listar().stream().anyMatch(d->d.id()==id),"Consulta geral");
        System.out.println("OK: RF01–RF06, persistência em novas conexões e concorrência. Dados fictícios mantidos na base de teste.");
    }
}
