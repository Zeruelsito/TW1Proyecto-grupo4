package com.tallerwebi.dominio;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service("servicioBalanceNetoIndividual")
@Transactional
public class ServicioBalanceNetoIndividualImpl implements ServicioBalanceNetoIndividual {

  @Override
  public void calcularBalance(Usuario usuario) {}
}
