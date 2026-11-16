package cl.jajoyeria.comunicaciones.repository;

import cl.jajoyeria.comunicaciones.entity.Suscripcion;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface SuscripcionRepository extends JpaRepository<Suscripcion,Long> {
    @Modifying
    @Query(value = """
            insert into comunicaciones.suscripciones (correo, alta_en) values (:correo, current_timestamp)
            on conflict do nothing
            """, nativeQuery = true)
    int insertar(String correo);

    @Modifying
    @Query(value = """
            update comunicaciones.suscripciones set alta_en=current_timestamp, baja_en=null
            where correo=:correo and baja_en is not null
            """, nativeQuery = true)
    int reactivar(String correo);

    @Modifying
    @Query(value = """
            update comunicaciones.suscripciones set baja_en=current_timestamp
            where id=:id and baja_en is null
            """, nativeQuery = true)
    int darDeBaja(long id);

    @Query("""
            select s from Suscripcion s where s.bajaEn is null and (:q = '' or locate(:q,s.correo) > 0)
            order by s.altaEn desc, s.id desc
            """)
    Page<Suscripcion> activas(String q, Pageable pagina);
}
