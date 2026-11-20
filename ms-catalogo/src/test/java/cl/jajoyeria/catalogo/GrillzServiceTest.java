package cl.jajoyeria.catalogo;
import cl.jajoyeria.catalogo.dto.GrillzRequest;
import cl.jajoyeria.catalogo.entity.OpcionGrillz;
import cl.jajoyeria.catalogo.repository.*;
import cl.jajoyeria.catalogo.service.GrillzService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class GrillzServiceTest {
    private final OpcionGrillzRepository opciones=mock(OpcionGrillzRepository.class);
    private final GrillzService servicio=new GrillzService(opciones,mock(DisenoGrillzRepository.class),new ObjectMapper());
    private void opcion(String codigo,String tipo,long precio) {
        var opcion=new OpcionGrillz();
        ReflectionTestUtils.setField(opcion,"tipo",tipo);ReflectionTestUtils.setField(opcion,"precioClp",precio);
        when(opciones.findById(codigo)).thenReturn(Optional.of(opcion));
    }
    private GrillzRequest diseno(GrillzRequest.Diente... dientes){return new GrillzRequest("Mi diseño","arcada-v1",List.of(dientes));}
    @Test void calculaElPrecioConLasTarifasDelServidor() {
        opcion("dorado","MATERIAL",35000);opcion("piedras","ESTILO",18000);
        assertEquals(106000,servicio.estimar(diseno(new GrillzRequest.Diente("S1","dorado","piedras"),new GrillzRequest.Diente("I2","dorado","piedras"))));
    }
    @Test void rechazaDientesRepetidos() {
        opcion("dorado","MATERIAL",35000);opcion("liso","ESTILO",0);
        var d=new GrillzRequest.Diente("S1","dorado","liso");
        assertThrows(ResponseStatusException.class,()->servicio.estimar(diseno(d,d)));
    }
    @Test void rechazaDientesFueraDelModelo() {
        assertThrows(ResponseStatusException.class,()->servicio.estimar(diseno(new GrillzRequest.Diente("S99","dorado","liso"))));
    }
    @Test void rechazaUnEstiloUsadoComoMaterial() {
        opcion("liso","ESTILO",0);
        assertThrows(ResponseStatusException.class,()->servicio.estimar(diseno(new GrillzRequest.Diente("S1","liso","liso"))));
    }
    @Test void rechazaUnaVersionDeModeloDesconocida() {
        assertThrows(ResponseStatusException.class,()->servicio.estimar(new GrillzRequest("Mi diseño","otro",List.of())));
    }
    @Test void aceptaElModeloNuevoConAcabadosNuevos() {
        opcion("plata","MATERIAL",25000);opcion("ventana","ESTILO",12000);
        assertEquals(37000,servicio.estimar(new GrillzRequest("Encías","arcada-v2",List.of(new GrillzRequest.Diente("S4","plata","ventana")))));
    }
    @Test void elModeloAnteriorNoAceptaAcabadosQueNoPuedeMostrar() {
        assertThrows(ResponseStatusException.class,()->servicio.estimar(diseno(new GrillzRequest.Diente("S4","plata","ventana"))));
    }
}
