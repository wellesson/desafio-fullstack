package com.bdc.fullstack.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContaInput {

	private String agencia;

	private String conta;

	private String saldo;

	private String status;

	private String resultado;
		

}
