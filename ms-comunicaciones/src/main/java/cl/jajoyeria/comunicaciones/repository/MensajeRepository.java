package cl.jajoyeria.comunicaciones.repository;

import cl.jajoyeria.comunicaciones.entity.MensajeContacto;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface MensajeRepository extends JpaRepository<MensajeContacto,Long> {
    @Query("""
            select m from MensajeContacto m
            where (:lectura = 'todos' or (:lectura = 'leidos' and m.leidoEn is not null)
                                      or (:lectura = 'sin-leer' and m.leidoEn is null))
              and (locate(:q, lower(m.nombre)) > 0 or locate(:q, m.correo) > 0
                   or locate(:q, lower(m.mensaje)) > 0 or :q = '')
            order by m.creadoEn desc, m.id desc
            """)
    Page<MensajeContacto> buscar(String q, String lectura, Pageable pagina);

    long countByLeidoEnIsNull();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MensajeContacto m where m.id = :id")
    Optional<MensajeContacto> bloquear(long id);
}
