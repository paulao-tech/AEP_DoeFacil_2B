package doefacil;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Acesso ao MySQL: somente valores ligados por parâmetros; conexões pertencem ao serviço. */
final class DoacaoDao {
    private static PreparedStatement preparar(Connection c,String sql,Object... args) throws SQLException {
        PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
        for(int i=0;i<args.length;i++) p.setObject(i+1,args[i]);
        return p;
    }
    static long inserir(Connection c,String sql,Object... args)throws SQLException {
        try(var p=preparar(c,sql,args)){p.executeUpdate();try(var r=p.getGeneratedKeys()){
            if(!r.next())throw new SQLException("ID não retornado pelo banco.");return r.getLong(1);
        }}
    }
    static void alterar(Connection c,String sql,Object... args)throws SQLException {
        try(var p=preparar(c,sql,args)){p.executeUpdate();}
    }
    static long numero(Connection c,String sql,Object... args)throws SQLException {
        try(var p=preparar(c,sql,args);var r=p.executeQuery()){
            if(!r.next())throw new IllegalArgumentException("Registro não encontrado.");return r.getLong(1);
        }
    }
    static void bloquear(Connection c,long doacao)throws SQLException {
        numero(c,"SELECT id FROM doacoes WHERE id=? FOR UPDATE",doacao);
    }
    static List<Doador> doadores(Connection c)throws SQLException {
        var lista=new ArrayList<Doador>();
        try(var p=c.prepareStatement("SELECT id,nome,telefone FROM doadores ORDER BY id");var r=p.executeQuery()){
            while(r.next())lista.add(new Doador(r.getLong(1),r.getString(2),r.getString(3)));
        }return lista;
    }
    static String tipo(ItemDoacao i){return i instanceof Roupa?"ROUPA":"ALIMENTO";}
    static String tamanho(ItemDoacao i){return i instanceof Roupa r?r.tamanho():null;}
    static String condicao(ItemDoacao i){return i instanceof Roupa r?r.condicao():null;}
    static LocalDate validade(ItemDoacao i){return i instanceof Alimento a?a.validade():null;}
    static void incluirItem(Connection c,long doa,ItemDoacao i)throws SQLException {
        inserir(c,"INSERT INTO itens_doacao(doacao_id,tipo,descricao,quantidade_recebida,tamanho,condicao,validade) VALUES(?,?,?,?,?,?,?)",
            doa,tipo(i),i.descricao(),i.quantidadeRecebida(),tamanho(i),condicao(i),validade(i));
    }
    static void editarItem(Connection c,long item,ItemDoacao i)throws SQLException {
        alterar(c,"UPDATE itens_doacao SET tipo=?,descricao=?,quantidade_recebida=?,tamanho=?,condicao=?,validade=? WHERE id=?",
            tipo(i),i.descricao(),i.quantidadeRecebida(),tamanho(i),condicao(i),validade(i),item);
    }
    private static ItemDoacao lerItem(ResultSet r)throws SQLException {
        long id=r.getLong("item_id"); String descricao=r.getString("descricao");
        int qtd=r.getInt("quantidade_recebida"), saiu=r.getInt("saiu");
        return switch(r.getString("tipo")){
            case "ROUPA" -> new Roupa(id,descricao,qtd,saiu,r.getString("tamanho"),r.getString("condicao"));
            case "ALIMENTO" -> new Alimento(id,descricao,qtd,saiu,r.getDate("validade").toLocalDate());
            default -> throw new SQLException("Tipo de item desconhecido.");
        };
    }
    // Uma única consulta dá uma visão consistente das doações e dos seus saldos.
    static List<Doacao> consultar(Connection c,Long filtro)throws SQLException {
        String sql="""
            SELECT d.id,d.doador_id,d.data_recebimento,i.id AS item_id,i.tipo,i.descricao,
                   i.quantidade_recebida,i.tamanho,i.condicao,i.validade,
                   (SELECT COALESCE(SUM(s.quantidade),0) FROM distribuicoes s WHERE s.item_id=i.id) AS saiu
            FROM doacoes d JOIN itens_doacao i ON i.doacao_id=d.id
            """+(filtro==null?"":" WHERE d.id=?")+" ORDER BY d.id,i.id";
        List<Doacao> lista=new ArrayList<>();
        try(var p=c.prepareStatement(sql)){
            if(filtro!=null)p.setLong(1,filtro);
            try(var r=p.executeQuery()){
                long atual=-1,doador=0;LocalDate data=null;var itens=new ArrayList<ItemDoacao>();
                while(r.next()){
                    long id=r.getLong("id");
                    if(id!=atual){
                        if(atual!=-1)lista.add(new Doacao(atual,doador,data,itens));
                        itens.clear();atual=id;doador=r.getLong("doador_id");data=r.getDate("data_recebimento").toLocalDate();
                    }
                    itens.add(lerItem(r));
                }
                if(atual!=-1)lista.add(new Doacao(atual,doador,data,itens));
            }
        }return lista;
    }
}
