package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.excepcion.TareaYaCompletadaException;
import com.tallerwebi.dominio.excepcion.ValorCreditoInvalidoException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioCreditoTareaTest {

  private RepositorioTarea repositorioTareaMock;
  private RepositorioUsuario repositorioUsuarioMock;
  private ServicioCreditoTarea servicioCreditoTarea;
  private RepositorioTareaRepetitiva repositorioTareaRepetitivaMock;

  private List<Tarea> tareasEnMemoria;

  @BeforeEach
  public void init() {
    repositorioTareaMock = mock(RepositorioTarea.class);
    repositorioUsuarioMock = mock(RepositorioUsuario.class);
    repositorioTareaRepetitivaMock = mock(RepositorioTareaRepetitiva.class);
    servicioCreditoTarea =
      new ServicioCreditoTareaImpl(
        repositorioTareaMock,
        repositorioUsuarioMock,
        repositorioTareaRepetitivaMock
      );

    tareasEnMemoria = new ArrayList<>();

    // Simulamos comportamiento de los repositorios para que el servicio opere correctamente en memoria
    when(repositorioTareaMock.obtenerTodas()).thenAnswer(invocation -> tareasEnMemoria);
    doAnswer(invocation -> {
        Tarea t = invocation.getArgument(0);
        if (t.getId() == null) {
          t.setId((long) (tareasEnMemoria.size() + 1));
        }
        if (!tareasEnMemoria.contains(t)) {
          tareasEnMemoria.add(t);
        }
        return null;
      })
      .when(repositorioTareaMock)
      .guardar(any(Tarea.class));

    when(repositorioTareaMock.buscarPorId(any(Long.class)))
      .thenAnswer(invocation -> {
        Long id = invocation.getArgument(0);
        return tareasEnMemoria.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
      });

    when(repositorioTareaMock.obtenerPorUsuario(any(Usuario.class)))
      .thenAnswer(invocation -> {
        Usuario u = invocation.getArgument(0);
        return tareasEnMemoria
          .stream()
          .filter(t -> t.getUsuarioRealizador() != null && t.getUsuarioRealizador().equals(u))
          .toList();
      });
  }

  @Test
  public void debeGenerarCreditoMonetarioAlCompletarUnaTarea() {
    //given (2.5 hs a $1000/h = $2500)
    Tarea tarea = givenUnaTareaValida();
    servicioCreditoTarea.registrarTarea(tarea);

    Long usuarioId = 10L;
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(usuarioId);
    when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(usuario);

    //when
    servicioCreditoTarea.completarTareaYGenerarCredito(tarea.getId(), usuarioId);

    //then
    thenObtengoElCreditoEsperado(usuarioId);
  }

  @Test
  public void debeDescontarCreditoAcumuladoDelMontoBrutoACompartir() {
    // 1 hs = $1000 y 0.5 hs = $500 (Total $1500)
    Tarea tarea1 = new Tarea("Lavar los platos", 1.0, 1000.0, null);
    Tarea tarea2 = new Tarea("Sacar la basura", 0.5, 1000.0, null);
    servicioCreditoTarea.registrarTarea(tarea1);
    servicioCreditoTarea.registrarTarea(tarea2);

    Long usuarioId = 10L;
    Usuario usuario = new Usuario("Juan", "juan@kumo.com", "1234");
    usuario.setId(usuarioId);
    when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(usuario);

    servicioCreditoTarea.completarTareaYGenerarCredito(tarea1.getId(), usuarioId);
    servicioCreditoTarea.completarTareaYGenerarCredito(tarea2.getId(), usuarioId);

    Double montoBrutoGasto = 5000.0;
    Double montoNeto = servicioCreditoTarea.calcularMontoNetoAPagar(montoBrutoGasto, usuarioId);

    assertThat(montoNeto, equalTo(3500.0));
  }

  @Test
  public void debeLanzarExcepcionSiSeIntentaCompletarUnaTareaYaCompletada() {
    //given
    Tarea tarea = new Tarea("Limpiar baño", 3.0, 1000.0, null);
    servicioCreditoTarea.registrarTarea(tarea);

    Long usuario1 = 10L;
    Long usuario2 = 20L;
    Usuario u1 = new Usuario("Juan", "juan@kumo.com", "1234");
    u1.setId(usuario1);
    Usuario u2 = new Usuario("Maria", "maria@kumo.com", "1212");
    u2.setId(usuario2);

    when(repositorioUsuarioMock.buscarPorId(usuario1)).thenReturn(u1);
    when(repositorioUsuarioMock.buscarPorId(usuario2)).thenReturn(u2);

    //when
    servicioCreditoTarea.completarTareaYGenerarCredito(tarea.getId(), usuario1);

    //then
    thenDebeLanzarExceptionAlCompletarUnaTareaDosVeces(tarea.getId(), usuario2);
  }

  @Test
  public void debeLanzarExcepcionSiLasHorasDedicadasSonInvalidas() {
    //given + when + then
    thenDebeLanzarExceptionAlRegistrarUnaTareaInvalida(givenUnaTareaInvalida());
  }

  @Test
  public void dadaUnaTareaDelCatalogoConValorPropioElCreditoUsaEseValor() {

    Hogar hogar = givenTengoUnHogar(1L, 1000.00,"casa de ori");
    givenUnaTareaDelCatalogo(7L, "compra semanal", hogar, 1500.00);

    servicioCreditoTarea.registrarTareaDesdeCatalogo(7L,2.0, 1L);
  }





  private TareaRepetitiva givenUnaTareaDelCatalogo(Long id, String titulo,Hogar hogar,Double valorPorHora) {
    TareaRepetitiva tarea = new TareaRepetitiva(id, titulo);
    tarea.setHogar(hogar);
    tarea.setValorPorHora(valorPorHora);
    when(repositorioTareaRepetitivaMock.obtenerPorId(id)).thenReturn(tarea);
    return tarea;
  }

  private Hogar givenTengoUnHogar(Long id, Double precioHora, String nombre) {
    Hogar hogar = new Hogar(nombre);
    hogar.setId(id);
    hogar.setPrecioHoraTarea(precioHora);
    return hogar;
  }
///////

  private Tarea givenUnaTareaInvalida() {
    Tarea tareaInvalida = new Tarea("Pintar pared", -1.0, 1000.0, null);
    return tareaInvalida;
  }

  private void thenDebeLanzarExceptionAlRegistrarUnaTareaInvalida(Tarea tarea) {
    assertThrows(
      ValorCreditoInvalidoException.class,
      () -> {
        servicioCreditoTarea.registrarTarea(tarea);
      }
    );
  }

  private void thenObtengoElCreditoEsperado(Long usuarioId) {
    Double creditoObtenido = servicioCreditoTarea.obtenerCreditoAcumuladoPorUsuario(usuarioId);
    assertThat(creditoObtenido, equalTo(2500.0));
  }

  private Tarea givenUnaTareaValida() {
    Tarea tarea = new Tarea("Limpieza de cocina", 2.5, 1000.0, null);
    return tarea;
  }

  private void thenDebeLanzarExceptionAlCompletarUnaTareaDosVeces(Long tareaId, Long usuario2) {
    assertThrows(
      TareaYaCompletadaException.class,
      () -> {
        servicioCreditoTarea.completarTareaYGenerarCredito(tareaId, usuario2);
      }
    );
  }
}
