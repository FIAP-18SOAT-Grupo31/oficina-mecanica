package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento;

import br.com.fiap.oficina_mecanica.ordemservico.domain.evento.OrdemServicoCancelada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicadorEventosSpringAdapterTest {

    @Mock
    private ApplicationEventPublisher publisher;

    @Test
    void repassaOEventoParaOPublicadorDoSpring() {
        OrdemServicoCancelada evento = new OrdemServicoCancelada(UUID.randomUUID());

        new PublicadorEventosSpringAdapter(publisher).publicar(evento);

        verify(publisher).publishEvent(evento);
    }
}
