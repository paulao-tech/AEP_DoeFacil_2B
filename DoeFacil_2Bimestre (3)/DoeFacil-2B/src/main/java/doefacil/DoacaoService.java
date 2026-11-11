package doefacil;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import static doefacil.DoacaoDao.*;

public final class DoacaoService {
    public long cadastrarDoador(Doador d)throws SQLException {
        return Banco.transacao(c->inserir(c,"INSERT INTO doadores(nome,telefone) VALUES(?,?)",d.nome(),d.telefone()));
    }
    public List<Doador> listarDoadores()throws SQLException {
        try(var c=Banco.abrir()){return doadores(c);}
    }
    public long receber(Doacao d)throws SQLException {
        return Banco.transacao(c->{
            numero(c,"SELECT id FROM doadores WHERE id=?",d.doadorId());
            long id=inserir(c,"INSERT INTO doacoes(doador_id,data_recebimento) VALUES(?,?)",d.doadorId(),d.dataRecebimento());
            for(var i:d.itens())incluirItem(c,id,i);
            return id;
        });
    }
    public List<Doacao> listar()throws SQLException {try(var c=Banco.abrir()){return consultar(c,null);}}
    public Doacao buscar(long id)throws SQLException {try(var c=Banco.abrir()){return buscar(c,id);}}
    private Doacao buscar(Connection c,long id)throws SQLException {
        return consultar(c,id).stream().findFirst().orElseThrow(()->new IllegalArgumentException("Doação não encontrada."));
    }
    private ItemDoacao item(Doacao d,long id){
        return d.itens().stream().filter(i->i.id()==id).findFirst().orElseThrow(()->new IllegalArgumentException("Item não pertence à doação."));
    }
    public void editarCabecalho(long id,long doador,LocalDate data)throws SQLException {
        Banco.transacao(c->{bloquear(c,id);numero(c,"SELECT id FROM doadores WHERE id=?",doador);
            if(numero(c,"SELECT COUNT(*) FROM distribuicoes s JOIN itens_doacao i ON i.id=s.item_id WHERE i.doacao_id=? AND s.data<?",id,data)>0)
                throw new IllegalArgumentException("O recebimento não pode ser posterior a uma saída existente.");
            alterar(c,"UPDATE doacoes SET doador_id=?,data_recebimento=? WHERE id=?",doador,data,id);return null;
        });
    }
    public void corrigirItem(long doacao,long id,ItemDoacao novo)throws SQLException {
        Banco.transacao(c->{bloquear(c,doacao);var anterior=item(buscar(c,doacao),id);
            int saiu=anterior.quantidadeRecebida()-anterior.calcularSaldo();
            if(novo.quantidadeRecebida()<saiu)throw new IllegalArgumentException("Quantidade inferior ao total já distribuído.");
            if(saiu>0 && anterior.getClass()!=novo.getClass())throw new IllegalArgumentException("Item com distribuição não pode mudar de tipo.");
            if(novo instanceof Alimento a && numero(c,"SELECT COUNT(*) FROM distribuicoes WHERE item_id=? AND data>?",id,a.validade())>0)
                throw new IllegalArgumentException("Validade incompatível com uma saída já registrada.");
            editarItem(c,id,novo);return null;
        });
    }
    public void excluir(long id)throws SQLException {
        Banco.transacao(c->{bloquear(c,id);
            if(numero(c,"SELECT COUNT(*) FROM distribuicoes s JOIN itens_doacao i ON i.id=s.item_id WHERE i.doacao_id=?",id)>0)
                throw new IllegalArgumentException("Doação com distribuição não pode ser excluída.");
            alterar(c,"DELETE FROM doacoes WHERE id=?",id);return null;
        });
    }
    public void distribuir(long id,Distribuicao s)throws SQLException {
        Banco.transacao(c->{bloquear(c,id);Doacao d=buscar(c,id);
            if(s.data().isBefore(d.dataRecebimento()) || s.data().isAfter(LocalDate.now()))
                throw new IllegalArgumentException("Data de saída deve estar entre o recebimento e hoje.");
            item(d,s.itemId()).validarSaida(s.quantidade(),s.data());
            inserir(c,"INSERT INTO distribuicoes(item_id,data,quantidade) VALUES(?,?,?)",s.itemId(),s.data(),s.quantidade());
            return null;
        });
    }
}
