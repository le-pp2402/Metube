package com.phatpl.metube.common.listener;

import org.hibernate.SessionFactory;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.springframework.stereotype.Component;
import org.hibernate.Session;
import com.phatpl.metube.common.model.ISoftDelete;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SoftDeleteEventLister implements PreDeleteEventListener {

  private final EntityManagerFactory emf;

  @PostConstruct
  public void register() {
    SessionFactory sf = emf.unwrap(SessionFactory.class);
    ServiceRegistryImplementor registry = sf.unwrap(ServiceRegistryImplementor.class);
    registry.getService(EventListenerRegistry.class).appendListeners(EventType.PRE_DELETE, this);
  }

  @Override
  public boolean onPreDelete(PreDeleteEvent event) {
    Object entity = event.getEntity();
    if (entity instanceof ISoftDelete softDelEntity) {
      softDelEntity.delete();
      ((Session) event.getSession()).merge(entity);
      return true;
    }
    return false;
  }

}
