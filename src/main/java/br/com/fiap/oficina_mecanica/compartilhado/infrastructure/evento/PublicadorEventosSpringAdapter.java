package br.com.fiap.oficina_mecanica.compartilhado.infrastructure.evento;

import br.com.fiap.oficina_mecanica.compartilhado.application.PublicadorEventos;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class PublicadorEventosSpringAdapter implements PublicadorEventos {

    private final ApplicationEventPublisher publisher;

    public PublicadorEventosSpringAdapter(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publicar(Object evento) {
        publisher.publishEvent(evento);
    }
}
