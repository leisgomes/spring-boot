package com.leisgomes.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leisgomes.cursomc.domain.ItemPedido;
import com.leisgomes.cursomc.domain.PagamentoComBoleto;
import com.leisgomes.cursomc.domain.Pedido;
import com.leisgomes.cursomc.domain.enums.EstadoPagamento;
import com.leisgomes.cursomc.repositories.ItemPedidoRepository;
import com.leisgomes.cursomc.repositories.PagamentoRepository;
import com.leisgomes.cursomc.repositories.PedidoRepository;
import com.leisgomes.cursomc.repositories.ProdutoRepository;
import com.leisgomes.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoRepository produtoRepository;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
		"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
		}
	
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.getPagamento().setTipo(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamento(pagto,obj.getInstante());
		}
		
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for(ItemPedido item: obj.getItens()) {
			item.setDesconto(0.0);
			item.setPreco(produtoRepository.findById(item.getProduto().getId()).get().getPreco());
			item.setPedido(obj);
		}
		
		itemPedidoRepository.saveAll(obj.getItens());
		
		return obj;
		
		
		

	}
}
